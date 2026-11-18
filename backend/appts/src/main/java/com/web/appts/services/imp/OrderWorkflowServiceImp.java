package com.web.appts.services.imp;

import com.web.appts.DTO.OrderDto;
import com.web.appts.entities.OrderDepartment;
import com.web.appts.repositories.OrderRepo;
import com.web.appts.services.OrderWorkflowService;
import org.hibernate.StaleStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.lang.reflect.Method;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class OrderWorkflowServiceImp implements OrderWorkflowService {
    private static final Logger logger = LoggerFactory.getLogger(OrderWorkflowServiceImp.class);

    private final OrderRepo orderRepo;

    public OrderWorkflowServiceImp(OrderRepo orderRepo) {
        this.orderRepo = orderRepo;
    }

    @Override
    @Transactional
    public List<OrderDto> updateOrderColors(
            String orderNumber,
            String orderDep,
            String orderStatus,
            String flowVal,
            Map<String, OrderDto> ordersMap,
            Supplier<List<OrderDto>> activeOrdersLoader
    ) {
        try {
            activeOrdersLoader.get();
            List<Integer> matchingIds = ordersMap.values()
                    .stream()
                    .filter(order -> orderNumber.equals(order.getOrderNumber()))
                    .map(OrderDto::getId)
                    .collect(Collectors.toList());

            WorkflowTransition transition = applyTransitions(orderNumber, orderDep, orderStatus, flowVal, ordersMap);
            if (transition.color.isEmpty()) {
                return new ArrayList<>();
            }

            persistMainDepartmentColor(orderDep, transition.color, transition.previousStatus, matchingIds);
            return sortedOrders(ordersMap);
        } catch (ObjectOptimisticLockingFailureException | StaleStateException e) {
            logger.warn("Optimistic locking failed while updating workflow color for order {}", orderNumber, e);
            return sortedOrders(ordersMap);
        } catch (DataIntegrityViolationException e) {
            Throwable rootCause = e.getRootCause();
            if (rootCause instanceof SQLIntegrityConstraintViolationException) {
                logger.warn("SQL constraint violation while updating workflow color for order {}: {}", orderNumber, rootCause.getMessage(), e);
            } else {
                logger.warn("Data integrity violation while updating workflow color for order {}", orderNumber, e);
            }
            return sortedOrders(ordersMap);
        }
    }

    private WorkflowTransition applyTransitions(
            String orderNumber,
            String orderDep,
            String orderStatus,
            String flowVal,
            Map<String, OrderDto> ordersMap
    ) {
        WorkflowTransition transition = new WorkflowTransition();
        String setterMethodName = "set" + orderDep.substring(0, 1).toUpperCase() + orderDep.substring(1);
        String getterMethodName = "get" + orderDep.substring(0, 1).toUpperCase() + orderDep.substring(1);

        try {
            Method setterMethod = OrderDto.class.getMethod(setterMethodName, String.class);
            Method getterMethod = OrderDto.class.getMethod(getterMethodName);

            for (Map.Entry<String, OrderDto> entry : ordersMap.entrySet()) {
                OrderDto orderDto = entry.getValue();
                if (!orderNumber.equals(orderDto.getOrderNumber())) {
                    continue;
                }

                transition.color = transitionMainField(
                        orderDto,
                        orderDep,
                        orderStatus,
                        flowVal,
                        setterMethod,
                        getterMethod,
                        transition.color
                );
                transition.previousStatus = transitionDepartment(orderDto, orderDep, flowVal, transition.previousStatus);
                markCompletedIfAllDepartmentsDone(orderDto);
                ordersMap.put(entry.getKey(), orderDto);
            }
        } catch (Exception e) {
            logger.warn("Failed to update workflow transition for order {} department {}", orderNumber, orderDep, e);
        }

        return transition;
    }

    private String transitionMainField(
            OrderDto orderDto,
            String orderDep,
            String orderStatus,
            String flowVal,
            Method setterMethod,
            Method getterMethod,
            String currentColor
    ) throws Exception {
        Object currentValue = getterMethod.invoke(orderDto);

        if ("R".equals(currentValue) && "R".equals(orderStatus) && ("FWD".equals(flowVal) || "RVS".equals(flowVal))) {
            setterMethod.invoke(orderDto, "Y");
            return "Y";
        }
        if ("R".equals(currentValue) && "R".equals(orderStatus) && "HLT".equals(flowVal)) {
            setterMethod.invoke(orderDto, "B");
            return "B";
        }
        if ("Y".equals(currentValue) && "Y".equals(orderStatus) && "HLT".equals(flowVal)) {
            setterMethod.invoke(orderDto, "B");
            return "B";
        }
        if ("Y".equals(currentValue) && "Y".equals(orderStatus)) {
            setterMethod.invoke(orderDto, "G");
            return "G";
        }
        if ("B".equals(currentValue) && "B".equals(orderStatus)) {
            String previousStatus = findPreviousDepartmentStatus(orderDto, orderDep).orElse("R");
            setterMethod.invoke(orderDto, previousStatus);
            return previousStatus;
        }

        return currentColor;
    }

    private Optional<String> findPreviousDepartmentStatus(OrderDto orderDto, String orderDep) {
        if (orderDto.getDepartments() == null) {
            return Optional.empty();
        }

        return orderDto.getDepartments()
                .stream()
                .filter(department -> orderDep.toUpperCase().equals(department.getDepName()))
                .map(OrderDepartment::getPrevStatus)
                .filter(previousStatus -> previousStatus != null && !previousStatus.isEmpty())
                .findFirst();
    }

    private String transitionDepartment(OrderDto orderDto, String orderDep, String flowVal, String previousStatus) {
        if (orderDto.getDepartments() == null) {
            return previousStatus;
        }

        for (OrderDepartment department : orderDto.getDepartments()) {
            if (!orderDep.toUpperCase().equals(department.getDepName())) {
                continue;
            }

            if ("R".equals(department.getStatus()) && ("FWD".equals(flowVal) || "RVS".equals(flowVal))) {
                department.setStatus("Y");
                department.setPrevStatus("R");
                return department.getPrevStatus();
            }
            if ("R".equals(department.getStatus()) && "HLT".equals(flowVal)) {
                department.setStatus("B");
                department.setPrevStatus("R");
                return department.getPrevStatus();
            }
            if ("Y".equals(department.getStatus()) && "HLT".equals(flowVal)) {
                department.setStatus("B");
                department.setPrevStatus("Y");
                return department.getPrevStatus();
            }
            if ("Y".equals(department.getStatus())) {
                department.setStatus("G");
                department.setPrevStatus("Y");
                return department.getPrevStatus();
            }
            if ("B".equals(department.getStatus())) {
                department.setStatus(department.getPrevStatus() != null && !department.getPrevStatus().isEmpty()
                        ? department.getPrevStatus()
                        : "R");
                department.setPrevStatus("B");
                return department.getPrevStatus();
            }
        }

        return previousStatus;
    }

    private void markCompletedIfAllDepartmentsDone(OrderDto orderDto) {
        if (orderDto.getDepartments() == null) {
            return;
        }

        boolean allStatusGOrEmpty = orderDto.getDepartments().stream()
                .allMatch((department) -> "G".equals(department.getStatus()) || "".equals(department.getStatus()));
        if (allStatusGOrEmpty) {
            orderDto.setCompleted("C");
        }
    }

    private void persistMainDepartmentColor(String orderDep, String color, String previousStatus, List<Integer> matchingIds) {
        if ("monLb".equals(orderDep)) {
            orderRepo.updateFieldForIdsMainmonLb(color, matchingIds);
        }
        if ("monTr".equals(orderDep)) {
            orderRepo.updateFieldForIdsMainmonTr(color, matchingIds);
        }
        if ("mwe".equals(orderDep)) {
            orderRepo.updateFieldForIdsMainmwe(color, matchingIds);
        }
        if ("ser".equals(orderDep)) {
            orderRepo.updateFieldForIdsMainser(color, matchingIds);
        }
        if ("exp".equals(orderDep)) {
            orderRepo.updateFieldForIdsMainexp(color, matchingIds);
        }

        orderRepo.updateOrderDepartmentStatusMain(color, previousStatus, orderDep.toUpperCase(), matchingIds);
    }

    private List<OrderDto> sortedOrders(Map<String, OrderDto> ordersMap) {
        List<OrderDto> orderDtos = new ArrayList<>();
        for (OrderDto orderDto : ordersMap.values()) {
            if (orderDto.getDepartments() != null) {
                orderDto.getDepartments().sort(Comparator.comparingInt(OrderDepartment::getDepId));
            }
            orderDtos.add(orderDto);
        }
        return orderDtos;
    }

    private static class WorkflowTransition {
        private String color = "";
        private String previousStatus = "";
    }
}

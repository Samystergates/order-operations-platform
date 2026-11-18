package com.web.appts.services.imp;

import com.web.appts.DTO.OrderDto;
import com.web.appts.entities.OrderDepartment;
import com.web.appts.repositories.OrderRepo;
import com.web.appts.services.OrderTransportLinkService;
import org.hibernate.StaleStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OrderTransportLinkServiceImp implements OrderTransportLinkService {
    private static final Logger logger = LoggerFactory.getLogger(OrderTransportLinkServiceImp.class);

    private final OrderRepo orderRepo;

    public OrderTransportLinkServiceImp(OrderRepo orderRepo) {
        this.orderRepo = orderRepo;
    }

    @Override
    @Transactional
    public Boolean updateTraColors(String ids, Map<String, OrderDto> ordersMap) {
        List<Integer> idList = parseIds(ids);
        if (idList.isEmpty()) {
            logger.info("Skipping TRA color update because no order ids were provided");
            return false;
        }

        String color = "";
        String previousStatus = "";

        for (Map.Entry<String, OrderDto> entry : ordersMap.entrySet()) {
            OrderDto orderDto = entry.getValue();
            if (orderDto == null || !idList.contains(orderDto.getId())) {
                continue;
            }

            if ("R".equals(orderDto.getTra())) {
                orderDto.setTra("Y");
                color = "Y";
            } else if ("Y".equals(orderDto.getTra())) {
                orderDto.setTra("G");
                color = "G";
            }

            previousStatus = advanceTraDepartmentStatus(orderDto, previousStatus);
            markCompletedIfAllDepartmentsDone(orderDto);
            ordersMap.put(entry.getKey(), orderDto);
        }

        return persistTraState(ids, idList, color, previousStatus);
    }

    private List<Integer> parseIds(String ids) {
        List<Integer> idList = new ArrayList<>();
        if (ids == null || ids.trim().isEmpty()) {
            return idList;
        }

        String[] idArray = ids.split(",");
        for (String id : idArray) {
            String trimmedId = id.trim();
            if (!trimmedId.isEmpty()) {
                idList.add(Integer.parseInt(trimmedId));
            }
        }
        return idList;
    }

    private String advanceTraDepartmentStatus(OrderDto orderDto, String previousStatus) {
        if (orderDto.getDepartments() == null) {
            return previousStatus;
        }

        for (OrderDepartment department : orderDto.getDepartments()) {
            if (!"TRA".equals(department.getDepName())) {
                continue;
            }

            if ("R".equals(department.getStatus())) {
                department.setStatus("Y");
                department.setPrevStatus("R");
                return department.getPrevStatus();
            }

            if ("Y".equals(department.getStatus())) {
                department.setStatus("G");
                department.setPrevStatus("Y");
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

    private Boolean persistTraState(String ids, List<Integer> idList, String color, String previousStatus) {
        try {
            if ("Y".equals(color)) {
                orderRepo.updateFieldForRIds(color, idList);
                orderRepo.updateOrderDepartmentStatusR(color, previousStatus, idList);
                return true;
            }

            if ("G".equals(color)) {
                orderRepo.updateFieldForYIds(color, idList);
                orderRepo.updateOrderDepartmentStatusY(color, previousStatus, idList);
                return true;
            }

            return false;
        } catch (ObjectOptimisticLockingFailureException | StaleStateException e) {
            logger.warn("Optimistic locking failed while updating TRA colors for ids {}", ids, e);
            return false;
        } catch (DataIntegrityViolationException e) {
            Throwable rootCause = e.getRootCause();
            if (rootCause instanceof SQLIntegrityConstraintViolationException) {
                logger.warn("SQL constraint violation while updating TRA colors for ids {}: {}", ids, rootCause.getMessage(), e);
            } else {
                logger.warn("Data integrity violation while updating TRA colors for ids {}", ids, e);
            }
            return false;
        }
    }
}

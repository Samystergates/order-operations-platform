package com.web.appts.services.imp;

import com.web.appts.DTO.ArchivedOrdersDto;
import com.web.appts.DTO.OrderDto;
import com.web.appts.entities.OrderPreserves;
import com.web.appts.entities.OrderSME;
import com.web.appts.entities.OrderSPU;
import com.web.appts.repositories.OrderPreservesRepo;
import com.web.appts.repositories.OrderRepo;
import com.web.appts.repositories.OrderSMERepo;
import com.web.appts.repositories.OrderSPURepo;
import com.web.appts.services.ArchivedOrdersService;
import org.hibernate.StaleStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OrderArchiveCoordinator {
    private static final Logger logger = LoggerFactory.getLogger(OrderArchiveCoordinator.class);

    private final ArchivedOrdersService archivedOrdersService;
    private final OrderRepo orderRepo;
    private final OrderPreservesRepo orderPreservesRepo;
    private final OrderSMERepo orderSMERepo;
    private final OrderSPURepo orderSPURepo;

    public OrderArchiveCoordinator(
            ArchivedOrdersService archivedOrdersService,
            OrderRepo orderRepo,
            OrderPreservesRepo orderPreservesRepo,
            OrderSMERepo orderSMERepo,
            OrderSPURepo orderSPURepo
    ) {
        this.archivedOrdersService = archivedOrdersService;
        this.orderRepo = orderRepo;
        this.orderPreservesRepo = orderPreservesRepo;
        this.orderSMERepo = orderSMERepo;
        this.orderSPURepo = orderSPURepo;
    }

    @Transactional
    public Boolean archiveOrder(OrderDto orderDto) {
        return archivedOrdersService.createArchivedOrder(orderDto);
    }

    @Transactional
    public void moveToArchive(
            List<Integer> ids,
            Map<String, OrderDto> ordersMap,
            Map<String, OrderPreserves> ordersPreserveMap
    ) {
        renameWheelOrdersBeforeArchive(ids, ordersMap, ordersPreserveMap);

        boolean isMoved = false;
        Iterator<Map.Entry<String, OrderDto>> iterator = ordersMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, OrderDto> entry = iterator.next();
            OrderDto orderDto = entry.getValue();
            if (ids.contains(orderDto.getId())) {
                isMoved = archiveOrder(orderDto);
                if (isMoved) {
                    iterator.remove();
                    logger.info("Archived and removed order id {} from active map", orderDto.getId());
                }
            }
        }

        if (isMoved) {
            deleteOrderData(ids, ordersMap);
        }
    }

    @Transactional
    public List<Integer> preserveBeforeDelete(
            Set<String> orderNumbers,
            Map<String, OrderDto> ordersMap,
            Map<String, OrderPreserves> ordersPreserveMap
    ) {
        List<Integer> ids = new ArrayList<>();

        ordersMap.values().forEach(order -> {
            if (orderNumbers.contains(order.getOrderNumber())) {
                OrderPreserves preserve = toPreserve(order);
                orderPreservesRepo.save(preserve);
                ordersPreserveMap.put(preserve.getOrderNumber() + "," + preserve.getRegel(), preserve);
                ids.add(order.getId());
            }
        });

        return ids;
    }

    @Transactional
    public void deleteOrderData(List<Integer> ids, Map<String, OrderDto> ordersMap) {
        if (ids == null || ids.isEmpty()) {
            logger.info("Skipping order data delete because no ids were provided");
            return;
        }

        try {
            orderRepo.deleteMonSubsForIds(ids);
            orderRepo.deleteODForIds(ids);
            orderRepo.deleteOrdersByIds(ids);

            boolean stillInMap = ordersMap.entrySet().stream()
                    .anyMatch(order -> ids.contains(order.getValue().getId()));
            logger.info("Deleted order data for ids {}. Active-map still contains deleted ids: {}", ids, stillInMap);
        } catch (ObjectOptimisticLockingFailureException | StaleStateException e) {
            logger.warn("Optimistic locking failed while deleting order data for ids {}", ids, e);
        } catch (DataIntegrityViolationException e) {
            Throwable rootCause = e.getRootCause();
            if (rootCause instanceof SQLIntegrityConstraintViolationException) {
                logger.warn("SQL constraint violation while deleting order data for ids {}: {}", ids, rootCause.getMessage(), e);
            } else {
                logger.warn("Data integrity violation while deleting order data for ids {}", ids, e);
            }
        }
    }

    public void removeSameArchivedOrders(Map<String, OrderDto> ordersMap) {
        List<ArchivedOrdersDto> archivedOrders = archivedOrdersService.getAllArchivedOrders();
        List<Integer> matchingIds = ordersMap.values()
                .stream()
                .filter(order -> archivedOrders
                        .stream()
                        .anyMatch(archivedOrder -> archivedOrder.getOrderNumber().equals(order.getOrderNumber())))
                .map(OrderDto::getId)
                .collect(Collectors.toList());

        deleteOrderData(matchingIds, ordersMap);
    }

    private void renameWheelOrdersBeforeArchive(
            List<Integer> ids,
            Map<String, OrderDto> ordersMap,
            Map<String, OrderPreserves> ordersPreserveMap
    ) {
        ordersMap.values().forEach(order -> {
            if (!ids.contains(order.getId())) {
                return;
            }

            String originalOrderNumber = order.getOrderNumber();
            String preserveKey = originalOrderNumber + "," + order.getRegel();
            if (!ordersPreserveMap.isEmpty() && ordersPreserveMap.containsKey(preserveKey)) {
                return;
            }

            OrderSPU orderSPU = orderSPURepo.findByOrderNumberAndRegel(originalOrderNumber, order.getRegel());
            if (orderSPU != null) {
                orderSPU.setOrderNumber("X" + originalOrderNumber);
                orderSPURepo.save(orderSPU);
            }

            OrderSME orderSME = orderSMERepo.findByOrderNumberAndRegel(originalOrderNumber, order.getRegel());
            if (orderSME != null) {
                orderSME.setOrderNumber("X" + originalOrderNumber);
                orderSMERepo.save(orderSME);
            }
        });
    }

    private OrderPreserves toPreserve(OrderDto order) {
        OrderPreserves preserve = new OrderPreserves();
        preserve.setOrderNumber(order.getOrderNumber());
        preserve.setRegel(order.getRegel());
        preserve.setSme(order.getSme());
        preserve.setSpu(order.getSpu());
        preserve.setMonLb(order.getMonLb());
        preserve.setMonTr(order.getMonTr());
        preserve.setMwe(order.getMwe());
        preserve.setSer(order.getSer());
        preserve.setExp(order.getExp());
        preserve.setTra(order.getTra());
        preserve.setExclamation(order.getExclamation());
        preserve.setExcNote(order.getExcNote());
        return preserve;
    }
}

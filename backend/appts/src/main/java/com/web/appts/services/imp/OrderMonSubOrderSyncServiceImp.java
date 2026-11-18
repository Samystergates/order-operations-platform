package com.web.appts.services.imp;

import com.web.appts.DTO.OrderDto;
import com.web.appts.DTO.OrdersFeignData;
import com.web.appts.client.OrdersFeignClient;
import com.web.appts.entities.MonSubOrders;
import com.web.appts.entities.Order;
import com.web.appts.repositories.MonSubOrdersRepo;
import com.web.appts.repositories.OrderRepo;
import com.web.appts.services.OrderMonSubOrderSyncService;
import org.hibernate.StaleStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
public class OrderMonSubOrderSyncServiceImp implements OrderMonSubOrderSyncService {
    private static final Logger logger = LoggerFactory.getLogger(OrderMonSubOrderSyncServiceImp.class);
    private static final Set<String> MON_SUB_ORDER_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "MLE", "MSE", "MSP", "MWP", "MLP", "MAP",
            "MST", "MLT", "MSO", "MWO", "MLO", "MAO"
    )));

    private final OrdersFeignClient ordersFeignClient;
    private final MonSubOrdersRepo monSubOrdersRepo;
    private final OrderRepo orderRepo;

    public OrderMonSubOrderSyncServiceImp(
            OrdersFeignClient ordersFeignClient,
            MonSubOrdersRepo monSubOrdersRepo,
            OrderRepo orderRepo
    ) {
        this.ordersFeignClient = ordersFeignClient;
        this.monSubOrdersRepo = monSubOrdersRepo;
        this.orderRepo = orderRepo;
    }

    @Override
    public void createMonSubOrders(
            Map<String, OrderDto> ordersMap,
            Supplier<List<OrderDto>> allOrdersLoader,
            Consumer<List<OrderDto>> orderDtoListUpdater,
            Function<Order, OrderDto> orderMapper
    ) {
        try {
            List<String> formattedOrders = new ArrayList<>();
            for (OrderDto order : ordersMap.values()) {
                if (MON_SUB_ORDER_TYPES.contains(order.getOrderType())) {
                    logger.info("Added order {} to MON suborder fetch", order.getOrderNumber());
                    formattedOrders.add("'" + order.getOrderNumber() + "'");
                }
            }

            if (!formattedOrders.isEmpty()) {
                String orderNumbers = String.join(",", formattedOrders);
                try {
                    List<OrdersFeignData> ordersFeignDataList =
                            ordersFeignClient.sendAndReceiveMonSub(orderNumbers);

                    if (ordersFeignDataList != null) {
                        for (OrdersFeignData data : ordersFeignDataList) {
                            if (data == null) {
                                continue;
                            }

                            String orderNumber = data.getOrderNumber();
                            String regel = data.getRegel();
                            String product = data.getProduct();
                            String description = data.getDescription();

                            if (ordersMap.isEmpty()) {
                                try {
                                    allOrdersLoader.get();
                                } catch (DataIntegrityViolationException e) {
                                    Throwable rootCause = e.getRootCause();
                                    if (rootCause instanceof SQLIntegrityConstraintViolationException) {
                                        logger.warn("Could not load orders while creating MON suborders", e);
                                        orderDtoListUpdater.accept(allOrdersLoader.get());
                                    } else {
                                        logger.error("Could not load orders while creating MON suborders", e);
                                        orderDtoListUpdater.accept(allOrdersLoader.get());
                                    }
                                }
                            }

                            OrderDto orderDto = ordersMap.getOrDefault(orderNumber + "," + regel, null);
                            Map<String, Boolean> reminderMap = new HashMap<>();
                            Optional<MonSubOrders> existingSubOrder = Optional.empty();
                            try {
                                existingSubOrder = monSubOrdersRepo
                                        .findByOrderNumberAndRegelAndProduct(orderNumber, regel, product);
                            } catch (DataIntegrityViolationException e) {
                                Throwable rootCause = e.getRootCause();
                                if (rootCause instanceof SQLIntegrityConstraintViolationException) {
                                    logger.warn("Could not find existing MON suborder for {}", orderNumber, e);
                                    orderDtoListUpdater.accept(allOrdersLoader.get());
                                } else {
                                    logger.error("Could not find existing MON suborder for {}", orderNumber, e);
                                    orderDtoListUpdater.accept(allOrdersLoader.get());
                                }
                            }

                            if (orderDto != null
                                    && !product.equals("L")
                                    && !existingSubOrder.isPresent()
                                    && reminderMap.getOrDefault(orderNumber + "," + product, true)) {
                                try {
                                    Order order = orderRepo.findById(orderDto.getId()).get();
                                    MonSubOrders subOrder = new MonSubOrders();
                                    subOrder.setOrderNumber(orderNumber);
                                    subOrder.setProduct(product);
                                    subOrder.setRegel(regel);
                                    subOrder.setAantal(order.getAantal());
                                    subOrder.setOmsumin(description);
                                    subOrder.setOrder(order);

                                    if (order.getMonSubOrders() != null) {
                                        order.getMonSubOrders().add(subOrder);
                                    } else {
                                        List<MonSubOrders> subOrdersList = new ArrayList<>();
                                        subOrdersList.add(subOrder);
                                        order.setMonSubOrders(subOrdersList);
                                    }
                                    try {
                                        orderRepo.save(order);
                                    } catch (ObjectOptimisticLockingFailureException | StaleStateException e) {
                                        logger.warn("Could not save MON suborder for order {} due to a concurrent update", order.getId(), e);
                                    } catch (DataIntegrityViolationException e) {
                                        Throwable rootCause = e.getRootCause();
                                        if (rootCause instanceof SQLIntegrityConstraintViolationException) {
                                            logger.warn("Could not save MON suborder for order {} due to a database constraint violation", order.getId(), e);
                                            orderDtoListUpdater.accept(allOrdersLoader.get());
                                        } else {
                                            logger.error("Could not save MON suborder for order {}", order.getId(), e);
                                            orderDtoListUpdater.accept(allOrdersLoader.get());
                                        }
                                    }
                                    ordersMap.put(orderNumber + "," + regel, orderMapper.apply(order));
                                    reminderMap.put(orderNumber + "," + product, false);
                                } catch (Exception e) {
                                    logger.error("Failed to create MON suborder for order {}", orderNumber, e);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("Failed to create MON suborders from ERP data", e);
                    orderDtoListUpdater.accept(allOrdersLoader.get());
                }
            }
        } catch (Exception e) {
            logger.error("Failed to prepare MON suborders from ERP data", e);
            orderDtoListUpdater.accept(allOrdersLoader.get());
        }
    }
}

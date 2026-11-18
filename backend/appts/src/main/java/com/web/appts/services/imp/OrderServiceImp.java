
package com.web.appts.services.imp;

import com.web.appts.DTO.*;
import com.web.appts.client.OrdersFeignClient;
import com.web.appts.entities.*;
import com.web.appts.exceptions.ResourceNotFoundException;
import com.web.appts.repositories.*;
import com.web.appts.services.ArchivedOrdersService;
import com.web.appts.services.OrderCustomOrderService;
import com.web.appts.services.OrderErpMaintenanceService;
import com.web.appts.services.OrderErpConnectionRecoveryService;
import com.web.appts.services.OrderErpSyncService;
import com.web.appts.services.OrderErpQueryMapperService;
import com.web.appts.services.OrderExportService;
import com.web.appts.services.OrderMonSubOrderSyncService;
import com.web.appts.services.OrderMutationService;
import com.web.appts.services.OrderService;
import com.web.appts.services.OrderTransportLinkService;
import com.web.appts.services.OrderWorkflowService;

import java.io.OutputStream;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImp implements OrderService {
    Map<String, OrderDto> ordersMap = new ConcurrentHashMap();
    Map<String, OrderPreserves> ordersPreserveMap = new ConcurrentHashMap();

    Map<String, OrderDto> archivedOrdersMap = new HashMap();
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImp.class);
    List<OrderDto> orderDtoList;
    private final ModelMapper modelMapper;
    private final OrdersFeignClient ordersFeignClient;
    private final OrderRepo orderRepo;
    private final OrderPreservesRepo orderPreservesRepo;
    private final OrderCustomOrderService orderCustomOrderService;
    private final OrderExportService orderExportService;
    private final OrderArchiveCoordinator orderArchiveCoordinator;
    private final OrderTransportLinkService orderTransportLinkService;
    private final OrderErpSyncService orderErpSyncService;
    private final OrderErpMaintenanceService orderErpMaintenanceService;
    private final OrderErpConnectionRecoveryService orderErpConnectionRecoveryService;
    private final OrderErpQueryMapperService orderErpQueryMapperService;
    private final OrderMonSubOrderSyncService orderMonSubOrderSyncService;
    private final OrderMutationService orderMutationService;
    private final OrderWorkflowService orderWorkflowService;
    private final OrderParentCoordinator orderParentCoordinator;
    private final OrderErpTextUpdateApplier orderErpTextUpdateApplier;
    private final ArchivedOrdersService archivedOrdersService;
    private final OrderSMERepo orderSMERepo;
    private final OrderSPURepo orderSPURepo;
    public OrderServiceImp(
            ModelMapper modelMapper,
            OrdersFeignClient ordersFeignClient,
            OrderRepo orderRepo,
            OrderPreservesRepo orderPreservesRepo,
            OrderCustomOrderService orderCustomOrderService,
            OrderExportService orderExportService,
            OrderArchiveCoordinator orderArchiveCoordinator,
            OrderTransportLinkService orderTransportLinkService,
            OrderErpSyncService orderErpSyncService,
            OrderErpMaintenanceService orderErpMaintenanceService,
            OrderErpConnectionRecoveryService orderErpConnectionRecoveryService,
            OrderErpQueryMapperService orderErpQueryMapperService,
            OrderMonSubOrderSyncService orderMonSubOrderSyncService,
            OrderMutationService orderMutationService,
            OrderWorkflowService orderWorkflowService,
            OrderParentCoordinator orderParentCoordinator,
            OrderErpTextUpdateApplier orderErpTextUpdateApplier,
            ArchivedOrdersService archivedOrdersService,
            OrderSMERepo orderSMERepo,
            OrderSPURepo orderSPURepo
    ) {
        this.modelMapper = modelMapper;
        this.ordersFeignClient = ordersFeignClient;
        this.orderRepo = orderRepo;
        this.orderPreservesRepo = orderPreservesRepo;
        this.orderCustomOrderService = orderCustomOrderService;
        this.orderExportService = orderExportService;
        this.orderArchiveCoordinator = orderArchiveCoordinator;
        this.orderTransportLinkService = orderTransportLinkService;
        this.orderErpSyncService = orderErpSyncService;
        this.orderErpMaintenanceService = orderErpMaintenanceService;
        this.orderErpConnectionRecoveryService = orderErpConnectionRecoveryService;
        this.orderErpQueryMapperService = orderErpQueryMapperService;
        this.orderMonSubOrderSyncService = orderMonSubOrderSyncService;
        this.orderMutationService = orderMutationService;
        this.orderWorkflowService = orderWorkflowService;
        this.orderParentCoordinator = orderParentCoordinator;
        this.orderErpTextUpdateApplier = orderErpTextUpdateApplier;
        this.archivedOrdersService = archivedOrdersService;
        this.orderSMERepo = orderSMERepo;
        this.orderSPURepo = orderSPURepo;
    }

    private List<OrderDto> cacheOrders(List<Order> orders) {
        List<OrderDto> orderDtos = orders.stream().map(order -> {
            order.getDepartments().sort(Comparator.comparingInt(OrderDepartment::getDepId));
            return this.orderToDto(order);
        }).collect(Collectors.toList());
        for (OrderDto orderDto : orderDtos) {
            this.ordersMap.put(orderDto.getOrderNumber() + "," + orderDto.getRegel(), orderDto);
        }
        return orderDtos;
    }

    private List<OrderDto> getCachedOrders() {
        this.orderDtoList = new ArrayList<>(this.ordersMap.values());
        for (OrderDto orderDto : this.orderDtoList) {
            orderDto.getDepartments().sort(Comparator.comparingInt(OrderDepartment::getDepId));
        }
        return this.orderDtoList;
    }

    public Map<String, OrderDto> getMap() {
        if (this.ordersMap.isEmpty()) {
            cacheOrders(this.orderRepo.findAll());
        }
        return this.ordersMap;
    }

    @Transactional
    public Boolean createOrder(OrderDto orderDto) {
        return orderMutationService.createOrder(orderDto);
    }

    @Override
    public CustomOrderDto createCustomOrder(CustomOrderDto customOrderDto) {
        return orderCustomOrderService.createCustomOrder(customOrderDto);
    }

    @Override
    public DeleteCustOrderDto deleteCustomOrder(CustomOrderDto customOrderDto) {
        return orderCustomOrderService.deleteCustomOrder(customOrderDto);
    }

    @Override
    public List<CustomOrderDto> getAllCustomOrders() {
        return orderCustomOrderService.getAllCustomOrders();
    }

    @Transactional
    public Boolean archiveOrder(OrderDto orderDto) {
        return this.orderArchiveCoordinator.archiveOrder(orderDto);
    }

    @Transactional
    public void moveToArchive(List<Integer> ids) {
        this.orderArchiveCoordinator.moveToArchive(ids, ordersMap, ordersPreserveMap);
    }

    @Transactional
    public List<Integer> preserveBeforeDelete(Set<String> orderNumbers) {
        return this.orderArchiveCoordinator.preserveBeforeDelete(orderNumbers, ordersMap, ordersPreserveMap);
    }

    @Transactional
    public void deleteOrderData(List<Integer> ids) {
        orderMutationService.deleteOrderData(ids, ordersMap);
    }

    public synchronized List<OrderDto> updateOrder(OrderDto orderDto, Integer orderId, Boolean flowUpdate) {
        this.orderDtoList = orderMutationService.updateOrder(orderDto, orderId, flowUpdate, ordersMap, this::checkMap);
        return this.orderDtoList;
    }

    @Transactional
    public List<OrderDto> updateOrderColors(String orderNumber, String orderDep, String orderStatus, String flowVal) {
        return orderWorkflowService.updateOrderColors(
                orderNumber,
                orderDep,
                orderStatus,
                flowVal,
                ordersMap,
                this::checkMap
        );
    }

    public void deleteOrder(Integer orderId) {
        Order order = (Order) this.orderRepo.findById(orderId).orElseThrow(() -> {
            return new ResourceNotFoundException("Order", "id", (long) orderId);
        });
        this.orderRepo.delete(order);
    }

    public OrderDto getOrderById(Integer orderId) {
        List<OrderDto> lo = this.checkMap();
        Order order = (Order) this.orderRepo.findById(orderId).orElseThrow(() -> {
            return new ResourceNotFoundException("User", "id", (long) orderId);
        });
        return this.orderToDto(order);
    }

    public List<OrderDto> getOrdersByUser(String user) {
        if (this.ordersMap.isEmpty()) {
            List<Order> ordersByUser = this.orderRepo.findByUser(user);
            List<OrderDto> orderDtos = (List) ordersByUser.stream().map((order) -> {
                return this.orderToDto(order);
            }).collect(Collectors.toList());
            return orderDtos;
        } else {
            this.orderDtoList = new ArrayList();
            Iterator var2 = this.ordersMap.entrySet().iterator();

            while (var2.hasNext()) {
                Map.Entry<String, OrderDto> entry = (Map.Entry) var2.next();
                if (((OrderDto) entry.getValue()).getUser().equals(user)) {
                    OrderDto orderDto = (OrderDto) entry.getValue();
                    this.orderDtoList.add(orderDto);
                }
            }

            return this.orderDtoList;
        }
    }

    @PostConstruct
    @Transactional
    public void init() {
        getAllPreservedOrders();
        getAllOrders();
    }

    public List<OrderDto> getAllOrders() {
        List<OrderDto> localOrders = loadCachedOrders();
        return localOrders.isEmpty() ? this.getCRMOrders() : localOrders;
    }

    private List<OrderDto> loadCachedOrders() {
        if (this.ordersMap.isEmpty()) {
            this.orderDtoList = cacheOrders(this.orderRepo.findAll());
            return this.orderDtoList;
        }
        return getCachedOrders();
    }

    public void getAllPreservedOrders() {
        if (ordersPreserveMap.isEmpty()) {
            List<OrderPreserves> orderPreserves = orderPreservesRepo.findAll();
            for (OrderPreserves orderPreserve : orderPreserves) {
                ordersPreserveMap.put(orderPreserve.getOrderNumber() + "," + orderPreserve.getRegel(), orderPreserve);
            }
        }
    }

    @Transactional
    public List<OrderDto> getOrdersByRegel(String regel) {
		List<Order> orders = orderRepo.findByRegel(regel);
		return orders.stream().map(this::orderToDto).collect(Collectors.toList());
	}

    public List<OrderDto> checkMap() {
        if (this.ordersMap.isEmpty()) {
            List<Order> allOrders = this.orderRepo.findAll();
            if (!allOrders.isEmpty() && allOrders != null) {
                return cacheOrders(allOrders);
            }
            return this.getCRMOrders();
        }

        if (this.orderRepo.count() != this.ordersMap.size()) {
            this.ordersMap.clear();
            cacheOrders(this.orderRepo.findAll());
        }
        return getCachedOrders();
    }

    @Override
    public void verifyCRMOrders() {
        getCRMOrders();
    }

    public void removingSameArchivedOrders() {
        this.orderArchiveCoordinator.removeSameArchivedOrders(ordersMap);
    }

    @Async("taskExecutor")
    @Scheduled(initialDelay = 300000, fixedRate = 300000)
    public void markExpired() {
        if (!ordersMap.isEmpty()) {
            orderErpSyncService.execute(
                    "markExpired",
                    () -> {
                        markExpiredInner();
                        return null;
                    },
                    this::recoverErpConnection,
                    this::exitProgram
            );
        }
    }

    @Async("taskExecutor")
    @Scheduled(initialDelay = 370000, fixedRate = 370000)
    @Override
    public void checkOrderExistence() {
        if (!ordersMap.isEmpty()) {
            orderErpSyncService.execute(
                    "checkOrderExistence",
                    () -> {
                        checkOrderExistenceInner();
                        return null;
                    },
                    this::recoverErpConnection,
                    this::exitProgram
            );
        }
    }

    public void deleteOrderIds(List<Integer> idsToDelete) {
        if (!ordersMap.isEmpty()) {
            orderErpSyncService.execute(
                    "deleteOrderIds",
                    () -> {
                        deleteOrderIdsInner(idsToDelete);
                        return null;
                    },
                    this::recoverErpConnection,
                    this::exitProgram
            );
        }
    }

    private void exitProgram() {
    }

    public void markExpiredInner() {
        orderErpMaintenanceService.markExpiredOrders(
                ordersMap,
                order -> updateOrder(order, order.getId(), false),
                this::moveToArchive
        );
    }


    public void checkOrderExistenceInner() {
        orderErpMaintenanceService.checkOrderExistence(ordersMap, this::deleteOrderIds);
    }

    private void deleteOrderIdsInner(List<Integer> orderIdsToDelete) {
        orderErpMaintenanceService.reconcileDeletedOrders(
                orderIdsToDelete,
                ordersMap,
                ordersPreserveMap
        );
    }


    public List<OrderDto> getCRMOrders() {
        return orderErpSyncService.getCRMOrders(
                this::getCRMOrdersInner,
                this::recoverErpConnection,
                this::exitProgram
        );
    }

    private void recoverErpConnection() {
        orderErpConnectionRecoveryService.recover();
    }

    public List<OrderDto> getCRMOrdersInner() {
        this.orderDtoList = orderErpQueryMapperService.getCRMOrdersInner(
                ordersMap,
                ordersPreserveMap,
                this::loadCachedOrders
        );
        return this.orderDtoList;
    }
    @Override
    public void adjustParentOrders() {
        orderParentCoordinator.adjustParentOrders(
                ordersMap,
                order -> updateOrder(order, order.getId(), false)
        );
    }


    public void updateProductNotes() {
        orderErpSyncService.execute(
                "updateProductNotes",
                () -> {
                    updateProductNotesInner();
                    return null;
                },
                this::recoverErpConnection,
                this::exitProgram
        );
    }

    public void updateProductNotesInner() {
        try {
            List<String> formattedOrders = new ArrayList<>();
            for (Map.Entry<String, OrderDto> entry : ordersMap.entrySet()) {
                String orderNumbers = entry.getValue().getOrderNumber();
                formattedOrders.add("'" + orderNumbers + "'");
            }

            if (!formattedOrders.isEmpty()) {
                String stringOfOrdersWithCommaAndQuotations = String.join(",", formattedOrders);
                List<OrdersFeignData> dataList = ordersFeignClient.sendAndReceiveProductNotes(stringOfOrdersWithCommaAndQuotations);
                orderErpTextUpdateApplier.applyProductNotes(
                        dataList,
                        ordersMap,
                        orderDto -> updateOrder(orderDto, orderDto.getId(), false)
                );
            }
        } catch (Exception e) {
            logger.error("Failed to prepare ERP product-note update", e);
        }
    }

    @Override
    public List<OrderDto> updateAllTekst(String orderNumOrIds) {
        this.orderDtoList = orderMutationService.updateAllTekst(orderNumOrIds, ordersMap);
        return this.orderDtoList;
    }

    @Override
    @Transactional
    public Map<String, OrderDto> createMonSub() {
        return orderErpSyncService.execute(
                "createMonSub",
                this::createMonSubInner,
                this::recoverErpConnection,
                this::exitProgram
        );
    }

    public Map<String, OrderDto> createMonSubInner() {
        orderMonSubOrderSyncService.createMonSubOrders(
                ordersMap,
                this::getAllOrders,
                orders -> this.orderDtoList = orders,
                this::orderToDto
        );
        return null;
    }

    @Transactional
    public Boolean updateTraColors(String ids, Long entryId) {
        return orderTransportLinkService.updateTraColors(ids, ordersMap);
    }

    public void generateExcelFile(OutputStream outputStream) {
        orderExportService.generateExcelFile(getAllOrders(), outputStream);
    }

    public Order dtoToOrder(OrderDto orderDto) {
        Order order = (Order) this.modelMapper.map(orderDto, Order.class);
        return order;
    }

    @Transactional
    public OrderDto orderToDto(Order order) {
        OrderDto orderDto = (OrderDto) this.modelMapper.map(order, OrderDto.class);
        return orderDto;
    }
}

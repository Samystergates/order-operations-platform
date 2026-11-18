package com.web.appts.services.imp;

import com.web.appts.DTO.ArchivedOrdersDto;
import com.web.appts.DTO.OrderDto;
import com.web.appts.DTO.OrderResultSetVals;
import com.web.appts.client.OrdersFeignClient;
import com.web.appts.entities.OrderPreserves;
import com.web.appts.entities.OrderSME;
import com.web.appts.entities.OrderSPU;
import com.web.appts.repositories.OrderSMERepo;
import com.web.appts.repositories.OrderSPURepo;
import com.web.appts.services.ArchivedOrdersService;
import com.web.appts.services.OrderErpQueryMapperService;
import com.web.appts.services.OrderMutationService;
import org.hibernate.StaleStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class OrderErpQueryMapperServiceImp implements OrderErpQueryMapperService {
    private static final Logger logger = LoggerFactory.getLogger(OrderErpQueryMapperServiceImp.class);
    private static final String ODBC_INVALID_BUFFER_ERROR = "[Microsoft][ODBC Driver Manager] Invalid string or buffer length";

    private final OrdersFeignClient ordersFeignClient;
    private final ArchivedOrdersService archivedOrdersService;
    private final OrderSMERepo orderSMERepo;
    private final OrderSPURepo orderSPURepo;
    private final OrderMutationService orderMutationService;

    public OrderErpQueryMapperServiceImp(
            OrdersFeignClient ordersFeignClient,
            ArchivedOrdersService archivedOrdersService,
            OrderSMERepo orderSMERepo,
            OrderSPURepo orderSPURepo,
            OrderMutationService orderMutationService
    ) {
        this.ordersFeignClient = ordersFeignClient;
        this.archivedOrdersService = archivedOrdersService;
        this.orderSMERepo = orderSMERepo;
        this.orderSPURepo = orderSPURepo;
        this.orderMutationService = orderMutationService;
    }

    @Override
    public List<OrderDto> getCRMOrdersInner(
            Map<String, OrderDto> ordersMap,
            Map<String, OrderPreserves> ordersPreserveMap,
            Supplier<List<OrderDto>> activeOrdersLoader
    ) {
        boolean adjustParentCalled = false;
        boolean createMonCalled = false;
        boolean productNotesCalled = false;

        try {
            List<OrderResultSetVals> resultSetList = ordersFeignClient.sendAndReceiveOrders();
            try {
                importRows(resultSetList, ordersMap, ordersPreserveMap, activeOrdersLoader);
            } catch (Exception e) {
                logger.error("Failed to map ERP order refresh rows", e);

                if (!adjustParentCalled && !createMonCalled && !productNotesCalled) {
                    adjustParentCalled = true;
                    createMonCalled = true;
                    productNotesCalled = true;
                }
                return activeOrdersLoader.get();
            }

            if (!createMonCalled && !productNotesCalled) {
                createMonCalled = true;
                productNotesCalled = true;
            }
            ordersMap.clear();
            return activeOrdersLoader.get();
        } catch (Exception e) {
            logger.error("Failed to refresh orders from ERP", e);
            if (Objects.equals(e.getMessage(), ODBC_INVALID_BUFFER_ERROR)) {
                throw new RuntimeException(e.getMessage(), e);
            }
            if (!adjustParentCalled || (!createMonCalled && !productNotesCalled)) {
                logger.info("ERP refresh fallback returned existing app orders");
            }
            return activeOrdersLoader.get();
        }
    }

    private void importRows(
            List<OrderResultSetVals> resultSetList,
            Map<String, OrderDto> ordersMap,
            Map<String, OrderPreserves> ordersPreserveMap,
            Supplier<List<OrderDto>> activeOrdersLoader
    ) {
        if (resultSetList == null) {
            return;
        }

        List<OrderResultSetVals> orderResultSetVals = resultSetList.stream()
                .filter(Objects::nonNull)
                .map(this::copyErpRow)
                .collect(Collectors.toList());

        logger.info("main app orders length1: {}", orderResultSetVals.size());
        if (orderResultSetVals.isEmpty()) {
            logger.info("main orders length empty");
            throw new RuntimeException("no order found");
        }

        Set<String> archivedKeys = archivedOrdersService.getAllArchivedOrders()
                .stream()
                .map(this::archiveKey)
                .collect(Collectors.toCollection(HashSet::new));

        int totalOrders = orderResultSetVals.size();
        for (OrderResultSetVals erpRow : orderResultSetVals) {
            logger.info(
                    "main orders length remaining: {} for: {}, {}",
                    totalOrders,
                    erpRow.getOrderNumber(),
                    erpRow.getRegel()
            );
            totalOrders--;
            importRow(erpRow, ordersMap, ordersPreserveMap, archivedKeys, activeOrdersLoader);
        }
    }

    private OrderResultSetVals copyErpRow(OrderResultSetVals resultSet) {
        OrderResultSetVals orderResultSetVal = new OrderResultSetVals();
        orderResultSetVal.setOrderNumber(resultSet.getOrderNumber());
        orderResultSetVal.setOrderType(resultSet.getOrderType());
        orderResultSetVal.setBackOrder(resultSet.getBackOrder());
        orderResultSetVal.setUser(resultSet.getUser());
        orderResultSetVal.setOrganization(resultSet.getOrganization());
        orderResultSetVal.setCustomerName(resultSet.getCustomerName());
        orderResultSetVal.setStreet(resultSet.getStreet());
        orderResultSetVal.setHouseNR(resultSet.getHouseNR());
        orderResultSetVal.setAdditionalAdd(resultSet.getAdditionalAdd());
        orderResultSetVal.setPostCode(resultSet.getPostCode());
        orderResultSetVal.setCity(resultSet.getCity());
        orderResultSetVal.setCountry(resultSet.getCountry());
        orderResultSetVal.setDeliveryDate(resultSet.getDeliveryDate());
        orderResultSetVal.setReferenceInfo(resultSet.getReferenceInfo());
        orderResultSetVal.setCreationDate(resultSet.getCreationDate());
        orderResultSetVal.setModificationDate(resultSet.getModificationDate());
        orderResultSetVal.setVerifierUser(resultSet.getVerifierUser());
        orderResultSetVal.setRegel(resultSet.getRegel());
        orderResultSetVal.setAantal(resultSet.getAantal());
        orderResultSetVal.setProduct(resultSet.getProduct());
        orderResultSetVal.setOmsumin(resultSet.getOmsumin());
        orderResultSetVal.setCdProdGrp(resultSet.getCdProdGrp());
        orderResultSetVal.setZoeknaam(resultSet.getZoeknaam());
        orderResultSetVal.setGel(resultSet.getGel());
        orderResultSetVal.setDeliveryDate2("");
        orderResultSetVal.setFinalOrderNumber(resultSet.getOrderNumber());
        logger.debug("Mapped ERP order {}", resultSet.getOrderNumber());
        return orderResultSetVal;
    }

    private void importRow(
            OrderResultSetVals erpRow,
            Map<String, OrderDto> ordersMap,
            Map<String, OrderPreserves> ordersPreserveMap,
            Set<String> archivedKeys,
            Supplier<List<OrderDto>> activeOrdersLoader
    ) {
        String orderNumber = erpRow.getOrderNumber();
        String regel = erpRow.getRegel();
        String orderKey = orderKey(orderNumber, regel);

        if (archivedKeys.contains(orderKey(erpRow.getFinalOrderNumber(), regel))) {
            logger.debug("ERP order {} exists in archive; preserving current archive behavior", orderKey);
        }

        if (!ordersMap.containsKey(orderKey)) {
            createMissingOrder(erpRow, ordersMap, ordersPreserveMap);
        }

        if (ordersMap.containsKey(orderKey)) {
            updateExistingOrder(erpRow, ordersMap, activeOrdersLoader);
        }
    }

    private void createMissingOrder(
            OrderResultSetVals erpRow,
            Map<String, OrderDto> ordersMap,
            Map<String, OrderPreserves> ordersPreserveMap
    ) {
        String orderNumber = erpRow.getOrderNumber();
        String regel = erpRow.getRegel();
        String orderKey = orderKey(orderNumber, regel);
        logger.info("not contains order: {}, {}", orderNumber, regel);

        OrderDto orderDto = toOrderDto(erpRow);
        boolean hasExistingParent = ordersMap.values()
                .stream()
                .anyMatch(existingOrder -> orderNumber.equals(existingOrder.getOrderNumber()));
        orderDto.setIsParent(hasExistingParent ? 0 : 1);
        orderDto.setId(nextInMemoryId(ordersMap));

        if (!ordersPreserveMap.isEmpty() && ordersPreserveMap.containsKey(orderKey)) {
            orderMutationService.setupPreservedFlow(orderDto, ordersPreserveMap);
        } else {
            orderMutationService.setupDefaultFlow(orderDto);
        }

        if (orderMutationService.createOrder(orderDto)) {
            ordersMap.put(orderKey, orderDto);
            logger.info("CREATED ORDER : {}", orderDto.getOrderNumber());
        } else {
            logger.info("Failed to create record in app");
        }
    }

    private void updateExistingOrder(
            OrderResultSetVals erpRow,
            Map<String, OrderDto> ordersMap,
            Supplier<List<OrderDto>> activeOrdersLoader
    ) {
        String orderNumber = erpRow.getOrderNumber();
        String regel = erpRow.getRegel();
        String orderKey = orderKey(orderNumber, regel);
        OrderDto existingOrderDto = ordersMap.get(orderKey);
        OrderDto orderDto = toOrderDto(erpRow);
        orderDto.setId(existingOrderDto.getId());
        orderDto.setIsParent(existingOrderDto.getIsParent());
        orderDto.setRegel(existingOrderDto.getRegel());

        Map<String, Boolean> fieldsCheckMap = checkForFieldsChange(existingOrderDto, orderDto);
        fieldsCheckMap.forEach((key, value) -> logger.debug("ERP field change {}={}", key, value));

        if (requiresFlowRebuild(fieldsCheckMap)) {
            rebuildOrderFlow(orderDto, existingOrderDto, fieldsCheckMap, ordersMap);
            return;
        }

        if (requiresOrderUpdate(fieldsCheckMap, existingOrderDto, erpRow.getDeliveryDate2())) {
            updateOrderFromErp(orderDto, existingOrderDto, fieldsCheckMap, ordersMap, activeOrdersLoader);
        }
    }

    private OrderDto toOrderDto(OrderResultSetVals erpRow) {
        OrderDto orderDto = new OrderDto();
        orderDto.setOrderNumber(erpRow.getOrderNumber());
        orderDto.setRegel(erpRow.getRegel());
        orderDto.setOrderType(erpRow.getOrderType());
        orderDto.setBackOrder(erpRow.getBackOrder());
        orderDto.setCdProdGrp(erpRow.getCdProdGrp());
        orderDto.setZoeknaam(erpRow.getZoeknaam());
        orderDto.setUser(erpRow.getUser());
        orderDto.setOrganization(erpRow.getOrganization());
        orderDto.setCustomerName(erpRow.getCustomerName());
        orderDto.setStreet(erpRow.getStreet());
        orderDto.setHouseNR(erpRow.getHouseNR());
        orderDto.setAdditionalAdd(erpRow.getAdditionalAdd());
        orderDto.setPostCode(erpRow.getPostCode());
        orderDto.setCity(erpRow.getCity());
        orderDto.setCountry(erpRow.getCountry());
        orderDto.setDeliveryDate(erpRow.getDeliveryDate() == null ? "" : erpRow.getDeliveryDate());
        orderDto.setReferenceInfo(erpRow.getReferenceInfo());
        orderDto.setCreationDate(erpRow.getCreationDate());
        orderDto.setModificationDate(erpRow.getModificationDate());
        orderDto.setVerifierUser(erpRow.getVerifierUser());
        orderDto.setAantal(erpRow.getAantal());
        orderDto.setGel(erpRow.getGel());
        orderDto.setProduct(erpRow.getProduct());
        orderDto.setOmsumin(erpRow.getOmsumin());
        return orderDto;
    }

    private void rebuildOrderFlow(
            OrderDto orderDto,
            OrderDto existingOrderDto,
            Map<String, Boolean> fieldsCheckMap,
            Map<String, OrderDto> ordersMap
    ) {
        try {
            logger.info(
                    "ERP flow rebuild for order {} because cdProdGrpChanged={}, orderTypeChanged={}",
                    orderDto.getOrderNumber(),
                    fieldsCheckMap.getOrDefault("cdProdGrp", false),
                    fieldsCheckMap.getOrDefault("orderType", false)
            );
            orderMutationService.deleteOrderData(Collections.singletonList(existingOrderDto.getId()), ordersMap);
            logger.info("DELETING : {}", existingOrderDto.getId());
        } catch (ObjectOptimisticLockingFailureException | StaleStateException e) {
            logger.warn("Optimistic locking failed while deleting ERP changed order {}", existingOrderDto.getId(), e);
        } catch (DataIntegrityViolationException e) {
            logDataIntegrityViolation("delete", existingOrderDto.getId(), e);
        }

        orderMutationService.setupDefaultFlow(orderDto);
        try {
            orderMutationService.createOrder(orderDto);
        } catch (ObjectOptimisticLockingFailureException | StaleStateException e) {
            logger.warn("Optimistic locking failed while recreating ERP changed order {}", orderDto.getId(), e);
        } catch (DataIntegrityViolationException e) {
            logDataIntegrityViolation("create", orderDto.getId(), e);
        }
        ordersMap.put(orderKey(orderDto.getOrderNumber(), orderDto.getRegel()), orderDto);
    }

    private void updateOrderFromErp(
            OrderDto orderDto,
            OrderDto existingOrderDto,
            Map<String, Boolean> fieldsCheckMap,
            Map<String, OrderDto> ordersMap,
            Supplier<List<OrderDto>> activeOrdersLoader
    ) {
        try {
            logger.info(
                    "orderDto updating: {}, {}, {}, {}",
                    orderDto.getOrderNumber(),
                    orderDto.getRegel(),
                    orderDto.getBackOrder(),
                    orderDto.getSme()
            );
            orderMutationService.updateOrder(orderDto, orderDto.getId(), false, ordersMap, activeOrdersLoader);
            reconcileBackOrderDepartments(orderDto, existingOrderDto, fieldsCheckMap, ordersMap, activeOrdersLoader);
            logger.info("UPDATING : {}", orderDto.getId());
        } catch (ObjectOptimisticLockingFailureException | StaleStateException e) {
            logger.warn("Optimistic locking failed while updating ERP changed order {}", orderDto.getId(), e);
        } catch (DataIntegrityViolationException e) {
            logDataIntegrityViolation("update", orderDto.getId(), e);
        }
    }

    private void reconcileBackOrderDepartments(
            OrderDto orderDto,
            OrderDto existingOrderDto,
            Map<String, Boolean> fieldsCheckMap,
            Map<String, OrderDto> ordersMap,
            Supplier<List<OrderDto>> activeOrdersLoader
    ) {
        if (!fieldsCheckMap.getOrDefault("backOrder", false)) {
            return;
        }

        logger.info("yes backorder changed: {}", orderDto.getOrderNumber());
        if ("B".equals(existingOrderDto.getSme())) {
            OrderSME orderSME = orderSMERepo.findByOrderNumberAndRegel(orderDto.getOrderNumber(), orderDto.getRegel());
            orderDto.setSme(orderSME != null ? "R" : "");
            orderMutationService.updateOrder(orderDto, orderDto.getId(), true, ordersMap, activeOrdersLoader);
        }

        if ("B".equals(existingOrderDto.getSpu())) {
            orderDto.setSpu("");
            orderMutationService.updateOrder(orderDto, orderDto.getId(), true, ordersMap, activeOrdersLoader);

            OrderSPU orderSPU = orderSPURepo.findByOrderNumberAndRegel(orderDto.getOrderNumber(), orderDto.getRegel());
            orderDto.setSpu(orderSPU != null ? "R" : "");
            orderMutationService.updateOrder(orderDto, orderDto.getId(), true, ordersMap, activeOrdersLoader);
        }
    }

    private boolean requiresFlowRebuild(Map<String, Boolean> fieldsCheckMap) {
        return fieldsCheckMap.getOrDefault("cdProdGrp", false)
                || fieldsCheckMap.getOrDefault("orderType", false);
    }

    private boolean requiresOrderUpdate(
            Map<String, Boolean> fieldsCheckMap,
            OrderDto existingOrderDto,
            String deliveryDate2
    ) {
        return fieldsCheckMap.getOrDefault("aantal", false)
                || fieldsCheckMap.getOrDefault("custName", false)
                || fieldsCheckMap.getOrDefault("product", false)
                || fieldsCheckMap.getOrDefault("reference", false)
                || fieldsCheckMap.getOrDefault("organization", false)
                || fieldsCheckMap.getOrDefault("city", false)
                || fieldsCheckMap.getOrDefault("country", false)
                || fieldsCheckMap.getOrDefault("backOrder", false)
                || (!Objects.equals(existingOrderDto.getDeliveryDate(), deliveryDate2) && deliveryDate2 != null && !deliveryDate2.isEmpty());
    }

    private Map<String, Boolean> checkForFieldsChange(OrderDto existing, OrderDto orderDto) {
        Map<String, Boolean> checkFieldsMap = new HashMap<>();
        checkFieldsMap.put("cdProdGrp", false);
        checkFieldsMap.put("orderType", false);

        if (!Objects.equals(existing.getOrderType(), orderDto.getOrderType())) {
            checkFieldsMap.put("orderType", true);
        }
        if (!Objects.equals(existing.getOrganization(), orderDto.getOrganization())) {
            checkFieldsMap.put("organization", true);
        }
        if (!Objects.equals(existing.getCity(), orderDto.getCity())) {
            checkFieldsMap.put("city", true);
        }
        if (!Objects.equals(existing.getCountry(), orderDto.getCountry())) {
            checkFieldsMap.put("country", true);
        }
        if (!Objects.equals(existing.getProduct(), orderDto.getProduct())) {
            checkFieldsMap.put("product", true);
        }
        if (!Objects.equals(existing.getCdProdGrp(), orderDto.getCdProdGrp())) {
            checkFieldsMap.put("cdProdGrp", true);
        }
        if (!Objects.equals(existing.getBackOrder(), orderDto.getBackOrder())) {
            checkFieldsMap.put("backOrder", true);
        }
        if (!Objects.equals(existing.getAantal(), orderDto.getAantal())) {
            checkFieldsMap.put("aantal", true);
        }
        if (!Objects.equals(existing.getCustomerName(), orderDto.getCustomerName())) {
            checkFieldsMap.put("custName", true);
        }
        if (!Objects.equals(existing.getReferenceInfo(), orderDto.getReferenceInfo())) {
            checkFieldsMap.put("reference", true);
        }

        return checkFieldsMap;
    }

    private int nextInMemoryId(Map<String, OrderDto> ordersMap) {
        return ordersMap.values().stream().mapToInt(OrderDto::getId).max().orElse(0) + 1;
    }

    private String archiveKey(ArchivedOrdersDto archivedOrder) {
        return orderKey(archivedOrder.getOrderNumber(), archivedOrder.getRegel());
    }

    private String orderKey(String orderNumber, String regel) {
        return orderNumber + "," + regel;
    }

    private void logDataIntegrityViolation(String action, int orderId, DataIntegrityViolationException e) {
        Throwable rootCause = e.getRootCause();
        if (rootCause instanceof SQLIntegrityConstraintViolationException) {
            logger.warn("SQL constraint violation during ERP {} for order {}: {}", action, orderId, rootCause.getMessage(), e);
        } else {
            logger.warn("Data integrity violation during ERP {} for order {}", action, orderId, e);
        }
    }
}

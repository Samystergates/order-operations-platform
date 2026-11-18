package com.web.appts.services.imp;

import com.web.appts.DTO.OrderDto;
import com.web.appts.entities.Order;
import com.web.appts.entities.OrderDepartment;
import com.web.appts.entities.OrderPreserves;
import com.web.appts.exceptions.ResourceNotFoundException;
import com.web.appts.repositories.OrderPreservesRepo;
import com.web.appts.repositories.OrderRepo;
import com.web.appts.services.OrderMutationService;
import org.hibernate.StaleStateException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class OrderMutationServiceImp implements OrderMutationService {
    private static final Logger logger = LoggerFactory.getLogger(OrderMutationServiceImp.class);

    private final ModelMapper modelMapper;
    private final OrderRepo orderRepo;
    private final OrderPreservesRepo orderPreservesRepo;
    private final OrderArchiveCoordinator orderArchiveCoordinator;
    private final OrderFlowUpdateApplier orderFlowUpdateApplier;

    public OrderMutationServiceImp(
            ModelMapper modelMapper,
            OrderRepo orderRepo,
            OrderPreservesRepo orderPreservesRepo,
            OrderArchiveCoordinator orderArchiveCoordinator,
            OrderFlowUpdateApplier orderFlowUpdateApplier
    ) {
        this.modelMapper = modelMapper;
        this.orderRepo = orderRepo;
        this.orderPreservesRepo = orderPreservesRepo;
        this.orderArchiveCoordinator = orderArchiveCoordinator;
        this.orderFlowUpdateApplier = orderFlowUpdateApplier;
    }

    private List<OrderDto> orderDtoList;
    @Transactional
    public Boolean createOrder(OrderDto orderDto) {
        Order order = this.dtoToOrder(orderDto);
        Order savedOrder = null;
        try {
            savedOrder = (Order) this.orderRepo.save(order);
        } catch (ObjectOptimisticLockingFailureException | StaleStateException e) {
            logger.warn("Could not create order {} due to a concurrent update", order.getId(), e);
        } catch (DataIntegrityViolationException e) {
            Throwable rootCause = e.getRootCause();
            if (rootCause instanceof SQLIntegrityConstraintViolationException) {
                logger.warn("Could not create order {} due to a database constraint violation", order.getId(), e);
                return savedOrder != null;
            } else {
                logger.error("Could not create order {}", order.getId(), e);
                return savedOrder != null;
            }
        }
        return savedOrder != null;
    }

    @Transactional
    public synchronized List<OrderDto> updateOrder(OrderDto orderDto, Integer orderId, Boolean flowUpdate, Map<String, OrderDto> ordersMap, Supplier<List<OrderDto>> activeOrdersLoader) {
        List<OrderDto> lo = activeOrdersLoader.get();
        Order order = (Order) this.orderRepo.findById(orderId).orElseThrow(() -> {
            return new ResourceNotFoundException("Order", "id", (long) orderId);
        });
        order.setDeliveryDate(orderDto.getDeliveryDate());
        order.setReferenceInfo(orderDto.getReferenceInfo());
        order.setAantal(orderDto.getAantal());
        order.setGel(orderDto.getGel());
        order.setProduct(orderDto.getProduct());
        order.setCountry(orderDto.getCountry());
        order.setCity(orderDto.getCity());
        order.setOrganization(orderDto.getOrganization());
        order.setCustomerName(orderDto.getCustomerName());
        order.setIsParent(orderDto.getIsParent());
        order.setTekst(orderDto.getTekst());
        order.setBackOrder(orderDto.getBackOrder());
        if (orderDto.getIsExpired() != null) {
            order.setIsExpired(orderDto.getIsExpired());
        }

        Boolean isSmeOrSpu = false;
        if (flowUpdate) {
            if (!order.hasOnlyOneDifference(this.dtoToOrder(orderDto))) {
                return new ArrayList();
            }
            if (order.getSme() != null && orderDto.getSme() != null && !order.getSme().equals(orderDto.getSme())) {
                isSmeOrSpu = true;
            }
            if (order.getSpu() != null && orderDto.getSpu() != null && !order.getSpu().equals(orderDto.getSpu())) {
                isSmeOrSpu = true;
            }
            orderFlowUpdateApplier.apply(order, orderDto);
        }

        if (order.getCompleted() == null) {
            order.setCompleted("");
        }

        Order updatedOrder = null;
        try {
            if (isSmeOrSpu && flowUpdate) {
                this.updateOneTekst(order);
            }
            updatedOrder = (Order) this.orderRepo.save(order);
            if (!isSmeOrSpu && flowUpdate) {
                this.updateAllTekst(updatedOrder.getOrderNumber(), ordersMap);
            }

            logger.info("order updating: " + updatedOrder.getOrderNumber() + ", " + updatedOrder.getRegel() + ", " + updatedOrder.getBackOrder() + ", " + updatedOrder.getSme());

            OrderDto updatedOrderDto = this.orderToDto(updatedOrder);
            ordersMap.put(updatedOrderDto.getOrderNumber() + "," + updatedOrderDto.getRegel(), updatedOrderDto);
            boolean allOrdersComplete = ordersMap.values().stream().filter((ord) -> {
                return ord.getOrderNumber().equals(updatedOrderDto.getOrderNumber()) && updatedOrderDto.getId() != ord.getId();
            }).allMatch((ord) -> {
                return "C".equals(ord.getCompleted());
            });
            if (updatedOrder.getCompleted().equals("C") && allOrdersComplete) {
                this.orderDtoList = new ArrayList();
                Iterator var13 = ordersMap.entrySet().iterator();

                while (var13.hasNext()) {
                    Map.Entry<String, OrderDto> entry = (Map.Entry) var13.next();
                    OrderDto orderDto2 = (OrderDto) entry.getValue();
                    orderDto2.getDepartments().sort(Comparator.comparingInt(OrderDepartment::getDepId));
                    this.orderDtoList.add(orderDto2);
                }

                return this.orderDtoList;
            } else {
                this.orderDtoList = new ArrayList();
                Iterator var9 = ordersMap.entrySet().iterator();

                while (var9.hasNext()) {
                    Map.Entry<String, OrderDto> entry = (Map.Entry) var9.next();
                    OrderDto orderDto2 = (OrderDto) entry.getValue();
                    orderDto2.getDepartments().sort(Comparator.comparingInt(OrderDepartment::getDepId));
                    this.orderDtoList.add(orderDto2);
                }

                return this.orderDtoList;
            }
        } catch (ObjectOptimisticLockingFailureException | StaleStateException e) {
            logger.warn("Could not update order {} due to a concurrent update", order.getId(), e);
            return this.orderDtoList;
        } catch (DataIntegrityViolationException e) {
            Throwable rootCause = e.getRootCause();
            if (rootCause instanceof SQLIntegrityConstraintViolationException) {
                logger.warn("Could not update order {} due to a database constraint violation", order.getId(), e);
                return this.orderDtoList;
            } else {
                logger.error("Could not update order {}", order.getId(), e);
                return this.orderDtoList;
            }
        }
    }

    public void setupPreservedFlow(OrderDto orderDto, Map<String, OrderPreserves> ordersPreserveMap) {
        List<OrderDepartment> depList = new ArrayList();
        OrderPreserves op = ordersPreserveMap.get(orderDto.getOrderNumber() + "," + orderDto.getRegel());

        if (op.getSme() != null) {
            orderDto.setSme(op.getSme());
            depList.add(new OrderDepartment(2, "SME", op.getSme(), this.dtoToOrder(orderDto)));
        }

        if (op.getSpu() != null) {
            orderDto.setSpu(op.getSpu());
            depList.add(new OrderDepartment(3, "SPU", op.getSpu(), this.dtoToOrder(orderDto)));

        }

        if (op.getTra() != null) {
            orderDto.setTra(op.getTra());
            depList.add(new OrderDepartment(8, "TRA", op.getTra(), this.dtoToOrder(orderDto)));

        }

        if (op.getExp() != null) {
            orderDto.setExp(op.getExp());
            depList.add(new OrderDepartment(9, "EXP", op.getExp(), this.dtoToOrder(orderDto)));

        }

        if (op.getSer() != null) {
            orderDto.setSer(op.getSer());
            depList.add(new OrderDepartment(7, "SER", op.getSer(), this.dtoToOrder(orderDto)));

        }

        if (op.getMonLb() != null) {
            orderDto.setMonLb(op.getMonLb());
            depList.add(new OrderDepartment(4, "MONLB", op.getMonLb(), this.dtoToOrder(orderDto)));

        }

        if (op.getMonTr() != null) {
            orderDto.setMonTr(op.getMonTr());
            depList.add(new OrderDepartment(5, "MONTR", op.getMonTr(), this.dtoToOrder(orderDto)));

        }

        if (op.getMwe() != null) {
            orderDto.setMwe(op.getMwe());
            depList.add(new OrderDepartment(6, "MWE", op.getMwe(), this.dtoToOrder(orderDto)));

        }

        if (op.getBackOrder() != null) {
            orderDto.setBackOrder(op.getBackOrder());

        }

        if (op.getExclamation() != null) {
            orderDto.setExcNote(op.getExcNote());

        }

        if (op.getExcNote() != null) {
            orderDto.setExcNote(op.getExcNote());
        }

        deleteFromPreserve(op.getId(), op.getOrderNumber(), op.getRegel(), ordersPreserveMap);
        orderDto.setDepartments(depList);

    }

    private void deleteFromPreserve(Long id, String orderNumber, String regel, Map<String, OrderPreserves> ordersPreserveMap) {
        try {
            orderPreservesRepo.deleteById(id);
            ordersPreserveMap.remove(orderNumber + "," + regel);
        }catch (Exception e) {
            logger.info("delete preserve order error: "+e.getMessage());
        }

    }

    @Transactional
    public void setupDefaultFlow(OrderDto orderDto) {
        String orderType = orderDto.getOrderType();
        List<OrderDepartment> depList = new ArrayList();
        String wheelOrder = orderDto.getCdProdGrp();
        String pattern = ".*(182|183|184|401|402|410|421|440|441|820|821|822|823|824|825|826|850|851).*";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(wheelOrder);
        if (matcher.find()) {
            orderDto.setSme("");
            orderDto.setSpu("");
            depList.add(new OrderDepartment(2, "SME", "", this.dtoToOrder(orderDto)));
            depList.add(new OrderDepartment(3, "SPU", "", this.dtoToOrder(orderDto)));
        }

        if (orderDto.getCdProdGrp().contains("400") || orderDto.getCdProdGrp().contains("403") ||
                orderDto.getCdProdGrp().contains("404") || orderDto.getCdProdGrp().contains("405") ||
                orderDto.getCdProdGrp().contains("406") || orderDto.getCdProdGrp().contains("407") ||
                orderDto.getCdProdGrp().contains("408") || orderDto.getCdProdGrp().contains("409") ||
                orderDto.getCdProdGrp().contains("411") || orderDto.getCdProdGrp().contains("412") ||
                orderDto.getCdProdGrp().contains("413") || orderDto.getCdProdGrp().contains("414") ||
                orderDto.getCdProdGrp().contains("415") || orderDto.getCdProdGrp().contains("416") ||
                orderDto.getCdProdGrp().contains("417") || orderDto.getCdProdGrp().contains("418") ||
                orderDto.getCdProdGrp().contains("419") || orderDto.getCdProdGrp().contains("420")) {
            if (orderDto.getSme() == null) {
                orderDto.setSme("");
                depList.add(new OrderDepartment(2, "SME", "", this.dtoToOrder(orderDto)));
            }
        }

        if (orderDto.getCdProdGrp().equals("262") || orderDto.getCdProdGrp().equals("263")) {
            if (orderDto.getSpu() == null) {
                orderDto.setSpu("");
                depList.add(new OrderDepartment(3, "SPU", "", this.dtoToOrder(orderDto)));
            }
        }

        if (orderType.equals("LOS")) {
            orderDto.setTra("R");
            depList.add(new OrderDepartment(8, "TRA", "R", this.dtoToOrder(orderDto)));
        }

        if (orderType.equals("LAS")) {
            orderDto.setExp("R");
            depList.add(new OrderDepartment(9, "EXP", "R", this.dtoToOrder(orderDto)));
        }

        if (orderType.equals("LSO")) {
            orderDto.setSer("R");
            depList.add(new OrderDepartment(7, "SER", "R", this.dtoToOrder(orderDto)));
        }

        if (orderType.equals("LSL")) {
            orderDto.setSer("R");
            depList.add(new OrderDepartment(7, "SER", "R", this.dtoToOrder(orderDto)));
        }

        if (orderType.equals("LPL")) {
            orderDto.setSer("R");
            depList.add(new OrderDepartment(7, "SER", "R", this.dtoToOrder(orderDto)));
        }

        if (orderType.equals("MAO")) {
            orderDto.setMonLb("R");
            orderDto.setExp("R");
            depList.add(new OrderDepartment(4, "MONLB", "R", this.dtoToOrder(orderDto)));
            depList.add(new OrderDepartment(9, "EXP", "R", this.dtoToOrder(orderDto)));
        }

        if (orderType.equals("MLO")) {
            orderDto.setMonLb("R");
            orderDto.setTra("R");
            depList.add(new OrderDepartment(4, "MONLB", "R", this.dtoToOrder(orderDto)));
            depList.add(new OrderDepartment(8, "TRA", "R", this.dtoToOrder(orderDto)));
        }

        if (orderType.equals("MWO")) {
            orderDto.setMwe("R");
            depList.add(new OrderDepartment(6, "MWE", "R", this.dtoToOrder(orderDto)));
        }

        if (orderType.equals("MSO")) {
            orderDto.setMonLb("R");
            orderDto.setSer("R");
            depList.add(new OrderDepartment(4, "MONLB", "R", this.dtoToOrder(orderDto)));
            depList.add(new OrderDepartment(7, "SER", "R", this.dtoToOrder(orderDto)));
        }

        if (orderType.equals("MLT")) {
            orderDto.setMonTr("R");
            orderDto.setTra("R");
            depList.add(new OrderDepartment(5, "MONTR", "R", this.dtoToOrder(orderDto)));
            depList.add(new OrderDepartment(8, "TRA", "R", this.dtoToOrder(orderDto)));
        }

        if (orderType.equals("MST")) {
            orderDto.setMonTr("R");
            orderDto.setSer("R");
            depList.add(new OrderDepartment(5, "MONTR", "R", this.dtoToOrder(orderDto)));
            depList.add(new OrderDepartment(7, "SER", "R", this.dtoToOrder(orderDto)));
        }

        if (orderType.equals("LOP")) {
            orderDto.setTra("R");
            depList.add(new OrderDepartment(8, "TRA", "R", this.dtoToOrder(orderDto)));
        }

        if (orderType.equals("LAP")) {
            orderDto.setExp("R");
            depList.add(new OrderDepartment(9, "EXP", "R", this.dtoToOrder(orderDto)));
        }

        if (orderType.equals("LSP")) {
            orderDto.setSer("R");
            depList.add(new OrderDepartment(7, "SER", "R", this.dtoToOrder(orderDto)));
        }

        if (orderType.equals("MAP")) {
            orderDto.setMonLb("R");
            orderDto.setExp("R");
            depList.add(new OrderDepartment(4, "MONLB", "R", this.dtoToOrder(orderDto)));
            depList.add(new OrderDepartment(9, "EXP", "R", this.dtoToOrder(orderDto)));
        }

        if (orderType.equals("MLP")) {
            orderDto.setMonLb("R");
            orderDto.setTra("R");
            depList.add(new OrderDepartment(4, "MONLB", "R", this.dtoToOrder(orderDto)));
            depList.add(new OrderDepartment(8, "TRA", "R", this.dtoToOrder(orderDto)));
        }

        if (orderType.equals("MWP")) {
            orderDto.setMwe("R");
            depList.add(new OrderDepartment(6, "MWE", "R", this.dtoToOrder(orderDto)));
        }

        if (orderType.equals("MSP")) {
            orderDto.setMonLb("R");
            orderDto.setSer("R");
            depList.add(new OrderDepartment(4, "MONLB", "R", this.dtoToOrder(orderDto)));
            depList.add(new OrderDepartment(7, "SER", "R", this.dtoToOrder(orderDto)));
        }

        if (orderType.equals("MSE")) {
            orderDto.setMonTr("R");
            orderDto.setSer("R");
            depList.add(new OrderDepartment(5, "MONTR", "R", this.dtoToOrder(orderDto)));
            depList.add(new OrderDepartment(7, "SER", "R", this.dtoToOrder(orderDto)));
        }

        if (orderType.equals("MLE")) {
            orderDto.setMonTr("R");
            orderDto.setTra("R");
            depList.add(new OrderDepartment(5, "MONTR", "R", this.dtoToOrder(orderDto)));
            depList.add(new OrderDepartment(8, "TRA", "R", this.dtoToOrder(orderDto)));
        }

        if (orderType.equals("WEB")) {
            orderDto.setTra("R");
            depList.add(new OrderDepartment(8, "TRA", "R", this.dtoToOrder(orderDto)));
        }

        if (orderType.equals("BBA")) {
            orderDto.setTra("R");
            depList.add(new OrderDepartment(8, "TRA", "R", this.dtoToOrder(orderDto)));
        }

        orderDto.setDepartments(depList);
    }

    @Override
    public List<OrderDto> updateAllTekst(String orderNumOrIds, Map<String, OrderDto> ordersMap) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = (authentication != null) ? authentication.getName() : "startup";

        if (!userName.equals("startup")) {
            if (orderNumOrIds != null && orderNumOrIds.startsWith("V")) {

                List<Order> orders = ordersMap.values().stream()
                        .filter(o -> o.getOrderNumber().equals(orderNumOrIds))
                        .map(this::dtoToOrder).collect(Collectors.toList());

                for (Order order : orders) {
                    if (order.getTekst() == null) {
                        order.setTekst("");
                    }
                    order.setTekst(updateTekst(order.getTekst(), userName));
                    Order updatedOrder = orderRepo.save(order);
                    OrderDto updatedOrderDto = modelMapper.map(updatedOrder, OrderDto.class);
                    ordersMap.put(updatedOrderDto.getOrderNumber() + "," + updatedOrderDto.getRegel(), updatedOrderDto);

                }
            } else {

                List<Integer> idList = new ArrayList();
                String[] idArray = orderNumOrIds.split(",");
                String[] var7 = idArray;
                int var8 = idArray.length;

                for (int var9 = 0; var9 < var8; ++var9) {
                    String id = var7[var9];
                    String trimmedId = id.trim();
                    if (!trimmedId.isEmpty()) {
                        int parsedId = Integer.parseInt(trimmedId);
                        idList.add(parsedId);
                    }
                }
                for (int id : idList) {
                    Optional<OrderDto> orderDto = ordersMap.values().stream().filter(o -> o.getId() == id).findFirst();
                    if (orderDto.isPresent()) {
                        OrderDto orderDto1 = orderDto.get();
                        if (orderDto1.getTekst() == null) {
                            orderDto1.setTekst("");
                        }
                        Order order = dtoToOrder(orderDto1);
                        order.setTekst(updateTekst(order.getTekst(), userName));
                        Order updatedOrder = orderRepo.save(order);
                        OrderDto updatedOrderDto = modelMapper.map(updatedOrder, OrderDto.class);
                        ordersMap.put(updatedOrderDto.getOrderNumber() + "," + updatedOrderDto.getRegel(), updatedOrderDto);
                    }
                }
            }
        }

        this.orderDtoList = new ArrayList();
        Iterator var13 = ordersMap.entrySet().iterator();

        while (var13.hasNext()) {
            Map.Entry<String, OrderDto> entry = (Map.Entry) var13.next();
            OrderDto orderDto2 = (OrderDto) entry.getValue();
            orderDto2.getDepartments().sort(Comparator.comparingInt(OrderDepartment::getDepId));
            this.orderDtoList.add(orderDto2);
        }

        return this.orderDtoList;
    }

    public void updateOneTekst(Order order) {

        if (order.getTekst() == null) {
            order.setTekst("");
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = (authentication != null) ? authentication.getName() : "startup";

        if (!userName.equals("startup")) {
            order.setTekst(updateTekst(order.getTekst(), userName));
        }


    }

    public static String updateTekst(String originalTekst, String currentUser) {
        LocalDateTime now = LocalDateTime.now();
        String date = now.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        String time = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        String newHeader = String.format("Datum: %s  Tijd: %s  Gebruiker: %s", date, time, currentUser);

        Pattern pattern = Pattern.compile("^Datum:.*?Gebruiker: \\S+", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(originalTekst.trim());

        if (matcher.find()) {
            return matcher.replaceFirst(newHeader);
        } else {
            return newHeader + "\n" + originalTekst;
        }
    }

    @Transactional
    public void deleteOrderData(List<Integer> ids, Map<String, OrderDto> ordersMap) {
        this.orderArchiveCoordinator.deleteOrderData(ids, ordersMap);
    }

    private Order dtoToOrder(OrderDto orderDto) {
        return this.modelMapper.map(orderDto, Order.class);
    }

    private OrderDto orderToDto(Order order) {
        return this.modelMapper.map(order, OrderDto.class);
    }
}

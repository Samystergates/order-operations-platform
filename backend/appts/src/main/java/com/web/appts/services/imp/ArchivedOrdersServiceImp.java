package com.web.appts.services.imp;

import com.web.appts.DTO.ArchivedOrdersDto;
import com.web.appts.DTO.OrderDto;
import com.web.appts.entities.ArchivedOrders;
import com.web.appts.exceptions.ResourceNotFoundException;
import com.web.appts.repositories.ArchivedOrderRepo;
import com.web.appts.services.ArchivedOrdersService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ArchivedOrdersServiceImp implements ArchivedOrdersService {
    private static final Logger logger = LoggerFactory.getLogger(ArchivedOrdersServiceImp.class);

    private final Map<String, ArchivedOrdersDto> archivedOrdersMap = new HashMap<>();
    private final ModelMapper modelMapper;
    private final ArchivedOrderRepo archivedOrdersRepo;
    private List<ArchivedOrdersDto> archivedOrderDtoList;

    public ArchivedOrdersServiceImp(ModelMapper modelMapper, ArchivedOrderRepo archivedOrdersRepo) {
        this.modelMapper = modelMapper;
        this.archivedOrdersRepo = archivedOrdersRepo;
    }

    @Override
    public List<ArchivedOrdersDto> getArchivedOrdersByRegel(String regel) {
        return archivedOrdersRepo.findByRegel(regel)
                .stream()
                .map(this::archivedOrderToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Boolean createArchivedOrder(OrderDto orderDto) {
        String key = archiveKey(orderDto.getOrderNumber(), orderDto.getRegel());
        if (archivedOrdersMap.containsKey(key)) {
            return true;
        }

        ArchivedOrders archivedOrder = orderDtoToArchivedOrder(orderDto);
        ArchivedOrders savedArchivedOrder = archivedOrdersRepo.save(archivedOrder);
        archivedOrdersMap.put(key, archivedOrderToDto(savedArchivedOrder));
        archivedOrderDtoList = null;
        return savedArchivedOrder != null;
    }

    @Override
    public ArchivedOrdersDto getArchivedOrderById(Long orderId) {
        ArchivedOrders order = archivedOrdersRepo.findById(orderId).orElseThrow(() ->
                new ResourceNotFoundException("ArchivedOrders", "id", orderId)
        );
        return archivedOrderToDto(order);
    }

    @Override
    public List<ArchivedOrdersDto> getArchivedOrdersByUser(String user) {
        if (archivedOrdersMap.isEmpty()) {
            List<ArchivedOrdersDto> archivedOrderDtos = archivedOrdersRepo.findByUser(user)
                    .stream()
                    .map(this::archivedOrderToDto)
                    .collect(Collectors.toList());
            return archivedOrderDtos.isEmpty() ? new ArrayList<>() : archivedOrderDtos;
        }

        return archivedOrdersMap.values()
                .stream()
                .filter(order -> user.equals(order.getUser()))
                .collect(Collectors.toList());
    }

    public void deleteFromArchive(String orderNumber, String regel) {
        logger.info("Deleting archive entry at {} for order={}, regel={}", LocalDateTime.now(), orderNumber, regel);

        getAllArchivedOrders().forEach(archivedOrder -> {
            if (orderNumber.equals(archivedOrder.getOrderNumber()) && regel.equals(archivedOrder.getRegel())) {
                try {
                    archivedOrdersRepo.deleteById(archivedOrder.getId());
                } catch (Exception e) {
                    logger.warn("Failed to delete archived order id={}", archivedOrder.getId(), e);
                }
            }
        });

        archivedOrdersMap.entrySet().removeIf(entry ->
                orderNumber.equals(entry.getValue().getOrderNumber()) && regel.equals(entry.getValue().getRegel())
        );
        if (archivedOrderDtoList != null) {
            archivedOrderDtoList.removeIf(order ->
                    orderNumber.equals(order.getOrderNumber()) && regel.equals(order.getRegel())
            );
        }
    }

    @PostConstruct
    @Transactional
    public void init() {
        logger.info("Initializing archived orders cache");
        getAllArchivedOrders();
    }

    @Override
    public List<ArchivedOrdersDto> getAllArchivedOrders() {
        if (archivedOrdersMap.isEmpty()) {
            List<ArchivedOrders> allArchivedOrders = archivedOrdersRepo.findAll();
            if (allArchivedOrders == null || allArchivedOrders.isEmpty()) {
                return new ArrayList<>();
            }

            allArchivedOrders.stream()
                    .map(this::archivedOrderToDto)
                    .forEach(this::putInCache);
        }

        if (archivedOrderDtoList == null || archivedOrderDtoList.size() != archivedOrdersMap.size()) {
            archivedOrderDtoList = new ArrayList<>(archivedOrdersMap.values());
        }

        return archivedOrderDtoList;
    }

    @Override
    public void validateArchiveMap() {
        long dataLength = archivedOrdersRepo.count();
        if (dataLength == archivedOrdersMap.size()) {
            return;
        }

        archivedOrdersMap.clear();
        List<ArchivedOrders> allArchivedOrders = archivedOrdersRepo.findAll();
        if (allArchivedOrders == null || allArchivedOrders.isEmpty()) {
            archivedOrderDtoList = new ArrayList<>();
            return;
        }

        allArchivedOrders.stream()
                .map(this::archivedOrderToDto)
                .forEach(this::putInCache);
        archivedOrderDtoList = new ArrayList<>(archivedOrdersMap.values());
    }

    public ArchivedOrders dtoToArchivedOrder(ArchivedOrdersDto archivedOrderDto) {
        return modelMapper.map(archivedOrderDto, ArchivedOrders.class);
    }

    public ArchivedOrders orderDtoToArchivedOrder(OrderDto orderDto) {
        return modelMapper.map(orderDto, ArchivedOrders.class);
    }

    public ArchivedOrdersDto archivedOrderToDto(ArchivedOrders archivedOrder) {
        return modelMapper.map(archivedOrder, ArchivedOrdersDto.class);
    }

    private void putInCache(ArchivedOrdersDto archivedOrderDto) {
        archivedOrdersMap.put(
                archiveKey(archivedOrderDto.getOrderNumber(), archivedOrderDto.getRegel()),
                archivedOrderDto
        );
    }

    private String archiveKey(String orderNumber, String regel) {
        return orderNumber + "," + regel;
    }
}

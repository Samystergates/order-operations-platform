package com.web.appts.services.imp;

import com.web.appts.DTO.CustomOrderDto;
import com.web.appts.DTO.DeleteCustOrderDto;
import com.web.appts.entities.CustomOrder;
import com.web.appts.entities.TransportOrderLines;
import com.web.appts.repositories.CustomOrderRepo;
import com.web.appts.repositories.TransportOrderLinesRepo;
import com.web.appts.services.OrderCustomOrderService;
import com.web.appts.utils.OrderType;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderCustomOrderServiceImp implements OrderCustomOrderService {
    private final CustomOrderRepo customOrderRepo;
    private final TransportOrderLinesRepo transportOrderLinesRepo;
    private final ModelMapper modelMapper;

    public OrderCustomOrderServiceImp(
            CustomOrderRepo customOrderRepo,
            TransportOrderLinesRepo transportOrderLinesRepo,
            ModelMapper modelMapper
    ) {
        this.customOrderRepo = customOrderRepo;
        this.transportOrderLinesRepo = transportOrderLinesRepo;
        this.modelMapper = modelMapper;
    }

    @Override
    public CustomOrderDto createCustomOrder(CustomOrderDto customOrderDto) {
        CustomOrder customOrder = modelMapper.map(customOrderDto, CustomOrder.class);
        CustomOrder savedCustomOrder = customOrderRepo.save(customOrder);
        return modelMapper.map(savedCustomOrder, CustomOrderDto.class);
    }

    @Override
    public DeleteCustOrderDto deleteCustomOrder(CustomOrderDto customOrderDto) {
        CustomOrder customOrder = modelMapper.map(customOrderDto, CustomOrder.class);
        try {
            customOrderRepo.delete(customOrder);
            boolean isDeleted = !customOrderRepo.existsById(customOrder.getId());
            if (isDeleted) {
                Optional<TransportOrderLines> transportOrderLines =
                        transportOrderLinesRepo.findByOrderIdAndOrderType(customOrder.getId(), OrderType.CUSTOM);
                if (transportOrderLines.isPresent()) {
                    transportOrderLinesRepo.deleteById(transportOrderLines.get().getId());
                    return new DeleteCustOrderDto(transportOrderLines.get(), true);
                }
            }
            return new DeleteCustOrderDto(null, isDeleted);
        } catch (Exception e) {
            return new DeleteCustOrderDto(null, false);
        }
    }

    @Override
    public List<CustomOrderDto> getAllCustomOrders() {
        return customOrderRepo.findAll()
                .stream()
                .map(customOrder -> modelMapper.map(customOrder, CustomOrderDto.class))
                .collect(Collectors.toList());
    }
}

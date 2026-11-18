package com.web.appts.services.imp;

import com.web.appts.DTO.OrderDto;
import com.web.appts.DTO.OrdersFeignData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class OrderErpTextUpdateApplier {
    private static final Logger logger = LoggerFactory.getLogger(OrderErpTextUpdateApplier.class);

    public void applyProductNotes(
            List<OrdersFeignData> dataList,
            Map<String, OrderDto> ordersMap,
            Consumer<OrderDto> orderUpdater
    ) {
        if (dataList == null) {
            return;
        }

        for (OrdersFeignData data : dataList) {
            if (data == null) {
                continue;
            }

            String normalizedLine;
            try {
                normalizedLine = String.valueOf(Integer.parseInt(data.getRegel()));
            } catch (RuntimeException exception) {
                logger.warn(
                        "Skipping ERP product note with invalid line number '{}' for order '{}'",
                        data.getRegel(),
                        data.getOrderNumber()
                );
                continue;
            }

            OrderDto orderDto = ordersMap.get(
                    data.getOrderNumber() + "," + normalizedLine
            );
            if (orderDto == null) {
                continue;
            }

            String text = data.getNotes();
            if (orderDto.getTekst() == null
                    || !orderDto.getTekst().equals(text)) {
                orderDto.setTekst(text);
                orderUpdater.accept(orderDto);
            }
        }
    }
}

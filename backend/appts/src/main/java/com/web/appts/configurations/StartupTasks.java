package com.web.appts.configurations;

import com.web.appts.services.OrderService;
import com.web.appts.services.OrderStartUpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
public class StartupTasks {
    private static final Logger logger = LoggerFactory.getLogger(StartupTasks.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderStartUpService orderStartUpService;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        orderService.markExpired();
        orderService.checkOrderExistence();
        orderStartUpService.verifyUpdateAddSmeSpuDots();
        logger.info("Startup order checks completed.");
    }

}

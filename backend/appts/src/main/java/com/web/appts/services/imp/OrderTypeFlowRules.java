package com.web.appts.services.imp;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

final class OrderTypeFlowRules {
    private static final Map<String, Set<OrderFlowField>> RULES = new HashMap<>();

    static {
        register("LOS", OrderFlowField.TRA);
        register("LAS", OrderFlowField.EXP);
        register("LSO", OrderFlowField.SER);
        register("LSL", OrderFlowField.SER);
        register("LPL", OrderFlowField.SER);
        register("MAO", OrderFlowField.MON_LB, OrderFlowField.EXP);
        register("MLO", OrderFlowField.MON_LB, OrderFlowField.TRA);
        register("MWO", OrderFlowField.MWE);
        register("MSO", OrderFlowField.MON_LB, OrderFlowField.SER);
        register("MLT", OrderFlowField.MON_TR, OrderFlowField.TRA);
        register("MST", OrderFlowField.MON_TR, OrderFlowField.SER);
        register("LOP", OrderFlowField.TRA);
        register("LAP", OrderFlowField.EXP);
        register("LSP", OrderFlowField.SER);
        register("MAP", OrderFlowField.MON_LB, OrderFlowField.EXP);
        register("MLP", OrderFlowField.MON_LB, OrderFlowField.TRA);
        register("MWP", OrderFlowField.MWE);
        register("MSP", OrderFlowField.MON_LB, OrderFlowField.SER);
        register("MSE", OrderFlowField.MON_TR, OrderFlowField.SER);
        register("MLE", OrderFlowField.MON_TR, OrderFlowField.TRA);
        register("WEB", OrderFlowField.TRA);
        register("BBA", OrderFlowField.TRA);
    }

    private OrderTypeFlowRules() {
    }

    static Set<OrderFlowField> fieldsFor(String orderType) {
        return RULES.getOrDefault(orderType, Collections.emptySet());
    }

    private static void register(String orderType, OrderFlowField... fields) {
        RULES.put(orderType, Collections.unmodifiableSet(EnumSet.copyOf(Arrays.asList(fields))));
    }
}

package io.jmix.delivery.service;

import io.jmix.delivery.entity.Order;
import org.flowable.engine.RuntimeService;
import org.springframework.stereotype.Service;

@Service
public class OrderProcessService {
    public static final String PROCESS_ORDER_DEFINITION_ID = "order-process";

    private final RuntimeService runtimeService;

    public OrderProcessService(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    public void startOrderProcess(Order order) {
        // key of process instance is same as order id
        runtimeService.startProcessInstanceByKey(PROCESS_ORDER_DEFINITION_ID, order.getId().toString());
    }
}

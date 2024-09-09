package io.jmix.delivery.service.process.abc;


import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlans;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.delivery.entity.Order;
import io.jmix.delivery.repository.OrderRepository;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public abstract class AbstractStep implements JavaDelegate {
    protected final SystemAuthenticator systemAuthenticator;
    protected final RuntimeService runtimeService;
    protected final OrderRepository orderRepository;
    protected final FetchPlans fetchPlans;

    protected AbstractStep(SystemAuthenticator systemAuthenticator,
                           RuntimeService runtimeService,
                           OrderRepository orderRepository,
                           FetchPlans fetchPlans) {
        this.systemAuthenticator = systemAuthenticator;
        this.runtimeService = runtimeService;
        this.orderRepository = orderRepository;
        this.fetchPlans = fetchPlans;
    }

    protected abstract void runStep(DelegateExecution execution);

    @Override
    public void execute(DelegateExecution execution) {
        systemAuthenticator.runWithSystem(() -> runStep(execution));
    }

    protected Optional<Order> findOrder(DelegateExecution execution) {
        var fetchPlan = fetchPlans.builder(Order.class)
                .addFetchPlan(FetchPlan.BASE)
                .build();

        String orderId = execution.getProcessInstanceBusinessKey();
        return orderRepository.findById(UUID.fromString(orderId), fetchPlan);
    }
}

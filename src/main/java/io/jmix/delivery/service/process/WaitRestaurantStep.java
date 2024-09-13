package io.jmix.delivery.service.process;

import io.jmix.core.DataManager;
import io.jmix.core.FetchPlans;
import io.jmix.core.SaveContext;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.delivery.entity.Order;
import io.jmix.delivery.entity.OrderStatus;
import io.jmix.delivery.repository.OrderRepository;
import io.jmix.delivery.service.process.abc.AbstractTransactionalStep;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Component(value = "waitRestaurantStep")
public class WaitRestaurantStep extends AbstractTransactionalStep {

    protected WaitRestaurantStep(SystemAuthenticator systemAuthenticator,
                                 RuntimeService runtimeService,
                                 PlatformTransactionManager transactionManager,
                                 DataManager dataManager,
                                 OrderRepository orderRepository,
                                 FetchPlans fetchPlans) {
        super(systemAuthenticator, runtimeService, transactionManager, dataManager, orderRepository, fetchPlans);
    }

    @Override
    protected void doTransactionalStep(DelegateExecution execution, Order order, SaveContext saveContext) {
        order.setStatus(OrderStatus.ON_WAIT_RESTAURANT);
        doSomeWork();
        saveContext.saving(order);
    }
}
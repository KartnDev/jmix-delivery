package io.jmix.delivery.service.userprovider;

import io.jmix.bpm.provider.UserGroupListProvider;
import io.jmix.bpm.provider.UserListProvider;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlans;
import io.jmix.core.security.Authenticated;
import io.jmix.delivery.entity.Order;
import io.jmix.delivery.entity.Restaurant;
import io.jmix.delivery.entity.User;
import io.jmix.delivery.repository.OrderRepository;
import io.jmix.delivery.repository.RestaurantRepository;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@UserListProvider
public class RestaurantCandidatesProvider {


    private final RestaurantRepository restaurantRepository;
    private final OrderRepository orderRepository;
    private final FetchPlans fetchPlans;
    private final RuntimeService runtimeService;

    public RestaurantCandidatesProvider(RestaurantRepository restaurantRepository,
                                        OrderRepository orderRepository,
                                        FetchPlans fetchPlans, RuntimeService runtimeService) {
        this.restaurantRepository = restaurantRepository;
        this.orderRepository = orderRepository;
        this.fetchPlans = fetchPlans;
        this.runtimeService = runtimeService;
    }

    @Authenticated
    public List<String> getUserCandidatesForOrderByOrderId(UUID orderId) {
        FetchPlan orderFetchPlan = fetchPlans.builder(Order.class)
                .add("restaurant", FetchPlan.LOCAL)
                .build();

        Order order = orderRepository.getById(orderId, orderFetchPlan);
        Restaurant restaurant = order.getRestaurant();

        FetchPlan restaurntFetchPlan = fetchPlans.builder(Restaurant.class)
                .add("owners", FetchPlan.BASE)
                .build();

        Restaurant refetchedRestaurant = restaurantRepository.getById(restaurant.getId(), restaurntFetchPlan);
        return refetchedRestaurant.getOwners()
                .stream()
                .map(User::getUsername)
                .toList();
    }

    @Authenticated
    public List<String> getUserCandidatesForOrderByExecution(DelegateExecution execution) {

        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(execution.getProcessInstanceId())
                .singleResult();

        // As we run process with business id that belongs to order, we can extract business key and use it as order id
        String orderId = processInstance.getBusinessKey();

        Optional<Order> order = orderRepository.findById(UUID.fromString(orderId));
        if (order.isPresent()) {
            return getUserCandidatesForOrderByOrderId(order.orElseThrow().getId());
        }
        throw new RuntimeException("Order not found");
    }
}
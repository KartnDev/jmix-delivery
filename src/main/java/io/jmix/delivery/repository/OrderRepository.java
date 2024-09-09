package io.jmix.delivery.repository;

import io.jmix.core.repository.JmixDataRepository;
import io.jmix.delivery.entity.Order;

import java.util.UUID;

public interface OrderRepository extends JmixDataRepository<Order, UUID> {
}
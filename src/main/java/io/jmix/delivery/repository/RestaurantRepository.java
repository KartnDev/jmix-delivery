package io.jmix.delivery.repository;

import io.jmix.core.repository.JmixDataRepository;
import io.jmix.delivery.entity.Restaurant;

import java.util.UUID;

public interface RestaurantRepository extends JmixDataRepository<Restaurant, UUID> {
}
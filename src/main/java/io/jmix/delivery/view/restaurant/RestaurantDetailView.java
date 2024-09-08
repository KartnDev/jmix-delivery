package io.jmix.delivery.view.restaurant;

import com.vaadin.flow.router.Route;
import io.jmix.delivery.entity.Restaurant;
import io.jmix.delivery.view.main.MainView;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "restaurants/:id", layout = MainView.class)
@ViewController("Restaurant.detail")
@ViewDescriptor("restaurant-detail-view.xml")
@EditedEntityContainer("restaurantDc")
public class RestaurantDetailView extends StandardDetailView<Restaurant> {
}
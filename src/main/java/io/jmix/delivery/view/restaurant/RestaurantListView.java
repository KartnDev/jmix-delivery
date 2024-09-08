package io.jmix.delivery.view.restaurant;

import com.vaadin.flow.router.Route;
import io.jmix.delivery.entity.Restaurant;
import io.jmix.delivery.view.main.MainView;
import io.jmix.flowui.view.*;


@Route(value = "restaurants", layout = MainView.class)
@ViewController("Restaurant.list")
@ViewDescriptor("restaurant-list-view.xml")
@LookupComponent("restaurantsDataGrid")
@DialogMode(width = "64em")
public class RestaurantListView extends StandardListView<Restaurant> {
}
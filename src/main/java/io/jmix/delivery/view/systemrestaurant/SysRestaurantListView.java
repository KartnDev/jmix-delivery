package io.jmix.delivery.view.systemrestaurant;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.data.provider.DataKeyMapper;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.Rendering;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.Route;
import io.jmix.delivery.entity.Restaurant;
import io.jmix.delivery.entity.User;
import io.jmix.delivery.helper.UiComponentHelper;
import io.jmix.delivery.view.main.MainView;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Collectors;


@Route(value = "restaurants", layout = MainView.class)
@ViewController("Restaurant.list")
@ViewDescriptor("restaurant-list-view.xml")
@LookupComponent("restaurantsDataGrid")
@DialogMode(width = "64em")
public class SysRestaurantListView extends StandardListView<Restaurant> {
    @Autowired
    private UiComponentHelper uiComponentHelper;

    @Supply(to = "restaurantsDataGrid.icon", subject = "renderer")
    private Renderer<Restaurant> restaurantsDataGridIconRenderer() {
        return new ComponentRenderer<>(e -> uiComponentHelper.createAvatarIconForEntity(e));
    }

    @Supply(to = "restaurantsDataGrid.owners", subject = "renderer")
    private Renderer<Restaurant> restaurantsDataGridOwnersRenderer() {
        return new ComponentRenderer<>((e) -> new Text(
                e.getOwners()
                        .stream()
                        .map(User::getUsername)
                        .collect(Collectors.joining(", "))));
    }
}
package io.jmix.delivery.view.order;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import io.jmix.core.Messages;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.delivery.entity.Order;
import io.jmix.delivery.entity.OrderStatus;
import io.jmix.delivery.view.main.MainView;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.jmix.delivery.constants.OrderViewsPathConstants.*;


@Route(value = "orders", layout = MainView.class)
@ViewController("Order_.list")
@ViewDescriptor("order-list-view.xml")
@LookupComponent("ordersDataGrid")
@DialogMode(width = "64em")
public class OrderListView extends StandardListView<Order> {

    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private Messages messages;
    @Autowired
    private ViewNavigationSupport viewNavigationSupport;

    @ViewComponent
    private CollectionLoader<Order> ordersDl;
    @ViewComponent
    private DataGrid<Order> ordersDataGrid;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        ordersDl.setParameter("currentUser", currentAuthentication.getUser());
    }

    @Supply(to = "ordersDataGrid.restaurant", subject = "renderer")
    private Renderer<Order> ordersDataGridRestaurantRenderer() {
        return new ComponentRenderer<>(order -> {
            var horizontalLayout = new HorizontalLayout();
            horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
            var restaurant = order.getRestaurant();

            if (restaurant.getIcon() != null && restaurant.getIconName() != null) {
                var avatar = new Avatar(restaurant.getIconName());
                avatar.setImageResource(new StreamResource(Objects.requireNonNull(restaurant.getIconName()),
                        (InputStreamFactory) () -> new ByteArrayInputStream(Objects.requireNonNull(restaurant.getIcon()))));
                horizontalLayout.add(avatar);
            }
            horizontalLayout.add(new Text(restaurant.getName()));
            return horizontalLayout;
        });
    }

    @Supply(to = "ordersDataGrid.status", subject = "renderer")
    private Renderer<Order> ordersDataGridStatusRenderer() {
        return new ComponentRenderer<>(order -> {
            var horizontalLayout = new HorizontalLayout();
            horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
            var iconComponent = switch (order.getStatus()) {
                case NEW -> {
                    var icon = new Icon(VaadinIcon.CROSS_CUTLERY);
                    icon.getElement().getThemeList().add("icon");
                    icon.setColor("var(--lumo-contrast)"); // contrast - grey color
                    yield icon;
                }
                case ACCEPTED -> {
                    var icon = new Icon(VaadinIcon.CHECK);
                    icon.getElement().getThemeList().add("icon");
                    icon.setColor("var(--lumo-contrast)"); // contrast - grey color
                    yield icon;
                }
                case ON_WAIT_RESTAURANT -> {
                    var icon = new Icon(VaadinIcon.CLOCK);
                    icon.getElement().getThemeList().add("icon");
                    icon.setColor("var(--lumo-primary-color-50pct)"); // normal - blue color
                    yield icon;
                }
                case COOKING -> {
                    var icon = new Icon(VaadinIcon.CUTLERY);
                    icon.getElement().getThemeList().add("icon");
                    icon.setColor("var(--lumo-primary-color-50pct)"); // normal - blue color
                    yield icon;
                }
                case READY -> {
                    var icon = new Icon(VaadinIcon.CHECK);
                    icon.getElement().getThemeList().add("icon");
                    icon.setColor("var(--lumo-success-color)"); // success - green color
                    yield icon;
                }
            };
            horizontalLayout.add(new Text(messages.getMessage(OrderStatus.class, "OrderStatus." + order.getStatus().name())));
            horizontalLayout.add(iconComponent);
            return horizontalLayout;
        });
    }

    @Supply(to = "ordersDataGrid.items", subject = "renderer")
    private Renderer<Order> ordersDataGridItemsRenderer() {
        return new ComponentRenderer<>(order -> new Text(order.getFoodItems().stream().map(foodCountItem -> {
            if(foodCountItem.getCount() == 1) {
                return "#" + foodCountItem.getFood().getName();
            } else {
                return "#" + foodCountItem.getFood().getName() + " x" + foodCountItem.getCount();
            }

        }).collect(Collectors.joining(", "))));
    }

    @Subscribe(id = "showDetailsButton", subject = "clickListener")
    public void onShowDetailsButtonClick(final ClickEvent<JmixButton> event) {
        var selectedOrder = ordersDataGrid.getSingleSelectedItem();
        if(selectedOrder == null) {
            return;
        }

        viewNavigationSupport.navigate(OrderDetailView.class,
                new RouteParameters(ORDER_ID_PATH_PARAM, selectedOrder.getId().toString()),
                QueryParameters.of(RESTAURANT_ID_PATH_PARAM, String.valueOf(selectedOrder.getRestaurant().getId())));
    }

}
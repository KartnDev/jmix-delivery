package io.jmix.delivery.view.order;


import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.delivery.entity.Restaurant;
import io.jmix.delivery.helper.UiComponentHelper;
import io.jmix.delivery.view.main.MainView;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.*;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.springframework.beans.factory.annotation.Autowired;

import static io.jmix.delivery.constants.OrderViewsPathConstants.*;

@Route(value = "order-new", layout = MainView.class)
@ViewController("OrderChooseRestaurantView")
@ViewDescriptor("order-choose-restaurant-view.xml")
public class OrderChooseRestaurantView extends StandardView {
    @Autowired
    private ViewNavigationSupport viewNavigationSupport;
    @ViewComponent
    private VerticalLayout restaurantsListContainer;
    @ViewComponent
    private CollectionContainer<Restaurant> restaurantsDc;
    @Autowired
    private UiComponentHelper uiComponentHelper;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        uiComponentHelper.addVirtualList(restaurantsListContainer, restaurantsDc,
                (rest, infoLayout) -> restaurantsUpdater((Restaurant) rest, infoLayout));
    }

    private void restaurantsUpdater(Restaurant restaurant, UiComponentHelper.ListComponentContext componentContext) {
        componentContext.infoLayout().add(new Html("<strong>" + restaurant.getName() + "</strong>"));
        componentContext.infoLayout().add(new Text(restaurant.getDescription()));

        var detailButton = new Button(new Icon(VaadinIcon.EXIT_O));
        detailButton.setText("Order here");
        detailButton.addClickListener(e -> viewNavigationSupport.navigate(OrderDetailView.class,
                new RouteParameters(ORDER_ID_PATH_PARAM, NEW_ORDER_ID),
                QueryParameters.of(RESTAURANT_ID_PATH_PARAM, String.valueOf(restaurant.getId()))));
        detailButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);

        var details = new Details();
        details.add(new Text(restaurant.getDescription()));
        details.setSummaryText("Information");

        componentContext.infoLayout().add(details);
        componentContext.rootCardLayout().add(detailButton);
    }
}
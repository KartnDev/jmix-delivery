package io.jmix.delivery.view.orderchooserestaurant;


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
import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.delivery.entity.Restaurant;
import io.jmix.delivery.helper.UiComponentHelper;
import io.jmix.delivery.view.main.MainView;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "choose-restaurant", layout = MainView.class)
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
    @Autowired
    private DataManager dataManager;

    @Install(to = "restaurantsDl", target = Target.DATA_LOADER)
    private List<Restaurant> restaurantsLoaderLoadDelegate(final LoadContext<Restaurant> loadContext) {
        return dataManager.load(Restaurant.class)
                .all()
                .list();
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        uiComponentHelper.addListRenderer(restaurantsListContainer, restaurantsDc,
                (rest, infoLayout) -> restaurantsUpdater((Restaurant) rest, infoLayout));
    }

    private void restaurantsUpdater(Restaurant restaurant, UiComponentHelper.ListComponentContext componentContext) {
        componentContext.infoLayout().add(new Html("<strong>" + restaurant.getName() + "</strong>"));
        componentContext.infoLayout().add(new Text(restaurant.getDescription()));

        var detailButton = new Button(new Icon(VaadinIcon.EXIT_O));
        detailButton.setText("Order here");
//        detailButton.addClickListener(e -> viewNavigationSupport.navigate(OrderView.class,
//                new RouteParameters(ORDER_ID_PATH_PARAM, NEW_ORDER_ID),
//                QueryParameters.of(RESTAURANT_ID_PATH_PARAM, String.valueOf(restaurant.getId()))));
        detailButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);

        var details = new Details();
        details.add(new Text(restaurant.getDescription()));
        details.setSummaryText("Information");

        componentContext.infoLayout().add(details);
        componentContext.rootLayout().add(detailButton);
    }
}
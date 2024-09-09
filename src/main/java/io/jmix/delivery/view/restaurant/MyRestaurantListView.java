package io.jmix.delivery.view.restaurant;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.core.Messages;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.delivery.entity.Restaurant;
import io.jmix.delivery.helper.UiComponentHelper;
import io.jmix.delivery.view.main.MainView;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.springframework.beans.factory.annotation.Autowired;


@Route(value = "my-restaurants", layout = MainView.class)
@ViewController("MyRestaurant.list")
@ViewDescriptor("my-restaurant-list-view.xml")
@DialogMode(width = "64em")
public class MyRestaurantListView extends StandardListView<Restaurant> {

    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private ViewNavigationSupport viewNavigationSupport;
    @Autowired
    private UiComponentHelper listComponents;
    @Autowired
    private Messages messages;
    @ViewComponent
    private MessageBundle messageBundle;
    @ViewComponent
    private CollectionLoader<Restaurant> restaurantsDl;
    @ViewComponent
    private VerticalLayout restaurantsListContainer;
    @ViewComponent
    private CollectionContainer<Restaurant> restaurantsDc;

    @Subscribe
    public void onInit(final InitEvent event) {
        restaurantsDl.setParameter("current_user", currentAuthentication.getUser());
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        listComponents.addVirtualList(restaurantsListContainer, restaurantsDc,
                (rest, context) -> restaurantsUpdater((Restaurant) rest, context));
    }

    private void restaurantsUpdater(Restaurant restaurant, UiComponentHelper.ListComponentContext componentContext) {
        componentContext.infoLayout().add(new Html(messageBundle.formatMessage("restaurantItemsHeader", restaurant.getName())));
        componentContext.infoLayout().add(new Text(restaurant.getDescription()));

        var detailButton = new Button(new Icon(VaadinIcon.PENCIL));
        detailButton.setText(messages.getMessage("actions.Edit"));
        detailButton.addClickListener(e -> viewNavigationSupport.navigate(MyRestaurantDetailView.class,
                new RouteParameters("id", String.valueOf(restaurant.getId()))));
        detailButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);

        var details = new Details();
        details.add(new Html(messageBundle.formatMessage("restaurantInformation", restaurant.getDescription())));
        details.setSummaryText(messages.getMessage("information"));

        componentContext.infoLayout().add(details);
        componentContext.rootCardLayout().add(detailButton);
    }
}
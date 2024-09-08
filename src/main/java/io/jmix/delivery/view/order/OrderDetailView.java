package io.jmix.delivery.view.order;


import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.delivery.entity.*;
import io.jmix.delivery.helper.UiComponentHelper;
import io.jmix.delivery.view.main.MainView;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.model.*;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;

import static io.jmix.delivery.constants.OrderViewsPathConstants.*;

@Route(value = "orders/:id", layout = MainView.class)
@ViewController("OrderDetailView")
@ViewDescriptor("order-detail-view.xml")
public class OrderDetailView extends StandardView {

    @ViewComponent
    private Div restaurantFoodContainer;
    @ViewComponent
    private Div orderContainer;
    @ViewComponent
    private MessageBundle messageBundle;
    @Autowired
    private Dialogs dialogs;
    @ViewComponent
    private InstanceContainer<Restaurant> restaurantDc;
    @Autowired
    private DataManager dataManager;
    @ViewComponent
    private InstanceLoader<Restaurant> restaurantDl;
    @Autowired
    private UiComponentHelper uiComponentHelper;
    @ViewComponent
    private CollectionContainer<Food> restaurantFoodDc;
    @ViewComponent
    private InstanceContainer<Order> orderDc;
    @ViewComponent
    private DataContext dataContext;
    @ViewComponent
    private CollectionPropertyContainer<FoodCountItem> orderFoodItemsDc;
    @ViewComponent
    private H3 orderTitle;


    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // happens before BeforeShowEvent
        String orderId = event.getRouteParameters().get(ORDER_ID_PATH_PARAM).orElse(NEW_ORDER_ID);
        if (NEW_ORDER_ID.equals(orderId)) {
            initNewOrderView(event);
        } else {
//            initHistoryOrderView(event);
        }

        super.beforeEnter(event);
    }

    private void initNewOrderView(BeforeEnterEvent event) {
        orderDc.setItem(dataContext.create(Order.class));
        QueryParameters queryParameters = event.getLocation().getQueryParameters();
        if (!queryParameters.getParameters().containsKey(RESTAURANT_ID_PATH_PARAM) &&
                queryParameters.getParameters().get(RESTAURANT_ID_PATH_PARAM).size() != 1) {
            handleNoRestaurantSelectedBehaviour();
        }
        UUID restaurantId = UUID.fromString(queryParameters.getParameters()
                .get(RESTAURANT_ID_PATH_PARAM)
                .getFirst());

        restaurantDl.setParameter("restaurantId", restaurantId);
        restaurantDl.load();


        uiComponentHelper.addVirtualList(restaurantFoodContainer, restaurantFoodDc,
                (HasIconEntity item, UiComponentHelper.ListComponentContext context) -> menuItemsUpdater(context, (Food) item));
        uiComponentHelper.addVirtualList(orderContainer, (CollectionContainer) orderFoodItemsDc,
                (foodItem, listComponentContext) -> foodItemsUpdater((FoodCountItem) foodItem, listComponentContext));
    }

    private void foodItemsUpdater(FoodCountItem item, UiComponentHelper.ListComponentContext componentContext) {
        var title = new Html(messageBundle.formatMessage("foodItemsHeader", item.getFood().getName(), item.getCount()));

        componentContext.infoLayout().add(title);

        var horizontalLayout = new HorizontalLayout();
        horizontalLayout.add(new Text(item.getFood().getDescription()));
        horizontalLayout.add(new Html(messageBundle.formatMessage("foodItemsDescriptionPrice", item.getFood().getPrice())));
        horizontalLayout.add(new Html(messageBundle.formatMessage("foodItemsDescriptionCountedPrice", item.getCount() * item.getFood().getPrice())));
        horizontalLayout.setPadding(false);
        horizontalLayout.setMargin(false);
        horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        componentContext.infoLayout().add(horizontalLayout);
    }


    private void menuItemsUpdater(UiComponentHelper.ListComponentContext componentContext,
                                  Food item) {
        componentContext.infoLayout().add(new Html(messageBundle.formatMessage("menusHeader", item.getName())));

        var horizontalLayout = new HorizontalLayout();
        horizontalLayout.add(new Text(item.getDescription()));
        horizontalLayout.add(new Html(messageBundle.formatMessage("menusPrice", item.getPrice())));
        horizontalLayout.setPadding(false);
        horizontalLayout.setMargin(false);
        horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        componentContext.infoLayout().add(horizontalLayout);

        var buttonsPanel = new VerticalLayout();
        buttonsPanel.setWidth("AUTO");
        buttonsPanel.setPadding(false);
        buttonsPanel.setMargin(false);
        buttonsPanel.setSpacing(false);

        var addButton = new Button(new Icon(VaadinIcon.PLUS));
        addButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        addButton.addClickListener(e -> {
            Order order = orderDc.getItem();
            if (CollectionUtils.isEmpty(order.getFoodItems()) || orderDoesNotContainsAnyFoodItem(order, item)) {
                FoodCountItem foodItemCountedEntity = dataContext.create(FoodCountItem.class);
                foodItemCountedEntity.setFood(item);
                foodItemCountedEntity.setCount(1);
                foodItemCountedEntity.setOrder(order);
                foodItemCountedEntity = dataContext.merge(foodItemCountedEntity);
                order.getFoodItems().add(foodItemCountedEntity);
                orderFoodItemsDc.getMutableItems().add(foodItemCountedEntity);
            } else {
                FoodCountItem foodItemCountedEntity = order.getFoodItems().stream()
                        .filter(foodItem -> foodItem.getFood().getId().equals(item.getId()))
                        .findFirst()
                        .orElseThrow();
                foodItemCountedEntity.setCount(foodItemCountedEntity.getCount() + 1);
                dataContext.merge(foodItemCountedEntity);
                refreshTotalCount();
            }
            refreshTotalCount();
            componentContext.tVirtualList().getDataProvider().refreshAll();
        });

        var removeButton = new Button(new Icon(VaadinIcon.MINUS));
        removeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR);
        removeButton.addClickListener(e -> {
            Order order = orderDc.getItem();
            FoodCountItem foodItemCountedEntity = order.getFoodItems().stream()
                    .filter(foodItem -> foodItem.getFood().getId().equals(item.getId()))
                    .findFirst()
                    .orElse(null);

            if (foodItemCountedEntity != null) {
                if (foodItemCountedEntity.getCount() == 1) {
                    order.getFoodItems().remove(foodItemCountedEntity);
                    dataContext.remove(foodItemCountedEntity);
                    orderFoodItemsDc.getMutableItems().remove(foodItemCountedEntity);
                } else {
                    foodItemCountedEntity.setCount(foodItemCountedEntity.getCount() - 1);
                    dataContext.merge(foodItemCountedEntity);
                }
            }
            refreshTotalCount();
            componentContext.tVirtualList().getDataProvider().refreshAll();
        });
        buttonsPanel.add(addButton, removeButton);
        componentContext.rootCardLayout().add(buttonsPanel);
    }

    private void refreshTotalCount() {
        var totalPrice = orderFoodItemsDc.getMutableItems()
                .stream()
                .mapToInt(item -> item.getCount() * item.getFood().getPrice())
                .sum();
        orderTitle.setText(messageBundle.formatMessage("OrderFormatted", totalPrice));
    }


    private boolean orderDoesNotContainsAnyFoodItem(Order order, Food item) {
        return order.getFoodItems()
                .stream()
                .map(FoodCountItem::getFood)
                .noneMatch(foodItem -> Objects.equals(foodItem.getId(), item.getId()));
    }


    private void handleNoRestaurantSelectedBehaviour() {
        dialogs.createOptionDialog()
                .withText(messageBundle.getMessage("noRestaurantSelected.message"))
                .withActions(new DialogAction(DialogAction.Type.OK)
                        .withHandler(e -> close(StandardOutcome.DISCARD)))
                .open();
    }
}
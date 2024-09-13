package io.jmix.delivery.view.order;


import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlans;
import io.jmix.core.Messages;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.delivery.entity.*;
import io.jmix.delivery.helper.UiComponentHelper;
import io.jmix.delivery.repository.OrderRepository;
import io.jmix.delivery.service.OrderProcessService;
import io.jmix.delivery.view.main.MainView;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.component.splitlayout.JmixSplitLayout;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.*;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayInputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static io.jmix.delivery.constants.OrderViewsPathConstants.*;
import static io.jmix.delivery.entity.OrderStatus.*;

@Route(value = "orders/:id", layout = MainView.class)
@ViewController("OrderDetailView")
@ViewDescriptor("order-detail-view.xml")
public class OrderDetailView extends StandardView {

    @Autowired
    private OrderProcessService orderProcessService;
    @Autowired
    private Dialogs dialogs;
    @Autowired
    private UiComponentHelper uiComponentHelper;


    @ViewComponent
    private MessageBundle messageBundle;
    @ViewComponent
    private DataContext dataContext;

    @ViewComponent
    private InstanceContainer<Restaurant> restaurantDc;
    @ViewComponent
    private InstanceLoader<Restaurant> restaurantDl;
    @ViewComponent
    private CollectionContainer<Food> restaurantFoodDc;
    @ViewComponent
    private InstanceContainer<Order> orderDc;
    @ViewComponent
    private CollectionPropertyContainer<FoodCountItem> orderFoodItemsDc;

    @ViewComponent
    private Div restaurantDescription;
    @ViewComponent
    private Div restaurantFoodContainer;
    @ViewComponent
    private Div orderContainer;
    @ViewComponent
    private Avatar restaurantIcon;
    @ViewComponent
    private Html totalPriceContainer;
    @ViewComponent
    private H2 restaurantTitle;
    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private OrderRepository orderRepository;
    @ViewComponent
    private JmixSplitLayout split;
    @ViewComponent
    private Div content;
    @ViewComponent
    private VerticalLayout orderDetailTab;
    @Autowired
    private FetchPlans fetchPlans;
    @ViewComponent
    private HorizontalLayout detailsActions;
    @Autowired
    private Messages messages;
    @ViewComponent
    private HorizontalLayout titleLayout;


    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // happens before BeforeShowEvent
        String orderId = event.getRouteParameters().get(ORDER_ID_PATH_PARAM).orElse(NEW_ORDER_ID);
        if (NEW_ORDER_ID.equals(orderId)) {
            initNewOrderView(event);
            initRestaurantFragment();
        } else {
            initHistoryOrderView(orderId);
            refreshTotalCount();
        }

        super.beforeEnter(event);
    }

    private void initHistoryOrderView(String orderId) {
        var orderFetchPlan = fetchPlans.builder(Order.class)
                .addFetchPlan("order-fetch-plan")
                .build();
        Order order = orderRepository.getById(UUID.fromString(orderId), orderFetchPlan);
        orderDc.setItem(order);

        content.remove(split);
        content.add(orderDetailTab);

        uiComponentHelper.addVirtualList(orderContainer, (CollectionContainer) orderFoodItemsDc,
                (foodItem, listComponentContext) -> orderCountedFoodsItemUpdater((FoodCountItem) foodItem, listComponentContext));

        initRestaurantFragment();
        initOrderStatusBarge();
        detailsActions.setVisible(false);
    }

    private void initOrderStatusBarge() {
        var order = orderDc.getItem();

        var statusMessageText = messages.getMessage(OrderStatus.class, "OrderStatus." + order.getStatus().name());

        var badge = switch (order.getStatus()) {
            case NEW -> {
                var rootSpan = new Span();
                rootSpan.getElement().getThemeList().add("badge contrast");

                var icon = new Icon(VaadinIcon.CROSS_CUTLERY);
                icon.getElement().getThemeList().add("badged-icon contrast");
                icon.getStyle().set("padding", "var(--lumo-space-xs)");

                var textSpan = new Span();
                textSpan.setText(statusMessageText);

                rootSpan.add(icon, textSpan);

                yield rootSpan;
            }
            case ACCEPTED -> {
                var rootSpan = new Span();
                rootSpan.getElement().getThemeList().add("badge contrast");

                var icon = new Icon(VaadinIcon.CHECK);
                icon.getElement().getThemeList().add("badged-icon contrast");
                icon.getStyle().set("padding", "var(--lumo-space-xs)");

                var textSpan = new Span();
                textSpan.setText(statusMessageText);

                rootSpan.add(icon, textSpan);

                yield rootSpan;
            }
            case ON_WAIT_RESTAURANT -> {
                var rootSpan = new Span();
                rootSpan.getElement().getThemeList().add("badge normal");

                var icon = new Icon(VaadinIcon.CLOCK);
                icon.getElement().getThemeList().add("badged-icon normal");
                icon.getStyle().set("padding", "var(--lumo-space-xs)");

                var textSpan = new Span();
                textSpan.setText(statusMessageText);

                rootSpan.add(icon, textSpan);

                yield rootSpan;
            }
            case COOKING -> {
                var rootSpan = new Span();
                rootSpan.getElement().getThemeList().add("badge normal");

                var icon = new Icon(VaadinIcon.CUTLERY);
                icon.getElement().getThemeList().add("badged-icon normal");
                icon.getStyle().set("padding", "var(--lumo-space-xs)");

                var textSpan = new Span();
                textSpan.setText(statusMessageText);

                rootSpan.add(icon, textSpan);

                yield rootSpan;
            }
            case READY -> {
                var rootSpan = new Span();
                rootSpan.getElement().getThemeList().add("badge success");

                var icon = new Icon(VaadinIcon.CHECK);
                icon.getElement().getThemeList().add("badged-icon success");
                icon.getStyle().set("padding", "var(--lumo-space-xs)");

                var textSpan = new Span();
                textSpan.setText(statusMessageText);

                rootSpan.add(icon, textSpan);

                yield rootSpan;
            }
        };

        titleLayout.add(badge);
    }

    private void initRestaurantFragment() {
        Restaurant restaurant = orderDc.getItem().getRestaurant();

        restaurantTitle.setText(restaurant.getName());
        restaurantDescription.add(new Html(messageBundle.formatMessage("restaurantDescriptionMessage", restaurant.getDescription())));
        if (restaurant.getIconName() != null && restaurant.getIcon() != null) {
            restaurantIcon.setImageResource(new StreamResource(restaurant.getIconName(),
                    (InputStreamFactory) () -> new ByteArrayInputStream(restaurant.getIcon())));
        }
    }

    private void initNewOrderView(BeforeEnterEvent event) {
        var order = dataContext.create(Order.class);
        order.setClient((User) currentAuthentication.getUser());
        orderDc.setItem(order);
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

        orderDc.getItem().setRestaurant(restaurantDc.getItem());

        uiComponentHelper.addVirtualList(restaurantFoodContainer, restaurantFoodDc,
                (HasIconEntity item, UiComponentHelper.ListComponentContext context) -> restaurantFoodItemUpdater(context, (Food) item));
        uiComponentHelper.addVirtualList(orderContainer, (CollectionContainer) orderFoodItemsDc,
                (foodItem, listComponentContext) -> orderCountedFoodsItemUpdater((FoodCountItem) foodItem, listComponentContext));
    }

    private void orderCountedFoodsItemUpdater(FoodCountItem item, UiComponentHelper.ListComponentContext componentContext) {
        var title = new Html(messageBundle.formatMessage("foodItemsHeader", item.getFood().getName(), item.getCount()));

        componentContext.infoLayout().add(title);

        var horizontalLayout = new HorizontalLayout();
        horizontalLayout.add(new Text(item.getFood().getDescription()));
        horizontalLayout.add(new Html(messageBundle.formatMessage("foodItemsPrice", item.getFood().getPrice())));
        horizontalLayout.add(new Html(messageBundle.formatMessage("foodItemsQuantity", item.getCount())));
        horizontalLayout.add(new Html(messageBundle.formatMessage("foodItemsCountedPrice", item.getCount() * item.getFood().getPrice())));
        horizontalLayout.setPadding(false);
        horizontalLayout.setMargin(false);
        horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        componentContext.infoLayout().add(horizontalLayout);
    }


    private void restaurantFoodItemUpdater(UiComponentHelper.ListComponentContext componentContext,
                                           Food item) {
        componentContext.infoLayout().add(new Html(messageBundle.formatMessage("foodHeader", item.getName())));

        var horizontalLayout = new HorizontalLayout();
        horizontalLayout.add(new Text(item.getDescription()));
        horizontalLayout.add(new Html(messageBundle.formatMessage("foodPrice", item.getPrice())));
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
        totalPriceContainer.setHtmlContent(messageBundle.formatMessage("orderTotalPriceFormatted", totalPrice));
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

    @Subscribe(target = Target.DATA_CONTEXT)
    public void onPostSave(final DataContext.PostSaveEvent event) {
        orderProcessService.startOrderProcess(orderDc.getItem());
    }

    @Subscribe("approveOrder")
    public void onApproveOrder(final ActionPerformedEvent event) {
        dataContext.save(true);
        close(StandardOutcome.DISCARD);
    }

    @Subscribe("cancelOrder")
    public void onCancelOrder(final ActionPerformedEvent event) {
        close(StandardOutcome.DISCARD);
    }
}
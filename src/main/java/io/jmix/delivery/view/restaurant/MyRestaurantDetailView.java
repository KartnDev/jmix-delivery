package io.jmix.delivery.view.restaurant;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import io.jmix.core.Messages;
import io.jmix.delivery.entity.Food;
import io.jmix.delivery.entity.Restaurant;
import io.jmix.delivery.helper.UiComponentHelper;
import io.jmix.delivery.view.food.FoodDetailView;
import io.jmix.delivery.view.main.MainView;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.component.SupportsTypedValue;
import io.jmix.flowui.component.tabsheet.JmixTabSheet;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.upload.FileUploadField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.component.upload.event.FileUploadFinishedEvent;
import io.jmix.flowui.model.*;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

import static io.jmix.delivery.constants.OrderViewsPathConstants.NEW_ORDER_ID;
import static io.jmix.delivery.constants.OrderViewsPathConstants.ORDER_ID_PATH_PARAM;

@Route(value = "my-restaurants/:id", layout = MainView.class)
@ViewController("MyRestaurant.detail")
@ViewDescriptor("my-restaurant-detail-view.xml")
@EditedEntityContainer("restaurantDc")
public class MyRestaurantDetailView extends StandardDetailView<Restaurant> {
    @ViewComponent
    private MessageBundle messageBundle;
    @Autowired
    private Messages messages;
    @Autowired
    private DialogWindows dialogWindows;
    @ViewComponent
    private CollectionContainer<Object> foodDc;
    @ViewComponent
    private DataContext dataContext;
    @ViewComponent
    private CollectionLoader<Food> foodDl;
    @ViewComponent
    private InstanceContainer<Restaurant> restaurantDc;
    @ViewComponent
    private InstanceLoader<Restaurant> restaurantDl;
    @ViewComponent
    private Avatar restaurantAvatarIcon;
    @ViewComponent
    private FileUploadField restaurantAvatarIconUpload;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        var restaurantId = UUID.fromString(event.getRouteParameters().get(ORDER_ID_PATH_PARAM).orElse(NEW_ORDER_ID));
        restaurantDl.setParameter("restaurantId", restaurantId);
        foodDl.setParameter("restaurantId", restaurantId);
        super.beforeEnter(event);
        updateIconField();
    }

    @Subscribe("restaurantAvatarIconUpload")
    public void onRestaurantAvatarIconUploadFileUploadFinished(final FileUploadFinishedEvent<FileUploadField> event) {
        getEditedEntity().setIcon(restaurantAvatarIconUpload.getValue());
        getViewData().getDataContext().setModified(getEditedEntity(), true);
        updateIconField();
    }

    private void updateIconField() {
        if(getEditedEntity().getIcon() != null && getEditedEntity().getIconName() != null) {
            restaurantAvatarIconUpload.setValue(getEditedEntity().getIcon());
            restaurantAvatarIcon.setImageResource(new StreamResource(Objects.requireNonNull(getEditedEntity().getIconName()),
                    (InputStreamFactory) () -> new ByteArrayInputStream(Objects.requireNonNull(getEditedEntity().getIcon()))));
        }
    }

    @Subscribe(id = "addBtn", subject = "clickListener")
    public void onAddBtnClick(final ClickEvent<JmixButton> event) {
        dialogWindows.detail(this, Food.class)
                .withViewClass(FoodDetailView.class)
                .newEntity()
                .withInitializer(e -> e.setBelongsToRestaurant(getEditedEntity()))
                .withAfterCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(StandardOutcome.SAVE)) {
                        foodDc.replaceItem(closeEvent.getSource().getView().getEditedEntity());
                    }
                })
                .open();
    }

    @Supply(to = "foodList", subject = "renderer")
    private Renderer<Food> foodListRenderer() {
        return new ComponentRenderer<>(item -> {
            HorizontalLayout rootCardLayout = new HorizontalLayout();
            rootCardLayout.setMargin(true);

            VerticalLayout infoLayout = new VerticalLayout();
            infoLayout.setSpacing(false);
            infoLayout.setPadding(false);
            infoLayout.setWidth("30%");

            Avatar avatar = new Avatar();
            avatar.addThemeVariants(AvatarVariant.LUMO_XLARGE);

            if (item.getIcon() != null && item.getIcon().length > 0) {
                String iconName = "%s.png".formatted(item.getName());
                avatar.setImageResource(new StreamResource(iconName, () -> new ByteArrayInputStream(item.getIcon())));
            }

            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setWidthFull();

            HorizontalLayout itemDetailLayout = new HorizontalLayout();
            itemDetailLayout.add(new Text(item.getDescription()));
            itemDetailLayout.add(new Html(
                    messageBundle.formatMessage("foodListItemDescription", item.getPrice()))
            );
            itemDetailLayout.setPadding(false);
            itemDetailLayout.setAlignItems(FlexComponent.Alignment.CENTER);

            infoLayout.add(new Html(messageBundle.formatMessage("foodItemTitle", item.getName())));
            infoLayout.add(itemDetailLayout);

            VerticalLayout buttonsPanel = new VerticalLayout();
            buttonsPanel.setWidth("AUTO");
            buttonsPanel.setPadding(false);
            buttonsPanel.setSpacing(false);

            Button detailButton = new Button(new Icon(VaadinIcon.PENCIL));
            detailButton.setText(messages.getMessage("actions.Edit"));
            detailButton.addClickListener(e -> dialogWindows.detail(this, Food.class)
                    .withViewClass(FoodDetailView.class)
                    .editEntity(item)
                    .withAfterCloseListener(closeEvent -> {
                        if (closeEvent.closedWith(StandardOutcome.SAVE)) {
                            foodDc.replaceItem(closeEvent.getSource().getView().getEditedEntity());
                        }
                    })
                    .open());
            detailButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);

            Button removeButton = new Button(new Icon(VaadinIcon.TRASH));
            removeButton.setText(messages.getMessage("actions.Remove"));
            removeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR);
            removeButton.addClickListener(e -> {
                foodDc.getMutableItems().remove(item);
                dataContext.remove(dataContext.merge(item));
            });

            buttonsPanel.add(detailButton, removeButton);
            rootCardLayout.add(avatar, infoLayout, buttonsPanel);
            return rootCardLayout;
        });
    }


}
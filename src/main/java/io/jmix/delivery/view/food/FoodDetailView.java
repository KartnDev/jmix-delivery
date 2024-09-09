package io.jmix.delivery.view.food;

import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import io.jmix.delivery.entity.Food;
import io.jmix.delivery.view.main.MainView;
import io.jmix.flowui.component.SupportsTypedValue;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.upload.FileUploadField;
import io.jmix.flowui.kit.component.upload.event.FileUploadFinishedEvent;
import io.jmix.flowui.view.*;

import java.io.ByteArrayInputStream;
import java.util.Objects;

@Route(value = "foods/:id", layout = MainView.class)
@ViewController("Food.detail")
@ViewDescriptor("food-detail-view.xml")
@EditedEntityContainer("foodDc")
public class FoodDetailView extends StandardDetailView<Food> {

    @ViewComponent
    private Avatar itemAvatarIcon;
    @ViewComponent
    private FileUploadField itemAvatarIconUpload;
    @ViewComponent
    private H2 itemName;

    @Subscribe
    public void obBeforeShow(final BeforeShowEvent event) {
        initIconFields();
    }

    @Subscribe("nameField")
    public void onNameFieldTypedValueChange(final SupportsTypedValue.TypedValueChangeEvent<TypedTextField<String>, String> event) {
        itemName.setText(getEditedEntity().getName());
    }

    @Subscribe("itemAvatarIconUpload")
    public void onRestaurantAvatarIconUploadFileUploadFinished(final FileUploadFinishedEvent<FileUploadField> event) {
        getEditedEntity().setIcon(itemAvatarIconUpload.getValue());
        getViewData().getDataContext().setModified(getEditedEntity(), true);
        initIconFields();
    }

    private void initIconFields() {
        if(getEditedEntity().getIcon() != null && getEditedEntity().getIconName() != null) {
            itemAvatarIconUpload.setValue(getEditedEntity().getIcon());
            itemAvatarIcon.setImageResource(new StreamResource(Objects.requireNonNull(getEditedEntity().getIconName()),
                    (InputStreamFactory) () -> new ByteArrayInputStream(Objects.requireNonNull(getEditedEntity().getIcon()))));
        }
    }
}
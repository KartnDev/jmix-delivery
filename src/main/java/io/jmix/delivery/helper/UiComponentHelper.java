package io.jmix.delivery.helper;

import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.server.StreamResource;
import io.jmix.delivery.entity.HasIconEntity;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;

import static com.vaadin.flow.server.PwaConfiguration.DEFAULT_ICON;


@Component
public final class UiComponentHelper {
    private UiComponentHelper() {}


    public Avatar createAvatarIconForEntity(HasIconEntity hasIcon) {
        Avatar avatar = new Avatar();
        avatar.addThemeVariants(AvatarVariant.LUMO_XLARGE);

        if (hasIcon.getIcon() == null || hasIcon.getIconName() == null) {
            avatar.setImage(DEFAULT_ICON);
            return avatar;
        }

        try {
            avatar.setImageResource(new StreamResource(hasIcon.getIconName(),
                    () -> new ByteArrayInputStream(hasIcon.getIcon())));
        } catch (RuntimeException e) {
            avatar.setImage(DEFAULT_ICON);
        }

        return avatar;
    }
}

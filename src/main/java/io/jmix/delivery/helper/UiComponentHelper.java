package io.jmix.delivery.helper;

import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.server.StreamResource;
import io.jmix.delivery.entity.HasIconEntity;
import io.jmix.flowui.data.items.ContainerDataProvider;
import io.jmix.flowui.model.CollectionContainer;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.function.BiConsumer;

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

    public <T extends HasIconEntity> void addListRenderer(HasComponents rootAttachComponent,
                                                          CollectionContainer<T> collectionContainer,
                                                          BiConsumer<HasIconEntity, ListComponentContext> infoLayoutUpdater) {
        rootAttachComponent.removeAll();
        var tVirtualList = new VirtualList<T>();
        tVirtualList.setWidthFull();
        collectionContainer.addCollectionChangeListener(e -> tVirtualList.getDataProvider().refreshAll());
        tVirtualList.setDataProvider(new ContainerDataProvider<>(collectionContainer));
        tVirtualList.getDataProvider().refreshAll();
        tVirtualList.setRenderer(new ComponentRenderer<>(item -> {
            var rootCardLayout = new HorizontalLayout();
            rootCardLayout.setMargin(true);
            rootCardLayout.setSpacing(true);

            var infoLayout = new VerticalLayout();
            infoLayout.setMargin(false);
            infoLayout.setSpacing(false);
            infoLayout.setPadding(false);

            var avatar = createAvatarIconForEntity(item);

            rootCardLayout.add(avatar, infoLayout);

            infoLayoutUpdater.accept(item, new ListComponentContext(rootCardLayout, infoLayout));

            return rootCardLayout;
        }));
        rootAttachComponent.add(tVirtualList);
    }

    public record ListComponentContext(HorizontalLayout rootLayout, VerticalLayout infoLayout) {
    }
}

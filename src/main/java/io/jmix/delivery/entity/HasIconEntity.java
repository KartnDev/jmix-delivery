package io.jmix.delivery.entity;


import jakarta.annotation.Nullable;

public interface HasIconEntity {

    @Nullable
    byte[] getIcon();

    @Nullable
    String getIconName();
}

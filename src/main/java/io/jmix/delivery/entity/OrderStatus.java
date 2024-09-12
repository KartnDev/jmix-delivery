package io.jmix.delivery.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum OrderStatus implements EnumClass<String> {

    NEW("NEW"),
    ACCEPTED("ACCEPTED"),
    ON_WAIT_RESTAURANT("ON_WAIT_RESTAURANT"),
    COOKING("COOKING"),
    READY("READY");

    private final String id;

    OrderStatus(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static OrderStatus fromId(String id) {
        for (OrderStatus at : OrderStatus.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}
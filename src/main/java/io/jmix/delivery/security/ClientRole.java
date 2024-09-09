package io.jmix.delivery.security;

import io.jmix.delivery.entity.*;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.annotation.SpecificPolicy;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "Client", code = ClientRole.CODE)
public interface ClientRole {
    String CODE = "client";

    @MenuPolicy(menuIds = "OrderChooseRestaurantView")
    @ViewPolicy(viewIds = {"MainView", "OrderChooseRestaurantView", "OrderDetailView"})
    void screens();

    @EntityAttributePolicy(entityClass = Food.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = Food.class, actions = EntityPolicyAction.READ)
    void food();

    @EntityAttributePolicy(entityClass = FoodCountItem.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = FoodCountItem.class, actions = EntityPolicyAction.ALL)
    void foodCountItem();

    @EntityAttributePolicy(entityClass = Order.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = Order.class, actions = EntityPolicyAction.ALL)
    void order();

    @EntityAttributePolicy(entityClass = Restaurant.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = Restaurant.class, actions = EntityPolicyAction.READ)
    void restaurant();

    @EntityAttributePolicy(entityClass = User.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void user();

    @SpecificPolicy(resources = "ui.loginToUi")
    void specific();
}
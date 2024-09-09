package io.jmix.delivery.security;

import io.jmix.delivery.entity.Food;
import io.jmix.delivery.entity.FoodCountItem;
import io.jmix.delivery.entity.Order;
import io.jmix.delivery.entity.Restaurant;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.annotation.SpecificPolicy;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "RestaurantAdministrator", code = RestaurantAdministratorRole.CODE)
public interface RestaurantAdministratorRole {
    String CODE = "restaurant-administrator";

    @MenuPolicy(menuIds = {"MyRestaurant.list", "OrderChooseRestaurantView"})
    @ViewPolicy(viewIds = {"MyRestaurant.list", "MyRestaurant.detail", "OrderDetailView", "OrderChooseRestaurantView", "Food.detail", "LoginView", "MainView"})
    void screens();

    @EntityAttributePolicy(entityClass = Food.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = Food.class, actions = EntityPolicyAction.ALL)
    void food();

    @EntityAttributePolicy(entityClass = FoodCountItem.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = FoodCountItem.class, actions = EntityPolicyAction.ALL)
    void foodCountItem();

    @EntityAttributePolicy(entityClass = Order.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = Order.class, actions = EntityPolicyAction.ALL)
    void order();

    @EntityAttributePolicy(entityClass = Restaurant.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = Restaurant.class, actions = {EntityPolicyAction.READ, EntityPolicyAction.UPDATE})
    void restaurant();

    @SpecificPolicy(resources = "ui.loginToUi")
    void specific();
}
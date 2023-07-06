package smartin.miapi.attributes;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.architectury.event.EventResult;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import smartin.miapi.events.Event;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class AttributeRegistry {
    public static Map<String, EntityAttribute> entityAttributeMap = new HashMap<>();

    public static EntityAttribute ITEM_DURABILITY;

    public static EntityAttribute REACH;
    public static EntityAttribute ATTACK_RANGE;

    public static EntityAttribute MINING_SPEED_PICKAXE;
    public static EntityAttribute MINING_SPEED_AXE;
    public static EntityAttribute MINING_SPEED_SHOVEL;
    public static EntityAttribute MINING_SPEED_HOE;

    public static EntityAttribute DAMAGE_RESISTANCE;
    public static EntityAttribute BACK_STAB;
    public static EntityAttribute ARMOR_CRUSHING;
    public static EntityAttribute SHIELD_BREAK;


    public static void setup() {
        Event.LIVING_HURT.register((livingHurtEvent -> {
            if (livingHurtEvent.livingEntity.getAttributes().hasAttribute(DAMAGE_RESISTANCE)) {
                livingHurtEvent.amount = (float) (livingHurtEvent.amount * (100 - livingHurtEvent.livingEntity.getAttributeValue(DAMAGE_RESISTANCE)) / 100);
            }
            return EventResult.pass();
        }));
        Event.LIVING_HURT.register((livingHurtEvent -> {
            if (livingHurtEvent.damageSource.getAttacker() instanceof LivingEntity attacker) {
                if (attacker.getAttributes().hasAttribute(BACK_STAB)) {
                    if (livingHurtEvent.damageSource.getAttacker().getRotationVector().dotProduct(livingHurtEvent.livingEntity.getRotationVector()) > 0) {
                        float backStab = (float) attacker.getAttributeValue(BACK_STAB);
                        livingHurtEvent.amount = livingHurtEvent.amount * (backStab);
                    }
                }
            }
            return EventResult.pass();
        }));
        Event.LIVING_HURT_AFTER.register((livingHurtEvent -> {
            if (livingHurtEvent.damageSource.getAttacker() instanceof LivingEntity attacker) {
                if (attacker.getAttributes().hasAttribute(SHIELD_BREAK)) {
                    double value = attacker.getAttributeValue(SHIELD_BREAK);
                    if (livingHurtEvent.livingEntity instanceof PlayerEntity player) {
                        if (value > 0) {
                            player.getItemCooldownManager().set(Items.SHIELD, (int) (value * 20));
                            player.clearActiveItem();
                            player.getWorld().sendEntityStatus(player, (byte) 30);
                        }
                    }
                }
            }
            return EventResult.pass();
        }));
        Event.LIVING_HURT.register((livingHurtEvent -> {
            if (livingHurtEvent.damageSource.getAttacker() instanceof LivingEntity attacker) {
                if (attacker.getAttributes().hasAttribute(ARMOR_CRUSHING)) {
                    double value = attacker.getAttributeValue(ARMOR_CRUSHING);
                    //((LivingEntityAccessor) livingHurtEvent.livingEntity).damageArmor(livingHurtEvent.damageSource, (float) (livingHurtEvent.livingEntity.getArmor() * value));
                }
            }
            return EventResult.pass();
        }));
    }

    public static double getAttribute(ItemStack stack, EntityAttribute attribute, EquipmentSlot slot, double defaultValue) {
        Collection<EntityAttributeModifier> attributes = stack.getAttributeModifiers(slot).get(attribute);
        Multimap<EntityAttribute, EntityAttributeModifier> map = HashMultimap.create();
        attributes.forEach(attributeModifier -> {
            map.put(attribute, attributeModifier);
        });

        DefaultAttributeContainer container = DefaultAttributeContainer.builder().add(attribute, defaultValue).build();

        AttributeContainer container1 = new AttributeContainer(container);

        container1.addTemporaryModifiers(map);

        return container1.getValue(attribute);
    }
}

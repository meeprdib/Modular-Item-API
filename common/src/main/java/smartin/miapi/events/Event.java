package smartin.miapi.events;

import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import smartin.miapi.modules.abilities.util.ItemProjectile.ItemProjectile;

public class Event {
    public static dev.architectury.event.Event<LivingHurt> LIVING_HURT = EventFactory.createEventResult();

    public static dev.architectury.event.Event<LivingHurt> LIVING_HURT_AFTER = EventFactory.createEventResult();

    public static class LivingHurtEvent {
        public final LivingEntity livingEntity;
        public DamageSource damageSource;
        public float amount;

        public LivingHurtEvent(LivingEntity livingEntity, DamageSource damageSource, float amount) {
            this.livingEntity = livingEntity;
            this.damageSource = damageSource;
            this.amount = amount;

        }

        public ItemStack getCausingItemStack() {
            if (damageSource.getSource() instanceof ProjectileEntity projectile && (projectile instanceof ItemProjectile itemProjectile)) {
                    return itemProjectile.asItemStack();

            }
            if (damageSource.getAttacker() instanceof LivingEntity attacker) {
                return attacker.getMainHandStack();
            }
            return ItemStack.EMPTY;
        }
    }

    public interface LivingHurt {
        EventResult hurt(LivingHurtEvent event);
    }
}

package smartin.miapi.modules.abilities.util;

import dev.architectury.event.events.common.TickEvent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.AbilityProperty;
import smartin.miapi.modules.properties.PotionEffectProperty;
import smartin.miapi.registries.MiapiRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * The ItemAbilityManager is the brain and control behind what Ability is executed on what Item.
 * Abilities need to be added in the Module Json so the {@link AbilityProperty} can pick them up properly
 * This class then checks all provided abilites and delegates the calls from the {@link net.minecraft.item.Item} to the {@link ItemUseAbility}
 */
public class ItemAbilityManager {
    private static final Map<PlayerEntity, ItemStack> playerActiveItems = new HashMap<>();
    private static final Map<PlayerEntity, ItemStack> playerActiveItemsClient = new HashMap<>();
    public static final MiapiRegistry<ItemUseAbility> useAbilityRegistry = MiapiRegistry.getInstance(ItemUseAbility.class);
    private static final EmptyAbility emptyAbility = new EmptyAbility();
    private static final Map<ItemStack, ItemUseAbility> abilityMap = new WeakHashMap<>();

    public static void setup() {
        TickEvent.PLAYER_PRE.register((playerEntity) -> {
            if (playerEntity instanceof ServerPlayerEntity) {
                ItemStack oldItem = playerActiveItems.get(playerEntity);
                ItemStack playerItem = playerEntity.getActiveItem();
                if (playerItem != null && !playerItem.equals(oldItem)) {
                    playerActiveItems.put(playerEntity, playerEntity.getActiveItem());
                    if (oldItem != null) {
                        getAbility(oldItem).onStoppedHolding(oldItem, playerEntity.world, playerEntity);
                        abilityMap.remove(oldItem);
                    }
                }
            } else {
                ItemStack oldItem = playerActiveItemsClient.get(playerEntity);
                ItemStack playerItem = playerEntity.getActiveItem();
                if (playerItem != null && !playerItem.equals(oldItem)) {
                    playerActiveItemsClient.put(playerEntity, playerEntity.getActiveItem());
                    if (oldItem != null) {
                        getAbility(oldItem).onStoppedHolding(oldItem, playerEntity.world, playerEntity);
                        abilityMap.remove(oldItem);
                    }
                }
            }
        });
        useAbilityRegistry.register("empty", emptyAbility);
    }

    public static ItemUseAbility getEmpty() {
        return emptyAbility;
    }

    private static ItemUseAbility getAbility(ItemStack itemStack) {
        ItemUseAbility useAbility = abilityMap.get(itemStack);
        return useAbility == null ? emptyAbility : useAbility;
    }

    private static ItemUseAbility getAbility(ItemStack itemStack, World world, PlayerEntity player, Hand hand) {
        for (ItemUseAbility ability : AbilityProperty.get(itemStack)) {
            if (ability.allowedOnItem(itemStack, world, player, hand)) {
                return ability;
            }
        }
        return emptyAbility;
    }

    public static UseAction getUseAction(ItemStack itemStack) {
        return getAbility(itemStack).getUseAction(itemStack);
    }

    public static int getMaxUseTime(ItemStack itemStack) {
        return getAbility(itemStack).getMaxUseTime(itemStack);
    }

    public static TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        ItemUseAbility ability = getAbility(itemStack, world, user, hand);
        abilityMap.put(itemStack, ability);

        List<PotionEffectProperty.StatusEffectData> potionEffects = PotionEffectProperty.property.get(itemStack);
        if (potionEffects != null) applyPotionEffects(user, potionEffects, PotionEffectProperty.ApplicationEvent.ABILITY_START);

        return ability.use(world, user, hand);
    }

    public static void applyPotionEffects(LivingEntity user, List<PotionEffectProperty.StatusEffectData> list, PotionEffectProperty.ApplicationEvent event) {
        for (PotionEffectProperty.StatusEffectData effect : list) {
            if (effect.event().equals(event))
                user.addStatusEffect(effect.creator().get());
        }
    }

    public static ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        ItemStack itemStack = getAbility(stack).finishUsing(stack, world, user);
        abilityMap.remove(stack);

        List<PotionEffectProperty.StatusEffectData> potionEffects = PotionEffectProperty.property.get(itemStack);
        if (potionEffects != null) applyPotionEffects(user, potionEffects, PotionEffectProperty.ApplicationEvent.ABILITY_FINISH);

        return itemStack;
    }

    public static void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        getAbility(stack).onStoppedUsing(stack, world, user, remainingUseTicks);

        List<PotionEffectProperty.StatusEffectData> potionEffects = PotionEffectProperty.property.get(stack);
        if (potionEffects != null) applyPotionEffects(user, potionEffects, PotionEffectProperty.ApplicationEvent.ABILITY_STOP);

        abilityMap.remove(stack);
    }

    public static void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        List<PotionEffectProperty.StatusEffectData> potionEffects = PotionEffectProperty.property.get(stack);
        if (potionEffects != null) applyPotionEffects(user, potionEffects, PotionEffectProperty.ApplicationEvent.ABILITY_TICK);

        getAbility(stack).usageTick(world, user, stack, remainingUseTicks);
    }

    public static ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        return getAbility(stack).useOnEntity(stack, user, entity, hand);
    }

    public static ActionResult useOnBlock(ItemUsageContext context) {
        return getAbility(context.getStack()).useOnBlock(context);
    }

    static class EmptyAbility implements ItemUseAbility {

        @Override
        public boolean allowedOnItem(ItemStack itemStack, World world, PlayerEntity player, Hand hand) {
            return true;
        }

        @Override
        public UseAction getUseAction(ItemStack itemStack) {
            return UseAction.NONE;
        }

        @Override
        public int getMaxUseTime(ItemStack itemStack) {
            return 0;
        }

        public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
            return TypedActionResult.pass(user.getStackInHand(hand));
        }
    }
}
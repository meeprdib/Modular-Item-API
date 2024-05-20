package smartin.miapi.forge.compat;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.extensions.IForgeItem;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.abilities.toolabilities.AxeAbility;
import smartin.miapi.modules.abilities.toolabilities.HoeAbility;
import smartin.miapi.modules.abilities.toolabilities.ShovelAbility;
import smartin.miapi.modules.properties.AbilityMangerProperty;
import smartin.miapi.modules.properties.mining.MiningLevelProperty;

public interface ModularItemInject extends IForgeItem {

    default boolean canPerformActionModular(ItemStack stack, ToolAction toolAction) {
        Miapi.LOGGER.info("Injection success!");
        if (stack.getItem() instanceof ModularItem) {
            if (toolAction.equals(ToolActions.AXE_DIG)) {
                return MiningLevelProperty.isSuitable(stack, "axe");
            }
            if (toolAction.equals(ToolActions.PICKAXE_DIG)) {
                return MiningLevelProperty.isSuitable(stack, "pickaxe");
            }
            if (toolAction.equals(ToolActions.SHOVEL_DIG)) {
                return MiningLevelProperty.isSuitable(stack, "shovel");
            }
            if (toolAction.equals(ToolActions.HOE_DIG)) {
                return MiningLevelProperty.isSuitable(stack, "hoe");
            }
            if (toolAction.equals(ToolActions.SHEARS_DIG)) {
                return MiningLevelProperty.isSuitable(stack, "shear");
            }
            if (toolAction.equals(ToolActions.SWORD_DIG)) {
                return MiningLevelProperty.isSuitable(stack, "sword");
            }
            if (ToolActions.DEFAULT_AXE_ACTIONS.contains(toolAction)) {
                return AbilityMangerProperty.get(stack).stream().anyMatch(ability -> ability instanceof AxeAbility);
            }
            if (ToolActions.DEFAULT_HOE_ACTIONS.contains(toolAction)) {
                return AbilityMangerProperty.get(stack).stream().anyMatch(ability -> ability instanceof HoeAbility);
            }
            if (ToolActions.DEFAULT_SHOVEL_ACTIONS.contains(toolAction)) {
                return AbilityMangerProperty.get(stack).stream().anyMatch(ability -> ability instanceof ShovelAbility);
            }
        }
        return false;
    }
}

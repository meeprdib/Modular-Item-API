package smartin.miapi.item.modular.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.properties.DisplayNameProperty;

import java.util.UUID;

public class ExampleModularItem extends Item implements ModularItem {
    public ExampleModularItem() {
        super(new Item.Settings());
    }

    public Text getName(ItemStack stack) {
        return DisplayNameProperty.getDisplayText(stack);
    }

    /**
     * Accessor for Properties
     */
    public static UUID attackDamageUUID(){
        return ATTACK_DAMAGE_MODIFIER_ID;
    }

    public static UUID attackSpeedUUID(){
        return ATTACK_SPEED_MODIFIER_ID;
    }
}
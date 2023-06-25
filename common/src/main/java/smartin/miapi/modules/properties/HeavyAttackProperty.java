package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

public class HeavyAttackProperty implements ModuleProperty {
    public static String KEY = "heavyAttack";
    public static HeavyAttackProperty property;

    public HeavyAttackProperty() {
        property = this;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        new HeavyAttackJson(data, new ItemModule.ModuleInstance(ItemModule.empty));
        return true;
    }

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        switch (type) {
            case OVERWRITE -> {
                return toMerge.deepCopy();
            }
            case EXTEND, SMART -> {
                return old.deepCopy();
            }
        }
        return old.deepCopy();
    }

    public HeavyAttackJson get(ItemStack itemStack) {
        JsonElement jsonElement = ItemModule.getMergedProperty(itemStack, property);
        if (jsonElement == null) {
            return null;
        }
        return Miapi.gson.fromJson(jsonElement, HeavyAttackJson.class);
    }

    public boolean hasHeavyAttack(ItemStack itemStack) {
        return get(itemStack) != null;
    }

    public static class HeavyAttackJson {
        public double damage;
        public double sweeping;
        public double range;
        public double minHold;
        public double cooldown;

        public HeavyAttackJson(JsonElement element, ItemModule.ModuleInstance instance) {
            JsonObject object = element.getAsJsonObject();
            damage = get(object.get("damage"), instance);
            sweeping = get(object.get("sweeping"), instance);
            range = get(object.get("range"), instance);
            minHold = get(object.get("minHold"), instance);
            cooldown = get(object.get("cooldown"), instance);
        }

        private double get(JsonElement object, ItemModule.ModuleInstance instance) {
            try {
                return object.getAsDouble();
            } catch (Exception e) {
                return StatResolver.resolveDouble(object.getAsString(), instance);
            }
        }

    }
}
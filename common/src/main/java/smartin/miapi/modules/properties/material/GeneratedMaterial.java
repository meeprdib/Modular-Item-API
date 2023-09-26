package smartin.miapi.modules.properties.material;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.mixin.MiningToolItemAccessor;
import smartin.miapi.mixin.client.SpriteContentsAccessor;
import smartin.miapi.modules.properties.material.palette.EmptyMaterialPalette;
import smartin.miapi.modules.properties.material.palette.MaterialPalette;
import smartin.miapi.modules.properties.material.palette.MaterialPaletteFromTexture;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.*;
import java.util.stream.Collectors;

public class GeneratedMaterial implements Material {
    public final ToolMaterial toolMaterial;
    public final ItemStack mainIngredient;
    public final String key;
    public final List<String> groups = new ArrayList<>();
    public final Map<String, Double> materialStats = new HashMap<>();
    public final Map<String, String> materialStatsString = new HashMap<>();
    public JsonObject jsonObject;
    @Environment(EnvType.CLIENT)
    public MaterialPalette materialPalette;
    public @Nullable MaterialIcons.MaterialIcon icon;
    public boolean statAssignentsuccess = true;

    public GeneratedMaterial(ToolMaterial toolMaterial, boolean isClient) {
        this(toolMaterial, isClient, toolMaterial.getRepairIngredient().getMatchingStacks()[0]);
    }

    public GeneratedMaterial(ToolMaterial toolMaterial, boolean isClient, ItemStack itemStack) {
        this.toolMaterial = toolMaterial;
        mainIngredient = itemStack;
        Arrays.stream(toolMaterial.getRepairIngredient().getMatchingStacks()).forEach(stack -> {
            Miapi.DEBUG_LOGGER.info("found item " + stack.getItem() + " " + isClient);
        });
        key = "generated_" + mainIngredient.getItem().getTranslationKey();
        if (mainIngredient.getItem().getTranslationKey().contains("ingot")) {
            groups.add("metal");
        }
        if (mainIngredient.getItem().getTranslationKey().contains("stone")) {
            groups.add("stone");
        }
        if (mainIngredient.getItem().getTranslationKey().contains("bone")) {
            groups.add("bone");
        }
        if (mainIngredient.isIn(ItemTags.PLANKS)) {
            groups.add("wood");
        }
        if (groups.isEmpty()) {
            groups.add("crystal");
        }
        //TODO:generate those sensible ig?
        //maybe scan all items assosiaated with the toolmaterial to get somewhat valid stats?
        materialStats.put("durability", (double) toolMaterial.getDurability());
        materialStats.put("mining_level", (double) toolMaterial.getMiningLevel());
        materialStats.put("mining_speed", (double) toolMaterial.getMiningSpeedMultiplier());
        materialStatsString.put("translation", mainIngredient.getItem().getTranslationKey());
        Identifier itemId = Registries.ITEM.getId(mainIngredient.getItem());
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append("\"items\":");
        builder.append("[");
        builder.append("{");
        builder.append("\"item\": \"").append(itemId).append("\",");
        builder.append("\"value\": 1.0");
        builder.append("}");
        builder.append("]");
        builder.append("}");
        jsonObject = Miapi.gson.fromJson(builder.toString(), JsonObject.class);
        if (isClient) {
            clientSetup();
        }
    }

    public boolean assignStats(List<ToolItem> toolItems) {
        List<Item> toolMaterials = toolItems.stream()
                .filter(material -> toolMaterial.equals(material.getMaterial()))
                .collect(Collectors.toList());
        Optional<Item> swordItem = toolMaterials.stream().filter(SwordItem.class::isInstance).findFirst();
        Optional<Item> axeItem = toolMaterials.stream().filter(AxeItem.class::isInstance).findFirst();
        Optional<Item> pickAxeItem = toolMaterials.stream().filter(PickaxeItem.class::isInstance).findFirst();
        Optional<Item> shovelItem = toolMaterials.stream().filter(ShovelItem.class::isInstance).findFirst();
        Optional<Item> hoeItem = toolMaterials.stream().filter(HoeItem.class::isInstance).findFirst();
        if(axeItem.isEmpty()){
            axeItem = toolMaterials.stream().filter( MiningToolItem.class::isInstance).filter(miningTool -> ((MiningToolItemAccessor)miningTool).getEffectiveBlocks().equals(BlockTags.AXE_MINEABLE)).findFirst();
        }
        if (swordItem.isPresent() && axeItem.isPresent()) {
            if (swordItem.get() instanceof SwordItem swordItem1 && axeItem.get() instanceof MiningToolItem axeItem1) {
                materialStats.put("hardness", (double) swordItem1.getAttackDamage());

                double firstPart = Math.floor(Math.pow((swordItem1.getAttackDamage() - 3.4) * 2.3, 1.0 / 3.0)) + 7;

                materialStats.put("density", ((axeItem1.getAttackDamage() - firstPart) / 2.0) * 4.0);
                materialStats.put("flexibility", (double) (toolMaterial.getMiningSpeedMultiplier() / 4));
                return true;
            }
        }
        return false;
    }

    public void copyStatsFrom(Material other) {
        materialStats.put("hardness", other.getDouble("hardness"));
        materialStats.put("density", other.getDouble("density"));
        materialStats.put("flexibility", other.getDouble("flexibility"));
        materialStats.put("durability", other.getDouble("durability"));
        materialStats.put("mining_level", other.getDouble("mining_level"));
        materialStats.put("mining_speed", other.getDouble("mining_speed"));
    }

    @Environment(EnvType.CLIENT)
    public boolean hasIcon() {
        return true;
    }

    @Environment(EnvType.CLIENT)
    public int renderIcon(DrawContext drawContext, int x, int y) {
        if (icon == null) return 0;
        return icon.render(drawContext, x, y);
    }

    @Environment(EnvType.CLIENT)
    private void clientSetup() {
        Identifier itemId = Registries.ITEM.getId(mainIngredient.getItem());
        StringBuilder iconBuilder = new StringBuilder();
        iconBuilder.append("{");
        iconBuilder.append("\"type\": \"").append("item").append("\",");
        iconBuilder.append("\"item\": \"").append(itemId).append("\"");
        iconBuilder.append("}");
        icon = MaterialIcons.getMaterialIcon(key, Miapi.gson.fromJson(iconBuilder.toString(), JsonObject.class));
        try {
            BakedModel itemModel = MinecraftClient.getInstance().getItemRenderer().getModel(mainIngredient, MinecraftClient.getInstance().world, null, 0);
            SpriteContents contents = itemModel.getParticleSprite().getContents();
            materialPalette = new MaterialPaletteFromTexture(this, ((SpriteContentsAccessor) contents).getImage());
        } catch (Exception e) {
            Miapi.DEBUG_LOGGER.warn("Error during palette creation", e);
            materialPalette = new EmptyMaterialPalette(this);
        }
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public List<String> getGroups() {
        return groups;
    }

    @Override
    public MaterialPalette getPalette() {
        return materialPalette;
    }

    @Override
    public Map<ModuleProperty, JsonElement> materialProperties(String key) {
        return new HashMap<>();
    }

    @Override
    public JsonElement getRawElement(String key) {
        return jsonObject.get(key);
    }

    @Override
    public double getDouble(String property) {
        if (materialStats.containsKey(property)) {
            return materialStats.get(property);
        }
        return 0;
    }

    @Override
    public String getData(String property) {
        return materialStatsString.get(property);
    }

    @Override
    public List<String> getTextureKeys() {
        List<String> keys = new ArrayList<>(this.groups);
        keys.add("default");
        return keys;
    }

    @Override
    public int getColor() {
        return 0;
    }

    @Override
    public double getValueOfItem(ItemStack item) {
        return 1;
    }
}
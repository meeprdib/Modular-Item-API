package smartin.miapi.modules.properties.render;

import com.google.gson.JsonElement;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.item.ItemStack;
import org.joml.Vector3f;
import smartin.miapi.Miapi;
import smartin.miapi.client.model.DynamicBakedModel;
import smartin.miapi.client.modelrework.MiapiItemModel;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.Map;

/**
 * This Property allows to have a simple way to change the gui position and size of an item
 */
@Environment(EnvType.CLIENT)
public class GuiOffsetProperty implements ModuleProperty {
    public static final String KEY = "guiOffset";
    public static ModuleProperty property;

    public GuiOffsetProperty() {
        property = this;
        MiapiItemModel.modelTransformers.add((matrices, itemStack, mode, modelType, tickDelta) -> {
            if(mode.equals(ModelTransformationMode.GUI)){
                GuiOffsetJson guiOffsetJson = new GuiOffsetJson();
                for (ItemModule.ModuleInstance instance : ItemModule.createFlatList(ItemModule.getModules(itemStack))) {
                    JsonElement element = instance.getProperties().get(property);
                    if (element != null) {
                        GuiOffsetJson add = Miapi.gson.fromJson(element, GuiOffsetJson.class);
                        guiOffsetJson.x += add.x;
                        guiOffsetJson.y += add.y;
                        guiOffsetJson.sizeX += add.sizeX;
                        guiOffsetJson.sizeY += add.sizeY;
                    }
                }
                //guiOffsetJson.x -= (guiOffsetJson.sizeX/2);
                //guiOffsetJson.y -= (guiOffsetJson.sizeY/2);
                guiOffsetJson.x = guiOffsetJson.x / 16f;
                guiOffsetJson.y = guiOffsetJson.y / 16f;
                guiOffsetJson.sizeX = 1 - guiOffsetJson.sizeX / 16.0f;
                guiOffsetJson.sizeY = 1 - guiOffsetJson.sizeY / 16.0f;
                matrices.translate(guiOffsetJson.x, guiOffsetJson.y, 0);
                matrices.scale(guiOffsetJson.sizeX, guiOffsetJson.sizeY, 1);

            }
            return matrices;
        });
        ModelProperty.modelTransformers.add(new ModelProperty.ModelTransformer() {

            @Override
            public Map<String,DynamicBakedModel> bakedTransform(Map<String,DynamicBakedModel> dynamicBakedModelmap, ItemStack stack) {
                dynamicBakedModelmap.forEach((id,dynamicBakedModel)->{
                    GuiOffsetJson guiOffsetJson = new GuiOffsetJson();
                    for (ItemModule.ModuleInstance instance : ItemModule.createFlatList(ItemModule.getModules(stack))) {
                        JsonElement element = instance.getProperties().get(property);
                        if (element != null) {
                            GuiOffsetJson add = Miapi.gson.fromJson(element, GuiOffsetJson.class);
                            guiOffsetJson.x += add.x;
                            guiOffsetJson.y += add.y;
                            guiOffsetJson.sizeX += add.sizeX;
                            guiOffsetJson.sizeY += add.sizeY;
                        }
                    }
                    Transform guiTransform = new Transform(dynamicBakedModel.getTransformation().getTransformation(ModelTransformationMode.GUI));
                    guiOffsetJson.x -= (guiOffsetJson.sizeX/2) /16;
                    guiOffsetJson.y -= (guiOffsetJson.sizeY/2) /16;
                    guiOffsetJson.sizeX = guiOffsetJson.sizeX / 16.0f;
                    guiOffsetJson.sizeY = guiOffsetJson.sizeY / 16.0f;
                    float guiZ = (guiOffsetJson.sizeX + guiOffsetJson.sizeY)/2;
                    guiTransform = new Transform(guiTransform.rotation,new Vector3f(guiOffsetJson.x / 16.0f, guiOffsetJson.y / 16.0f, 0),new Vector3f(guiOffsetJson.sizeX, guiOffsetJson.sizeY, guiZ));
                    dynamicBakedModel.modelTransformation = new ModelTransformation(
                            dynamicBakedModel.getTransformation().getTransformation(ModelTransformationMode.THIRD_PERSON_LEFT_HAND),
                            dynamicBakedModel.getTransformation().getTransformation(ModelTransformationMode.THIRD_PERSON_RIGHT_HAND),
                            dynamicBakedModel.getTransformation().getTransformation(ModelTransformationMode.FIRST_PERSON_LEFT_HAND),
                            dynamicBakedModel.getTransformation().getTransformation(ModelTransformationMode.FIRST_PERSON_RIGHT_HAND),
                            dynamicBakedModel.getTransformation().getTransformation(ModelTransformationMode.HEAD),
                            guiTransform.toTransformation(),
                            dynamicBakedModel.getTransformation().getTransformation(ModelTransformationMode.GROUND),
                            dynamicBakedModel.getTransformation().getTransformation(ModelTransformationMode.FIXED)
                    );
                    dynamicBakedModelmap.put(id,dynamicBakedModel);
                });
                return dynamicBakedModelmap;
            }
        });
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        return true;
    }

    private static class GuiOffsetJson {
        public float x = 0;
        public float y = 0;
        public float sizeX = 0;
        public float sizeY = 0;
    }
}

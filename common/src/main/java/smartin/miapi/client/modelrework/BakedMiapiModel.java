package smartin.miapi.client.modelrework;

import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.material.Material;
import smartin.miapi.modules.properties.material.MaterialProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.List;

public class BakedMiapiModel implements MiapiModel {
    ItemModule.ModuleInstance instance;
    Material material;
    List<BakedModel> models;
    float red;
    float green;
    float blue;

    public BakedMiapiModel(List<BakedModel> models, ItemModule.ModuleInstance instance) {
        this.models = models;
        this.instance = instance;
        material = MaterialProperty.getMaterial(instance);
        red = (float) Math.random();
        green = (float) Math.random();
        blue = (float) Math.random();
    }

    @Override
    public void render(MatrixStack matrices, ItemStack stack, ModelTransformationMode transformationMode, float tickDelta, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (!(vertexConsumers instanceof VertexConsumerProvider.Immediate immediate)) return;

        for (BakedModel model : models) {
            for (Direction direction : Direction.values()) {
                VertexConsumer consumer;
                if (material != null)
                    consumer = material.setupMaterialShader(immediate, RegistryInventory.Client.entityTranslucentMaterialRenderType, RegistryInventory.Client.entityTranslucentMaterialShader);
                else
                    consumer = Material.setupMaterialShader(immediate, RegistryInventory.Client.entityTranslucentMaterialRenderType, RegistryInventory.Client.entityTranslucentMaterialShader, Material.baseColorPalette);

                int lightValue = transformationMode == ModelTransformationMode.GUI ? LightmapTextureManager.MAX_LIGHT_COORDINATE : LightmapTextureManager.MAX_SKY_LIGHT_COORDINATE;
                model.getQuads(null, direction, Random.create()).forEach(bakedQuad -> {
                    consumer.quad(matrices.peek(), bakedQuad, red, green, blue, lightValue, overlay);
                });
                immediate.draw();

                if (stack.hasGlint()) {
                    VertexConsumer glintConsumer = immediate.getBuffer(RegistryInventory.Client.modularItemGlint);

                    model.getQuads(null, direction, Random.create()).forEach(bakedQuad -> {
                        //red, green, blue
                        glintConsumer.quad(matrices.peek(), bakedQuad, 1, 0, 1, lightValue, overlay);
                    });
                    immediate.draw();
                }
            }
        }
    }

    @Override
    public @Nullable Matrix4f subModuleMatrix(int submoduleId) {
        return new Matrix4f();
    }
}

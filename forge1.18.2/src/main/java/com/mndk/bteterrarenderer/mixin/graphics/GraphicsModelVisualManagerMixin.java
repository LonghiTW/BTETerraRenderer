package com.mndk.bteterrarenderer.mixin.graphics;

import com.mndk.bteterrarenderer.core.graphics.model.GraphicsModel;
import com.mndk.bteterrarenderer.core.graphics.GraphicsModelVisualManager;
import com.mndk.bteterrarenderer.mod.mixin.graphics.GraphicsModelVisualManagerImpl;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.experimental.UtilityClass;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.awt.image.BufferedImage;

@UtilityClass
@Mixin(value = GraphicsModelVisualManager.class, remap = false)
public class GraphicsModelVisualManagerMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void preRender() {
        GraphicsModelVisualManagerImpl.preRender();
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public Object allocateAndGetTextureObject(BufferedImage image) {
        return GraphicsModelVisualManagerImpl.allocateAndGetTextureObject(image);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void drawModel(Object poseStack, GraphicsModel model, double px, double py, double pz, float opacity) {
        GraphicsModelVisualManagerImpl.drawModel((PoseStack) poseStack, model, px, py, pz, opacity);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void deleteTextureObject(Object textureObject) {
        GraphicsModelVisualManagerImpl.deleteTexture((ResourceLocation) textureObject);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void postRender() {
        GraphicsModelVisualManagerImpl.postRender();
    }
}
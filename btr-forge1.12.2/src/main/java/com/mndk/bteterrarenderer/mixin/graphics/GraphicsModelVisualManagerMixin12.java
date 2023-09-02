package com.mndk.bteterrarenderer.mixin.graphics;

import com.mndk.bteterrarenderer.core.graphics.GraphicsModel;
import com.mndk.bteterrarenderer.core.graphics.GraphicsQuad;
import com.mndk.bteterrarenderer.core.graphics.GraphicsModelVisualManager;
import lombok.experimental.UtilityClass;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.awt.image.BufferedImage;

@UtilityClass
@Mixin(value = GraphicsModelVisualManager.class, remap = false)
public class GraphicsModelVisualManagerMixin12 {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void preRender() {
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public int allocateAndUploadTexture(BufferedImage image) {
        int glId = GL11.glGenTextures();
        int width = image.getWidth(), height = image.getHeight();
        TextureUtil.allocateTexture(glId, width, height);

        int[] imageData = new int[width * height];
        image.getRGB(0, 0, width, height, imageData, 0, width);
        TextureUtil.uploadTexture(glId, imageData, width, height);
        return glId;
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void drawModel(Object poseStack, GraphicsModel model, double px, double py, double pz, float opacity) {
        GlStateManager.bindTexture(model.getTextureGlId());

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuffer();
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

        for(GraphicsQuad<?> quad : model.getQuads()) {
            for (int i = 0; i < 4; i++) {
                GraphicsQuad.VertexInfo vertex = quad.getVertex(i);
                if(vertex instanceof GraphicsQuad.PosTexColor) {
                    GraphicsQuad.PosTexColor posTexColor = (GraphicsQuad.PosTexColor) vertex;
                    builder.pos(posTexColor.x - px, posTexColor.y - py, posTexColor.z - pz)
                            .tex(posTexColor.u, posTexColor.v)
                            .color(posTexColor.r, posTexColor.g, posTexColor.b, posTexColor.a * opacity)
                            .endVertex();
                } else {
                    // TODO
                    throw new UnsupportedOperationException("Not implemented");
                }
            }
            tessellator.draw();
        }
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void glDeleteTexture(int glId) {
        GlStateManager.deleteTexture(glId);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void postRender() {
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
    }
}
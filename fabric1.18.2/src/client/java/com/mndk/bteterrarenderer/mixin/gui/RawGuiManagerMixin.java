package com.mndk.bteterrarenderer.mixin.gui;

import com.mndk.bteterrarenderer.core.graphics.format.PosXY;
import com.mndk.bteterrarenderer.core.graphics.shape.GraphicsQuad;
import com.mndk.bteterrarenderer.core.gui.RawGuiManager;
import com.mndk.bteterrarenderer.core.gui.components.AbstractGuiScreenCopy;
import com.mndk.bteterrarenderer.core.gui.components.GuiAbstractWidgetCopy;
import com.mndk.bteterrarenderer.core.util.minecraft.IResourceLocation;
import com.mndk.bteterrarenderer.mod.client.mixin.delegate.IResourceLocationIdentifierImpl;
import com.mndk.bteterrarenderer.mod.client.mixin.graphics.AbstractGuiScreenImpl;
import com.mndk.bteterrarenderer.mod.client.mixin.gui.RawGuiManagerImpl;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import javax.annotation.Nonnull;

@UtilityClass
@Mixin(value = RawGuiManager.class, remap = false)
public class RawGuiManagerMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void displayGuiScreen(AbstractGuiScreenCopy gui) {
        MinecraftClient.getInstance().setScreen(new AbstractGuiScreenImpl(gui));
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void fillQuad(Object poseStack, GraphicsQuad<PosXY> quad, int color, float z) {
        PosXY v0 = quad.getVertex(0), v1 = quad.getVertex(1), v2 = quad.getVertex(2), v3 = quad.getVertex(3);
        Matrix4f matrix = ((MatrixStack) poseStack).peek().getPositionMatrix();

        float a = (float)(color >> 24 & 255) / 255.0F;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >>  8 & 255) / 255.0F;
        float b = (float)(color       & 255) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferbuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferbuilder.vertex(matrix, v0.x, v0.y, z).color(r, g, b, a).next();
        bufferbuilder.vertex(matrix, v1.x, v1.y, z).color(r, g, b, a).next();
        bufferbuilder.vertex(matrix, v2.x, v2.y, z).color(r, g, b, a).next();
        bufferbuilder.vertex(matrix, v3.x, v3.y, z).color(r, g, b, a).next();
        bufferbuilder.end();
        BufferRenderer.draw(bufferbuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void drawCheckBox(Object poseStack, int x, int y, int width, int height, boolean focused, boolean checked) {
        RenderSystem.setShaderTexture(0, RawGuiManagerImpl.CHECKBOX);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        Matrix4f matrix = ((MatrixStack) poseStack).peek().getPositionMatrix();

        float size = 20 / 64f;
        float u0 = focused ? size : 0, v0 = checked ? size : 0;
        float u1 = u0 + size, v1 = v0 + size;
        RawGuiManagerImpl.drawBufferPosTex(bufferbuilder, matrix, x, y, width, height, u0, v0, u1, v1);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void drawTextFieldHighlight(Object poseStack, int startX, int startY, int endX, int endY) {
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(0.0F, 0.0F, 1.0F, 1.0F);
        RenderSystem.disableTexture();
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        Matrix4f matrix = ((MatrixStack) poseStack).peek().getPositionMatrix();
        bufferbuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        bufferbuilder.vertex(matrix, startX, endY, 0).next();
        bufferbuilder.vertex(matrix, endX, endY, 0).next();
        bufferbuilder.vertex(matrix, endX, startY, 0).next();
        bufferbuilder.vertex(matrix, startX, startY, 0).next();
        tessellator.draw();

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableColorLogicOp();
        RenderSystem.enableTexture();

    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void drawImage(Object poseStack, IResourceLocation res, int x, int y, int w, int h, float u1, float v1, float u2, float v2) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, ((IResourceLocationIdentifierImpl) res).delegate());
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        Matrix4f matrix = ((MatrixStack) poseStack).peek().getPositionMatrix();
        RawGuiManagerImpl.drawBufferPosTex(bufferbuilder, matrix, x, y, w, h, u1, v1, u2, v2);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void drawButton(Object poseStack, int x, int y, int width, int height, GuiAbstractWidgetCopy.HoverState hoverState) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, ClickableWidget.WIDGETS_TEXTURE);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        int i = switch (hoverState) {
            case DISABLED -> 0;
            case DEFAULT -> 1;
            case MOUSE_OVER -> 2;
        };

        MatrixStack realPoseStack = (MatrixStack) poseStack;
        DrawableHelper.drawTexture(realPoseStack, x, y, 0, 0, 46 + i * 20, width / 2, height, 256, 256);
        DrawableHelper.drawTexture(realPoseStack, x + width / 2, y, 0, 200 - (float) width / 2, 46 + i * 20, width / 2, height, 256, 256);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void drawNativeImage(Object poseStack, Object allocatedTextureObject, int x, int y, int w, int h) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, (Identifier) allocatedTextureObject);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        Matrix4f matrix = ((MatrixStack) poseStack).peek().getPositionMatrix();
        bufferbuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferbuilder.vertex(matrix, x, y, 0).texture(0, 0).next();
        bufferbuilder.vertex(matrix, x, y+h, 0).texture(0, 1).next();
        bufferbuilder.vertex(matrix, x+w, y+h, 0).texture(1, 1).next();
        bufferbuilder.vertex(matrix, x+w, y, 0).texture(1, 0).next();
        bufferbuilder.end();
        BufferRenderer.draw(bufferbuilder);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public static void drawTooltipTextBox(Object poseStack, @Nonnull Object tooltipTextComponent, int hoverX, int hoverY) {
        Screen screen = MinecraftClient.getInstance().currentScreen;
        if(screen == null) return;
        screen.renderTooltip((MatrixStack) poseStack, (Text) tooltipTextComponent, hoverX, hoverY);
    }
}

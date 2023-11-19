package com.mndk.bteterrarenderer.mixin.gui;

import com.mndk.bteterrarenderer.core.gui.FontManager;
import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.List;

@UtilityClass
@Mixin(value = FontManager.class, remap = false)
public class FontManagerMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public int getFontHeight() {
        return MinecraftClient.getInstance().textRenderer.fontHeight;
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public int getStringWidth(String text) {
        return MinecraftClient.getInstance().textRenderer.getWidth(text);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public int getComponentWidth(Object textComponent) {
        if(textComponent instanceof StringVisitable visitable) {
            return MinecraftClient.getInstance().textRenderer.getWidth(visitable);
        }
        else if(textComponent instanceof OrderedText text) {
            return MinecraftClient.getInstance().textRenderer.getWidth(text);
        }
        return 0;
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public int drawStringWithShadow(Object poseStack, String text, float x, float y, int color) {
        return MinecraftClient.getInstance().textRenderer.drawWithShadow((MatrixStack) poseStack, text, x, y, color);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public int drawComponentWithShadow(Object poseStack, Object textComponent, float x, float y, int color) {
        if(textComponent instanceof Text text) {
            return MinecraftClient.getInstance().textRenderer.drawWithShadow((MatrixStack) poseStack, text, x, y, color);
        }
        else if(textComponent instanceof OrderedText text) {
            return MinecraftClient.getInstance().textRenderer.drawWithShadow((MatrixStack) poseStack, text, x, y, color);
        }
        return 0;
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public String trimStringToWidth(String text, int width) {
        return MinecraftClient.getInstance().textRenderer.trimToWidth(text, width);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public List<String> splitStringByWidth(String str, int wrapWidth) {
        return MinecraftClient.getInstance().textRenderer.getTextHandler().wrapLines(str, wrapWidth, Style.EMPTY)
                .stream().map(StringVisitable::getString).toList();
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public List<?> splitComponentByWidth(Object textComponent, int wrapWidth) {
        return ChatMessages.breakRenderedChatMessageLines((StringVisitable) textComponent, wrapWidth,
                MinecraftClient.getInstance().textRenderer);
    }

}

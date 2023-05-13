package com.mndk.bteterrarenderer.connector.gui;

import com.mndk.bteterrarenderer.connector.ConnectorImpl;
import net.minecraft.client.Minecraft;

import java.util.List;

@ConnectorImpl
public class FontRendererConnectorImpl implements FontRendererConnector {
    public int getFontHeight() {
        return Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;
    }

    public int getStringWidth(String text) {
        return Minecraft.getMinecraft().fontRenderer.getStringWidth(text);
    }

    public int getWordWrappedHeight(String text, int maxLength) {
        return Minecraft.getMinecraft().fontRenderer.getWordWrappedHeight(text, maxLength);
    }

    public void drawStringWithShadow(String text, float x, float y, int color) {
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(text, x, y, color);
    }

    public void drawSplitString(String str, int x, int y, int wrapWidth, int textColor) {
        Minecraft.getMinecraft().fontRenderer.drawSplitString(str, x, y, wrapWidth, textColor);
    }

    public String trimStringToWidth(String text, int width) {
        return Minecraft.getMinecraft().fontRenderer.trimStringToWidth(text, width);
    }

    public List<String> listFormattedStringToWidth(String str, int wrapWidth) {
        return Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(str, wrapWidth);
    }
}
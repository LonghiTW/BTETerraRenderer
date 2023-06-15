package com.mndk.bteterrarenderer.connector.gui;

import com.mndk.bteterrarenderer.connector.graphics.IScaledScreenSizeImpl18;
import com.mndk.bteterrarenderer.connector.minecraft.InputKey;
import com.mndk.bteterrarenderer.gui.components.AbstractGuiScreen;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

public class AbstractGuiScreenImpl18 extends Screen {
    public final AbstractGuiScreen delegate;

    protected AbstractGuiScreenImpl18(AbstractGuiScreen delegate) {
        super(TextComponent.EMPTY);
        this.delegate = delegate;
        delegate.guiWidth = () -> super.width;
        delegate.screenSize = IScaledScreenSizeImpl18::new;
    }

    protected void init() {
        delegate.initGui();
    }
    public void tick() {
        delegate.tick();
    }
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        delegate.drawScreen(poseStack, mouseX, mouseY, partialTicks);
    }
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        return delegate.mousePressed(mouseX, mouseY, mouseButton);
    }
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        super.mouseReleased(mouseX, mouseY, mouseButton);
        return delegate.mouseReleased(mouseX, mouseY, mouseButton);
    }
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dx, double dy) {
        super.mouseDragged(mouseX, mouseY, mouseButton, dx, dy);
        return delegate.mouseDragged(mouseX, mouseY, mouseButton, mouseX - dx, mouseY - dy);
    }
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        super.mouseScrolled(mouseX, mouseY, scrollAmount);
        return delegate.mouseScrolled(mouseX, mouseY, scrollAmount);
    }
    public boolean keyPressed(int keyCode, int scanCode, int mods) {
        boolean superResult = super.keyPressed(keyCode, scanCode, mods);
        boolean delegateResult = delegate.keyPressed(InputKey.fromGlfwKeyCode(keyCode));
        return superResult || delegateResult;
    }
    public boolean charTyped(char typedChar, int keyCode) {
        super.charTyped(typedChar, keyCode);
        return delegate.keyTyped(typedChar, keyCode);
    }

    public void onClose() {
        delegate.onClose();
        super.onClose();
    }
    public boolean isPauseScreen() {
        return delegate.doesScreenPauseGame();
    }
}
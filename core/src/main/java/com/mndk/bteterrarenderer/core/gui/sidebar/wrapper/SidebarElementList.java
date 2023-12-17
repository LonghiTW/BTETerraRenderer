package com.mndk.bteterrarenderer.core.gui.sidebar.wrapper;

import com.google.common.collect.Lists;
import com.mndk.bteterrarenderer.mcconnector.graphics.GlGraphicsManager;
import com.mndk.bteterrarenderer.mcconnector.gui.RawGuiManager;
import com.mndk.bteterrarenderer.core.gui.sidebar.GuiSidebarElement;
import com.mndk.bteterrarenderer.mcconnector.input.InputKey;
import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.core.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.mcconnector.client.MinecraftClientManager;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SidebarElementList extends GuiSidebarElement {

    private static final int VERTICAL_SLIDER_WIDTH = 6;
    private static final int VERTICAL_SLIDER_PADDING = 2;
    private static final int VERTICAL_SLIDER_COLOR = NORMAL_TEXT_COLOR;
    private static final int VERTICAL_SLIDER_COLOR_HOVERED = HOVERED_COLOR;
    private static final int VERTICAL_SLIDER_COLOR_CLICKED = FOCUSED_BORDER_COLOR;

    private final List<Entry> entryList = new ArrayList<>();
    private final int elementDistance;
    private final int sidePadding;
    private int elementsTotalPhysicalHeight, elementsTotalVisualHeight;
    private final boolean makeSound;

    @Nullable
    private final Supplier<Integer> maxHeight;
    private int verticalSliderValue, initialVerticalSliderValue;
    private boolean verticalSliderHoverState, verticalSliderChangingState;
    private double mouseClickY;

    /**
     * @param elementDistance Distance between elements
     * @param sidePadding Each individual elements' side padding
     * @param maxHeight Max height. Values other than {@code null} will make the vertical slider appear.
     * @param makeSound Whether to make sound when one of the elements is clicked
     */
    public SidebarElementList(int elementDistance, int sidePadding,
                              @Nullable Supplier<Integer> maxHeight, boolean makeSound) {
        this.elementDistance = elementDistance;
        this.sidePadding = sidePadding;
        this.makeSound = makeSound;

        this.maxHeight = maxHeight;
        this.verticalSliderValue = 0;
    }

    public SidebarElementList clear() {
        this.entryList.clear();
        return this;
    }

    public SidebarElementList add(GuiSidebarElement element) {
        if(element == null) return this;
        this.entryList.add(new Entry(element));
        if(this.getWidth() != -1) element.init(this.getWidth());
        return this;
    }

    /**
     * Skips null elements
     */
    @SuppressWarnings("UnusedReturnValue")
    public SidebarElementList addAll(GuiSidebarElement... elements) {
        for(GuiSidebarElement element : elements) this.add(element);
        return this;
    }

    /**
     * Skips null elements
     */
    @SuppressWarnings("UnusedReturnValue")
    public SidebarElementList addAll(List<GuiSidebarElement> elements) {
        for(GuiSidebarElement element : elements) this.add(element);
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public SidebarElementList addProperties(List<PropertyAccessor.Localized<?>> properties) {
        List<GuiSidebarElement> elements = properties.stream()
                .map(GuiSidebarElement::fromProperty)
                .collect(Collectors.toList());
        this.addAll(elements);
        return this;
    }

    @Override
    public int getPhysicalHeight() {
        return this.maxHeight != null ? this.maxHeight.get() : this.elementsTotalPhysicalHeight;
    }

    @Override
    public int getVisualHeight() {
        return this.maxHeight != null ? this.maxHeight.get() : this.elementsTotalVisualHeight;
    }

    @Override
    public void init() {
        for(Entry entry : entryList) {
            GuiSidebarElement element = entry.element;
            if(element == null) continue;
            element.init(this.getWidth() - 2 * this.sidePadding);
        }
        // This is to avoid the initial visual glitch
        this.calculateHeights();
    }

    private void calculateHeights() {
        this.elementsTotalPhysicalHeight = 0;
        this.elementsTotalVisualHeight = 0;

        for(Entry entry : entryList) {
            GuiSidebarElement element = entry.element;
            if(element == null || element.hide) continue;

            int physicalHeight = element.getPhysicalHeight();
            int visualHeight = element.getVisualHeight();

            if(visualHeight > 0) {
                int newTotalVisualHeight = this.elementsTotalPhysicalHeight + visualHeight;
                if (newTotalVisualHeight > this.elementsTotalVisualHeight) {
                    this.elementsTotalVisualHeight = newTotalVisualHeight;
                }
            }

            entry.yPos = this.elementsTotalPhysicalHeight;
            this.elementsTotalPhysicalHeight += physicalHeight + this.elementDistance;
        }
        this.elementsTotalPhysicalHeight -= this.elementDistance;
    }

    @Override
    public void tick() {
        for(Entry entry : entryList) {
            GuiSidebarElement element = entry.element;
            if(element == null || element.hide) continue;
            element.tick();
        }
    }

    @Override
    public boolean mouseHovered(double mouseX, double mouseY, float partialTicks, boolean mouseHidden) {
        this.calculateHeights();

        // Vertical slider
        if(this.maxHeight != null) {
            boolean result = this.verticalSliderHoverState = this.isMouseOnScrollBar(mouseX, mouseY);
            if (result) return true;
        }

        // Check for all elements
        boolean hovered = false;
        for(Entry entry : entryList) {
            GuiSidebarElement element = entry.element;
            if(element == null || element.hide) continue;

            boolean elementHovered = element.mouseHovered(
                    mouseX - this.sidePadding, mouseY - entry.yPos + verticalSliderValue, partialTicks,
                    mouseHidden || hovered);
            if(elementHovered) hovered = true;
        }
        return hovered;
    }

    @Override
    public void drawComponent(Object poseStack) {
        int prevYPos = 0;

        if(this.maxHeight != null) {
            //noinspection DataFlowIssue
            GlGraphicsManager.INSTANCE.pushRelativeScissor(poseStack, 0, 0, this.getWidth(), this.maxHeight.get());
            this.validateSliderValue();
        }
        GlGraphicsManager.INSTANCE.glPushMatrix(poseStack);
        GlGraphicsManager.INSTANCE.glTranslate(poseStack, this.sidePadding, -this.verticalSliderValue, 0);
        // Draw from the last so that the first element could appear in the front
        for(Entry entry : Lists.reverse(entryList)) {
            GuiSidebarElement element = entry.element;
            if(element == null || element.hide) continue;

            int yPos = entry.yPos;
            GlGraphicsManager.INSTANCE.glTranslate(poseStack, 0, yPos - prevYPos, element.getCount());

            element.drawComponent(poseStack);
            prevYPos = yPos;
        }
        if(this.maxHeight != null) {
            GlGraphicsManager.INSTANCE.popRelativeScissor();
        }
        GlGraphicsManager.INSTANCE.glPopMatrix(poseStack);
        this.drawVerticalSlider(poseStack);
    }

    private void drawVerticalSlider(Object poseStack) {
        if(this.maxHeight == null) return;

        int[] dimension = this.getVerticalSliderDimension();
        int color = this.verticalSliderHoverState ? VERTICAL_SLIDER_COLOR_HOVERED : VERTICAL_SLIDER_COLOR;
        if(this.verticalSliderChangingState) color = VERTICAL_SLIDER_COLOR_CLICKED;
        RawGuiManager.INSTANCE.fillRect(poseStack,
                dimension[0], dimension[1] + VERTICAL_SLIDER_PADDING,
                dimension[2] - VERTICAL_SLIDER_PADDING, dimension[3] - VERTICAL_SLIDER_PADDING, color);
    }

    @Override
    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        this.initialVerticalSliderValue = this.verticalSliderValue;
        this.mouseClickY = mouseY;

        // Check for all elements
        for(Entry entry : entryList) {
            GuiSidebarElement element = entry.element;
            if (element == null || element.hide) continue;

            int yPos = entry.yPos;
            boolean elementPressed = element.mousePressed(
                    mouseX - this.sidePadding, mouseY - yPos + this.verticalSliderValue, mouseButton);
            if(elementPressed) {
                if(this.makeSound) MinecraftClientManager.playClickSound();
                return true;
            }
        }

        // Check vertical slider press
        if(this.maxHeight == null) return false;
        this.verticalSliderChangingState = this.isMouseOnScrollBar(mouseX, mouseY);
        return this.verticalSliderChangingState;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        for(Entry entry : entryList) {
            GuiSidebarElement element = entry.element;
            if(element == null || element.hide) continue;

            int yPos = entry.yPos;
            element.mouseReleased(
                    mouseX - this.sidePadding, mouseY - yPos + this.verticalSliderValue, mouseButton);
        }

        if(this.maxHeight != null) this.verticalSliderChangingState = false;
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        // Check for all elements
        for(Entry entry : entryList) {
            GuiSidebarElement element = entry.element;
            if(element == null || element.hide) continue;

            boolean elementScrolled = element.mouseScrolled(
                    mouseX - this.sidePadding, mouseY - entry.yPos + verticalSliderValue, scrollAmount);
            if(elementScrolled) return true;
        }

        if(this.maxHeight != null) {
            int maxHeight = this.maxHeight.get();
            if (0 <= mouseX && mouseX <= this.getWidth() && 0 <= mouseY && mouseY <= maxHeight) {
                this.verticalSliderValue -= (int) (Math.signum(scrollAmount) * 30);
                this.validateSliderValue();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double pMouseX, double pMouseY) {
        // Check vertical slider dragging
        if(this.maxHeight != null && this.verticalSliderChangingState) {
            double totalHeight = this.elementsTotalVisualHeight;
            double maxHeight = this.maxHeight.get();

            double dMouseY = mouseY - mouseClickY;
            double dValue = dMouseY * totalHeight / (maxHeight - 2);
            double newVerticalSliderValue = BTRUtil.clamp(
                    initialVerticalSliderValue + dValue, 0, Math.max(totalHeight - maxHeight, 0));

            this.verticalSliderValue = (int) newVerticalSliderValue;
            return true;
        }

        // Check for every element
        for(Entry entry : entryList) {
            GuiSidebarElement element = entry.element;
            if (element == null || element.hide) continue;

            int yPos = entry.yPos;
            boolean elementDragged = element.mouseDragged(
                    mouseX - this.sidePadding, mouseY - yPos + verticalSliderValue, mouseButton,
                    pMouseX - this.sidePadding, pMouseY - yPos + verticalSliderValue);
            if(elementDragged) return true;
        }
        return false;
    }

    private void validateSliderValue() {
        if(this.maxHeight != null && verticalSliderValue != 0) {
            int totalHeight = this.elementsTotalVisualHeight;
            if (this.verticalSliderValue > totalHeight - this.maxHeight.get()) {
                this.verticalSliderValue = totalHeight - this.maxHeight.get();
            }
            if (this.verticalSliderValue < 0) this.verticalSliderValue = 0;
        }
    }

    private boolean isMouseOnScrollBar(double mouseX, double mouseY) {
        int[] dimension = this.getVerticalSliderDimension();
        return mouseX >= dimension[0] && mouseX <= dimension[2] && mouseY >= dimension[1] && mouseY <= dimension[3];
    }

    /**
     * @return [ x1, y1, x2, y2 ]
     */
    private int[] getVerticalSliderDimension() {
        double elementsHeight = this.elementsTotalVisualHeight;
        if(this.maxHeight == null || elementsHeight <= this.maxHeight.get()) return new int[] { 0, 0, 0, 0 };

        int maxHeight = this.maxHeight.get();
        double multiplier = maxHeight / elementsHeight;
        int sliderY = (int) (this.verticalSliderValue * multiplier);
        int sliderHeight = (int) (maxHeight * multiplier);

        return new int[] {
                this.getWidth() - VERTICAL_SLIDER_WIDTH, sliderY,
                this.getWidth(), sliderY + sliderHeight
        };
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) {
        for(Entry entry : entryList) {
            GuiSidebarElement element = entry.element;
            if (element == null || element.hide) continue;
            if (element.keyTyped(typedChar, keyCode)) return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(InputKey key) {
        for(Entry entry : entryList) {
            GuiSidebarElement element = entry.element;
            if (element == null || element.hide) continue;
            if (element.keyPressed(key)) return true;
        }
        return false;
    }

    @Override
    public void onWidthChange() {
        for(Entry entry : entryList) {
            GuiSidebarElement element = entry.element;
            if(element == null || element.hide) continue;
            element.onWidthChange(this.getWidth() - 2 * this.sidePadding);
        }
    }

    @Override
    public int getCount() {
        return entryList.size();
    }

    @RequiredArgsConstructor
    private static class Entry {
        final GuiSidebarElement element;
        int yPos = 0;
    }
}

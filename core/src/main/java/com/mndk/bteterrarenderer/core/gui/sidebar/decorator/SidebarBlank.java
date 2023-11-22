package com.mndk.bteterrarenderer.core.gui.sidebar.decorator;

import com.mndk.bteterrarenderer.core.gui.sidebar.GuiSidebarElement;

public class SidebarBlank extends GuiSidebarElement {

    private final int height;

    public SidebarBlank(int height) {
        this.height = height;
    }

    @Override public int getPhysicalHeight() { return height; }
    @Override protected void init() {}
    @Override public void onWidthChange() {}
    @Override public void drawComponent(Object poseStack) {}
}
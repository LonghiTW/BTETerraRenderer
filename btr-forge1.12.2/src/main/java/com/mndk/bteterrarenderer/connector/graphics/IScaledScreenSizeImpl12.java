package com.mndk.bteterrarenderer.connector.graphics;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

@RequiredArgsConstructor @Getter
public class IScaledScreenSizeImpl12 implements IScaledScreenSize {
    private final int width, height;
    public IScaledScreenSizeImpl12() {
        this(new ScaledResolution(Minecraft.getMinecraft()));
    }
    public IScaledScreenSizeImpl12(ScaledResolution scaledResolution) {
        this(scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight());
    }
}

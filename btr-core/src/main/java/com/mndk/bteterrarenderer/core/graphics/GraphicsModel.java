package com.mndk.bteterrarenderer.core.graphics;

import lombok.Data;

import javax.annotation.Nonnull;
import java.util.List;

@Data
public class GraphicsModel {
    private final int textureGlId;
    @Nonnull
    private final List<GraphicsQuad<?>> quads;
}

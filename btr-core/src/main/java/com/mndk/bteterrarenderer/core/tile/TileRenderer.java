package com.mndk.bteterrarenderer.core.tile;

import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.core.graphics.GlGraphicsManager;
import com.mndk.bteterrarenderer.core.graphics.ModelGraphicsManager;
import com.mndk.bteterrarenderer.core.graphics.GraphicsModelBaker;
import com.mndk.bteterrarenderer.core.projection.Projections;

public class TileRenderer {

    public static void renderTiles(Object poseStack, double px, double py, double pz) {
        if(!BTETerraRendererConfig.INSTANCE.isDoRender()) return;

        BTETerraRendererConfig config = BTETerraRendererConfig.INSTANCE;
        BTETerraRendererConfig.HologramConfig hologramConfig = BTETerraRendererConfig.HologramConfig.INSTANCE;

        TileMapService tms = config.getTileMapService().getItem();
        if(tms == null) return;
        if(Projections.getServerProjection() == null) return;

        double yDiff = hologramConfig.getFlatMapYAxis() - py;
        if(Math.abs(yDiff) >= hologramConfig.getYDiffLimit()) return;

        GlGraphicsManager.glPushMatrix(poseStack);
        ModelGraphicsManager.preRender();

        String tmsId = config.getMapServiceCategory() + "." + config.getMapServiceId();
        tms.render(poseStack, tmsId,
                px + hologramConfig.getXAlign(), py, pz + hologramConfig.getZAlign(),
                (float) hologramConfig.getOpacity());
        GraphicsModelBaker.INSTANCE.cleanup();

        ModelGraphicsManager.postRender();
        GlGraphicsManager.glPopMatrix(poseStack);
    }
}

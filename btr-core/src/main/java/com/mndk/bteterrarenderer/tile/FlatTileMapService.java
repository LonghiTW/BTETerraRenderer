package com.mndk.bteterrarenderer.tile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mndk.bteterrarenderer.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.graphics.GraphicsModel;
import com.mndk.bteterrarenderer.connector.terraplusplus.HttpConnector;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.OutOfProjectionBoundsException;
import com.mndk.bteterrarenderer.loader.CategoryMapData;
import com.mndk.bteterrarenderer.loader.ProjectionYamlLoader;
import com.mndk.bteterrarenderer.projection.Projections;
import com.mndk.bteterrarenderer.projection.TileProjection;
import com.mndk.bteterrarenderer.util.BtrUtil;
import com.mndk.bteterrarenderer.graphics.GraphicsQuad;
import com.mndk.bteterrarenderer.graphics.GraphicsModelManager;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Data
public class FlatTileMapService implements CategoryMapData.ICategoryMapProperty {

    public static final int RETRY_COUNT = 3;
    public static final int DEFAULT_MAX_THREAD = 2;
    public static final int DEFAULT_ZOOM = 18;
    public static BufferedImage SOMETHING_WENT_WRONG;

    private transient String source = "";

    private final String name;
    private final String urlTemplate;
    private final TileProjection tileProjection;
    private final TileURLConverter urlConverter;
    private final ExecutorService downloadExecutor;

    /**
     * @throws NullPointerException If the projection corresponding to its id does not exist
     */
    @JsonCreator
    public FlatTileMapService(
            @JsonProperty(value = "name", required = true) String name,
            @JsonProperty(value = "tile_url", required = true) String urlTemplate,
            @JsonProperty(value = "projection", required = true) String projectionName,
            @JsonProperty(value = "max_thread", defaultValue = "2") Integer maxThread,
            @JsonProperty(value = "default_zoom", defaultValue = "18") Integer defaultZoom,
            @JsonProperty(value = "invert_lat", defaultValue = "false") Boolean invertLatitude,
            @JsonProperty(value = "flip_vert", defaultValue = "false") Boolean flipVertically,
            @JsonProperty(value = "invert_zoom", defaultValue = "false") Boolean invertZoom
    ) throws NullPointerException, CloneNotSupportedException {

        this.name = name;
        this.urlTemplate = urlTemplate;

        TileProjection projectionSearchResult = ProjectionYamlLoader.INSTANCE.result.get(projectionName);
        int _defaultZoom = BtrUtil.validateNull(defaultZoom, DEFAULT_ZOOM);
        boolean _invertZoom = BtrUtil.validateNull(invertZoom, false);

        if(projectionSearchResult != null) {
            this.tileProjection = projectionSearchResult.clone();
            tileProjection.setDefaultZoom(_defaultZoom);
            tileProjection.setInvertZoom(_invertZoom);
            tileProjection.setFlipVertically(BtrUtil.validateNull(flipVertically, false));
            tileProjection.setInvertLatitude(BtrUtil.validateNull(invertLatitude, false));
        } else {
            BTETerraRendererConstants.LOGGER.error("Couldn't find tile projection named \"" + projectionName + "\"");
            this.tileProjection = null;
        }

        this.urlConverter = new TileURLConverter(_defaultZoom, _invertZoom);
        this.downloadExecutor = Executors.newFixedThreadPool(BtrUtil.validateNull(maxThread, DEFAULT_MAX_THREAD));
    }

    public String getUrlFromGeoCoordinate(double longitude, double latitude, int relativeZoom) throws OutOfProjectionBoundsException {
        int[] tileCoord = this.tileProjection.geoCoordToTileCoord(longitude, latitude, relativeZoom);
        return this.getUrlFromTileCoordinate(tileCoord[0], tileCoord[1], relativeZoom);
    }

    public String getUrlFromTileCoordinate(int tileX, int tileY, int relativeZoom) {
        return this.urlConverter.convertToUrl(this.urlTemplate, tileX, tileY, relativeZoom);
    }

    public void renderTile(
            Object poseStack,
            int relativeZoom, String tmsId,
            double y, float opacity,
            double playerX, double playerY, double playerZ,
            int tileDeltaX, int tileDeltaY
    ) {
        if(this.tileProjection == null) return;
        try {
            double[] gameCoord, geoCoord = Projections.getServerProjection().toGeo(playerX, playerZ);
            int[] tileCoord = this.tileProjection.geoCoordToTileCoord(geoCoord[0], geoCoord[1], relativeZoom);
            int tileX = tileCoord[0] + tileDeltaX, tileY = tileCoord[1] + tileDeltaY;
            final String tileKey = this.genTileKey(tmsId, tileX, tileY, relativeZoom);

            GraphicsModelManager cache = GraphicsModelManager.getInstance();
            cache.registerAllModelsInQueue();

            // Return if the requested tile is still in the downloading state
            if(cache.isTextureInDownloadingState(tileKey)) return;

            if(cache.modelExists(tileKey)) {
                GraphicsModel model = cache.updateAndGetModel(tileKey);
                ModelGraphicsConnector.INSTANCE.drawModel(poseStack, model, playerX, playerY - y, playerZ, opacity);
                return;
            }

            // If the requested tile is not loaded, load it in the new thread
            /*
             *  i=0 ------ i=1
             *   |          |
             *   |   TILE   |
             *   |          |
             *  i=3 ------ i=2
             */
            GraphicsQuad<GraphicsQuad.PosTexColor> quad = new GraphicsQuad<>();
            for (int i = 0; i < 4; i++) {
                int[] mat = this.tileProjection.getCornerMatrix(i);
                geoCoord = tileProjection.tileCoordToGeoCoord(tileX + mat[0], tileY + mat[1], relativeZoom);
                gameCoord = Projections.getServerProjection().fromGeo(geoCoord[0], geoCoord[1]);

                quad.setVertex(i, new GraphicsQuad.PosTexColor(
                        gameCoord[0], 0, gameCoord[1],
                        mat[2], mat[3],
                        1f, 1f, 1f, 1f
                ));
            }
            String url = this.getUrlFromTileCoordinate(tileX, tileY, relativeZoom);
            this.downloadTile(tileKey, url, quad);

        } catch(OutOfProjectionBoundsException ignored) {
        } catch(Exception e) {
            BTETerraRendererConstants.LOGGER.warn("Caught exception while rendering tile images", e);
        }
    }

    private void downloadTile(String tileKey, String url, GraphicsQuad<GraphicsQuad.PosTexColor> quad) {
        GraphicsModelManager cache = GraphicsModelManager.getInstance();
        cache.setTextureInDownloadingState(tileKey);
        this.downloadExecutor.execute(new TileDownloadingTask(downloadExecutor, tileKey, url, quad, 0));
    }

    public boolean isRelativeZoomAvailable(int relativeZoom) {
        return tileProjection != null && tileProjection.isRelativeZoomAvailable(relativeZoom);
    }

    public String genTileKey(String id, int tileX, int tileY, int zoom) {
        return "tilemap_" + id + "_" + tileX + "_" + tileY + "_" + zoom;
    }

    @Override
    public String toString() {
        return FlatTileMapService.class.getName() + "{name=" + name + ", tile_url=" + urlTemplate + "}";
    }

    @RequiredArgsConstructor
    private static class TileDownloadingTask implements Runnable {

        private static final Timer TIMER = new Timer();

        private final ExecutorService es;
        private final String tileKey, url;
        private final GraphicsQuad<GraphicsQuad.PosTexColor> quad;
        private final int retry;

        @Override
        public void run() {
            GraphicsModelManager cache = GraphicsModelManager.getInstance();

            if (retry >= RETRY_COUNT + 1) {
                cache.textureDownloadingComplete(tileKey, SOMETHING_WENT_WRONG, Collections.singletonList(quad));
                return;
            }

            try {
                InputStream stream = HttpConnector.INSTANCE.download(url);
                cache.textureDownloadingComplete(tileKey, ImageIO.read(stream), Collections.singletonList(quad));
                return;
            } catch (Exception e) {
                BTETerraRendererConstants.LOGGER.error("Caught exception while downloading a tile image (" +
                        "TileKey=" + tileKey + ", Retry #" + (retry + 1) + ")");
            }

            TIMER.schedule(new TimerTask() {
                @Override
                public void run() {
                    es.execute(new TileDownloadingTask(es, tileKey, url, quad, retry + 1));
                }
            }, 1000);
        }
    }

    static {
        try {
            SOMETHING_WENT_WRONG = ImageIO.read(
                    Objects.requireNonNull(FlatTileMapService.class.getClassLoader().getResourceAsStream(
                            "assets/" + BTETerraRendererConstants.MODID + "/textures/internal_error_image.png"
                    ))
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
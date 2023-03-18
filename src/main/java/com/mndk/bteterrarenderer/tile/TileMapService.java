package com.mndk.bteterrarenderer.tile;

import com.mndk.bteterrarenderer.util.NullValidator;
import net.buildtheearth.terraplusplus.dep.com.fasterxml.jackson.annotation.JsonCreator;
import net.buildtheearth.terraplusplus.dep.com.fasterxml.jackson.annotation.JsonProperty;
import com.mndk.bteterrarenderer.BTETerraRenderer;
import com.mndk.bteterrarenderer.loader.CategoryMapData;
import com.mndk.bteterrarenderer.loader.ProjectionYamlLoader;
import com.mndk.bteterrarenderer.projection.Projections;
import com.mndk.bteterrarenderer.projection.TileProjection;
import io.netty.buffer.ByteBufInputStream;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.util.http.Http;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Data
public class TileMapService implements CategoryMapData.ICategoryMapProperty {


    public static final int RETRY_COUNT = 3;
    public static final int DEFAULT_ZOOM = 18;
    static final int DEFAULT_MAX_THREAD = 2;
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
    private TileMapService(
            @JsonProperty(value = "name", required = true) String name,
            @JsonProperty(value = "tile_url", required = true) String urlTemplate,
            @JsonProperty(value = "projection", required = true) String projectionName,
            @JsonProperty(value = "max_thread", defaultValue = "2") @Nullable Integer maxThread,
            @JsonProperty(value = "default_zoom", defaultValue = "18") @Nullable Integer defaultZoom,
            @JsonProperty(value = "invert_lat", defaultValue = "false") @Nullable Boolean invertLatitude,
            @JsonProperty(value = "invert_zoom", defaultValue = "false") @Nullable Boolean invertZoom
    ) throws NullPointerException, CloneNotSupportedException {

        this.name = name;
        this.urlTemplate = urlTemplate;

        TileProjection projectionSearchResult = ProjectionYamlLoader.INSTANCE.result.get(projectionName);
        int _defaultZoom = NullValidator.get(defaultZoom, DEFAULT_ZOOM);
        boolean _invertZoom = NullValidator.get(invertZoom, false);

        if(projectionSearchResult != null) {
            this.tileProjection = projectionSearchResult.clone();
            tileProjection.setDefaultZoom(_defaultZoom);
            tileProjection.setInvertZoom(_invertZoom);
            tileProjection.setInvertLatitude(NullValidator.get(invertLatitude, false));
        } else {
            BTETerraRenderer.logger.error("Couldn't find tile projection named \"" + projectionName + "\"");
            this.tileProjection = null;
        }

        this.urlConverter = new TileURLConverter(_defaultZoom, _invertZoom);
        this.downloadExecutor = Executors.newFixedThreadPool(NullValidator.get(maxThread, DEFAULT_MAX_THREAD));
    }


    public void renderTile(
            Tessellator t, BufferBuilder builder,
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

            TileImageCache cache = TileImageCache.getInstance();
            cache.cacheAllImagesInQueue();

            // Return if the requested tile is still in the downloading state
            if(cache.isTileInDownloadingState(tileKey)) return;

            if(!cache.textureExists(tileKey)) {
                // If the requested tile is not loaded, load it in the new thread and return
                String url = this.urlConverter.convertToUrl(this.urlTemplate, tileX, tileY, relativeZoom);
                this.downloadTile(tileKey, url);
                return;
            }

            cache.bindTexture(tileKey);
            // begin vertex
            builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            /*
             *  i=0 -------- i=1
             *   |            |
             *   |    TILE    |
             *   |            |
             *   |            |
             *  i=3 -------- i=2
             */
            for (int i = 0; i < 4; i++) {
                int[] mat = this.tileProjection.getCornerMatrix(i);
                geoCoord = tileProjection.tileCoordToGeoCoord(tileX + mat[0], tileY + mat[1], relativeZoom);
                gameCoord = Projections.getServerProjection().fromGeo(geoCoord[0], geoCoord[1]);

                builder.pos(gameCoord[0] - playerX, y - playerY, gameCoord[1] - playerZ)
                        .tex(mat[2], mat[3])
                        .color(1.f, 1.f, 1.f, opacity)
                        .endVertex();
            }
            t.draw();

        } catch(OutOfProjectionBoundsException ignored) {
        } catch(Exception e) {
            BTETerraRenderer.logger.warn("Caught exception while rendering tile images", e);
        }
    }


    private void downloadTile(String tileKey, String url) {
        TileImageCache cache = TileImageCache.getInstance();
        cache.tileIsBeingDownloaded(tileKey);
        this.downloadExecutor.execute(new TileDownloadingTask(downloadExecutor, tileKey, url, 0));
    }


    public boolean isRelativeZoomAvailable(int relativeZoom) {
        return tileProjection != null && tileProjection.isRelativeZoomAvailable(relativeZoom);
    }


    public String genTileKey(String id, int tileX, int tileY, int zoom) {
        return "tilemap_" + id + "_" + tileX + "_" + tileY + "_" + zoom;
    }


    @Override
    public String toString() {
        return TileMapService.class.getName() + "{name=" + name + ", tile_url=" + urlTemplate + "}";
    }


    @RequiredArgsConstructor
    private static class TileDownloadingTask implements Runnable {

        private static final Timer TIMER = new Timer();

        private final ExecutorService es;
        private final String tileKey, url;
        private final int retry;

        @Override
        public void run() {
            TileImageCache cache = TileImageCache.getInstance();
            boolean shouldRetry = false;

            if (retry >= RETRY_COUNT+1) {
                cache.tileDownloadingComplete(tileKey, SOMETHING_WENT_WRONG);
            }
            else {
                try {
                    ByteBufInputStream stream = new ByteBufInputStream(Http.get(url).get());
                    cache.tileDownloadingComplete(tileKey, ImageIO.read(stream));
                } catch (Exception e) {
                    BTETerraRenderer.logger.error("Caught exception while downloading a tile image (" +
                            "TileKey=" + tileKey + ", Retry #" + (retry + 1) + ")");
                    shouldRetry = true;
                }
            }

            if (shouldRetry) {
                TIMER.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        es.execute(new TileDownloadingTask(es, tileKey, url, retry + 1));
                    }
                }, 1000);
            }
        }
    }


    static {
        try {
            SOMETHING_WENT_WRONG = ImageIO.read(
                    Objects.requireNonNull(TileMapService.class.getClassLoader().getResourceAsStream(
                            "assets/" + BTETerraRenderer.MODID + "/textures/internal_error_image.png"
                    ))
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

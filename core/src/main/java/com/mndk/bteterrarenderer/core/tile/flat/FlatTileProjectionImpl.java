package com.mndk.bteterrarenderer.core.tile.flat;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.GeographicProjection;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.OutOfProjectionBoundsException;
import lombok.Data;

import java.util.Map;

@JsonDeserialize
public class FlatTileProjectionImpl extends FlatTileProjection {

    private final GeographicProjection projection;
    private final Map<Integer, TileMatrix> matrices;

    @JsonCreator
    public FlatTileProjectionImpl(
            @JsonProperty(value = "projection", required = true) GeographicProjection projection,
            @JsonProperty(value = "tile_matrices", required = true) Map<Integer, TileMatrix> matrices
    ) {
        this.projection = projection;
        this.matrices = matrices;
    }


    @Override
    public int[] toTileCoord(double longitude, double latitude, int absoluteZoom) throws OutOfProjectionBoundsException {
        double[] coordinate = this.projection.fromGeo(longitude, latitude);
        TileMatrix matrix = this.matrices.get(absoluteZoom);

        int tileX = (int) Math.floor((coordinate[0] - matrix.pointOfOrigin[0]) / matrix.tileSize[0]);
        int tileY = (int) Math.floor((matrix.pointOfOrigin[1] - coordinate[1]) / matrix.tileSize[1]);

        return new int[] { tileX, tileY };
    }


    @Override
    protected double[] toGeoCoord(int tileX, int tileY, int absoluteZoom) throws OutOfProjectionBoundsException {
        TileMatrix matrix = this.matrices.get(absoluteZoom);

        double tileCoordinateX = tileX * matrix.tileSize[0] + matrix.pointOfOrigin[0];
        double tileCoordinateY = matrix.pointOfOrigin[1] - tileY * matrix.tileSize[1];

        return this.projection.toGeo(tileCoordinateX, tileCoordinateY);
    }


    @Override
    public FlatTileProjection clone() {
        return new FlatTileProjectionImpl(this.projection, this.matrices);
    }


    @Override
    public boolean isAbsoluteZoomAvailable(int absoluteZoom) {
        return this.matrices.containsKey(absoluteZoom);
    }


    @Data
    @JsonDeserialize
    public static class TileMatrix {
        final double[] pointOfOrigin, tileSize;

        @JsonCreator
        TileMatrix(
                @JsonProperty(value = "origin", required = true) double[] pointOfOrigin,
                @JsonProperty(value = "size", required = true) double[] tileSize
        ) {
            this.pointOfOrigin = pointOfOrigin;
            this.tileSize = tileSize;
        }
    }

}
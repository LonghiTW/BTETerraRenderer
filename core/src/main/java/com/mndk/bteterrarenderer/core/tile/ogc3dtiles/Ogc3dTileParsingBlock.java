package com.mndk.bteterrarenderer.core.tile.ogc3dtiles;

import com.mndk.bteterrarenderer.core.graphics.PreBakedModel;
import com.mndk.bteterrarenderer.core.projection.Projections;
import com.mndk.bteterrarenderer.core.util.processor.block.MultiThreadedBlock;
import com.mndk.bteterrarenderer.ogc3dtiles.TileData;
import com.mndk.bteterrarenderer.ogc3dtiles.b3dm.Batched3DModel;
import com.mndk.bteterrarenderer.ogc3dtiles.gltf.TileGltfModel;
import com.mndk.bteterrarenderer.ogc3dtiles.i3dm.Instanced3DModel;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4;
import com.mndk.bteterrarenderer.ogc3dtiles.tile.Tileset;
import de.javagl.jgltf.model.GltfModel;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class Ogc3dTileParsingBlock<Key> extends MultiThreadedBlock<Key, ParsedData, List<PreBakedModel>> {

    public Ogc3dTileParsingBlock(ExecutorService executorService) {
        super(executorService, -1, 100);
    }

    @Override
    protected List<PreBakedModel> processInternal(Key key, ParsedData preParsedData) {
        Matrix4 transform = preParsedData.getTransform();
        TileData tileData = preParsedData.getTileData();

        GltfModel gltfModel = getGltfModel(tileData);
        if(gltfModel == null) return null;
        return GltfModelConverter.convertModel(gltfModel, transform, Projections.getServerProjection());
    }

    @Nullable
    private static GltfModel getGltfModel(TileData tileData) {
        if(tileData instanceof TileGltfModel) {
            return ((TileGltfModel) tileData).getInstance();
        }
        else if(tileData instanceof Batched3DModel) {
            return ((Batched3DModel) tileData).getGltfModel().getInstance();
        }
        else if(tileData instanceof Instanced3DModel) {
            return ((Instanced3DModel) tileData).getGltfModel().getInstance();
        }
        else if(tileData instanceof Tileset) {
            return null;
        }
        throw new UnsupportedOperationException("Unsupported tile data format: " + tileData.getDataFormat());
    }
}
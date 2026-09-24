package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsQuad;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsTriangle;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosTex;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosTexNorm;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoord;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoordTransformer;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Util;
import org.joml.Vector2f;

import java.util.function.BiFunction;
import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.vertex.*;

//? if >=1.21.11 {
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
//? } else {
/*import net.minecraft.Util;
*///? }
import net.minecraft.resources./*? if >=1.21.11 {*/Identifier/*? } else {*//*ResourceLocation*//*? }*/;

public class BufferBuildersManagerImpl implements BufferBuildersManager {

    private static final McCoord DEFAULT_NORMAL = new McCoord(0, 1, 0);

//? if >=1.21.11 {
    /**
     * Minecraft 1.21.11 switched RenderType creation to the RenderSetup API.
     */
    private static RenderSetup generateSetup(RenderPipeline pipeline, Identifier texture, boolean sort) {
        RenderSetup.RenderSetupBuilder builder = RenderSetup.builder(pipeline)
                // Sampler name must match what the pipeline declares via withSampler(...)
                .withTexture("Sampler0", texture)
                .useLightmap()
                .useOverlay()
                // .bufferSize(1536) // Default is 1536, removed in 26.2-snapshot-5
                .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE);
        // StagedVertexBuffer only supports sorting for QUADS topology; TRIANGLES throws
        // "Cannot sort draw with TRIANGLES" if sortOnUpload() is enabled.
        if (sort) {
            builder.sortOnUpload();
        }
        return builder.createRenderSetup();
    }

    private static final BiFunction</*? if >=26.2 {*/PrimitiveTopology/*? } else {*//*VertexFormat.Mode*//*? }*/, Boolean, RenderPipeline> PIPELINE = Util.memoize(
            (drawMode, cull) -> RenderPipelines.register(RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
                    .withLocation("pipeline/entity_translucent")
//? if >=26.2 {
                    .withBindGroupLayout(BindGroupLayouts.SAMPLER1)
                    .withVertexBinding(0, DefaultVertexFormat.ENTITY)
                    .withPrimitiveTopology(drawMode)
                    .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
//? } else if >=26.1 {
                    /*.withSampler("Sampler1")
                    .withVertexFormat(DefaultVertexFormat.ENTITY, drawMode)
                    .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
*///? } else {
                    /*.withSampler("Sampler1")
                    .withVertexFormat(DefaultVertexFormat.NEW_ENTITY, drawMode)
                    .withBlend(BlendFunction.TRANSLUCENT)
*///? }
                    .withCull(cull)
                    .build()
            )
    );

    private static final BiFunction</*? if >=1.21.11 {*/Identifier/*? } else {*//*ResourceLocation*//*? }*/, Boolean, RenderType> QUADS = Util.memoize(
            (texture, cull) -> {
                RenderPipeline pipeline = PIPELINE.apply(/*? if >=26.2 {*/PrimitiveTopology.QUADS/*? } else {*//*VertexFormat.Mode.QUADS*//*? }*/, cull);
                return RenderType.create("bteterrarenderer-quads", generateSetup(pipeline, texture, true));
            }
    );

    private static final BiFunction</*? if >=1.21.11 {*/Identifier/*? } else {*//*ResourceLocation*//*? }*/, Boolean, RenderType> TRIS = Util.memoize(
            (texture, cull) -> {
                RenderPipeline pipeline = PIPELINE.apply(/*? if >=26.2 {*/PrimitiveTopology.TRIANGLES/*? } else {*//*VertexFormat.Mode.TRIANGLES*//*? }*/, cull);
                return RenderType.create("bteterrarenderer-tris", generateSetup(pipeline, texture, false));
            }
    );
//? } else if >=1.21.5 {
    /*private static RenderType.CompositeState generateParameters(/^? if >=1.21.11 {^/Identifier/^? } else {^//^ResourceLocation^//^? }^/ texture) {
        return RenderType.CompositeState.builder()
//? if >=1.21.6 {
                .setTextureState(new RenderStateShard.TextureStateShard(texture, true))
//? } else {
                /^.setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.TRUE, true))
^///? }
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .setOverlayState(RenderStateShard.OVERLAY)
                .createCompositeState(true);
    }

    private static final BiFunction<VertexFormat.Mode, Boolean, RenderPipeline> PIPELINE = Util.memoize(
            (drawMode, cull) -> RenderPipelines.register(RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
                    .withLocation("pipeline/entity_translucent")
                    .withSampler("Sampler1")
                    .withVertexFormat(DefaultVertexFormat.NEW_ENTITY, drawMode)
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .withCull(cull)
                    .build()
            )
    );

    private static final BiFunction</^? if >=1.21.11 {^/Identifier/^? } else {^//^ResourceLocation^//^? }^/, Boolean, RenderType> QUADS = Util.memoize(
            (texture, cull) -> RenderType.create(
                    "bteterrarenderer-quads", 1536, true, true,
                    PIPELINE.apply(VertexFormat.Mode.QUADS, cull), generateParameters(texture)
            )
    );

    private static final BiFunction</^? if >=1.21.11 {^/Identifier/^? } else {^//^ResourceLocation^//^? }^/, Boolean, RenderType> TRIS = Util.memoize(
            (texture, cull) -> RenderType.create(
                    "bteterrarenderer-tris", 1536, true, true,
                    PIPELINE.apply(VertexFormat.Mode.TRIANGLES, cull), generateParameters(texture)
            )
    );
*///? } else {
    /*private static RenderType.CompositeState generateParameters(ResourceLocation texture, boolean cull) {
        return RenderType.CompositeState.builder()
                .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(texture, /^? if >=1.21.2 {^/TriState.TRUE/^? } else {^//^true^//^? }^/, true))
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .setCullState(cull ? RenderStateShard.CULL : RenderStateShard.NO_CULL)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .setOverlayState(RenderStateShard.OVERLAY)
                .createCompositeState(true);
    }

    private static final BiFunction<ResourceLocation, Boolean, RenderType> QUADS = Util.memoize((texture, cull) -> RenderType.create(
            "bteterrarenderer-quads", DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS, 1536, true, true, generateParameters(texture, cull)
    ));

    private static final BiFunction<ResourceLocation, Boolean, RenderType> TRIS = Util.memoize((texture, cull) -> RenderType.create(
            "bteterrarenderer-tris", DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.TRIANGLES, 1536, true, true, generateParameters(texture, cull)
    ));
*///? }

    @Override
    public BufferBuilderWrapper<GraphicsQuad<PosTex>> begin3dQuad(NativeTextureWrapper texture, float alpha, boolean cull) {
        var id = ((NativeTextureWrapperImpl) texture).delegate;
        RenderType renderLayer = QUADS.apply(id, cull);

        // DrawMode.QUADS
        // DefaultVertexFormat.NEW_ENTITY
        return new QuadBufferBuilderWrapper<>() {
            private WorldDrawContextWrapperImpl context;
            @Override
            public void setContext(WorldDrawContextWrapper context) {
                this.context = (WorldDrawContextWrapperImpl) context;
            }
            @Override
            public void nextShape(GraphicsQuad<PosTex> shape) {
//? if >=26.2 {
                context.submitNodeCollector().submitCustomGeometry(context.poseStack(), renderLayer, (pose, buffer) -> {
                    McCoordTransformer transformer = this.getTransformer();
                    nextVertex(pose, buffer, transformer.transform(shape.v0.pos), shape.v0.tex, DEFAULT_NORMAL, alpha);
                    nextVertex(pose, buffer, transformer.transform(shape.v1.pos), shape.v1.tex, DEFAULT_NORMAL, alpha);
                    nextVertex(pose, buffer, transformer.transform(shape.v2.pos), shape.v2.tex, DEFAULT_NORMAL, alpha);
                    nextVertex(pose, buffer, transformer.transform(shape.v3.pos), shape.v3.tex, DEFAULT_NORMAL, alpha);
                });
//? } else {
                /*PoseStack.Pose pose = context.poseStack().last();
                VertexConsumer buffer = context.bufferSource().getBuffer(renderLayer);
                McCoordTransformer transformer = this.getTransformer();
                nextVertex(pose, buffer, transformer.transform(shape.v0.pos), shape.v0.tex, DEFAULT_NORMAL, alpha);
                nextVertex(pose, buffer, transformer.transform(shape.v1.pos), shape.v1.tex, DEFAULT_NORMAL, alpha);
                nextVertex(pose, buffer, transformer.transform(shape.v2.pos), shape.v2.tex, DEFAULT_NORMAL, alpha);
                nextVertex(pose, buffer, transformer.transform(shape.v3.pos), shape.v3.tex, DEFAULT_NORMAL, alpha);
*///? }
            }
        };
    }

    @Override
    public BufferBuilderWrapper<GraphicsTriangle<PosTexNorm>> begin3dTri(NativeTextureWrapper texture,
                                                                         float alpha, boolean enableNormal, boolean cull) {
        var id = ((NativeTextureWrapperImpl) texture).delegate;
        RenderType renderLayer = TRIS.apply(id, cull);

        // DrawMode.QUADS
        // DefaultVertexFormat.NEW_ENTITY
        return new TriangleBufferBuilderWrapper<>() {
            private WorldDrawContextWrapperImpl context;
            @Override
            public void setContext(WorldDrawContextWrapper context) {
                this.context = (WorldDrawContextWrapperImpl) context;
            }
            @Override
            public void nextShape(GraphicsTriangle<PosTexNorm> shape) {
//? if >=26.2 {
                context.submitNodeCollector().submitCustomGeometry(context.poseStack(), renderLayer, (pose, buffer) -> {
                    McCoordTransformer transformer = this.getTransformer();
                    PosTexNorm tv0 = shape.v0.transform(transformer);
                    nextVertex(pose, buffer, tv0.pos, tv0.tex, enableNormal ? tv0.normal : DEFAULT_NORMAL, alpha);
                    PosTexNorm tv1 = shape.v1.transform(transformer);
                    nextVertex(pose, buffer, tv1.pos, tv1.tex, enableNormal ? tv1.normal : DEFAULT_NORMAL, alpha);
                    PosTexNorm tv2 = shape.v2.transform(transformer);
                    nextVertex(pose, buffer, tv2.pos, tv2.tex, enableNormal ? tv2.normal : DEFAULT_NORMAL, alpha);
                });
//? } else {
                /*PoseStack.Pose pose = context.poseStack().last();
                VertexConsumer buffer = context.bufferSource().getBuffer(renderLayer);
                McCoordTransformer transformer = this.getTransformer();
                PosTexNorm tv0 = shape.v0.transform(transformer);
                nextVertex(pose, buffer, tv0.pos, tv0.tex, enableNormal ? tv0.normal : DEFAULT_NORMAL, alpha);
                PosTexNorm tv1 = shape.v1.transform(transformer);
                nextVertex(pose, buffer, tv1.pos, tv1.tex, enableNormal ? tv1.normal : DEFAULT_NORMAL, alpha);
                PosTexNorm tv2 = shape.v2.transform(transformer);
                nextVertex(pose, buffer, tv2.pos, tv2.tex, enableNormal ? tv2.normal : DEFAULT_NORMAL, alpha);
*///? }
            }
        };
    }

    private static void nextVertex(PoseStack.Pose pose, VertexConsumer buffer,
                                   McCoord pos, Vector2f tex, McCoord normal, float alpha) {
//? if >=1.21 {
        buffer.addVertex(pose, (float) pos.getX(), pos.getY(), (float) pos.getZ())
                .setColor(1, 1, 1, alpha)
                .setUv(tex.x, tex.y)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0x00F000F0)
                .setNormal(pose, (float) normal.getX(), normal.getY(), (float) normal.getZ());
//? } else {
        /*buffer.vertex(pose/^? if <1.20.5 {^//^.pose()^//^? }^/, (float) pos.getX(), pos.getY(), (float) pos.getZ())
                .color(1, 1, 1, alpha)
                .uv(tex.x, tex.y)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(0x00F000F0)
                .normal(pose/^? if <1.20.5 {^//^.normal()^//^? }^/, (float) normal.getX(), normal.getY(), (float) normal.getZ())
                .endVertex();
*///? }
    }
}

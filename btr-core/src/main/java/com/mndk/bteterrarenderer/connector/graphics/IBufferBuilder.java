package com.mndk.bteterrarenderer.connector.graphics;

public interface IBufferBuilder {
    void beginQuads(VertexFormatConnectorEnum vertexFormat);
    IBufferBuilder pos(double x, double y, double z);
    IBufferBuilder tex(float u, float v);
    IBufferBuilder color(float red, float green, float blue, float alpha);
    void endVertex();
}

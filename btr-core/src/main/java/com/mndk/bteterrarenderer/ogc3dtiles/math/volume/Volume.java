package com.mndk.bteterrarenderer.ogc3dtiles.math.volume;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.core.util.JsonParserUtil;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Cartesian3;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Ellipsoidal3;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4;

import java.io.IOException;

@JsonDeserialize(using = Volume.Deserializer.class)
public abstract class Volume {

    // Not sure if this will be used
    public abstract boolean containsCartesian(Cartesian3 cartesian, Matrix4 transform);

    /**
     * @param coordinate In degrees.
     * @return Whether the object contains the geo coordinate "ray"
     */
    public boolean intersectsGeoCoordinateRay(double[] coordinate, Matrix4 transform) {
        return this.intersectsRay(
                new Ellipsoidal3(Math.toRadians(coordinate[0]), Math.toRadians(coordinate[1]), -100000)
                        .toCartesianCoordinate(),
                new Ellipsoidal3(Math.toRadians(coordinate[0]), Math.toRadians(coordinate[1]), 0)
                        .toCartesianCoordinate(),
                transform
        );
    }

    public boolean intersectsRay(Cartesian3 rayStart, Cartesian3 rayEnd, Matrix4 transform) {
        throw new UnsupportedOperationException("Not implemented");
    }

    static class Deserializer extends JsonDeserializer<Volume> {

        @Override
        public Volume deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String fieldName = p.nextFieldName();
            if (fieldName == null) {
                throw JsonMappingException.from(p, "expected volume type name, found: " + p.currentToken());
            }

            Volume result;
            double[] array = JsonParserUtil.readDoubleArray(p, true);
            switch(fieldName) {
                case "region": result = Region.fromArray(array); break;
                case "box": result = Box.fromArray(array); break;
                case "sphere": result = Sphere.fromArray(array); break;
                default: throw JsonMappingException.from(p, "unknown volume type: " + fieldName);
            }

            if (p.nextToken() != JsonToken.END_OBJECT) {
                throw JsonMappingException.from(p, "expected json object end, but found: " + p.currentToken());
            }

            return result;
        }
    }
}
package com.mndk.mapdisp4bte.map.bing;

public class BingTileConverter {
    public static String tileToQuadKey(int tileX, int tileY, int levelOfDetail) {
        StringBuilder quadKey = new StringBuilder();
        for (int i = levelOfDetail; i > 0; i--) {
            char digit = '0';
            int mask = 1 << (i - 1);
            if ((tileX & mask) != 0) digit++;
            if ((tileY & mask) != 0) digit+=2;
            quadKey.append(digit);
        }
        return quadKey.toString();
    }
}
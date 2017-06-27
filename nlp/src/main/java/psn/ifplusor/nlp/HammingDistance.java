package psn.ifplusor.nlp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author James Yin
 * @version 6/26/17
 */
public class HammingDistance {

    private static final boolean COUNT_TABLE = true;

    // char to bitmap
    private static final int offset = 6;
    private static final int mask = (1 << offset) - 1;
    private static long bitmask[];  // size: 2^6=64

    // bitmap to distance
    private static final int offset2 = 16;
    private static final int mask2 = (1 << offset2) - 1;
    private static int distance[];  // size: 2^16=65536

    static {
        bitmask = new long[1 << offset];
        for (int i = 0; i < (1 << offset); i++) {
            bitmask[i] |= 1L << i;
        }

        if (COUNT_TABLE) {
            distance = new int[1 << offset2];
            for (int i = 0; i < (1 << offset2); i++) {
                int t = i;
                while (t != 0) {
                    distance[i]++;
                    t -= t & -t;
                }
            }
        }
    }

    private static void setBitmap(String a, long[] bitmap) {
        for (char c : a.toCharArray()) {
            int r = c >>> offset;
            int p = c & mask;
            bitmap[r] |= bitmask[p];
        }
    }

    private static int calDistance(long[] aBitmap, long[] bBitmap) {
        int dist = 0;
        for (int i = 0; i < (1 << (16 - offset)); i++) {
            long t = aBitmap[i] & bBitmap[i];
            if (COUNT_TABLE) {
                // 稀疏位图，使用0检测可以提升效率
                if (t != 0) {
                    for (int j = 0; j < (1 << offset) / offset2; j++) {
                        int r = ((int) (t >>> (offset2 * j)) & mask2);
                        dist += distance[r];
                    }
                }
            } else {
                // 如果位图稀疏，则即时计算将比不加0检测的查表计数更高效
                while (t != 0) {
                    dist++;
                    t -= t & -t;
                }
            }
        }
        return dist;
    }

    public static long distance(String a, String b) {
        long[] aBitmap = new long[1 << (16 - offset)];
        long[] bBitmap = new long[1 << (16 - offset)];
        setBitmap(a, aBitmap);
        setBitmap(b, bBitmap);
        return calDistance(aBitmap, bBitmap);
    }
}

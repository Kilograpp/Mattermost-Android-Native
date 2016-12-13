package com.kilogramm.mattermost.utils;

import android.graphics.Color;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by ngers on 25.10.16.
 */

public class ColorGenerator {

    public static ColorGenerator MATERIAL;
    private static final float SHADE_FACTOR = 0.9f;

    private final List<Integer> mColors;
    private final Random mRandom;

    static {
        MATERIAL = create(Arrays.asList(
                0xffe57373,
                0xfff06292,
                0xffba68c8,
                0xff9575cd,
                0xff7986cb,
                0xff64b5f6,
                0xff4fc3f7,
                0xff4dd0e1,
                0xff4db6ac,
                0xff81c784,
                0xffaed581,
                0xffff8a65,
                0xffd4e157,
                0xffffd54f,
                0xffffb74d,
                0xffa1887f,
                0xff90a4ae
        ));
    }
    public static ColorGenerator create(List<Integer> colorList) {
        return new ColorGenerator(colorList);
    }

    public int getRandomColor() {
        int color = mColors.get(mRandom.nextInt(mColors.size()));
        return Color.rgb((int)(SHADE_FACTOR * Color.red(color)),
                (int)(SHADE_FACTOR * Color.green(color)),
                (int)(SHADE_FACTOR * Color.blue(color)));
    }

    private ColorGenerator(List<Integer> colorList) {
        mColors = colorList;
        mRandom = new Random(System.currentTimeMillis());
    }
}

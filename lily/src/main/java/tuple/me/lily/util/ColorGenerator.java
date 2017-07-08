package tuple.me.lily.util;

import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@SuppressWarnings({"UnusedDeclaration"})
public class ColorGenerator {

    public static ColorGenerator MATERIAL;


    private final List<Integer> mColors;
    @NonNull
    private final Random mRandom;
    int one = -1;
    int two = -1;

    @NonNull
    public static ColorGenerator create(List<Integer> colorList) {
        return new ColorGenerator(colorList);
    }

    private ColorGenerator(List<Integer> colorList) {
        mColors = colorList;
        mRandom = new Random(System.currentTimeMillis());
    }

    private static ColorGenerator getInstance() {
        if (MATERIAL == null) {
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
        return MATERIAL;
    }

    public int getRandomColor() {
        int random = mRandom.nextInt(mColors.size());
        if (one == random || two == random) {
            random = (mColors.size() - 1) % (Math.abs((one + two) / 2) + 1);
        }
        one = two;
        two = random;
        return mColors.get(random);
    }

    public int getColor(@NonNull Object key) {
        return mColors.get(Math.abs(key.hashCode()) % mColors.size());
    }
}

package tuple.me.lily.util;

import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;

import tuple.me.lily.Contexter;
import tuple.me.lily.core.Objects;

/**
 * Created by gokul.
 */

@SuppressWarnings({"UnusedDeclaration"})
public class FontCache {
    private static final HashMap<String, Typeface> fontCache = new HashMap<>();

    @Nullable
    public static Typeface getFont(@NonNull String font) {
        if (fontCache.containsKey(font)) {
            return fontCache.get(font);
        } else {
            Typeface typeface = null;
            try {
                typeface = Typeface.createFromAsset(Contexter.getAssets(), "fonts/" + font);
            } catch (Exception ignored) {
            }
            fontCache.put(font, typeface != null ? typeface : Typeface.DEFAULT);
            return Objects.ofNullable(typeface, Typeface.DEFAULT);
        }
    }
}

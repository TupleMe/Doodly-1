package tuple.me.lily.core;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by gokul-4192 on 0019 19-Feb-17.
 */
public class StringUtil {
    private StringUtil() {
    }

    public static boolean isEmpty(String string) {
        return string == null || string.length() == 0 || string.trim().length() == 0;
    }


    public static String toString(Collection collection) {
        return collection == null ? "null" : collection.toString();
    }

    public static String toString(List list) {
        return list == null ? "null" : list.toString();
    }

    public static String toString(Map map) {
        return map == null ? "null" : map.toString();
    }

    public static boolean equalsAny(String value, String[] stringsToCompare) {
        if (value != null) {
            for (String s : stringsToCompare) {
                if (s.equals(value)) {
                    return true;
                }
            }

        }
        for (String s : stringsToCompare) {
            if (s == null) {
                return true;
            }
        }
        return false;
    }

}

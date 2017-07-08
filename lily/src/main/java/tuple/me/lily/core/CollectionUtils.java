package tuple.me.lily.core;

import android.support.annotation.NonNull;
import android.support.v7.util.SortedList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by gokul.
 */
@SuppressWarnings({"UnusedDeclaration"})
public class CollectionUtils {

    @NonNull
    public static <T> List<T> copyIterator(@NonNull Iterator<T> iterator, boolean omitNulls) {
        List<T> copy = new ArrayList<>();
        while (iterator.hasNext())
            if (omitNulls) {
                T val = iterator.next();
                if (val != null) {
                    copy.add(val);
                }
            } else {
                copy.add(iterator.next());
            }
        return copy;
    }

    @NonNull
    public static <T> List<T> copyIterator(@NonNull Iterator<T> iterator) {
        return copyIterator(iterator, false);
    }

    @NonNull
    public static <T> List<T> copyIterator(@NonNull Iterator<T> iterator, @NonNull Predicate<T> predicate) {
        List<T> copy = new ArrayList<>();
        while (iterator.hasNext()) {
            T val = iterator.next();
            if (predicate.apply(val)) {
                copy.add(val);
            }
        }
        return copy;
    }

    public static <T> List<T> filter(@NonNull List<T> list, Predicate<Integer> position) {
        ArrayList<T> result = new ArrayList<>(list.size());
        for (int itr = list.size() - 1; itr >= 0; itr--) {
            if (position.apply(itr)) {
                result.add(list.get(itr));
            }
        }
        return result;
    }


    public static <T> List<T> filter(@NonNull SortedList<T> list, Predicate<Integer> position) {
        ArrayList<T> result = new ArrayList<>(list.size());
        for (int itr = list.size() - 1; itr >= 0; itr--) {
            if (position.apply(itr)) {
                result.add(list.get(itr));
            }
        }
        return result;
    }


    public static <T> List<T> filter(@NonNull SortedList<T> list, int initialCapacity, Predicate<Integer> position) {
        ArrayList<T> result = new ArrayList<>(initialCapacity);
        for (int itr = list.size() - 1; itr >= 0; itr--) {
            if (position.apply(itr)) {
                result.add(list.get(itr));
            }
        }
        return result;
    }

    public static <T> List<T> filter(@NonNull List<T> list, DoublePredicate<Integer, T> predicate) {
        ArrayList<T> result = new ArrayList<>(list.size());
        for (int itr = list.size() - 1; itr >= 0; itr--) {
            if (predicate.apply(itr, list.get(itr))) {
                result.add(list.get(itr));
            }
        }
        return result;
    }

    public static <T> List<T> filter(@NonNull List<T> list, int initialCapacity, Predicate<Integer> predicate) {
        ArrayList<T> result = new ArrayList<>(initialCapacity);
        for (int itr = list.size() - 1; itr >= 0; itr--) {
            if (predicate.apply(itr)) {
                result.add(list.get(itr));
            }
        }
        return result;
    }


    public static boolean removeAfter(List listToRemove, int index) {
        boolean retVal = false;
        if (listToRemove != null) {
            for (int i = listToRemove.size() - 1; i > index; i--) {
                listToRemove.remove(i);
                retVal = true;
            }
        }
        return retVal;
    }

    public static <T> boolean removeAfter(List<T> listToRemove, T objectToRemove) {
        int index = listToRemove.indexOf(objectToRemove);
        return index != -1 && removeAfter(listToRemove, index);
    }

    public static <T> int lastIndexOf(List<T> collection, T objectToFind) {
        if (objectToFind == null) {
            for (int itr = collection.size() - 1; itr >= 0; itr--) {
                if (collection.get(itr) == null) {
                    return itr;
                }
            }
        } else {
            for (int itr = collection.size() - 1; itr >= 0; itr--) {
                if (objectToFind.equals(collection.get(itr))) {
                    return itr;
                }
            }
        }
        return -1;
    }

    public static <T> T addIfNotContains(Collection<T> collection, T objectToAdd) {
        if (!collection.contains(objectToAdd)) {
            collection.add(objectToAdd);
        }
        return objectToAdd;
    }

    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }

    public static <T> Collection<T> addAll(Collection<T> collection, T... vals) {
        Collections.addAll(collection, vals);
        return collection;
    }

    public static int[] toIntArray(Collection<Integer> collection) {
        Object[] boxedArray = collection.toArray();
        int len = boxedArray.length;
        int[] array = new int[len];
        for (int i = 0; i < len; i++) {
            array[i] = (Integer) boxedArray[i];
        }
        return array;
    }

    public static <T> ArrayList<T> toArrayList(SortedList<T> list) {
        ArrayList<T> result = new ArrayList<>(list.size());
        for (int itr = list.size() - 1; itr >= 0; itr--) {
            result.add(list.get(itr));
        }
        return result;
    }
}

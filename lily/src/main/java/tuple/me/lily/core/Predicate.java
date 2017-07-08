package tuple.me.lily.core;

/**
 * Created by gokul.
 */
@SuppressWarnings({"UnusedDeclaration"})
public interface Predicate<T> {
    boolean apply(T val);
}

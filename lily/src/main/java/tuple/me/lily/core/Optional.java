package tuple.me.lily.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by gokul.
 */
@SuppressWarnings({"UnusedDeclaration"})
public class Optional<T> {

    private T val;
    private T defaultVal;

    private Optional() {

    }

    public Optional(T val) {
        this.val = val;
    }

    public Optional(T val, T defaultVal) {
        Objects.checkNotNull(defaultVal);
        this.defaultVal = defaultVal;
    }

    public T get() {
        if (this.val == null && this.defaultVal == null) {
            throw new IllegalStateException("Optional with value of Null, Make sure you check with isPresent,or prefer calling Optional.ofNullable");
        }
        return this.val != null ? this.val : this.defaultVal;
    }

    @NonNull
    public Optional<T> set(T val) {
        this.val = val;
        return this;
    }

    public T setAndGet(T val) {
        this.val = val;
        return get();
    }

    @Nullable
    public static <T> Optional<T> of(@Nullable T val) {
        return new Optional<>(val);
    }

    @Nullable
    public static <T> Optional<T> ofNullable(@Nullable T val, @NonNull T defaultVal) {
        Objects.checkNotNull(defaultVal);
        return new Optional<>(val, defaultVal);
    }

    public boolean isPresent() {
        return this.val != null || defaultVal != null;
    }
}

package tuple.me.lily.util.async;

/**
 * Created by gokul-4192 on 0019 19-Feb-17.
 */
public interface onComplete<T> {
    boolean onSuccess(T result);
    void onError(Exception exception);
}

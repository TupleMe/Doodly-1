package tuple.me.lily.util.async;

/**
 * Created by gokul-4192 on 0019 19-Feb-17.
 */
public interface doInBackground<T, P> {
    T doInBackground(P[] parms) throws Exception;
}

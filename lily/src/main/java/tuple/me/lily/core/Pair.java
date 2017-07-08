package tuple.me.lily.core;

import java.io.Serializable;

public class Pair<F, S> extends android.support.v4.util.Pair<F, S> implements Serializable {
    public Pair(F first, S second) {
        super(first, second);
    }
}

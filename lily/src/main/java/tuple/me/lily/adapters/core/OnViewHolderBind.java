package tuple.me.lily.adapters.core;

import android.support.v7.widget.RecyclerView;

/**
 * Created by gokul-4192 on 0016 16-May-17.
 */
public interface OnViewHolderBind<H extends RecyclerView.ViewHolder> {
    void onBind(H holder);
}

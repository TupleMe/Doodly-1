package tuple.me.lily.views;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Created by gokul.
 */
@SuppressWarnings({"UnusedDeclaration"})
public class ToastImageView extends ImageView implements View.OnLongClickListener {
    private OnLongClickListener mOnLongClickListener;

    public ToastImageView(Context context) {
        super(context);
        setOnLongClickListener(this);
    }

    public ToastImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnLongClickListener(this);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        if (l == this) {
            super.setOnLongClickListener(l);
            return;
        }
        mOnLongClickListener = l;
    }

    @Override
    public boolean onLongClick(View v) {
        if (mOnLongClickListener != null) {
            if (!mOnLongClickListener.onLongClick(v)) {
                handleLongClick();
                return true;
            }
        } else {
            handleLongClick();
            return true;
        }
        return false;
    }

    private void handleLongClick() {
        String contentDesc = getContentDescription().toString();
        if (!TextUtils.isEmpty(contentDesc)) {
            int[] pos = new int[2];
            getLocationInWindow(pos);
            Toast t = Toast.makeText(getContext(), contentDesc, Toast.LENGTH_SHORT);
            t.setGravity(Gravity.TOP | Gravity.START, pos[0] - ((contentDesc.length() / 2) * 12), pos[1] - 128);
            t.show();
        }
    }
}
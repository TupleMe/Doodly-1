package tuple.me.lily.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import tuple.me.lily.R;
import tuple.me.lily.util.FontCache;


/**
 * Created by gokul.
 */

@SuppressWarnings({"UnusedDeclaration"})
public class CustomTextView extends android.support.v7.widget.AppCompatTextView {
    public CustomTextView(Context context) {
        super(context);
        init(null);
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);

    }

    public CustomTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CustomTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        if (attrs != null && !this.isInEditMode()) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CustomTextView);
            if (a.hasValue(R.styleable.CustomTextView_font)) {
                int resId = a.getResourceId(R.styleable.CustomTextView_font, 0);
                if (resId != 0) {
                    setTypeface(FontCache.getFont(getResources().getString(resId)));
                } else {
                    CharSequence value = a.getString(R.styleable.CustomTextView_font);
                    if (value != null)
                        setTypeface(FontCache.getFont(value.toString()));
                }
            }
            a.recycle();
        }
    }
}

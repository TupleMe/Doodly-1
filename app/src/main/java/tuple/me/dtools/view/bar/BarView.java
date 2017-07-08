package tuple.me.dtools.view.bar;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import timber.log.Timber;
import tuple.me.dtools.R;

/**
 * Created by gokul-4192 on 0005 05-Mar-17.
 */
public class BarView extends View {

    private BarData barData;
    private int emptyColor;
    private int barColor;
    private Paint mPaint;

    public BarView(Context context) {
        super(context);
    }

    public BarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public BarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BarView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeWidth(0);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        if (attrs != null && !this.isInEditMode()) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.BarView);
            emptyColor = a.getColor(R.styleable.BarView_bar_empty_color, Color.TRANSPARENT);
            barColor = a.getColor(R.styleable.BarView_bar_color, Color.TRANSPARENT);
            a.recycle();
        }
    }

    public void setBarData(BarData data) {
        this.barData = data;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float width = (float) getWidth();
        float height = (float) getHeight();
        if (barData != null) {
            float total = barData.totalCap;
            float lastX = 0;
            for (BarData.Bar bar : barData.bars) {
                if (bar.color == 0) {
                    mPaint.setColor(barColor);
                } else {
                    mPaint.setColor(bar.color);
                }
                float end = lastX + width * (bar.cap / total);
                canvas.drawRect(lastX, 0, end, height, mPaint);
                lastX = end;
            }
            mPaint.setColor(emptyColor);
            canvas.drawRect(lastX, 0, width, height, mPaint);
        }
        canvas.save();
        super.onDraw(canvas);
    }
}

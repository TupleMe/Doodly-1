package tuple.me.dtools.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import tuple.me.dtools.R;
import tuple.me.dtools.file.util.FileManagerUtil;
import tuple.me.dtools.view.bar.BarData;
import tuple.me.dtools.view.bar.BarView;
import tuple.me.lily.util.FileUtils;

public class StorageList extends LinearLayout {
    private OnStorageClickListener listener;

    public StorageList(Context context) {
        super(context);
    }

    public StorageList(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StorageList(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public StorageList(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
    }


    public void setStorages(List<FileManagerUtil.StorageItem> storages) {
        setOrientation(VERTICAL);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (final FileManagerUtil.StorageItem storage : storages) {
            View view = inflater.inflate(R.layout.storage_list_item, null, false);
            ImageView icon = (ImageView) view.findViewById(R.id.icon);
            icon.setImageResource(storage.icon);
            TextView title = (TextView) view.findViewById(R.id.title);
            title.setText(storage.title);
            TextView percent = (TextView) view.findViewById(R.id.percent);
            //noinspection AndroidLintSetTextI18n
            percent.setText(FileUtils.getReadableFileSize(storage.usedSize) + "/" + FileUtils.getReadableFileSize(storage.totalSize));
            BarView barView = (BarView) view.findViewById(R.id.percent_bar);
            barView.setBarData(new BarData(storage.totalSize).add(new BarData.Bar(storage.usedSize).setColorResource(R.color.colorAccent)));
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onClick(storage);
                    }
                }
            });
            addView(view);
        }
    }

    public void setStorageClickListener(OnStorageClickListener listener) {
        this.listener = listener;
    }

    public interface OnStorageClickListener {
        void onClick(FileManagerUtil.StorageItem item);
    }
}

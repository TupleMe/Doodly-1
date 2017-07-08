package tuple.me.dtools.file;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import tuple.me.dtools.R;
import tuple.me.dtools.file.dup.DuplicateFileHeader;
import tuple.me.dtools.util.IconHolder;
import tuple.me.lily.ThemeEngine;
import tuple.me.lily.adapters.BaseSortedRecyclerAdapter;
import tuple.me.lily.model.Item;
import tuple.me.lily.util.CommonUtil;
import tuple.me.lily.util.FileUtils;
import tuple.me.lily.util.ViewUtils;

public class BaseFilesAdapter extends BaseSortedRecyclerAdapter<BaseFilesAdapter.ViewHolder, Item> {

    private int colorAccentSelected;
    protected IconHolder ic;
    private int dp;
    public BaseFilesAdapter(Context context) {
        super(context);
        ic = new IconHolder(context, true, false);
        setMultiSelectMode(true);
        dp = ViewUtils.convertDpToPx(context,15);
        colorAccentSelected = ContextCompat.getColor(context, R.color.colorAccentSelected);
    }

    public void cleanup() {
        ic.cleanup();
    }

    public BaseFilesAdapter(Context context, SortedList<Item> dataSet) {
        super(context, dataSet);
        setMultiSelectMode(true);
        colorAccentSelected = ContextCompat.getColor(context, R.color.colorAccentSelected);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(viewType, parent, false);
        if (viewType == R.layout.drawer_title_row) {
            return new HeaderHolder(itemView);
        }
        return new ChildHolder(itemView);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (holder instanceof ChildHolder) {
            super.onBindViewHolder(holder, position, true);
            ((ChildHolder) holder).bind((SystemFile) getItem(position), position);
            return;
        }
        super.onBindViewHolder(holder, position, false);
        ((HeaderHolder) holder).bind((DuplicateFileHeader) getItem(position));
    }

    @Override
    public int getItemViewType(int position) {
        Item item = getItem(position);
        if (item instanceof SystemFile) {
            return R.layout.file_row;
        }
        return R.layout.drawer_title_row;
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof ChildHolder) {
            ic.cancelLoad(((ChildHolder) holder).icon);
        }
    }

    public abstract class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class HeaderHolder extends ViewHolder {
        public TextView title;

        public HeaderHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.firstline);
        }

        public void bind(DuplicateFileHeader item) {
            title.setText(item.header);
            title.setTextColor(ThemeEngine.primary);
        }
    }

    public class ChildHolder extends ViewHolder {
        public TextView title;
        public TextView size;
        public View bg;
        public ImageView icon;

        public ChildHolder(View itemView) {
            super(itemView);
            bg = itemView.findViewById(R.id.item_bg);
            title = (TextView) itemView.findViewById(R.id.name);
            size = (TextView) itemView.findViewById(R.id.size);
            icon = (ImageView) itemView.findViewById(R.id.icon);
        }

        public void bind(SystemFile item, int position) {
            title.setText(item.name);
            icon.setImageResource(item.iconId);
            icon.setPadding(dp,dp,dp,dp);
            if (item.isDir) {
                //noinspection AndroidLintSetTextI18n
                size.setText(CommonUtil.getDateString(item.lastModified));
            } else {
                //noinspection AndroidLintSetTextI18n
                size.setText(CommonUtil.getDateString(item.lastModified) + " | " + FileUtils.getReadableFileSize(item.size));
                ic.cancelLoad(icon);
                ic.loadDrawable(icon, item.file.getPath());
            }
            if (isChecked(position)) {
                bg.setBackgroundColor(colorAccentSelected);
            } else {
                bg.setBackgroundColor(Color.TRANSPARENT);
            }
        }
    }


}

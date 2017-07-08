package tuple.me.dtools.file.dup;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import tuple.me.dtools.R;
import tuple.me.dtools.file.SystemFile;
import tuple.me.dtools.util.IconHolder;
import tuple.me.lily.ThemeEngine;
import tuple.me.lily.adapters.BaseRecyclerAdapter;
import tuple.me.lily.model.Item;
import tuple.me.lily.util.CommonUtil;
import tuple.me.lily.util.FileUtils;

public class DuplicateFileAdapter extends BaseRecyclerAdapter<DuplicateFileAdapter.ViewHolder, Item> {

    private int colorAccentSelected;
    protected IconHolder ic;

    public DuplicateFileAdapter(Context context, ArrayList<Item> dataSet) {
        super(context, dataSet);
        ic = new IconHolder(context, true, false);
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
            return R.layout.duplicate_file_row;
        }
        return R.layout.drawer_title_row;
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
        public TextView path;
        public View bg;
        public ImageView icon;

        public ChildHolder(View itemView) {
            super(itemView);
            bg = itemView.findViewById(R.id.item_bg);
            title = (TextView) itemView.findViewById(R.id.name);
            size = (TextView) itemView.findViewById(R.id.size);
            path = (TextView) itemView.findViewById(R.id.path);
            icon = (ImageView) itemView.findViewById(R.id.icon);
        }

        public void bind(SystemFile item, int position) {
            title.setText(item.name);
            path.setText(item.file.getPath());
            //noinspection AndroidLintSetTextI18n
            size.setText(FileUtils.getReadableFileSize(item.size) + " | " + CommonUtil.getDateString(item.lastModified));
            if (isChecked(position)) {
                bg.setBackgroundColor(colorAccentSelected);
            } else {
                bg.setBackgroundColor(Color.TRANSPARENT);
            }
            icon.setImageResource(item.iconId);
            ic.cancelLoad(icon);
            ic.loadDrawable(icon, item.file.getPath());
        }
    }
}

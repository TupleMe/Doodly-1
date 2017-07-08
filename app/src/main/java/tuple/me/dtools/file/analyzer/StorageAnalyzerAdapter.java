package tuple.me.dtools.file.analyzer;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import tuple.me.dtools.R;
import tuple.me.dtools.file.SystemFile;
import tuple.me.dtools.view.bar.BarData;
import tuple.me.dtools.view.bar.BarView;
import tuple.me.lily.Contexter;
import tuple.me.lily.adapters.BaseSortedRecyclerAdapter;
import tuple.me.lily.util.FileUtils;
import tuple.me.lily.views.fastscroll.SectionTitleProvider;

@SuppressWarnings("UnusedDeclaration")
public class StorageAnalyzerAdapter extends BaseSortedRecyclerAdapter<StorageAnalyzerAdapter.ViewHolder, SystemFile> implements SectionTitleProvider {
    private long totalSize;
    private int barColor;
    private int colorSelected;

    public StorageAnalyzerAdapter(Context context){
        super(context);
        barColor = Contexter.getColor(R.color.colorAccent);
        colorSelected = Contexter.getColor(R.color.colorAccentSelected);
    }

    public StorageAnalyzerAdapter(Context context, SortedList<SystemFile> dataSet) {
        super(context, dataSet);
        barColor = Contexter.getColor(R.color.colorAccent);
        colorSelected = Contexter.getColor(R.color.colorAccentSelected);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(viewType, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        SystemFile item = getItem(position);
        holder.title.setText(item.name);
        if (item.isDir) {
            holder.icon.setImageResource(R.drawable.ic_doc_folder);
            //noinspection AndroidLintSetTextI18n
            holder.subTitle.setText(FileUtils.getReadableFileSize(item.size) + " | " + item.childItemsCount + " items");
        } else {
            holder.icon.setImageResource(R.drawable.ic_doc_file);
            holder.subTitle.setText(FileUtils.getReadableFileSize(item.size));
        }
        holder.barView.setBarData(new BarData(totalSize).add(new BarData.Bar(item.size).setColor(barColor)));
        holder.barView.setAlpha(0.8F);
        if (isChecked(position)) {
            holder.itemView.setBackgroundColor(colorSelected);
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.sa_file_row;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public void resetDataSet(SortedList<SystemFile> dataSet, long totalSize) {
        this.totalSize = totalSize;
        super.resetDataSet(dataSet);
    }

    @Override
    public String getSectionTitle(int position) {
        return FileUtils.getReadableFileSize(getItem(position).size);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title;
        TextView subTitle;
        BarView barView;

        public ViewHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            title = (TextView) itemView.findViewById(R.id.title);
            subTitle = (TextView) itemView.findViewById(R.id.sub_title);
            barView = (BarView) itemView.findViewById(R.id.bar);
        }
    }
}

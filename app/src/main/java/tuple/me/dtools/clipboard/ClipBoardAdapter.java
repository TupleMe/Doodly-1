package tuple.me.dtools.clipboard;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.arasthel.asyncjob.AsyncJob;

import java.util.ArrayList;

import tuple.me.dtools.R;
import tuple.me.dtools.sugarmodel.clipboard.ClipboardItem;
import tuple.me.lily.Contexter;
import tuple.me.lily.adapters.BaseRecyclerAdapter;
import tuple.me.lily.util.CommonUtil;
import tuple.me.lily.views.CustomTextView;
import tuple.me.lily.views.toasty.Toasty;

public class ClipBoardAdapter extends BaseRecyclerAdapter<ClipBoardAdapter.ItemViewHolder, ClipboardItem> {

    private Context context;
    int colorSelected;

    public ClipBoardAdapter(Context context, ArrayList<ClipboardItem> dataSet) {
        super(context);
        this.context = context;
        colorSelected = Contexter.getColor(R.color.colorAccentSelected);
        setMultiSelectMode(true);
        setDataSet(dataSet);
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.clipboard_list_item, parent, false);
        return new ItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        final ClipboardItem item = dataSet.get(position);
        holder.text.setText(item.text);
        holder.date.setText(CommonUtil.getDateString(item.time));
        if (isChecked(position)) {
            holder.bg.setBackgroundColor(colorSelected);
        } else {
            holder.bg.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        CustomTextView text;
        CustomTextView date;
        RelativeLayout bg;

        public ItemViewHolder(View v) {
            super(v);
            text = (CustomTextView) v.findViewById(R.id.text);
            date = (CustomTextView) v.findViewById(R.id.date);
            bg = (RelativeLayout) v.findViewById(R.id.bg);
        }
    }
}

package tuple.me.dtools.apk.backuprestore;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.greenfrvr.hashtagview.HashtagView;

import tuple.me.dtools.R;
import tuple.me.dtools.apk.ApkModel;
import tuple.me.lily.adapters.BaseSortedRecyclerAdapter;
import tuple.me.lily.core.CollectionUtils;
import tuple.me.lily.views.fastscroll.SectionTitleProvider;

public class ApkAdapter extends BaseSortedRecyclerAdapter<ApkAdapter.ViewHolder, ApkModel> implements SectionTitleProvider {
    int colorAccentSelected;
    PackageManager pm;

    public ApkAdapter(Context context) {
        super(context);
        colorAccentSelected = ContextCompat.getColor(context, R.color.colorAccentSelected);
        pm = context.getPackageManager();
    }

    @SuppressWarnings("UnusedDeclaration")
    public ApkAdapter(Context context, SortedList<ApkModel> dataSet) {
        super(context, dataSet);
        colorAccentSelected = ContextCompat.getColor(context, R.color.colorAccentSelected);
        pm = context.getPackageManager();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.apk_list_item, parent, false);
        return new ViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        ApkModel model = getItem(position);
        holder.setData(model, position);
    }

    @Override
    public String getSectionTitle(int position) {
        return getItem(position).getSectionChar() + "";
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout bg;
        public ImageView icon;
        public TextView title;
        public TextView subTitle;
        public ImageView more;
        public HashtagView hashtagView;

        public ViewHolder(View itemView) {
            super(itemView);
            bg = (LinearLayout) itemView.findViewById(R.id.item_bg);
            icon = (ImageView) itemView.findViewById(R.id.item_img);
            title = (TextView) itemView.findViewById(R.id.item_title);
            subTitle = (TextView) itemView.findViewById(R.id.item_subtitle);
            more = (ImageView) itemView.findViewById(R.id.item_menu);
            hashtagView = (HashtagView) itemView.findViewById(R.id.hash_tag);
        }

        public void setData(ApkModel data, int position) {
            if (isChecked(position)) {
                bg.setBackgroundColor(colorAccentSelected);
            } else {
                bg.setBackgroundColor(Color.TRANSPARENT);
            }
            title.setText(data.getAppName());
            subTitle.setText(data.getSize());
            icon.setImageDrawable(data.getApplicationInfo().loadIcon(pm));
            if (!CollectionUtils.isEmpty(data.permissionsString)) {
                hashtagView.setData(data.permissionsString, new HashtagView.DataTransform<Integer>() {
                    @Override
                    public CharSequence prepare(Integer item) {
                        return new SpannableString(context.getString(item));
                    }
                });
                hashtagView.setVisibility(View.VISIBLE);
            } else {
                hashtagView.setVisibility(View.GONE);
            }
        }
    }
}

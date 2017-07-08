package tuple.me.lily.views.bs;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import tuple.me.lily.R;
import tuple.me.lily.util.ViewUtils;
import tuple.me.lily.model.IconListItem;


/**
 * Created by gokul-4192 on 0007 07-Oct-16.
 */
public class IconListAdapter extends ArrayAdapter<IconListItem> {

    private final Context context;
    private ArrayList<IconListItem> dataset;

    public IconListAdapter(Context context, @NonNull ArrayList<IconListItem> dataset) {
        super(context, R.layout.bottom_icon_list_item, dataset);
        this.context = context;
        this.dataset = dataset;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View rowView = inflater.inflate(R.layout.bottom_icon_list_item, null, true);
        TextView itemName = (TextView) rowView.findViewById(R.id.bottom_item_name);
        ImageView itemImage = (ImageView) rowView.findViewById(R.id.bottom_item_icon);
        itemName.setText(dataset.get(position).title);
        ViewUtils.setColorFilter(itemImage,dataset.get(position).drawable);
        return rowView;
    }

}

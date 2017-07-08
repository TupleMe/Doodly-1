package tuple.me.lily.views.bs;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import tuple.me.lily.R;
import tuple.me.lily.ThemeEngine;
import tuple.me.lily.core.Pair;

/**
 * Created by gokul-4192 on 0016 16-Apr-17.
 */
public class PropertiesListAdapter extends ArrayAdapter<Pair<String, String>> {

    private final Context context;
    private ArrayList<Pair<String, String>> dataset;

    public PropertiesListAdapter(Context context, @NonNull ArrayList<Pair<String, String>> dataset) {
        super(context, R.layout.bottom_icon_list_item, dataset);
        this.context = context;
        this.dataset = dataset;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View rowView = inflater.inflate(R.layout.bottom_prop_list_item, null, true);
        TextView title = (TextView) rowView.findViewById(R.id.bottom_item_title);
        TextView subTitle = (TextView) rowView.findViewById(R.id.bottom_item_subtitle);
        title.setText(dataset.get(position).first);
        subTitle.setText(dataset.get(position).second);
        title.setTextColor(ThemeEngine.primary);
        return rowView;
    }

}


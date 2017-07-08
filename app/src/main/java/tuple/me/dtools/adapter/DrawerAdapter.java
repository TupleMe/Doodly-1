package tuple.me.dtools.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

import timber.log.Timber;
import tuple.me.dtools.R;
import tuple.me.lily.Contexter;
import tuple.me.lily.ThemeEngine;
import tuple.me.lily.model.IconListItem;
import tuple.me.lily.model.Item;
import tuple.me.lily.model.ListItem;
import tuple.me.lily.views.CustomTextView;

public class DrawerAdapter extends ArrayAdapter<Item> {
    @NonNull
    private final Context context;
    @NonNull
    private final ArrayList<Item> values;
    private int selectedItem = 0;
    LayoutInflater inflater;

    public DrawerAdapter(@NonNull Context context, @NonNull ArrayList<Item> values) {
        super(context, R.layout.drawer_row, values);
        this.context = context;
        this.values = values;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (values.get(position) instanceof IconListItem) {
            View view = inflater.inflate(R.layout.drawer_row, parent, false);
            final CustomTextView txtTitle = (CustomTextView) view.findViewById(R.id.firstline);
            final ImageView imageView = (ImageView) view.findViewById(R.id.icon);
            view.setBackgroundResource(R.drawable.safr_ripple_black);
            txtTitle.setText(((ListItem) (values.get(position))).title);
            imageView.setImageDrawable(getDrawable(position));
            if (position == selectedItem) {
                view.setBackgroundColor(Contexter.getColor(R.color.colorAccentSelected));
            } else {
                view.setBackgroundColor(Contexter.getColor(android.R.color.transparent));
            }
            imageView.clearColorFilter();
            imageView.setColorFilter(Color.LTGRAY);
            return view;
        } else if (values.get(position) instanceof ListItem) {
            View view = inflater.inflate(R.layout.drawer_title_row, parent, false);
            final CustomTextView txtTitle = (CustomTextView) view.findViewById(R.id.firstline);
            txtTitle.setText(((ListItem) (values.get(position))).title);
            txtTitle.setTextColor(ThemeEngine.primary);
            view.setClickable(false);
            return view;
        } else {
            ImageView view = new ImageView(context);
            view.setImageResource(R.color.divider);
            view.setClickable(false);
            view.setFocusable(false);
            view.setBackgroundColor(Color.WHITE);
            //noinspection AndroidLintPrivateResource
            view.setBackgroundResource(R.color.background_material_dark);
            view.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Contexter.dpToPixels(17)));
            view.setPadding(0, Contexter.dpToPixels(8), 0, Contexter.dpToPixels(8));
            return view;
        }
    }

    public void setSelectedItem(int res) {
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i) instanceof IconListItem) {
                if (((ListItem) values.get(i)).title == res) {
                    selectedItem = i;
                    notifyDataSetChanged();
                    return;
                }
            }
        }
    }

    Drawable getDrawable(int position) {
        return ((IconListItem) getItem(position)).getDrawable();
    }
}
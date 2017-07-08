package tuple.me.lily.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.List;

import tuple.me.lily.R;

/**
 * Created by gokul-4192 on 0031 31-Dec-16.
 */
public class ColorAdapter extends ArrayAdapter<String> {

    public int pos;
    public ColorAdapter.onClickListener listener;
    public ColorAdapter(Context context, List<String> arrayList , int pos) {
        super(context, R.layout.dialog_grid_item, arrayList);
        this.pos= pos;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.dialog_grid_item, parent, false);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        if (position == pos) imageView.setImageResource((R.drawable.ic_check_circle));
        imageView.clearColorFilter();
        imageView.setColorFilter(Color.WHITE);
        GradientDrawable gradientDrawable = (GradientDrawable) imageView.getBackground();
        gradientDrawable.setColor(Color.parseColor(getItem(position)));
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pos = position;
                notifyDataSetChanged();
                if(listener!=null){
                    listener.onClick(position);
                }
            }
        });
        return rowView;
    }

    public interface  onClickListener{
        void onClick(int position);
    }
}

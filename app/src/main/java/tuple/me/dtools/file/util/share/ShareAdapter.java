package tuple.me.dtools.file.util.share;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

import tuple.me.lily.R;
import tuple.me.lily.adapters.BaseRecyclerAdapter;


public class ShareAdapter extends BaseRecyclerAdapter< ShareAdapter.ViewHolder,Intent> {
    private MaterialDialog dialog;
    private ArrayList<String> labels;
    private ArrayList<Drawable> drawables;
    private Context context;

    public void updateMatDialog(MaterialDialog b) {
        this.dialog = b;
    }

    public ShareAdapter(Context context,
                        ArrayList<Intent> intents,
                        ArrayList<String> labels,
                        ArrayList<Drawable> arrayList1) {
        super(context,intents);
        this.context = context;
        this.labels = labels;
        this.drawables = arrayList1;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.simplerow, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.render(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private View rootView;

        private TextView a;
        private ImageView v;

        ViewHolder(View view) {
            super(view);

            rootView = view;

            a = ((TextView) view.findViewById(R.id.firstline));
            v = (ImageView) view.findViewById(R.id.icon);
        }

        void render(final int position) {
            if (drawables.get(position) != null)
                v.setImageDrawable(drawables.get(position));
            a.setVisibility(View.VISIBLE);
            a.setText(labels.get(position));
            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (dialog != null && dialog.isShowing()) dialog.dismiss();
                    context.startActivity(getItem(position));
                }
            });
        }
    }

}

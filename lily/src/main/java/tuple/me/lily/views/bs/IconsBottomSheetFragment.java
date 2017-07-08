package tuple.me.lily.views.bs;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import tuple.me.lily.Contexter;
import tuple.me.lily.R;
import tuple.me.lily.ThemeEngine;
import tuple.me.lily.model.IconListItem;
import tuple.me.lily.views.CustomTextView;

public class IconsBottomSheetFragment extends BottomSheetDialogFragment {

    private OnItemClickListener listener;
    private String titleText;

    @NonNull
    public static IconsBottomSheetFragment newInstance(@StringRes int[] titles, @DrawableRes int[] drawables) {
        IconsBottomSheetFragment fragment = new IconsBottomSheetFragment();
        Bundle args = new Bundle();
        args.putIntArray("titles", titles);
        args.putIntArray("drawables", drawables);
        fragment.setArguments(args);
        return fragment;
    }

    public IconsBottomSheetFragment setTitle(String title) {
        titleText = title;
        return this;
    }

    public IconsBottomSheetFragment setTitle(@StringRes int title) {
        titleText = getString(title);
        return this;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet_dialog, container, false);
        ListView iconList = (ListView) v.findViewById(R.id.bottom_list);
        CustomTextView title = (CustomTextView) v.findViewById(R.id.bottom_sheet_title);
        if (titleText == null) {
            title.setVisibility(View.GONE);
        } else {
            title.setText(titleText);
            title.setVisibility(View.VISIBLE);
        }
        ArrayList<IconListItem> list = new ArrayList<>();
        int[] titles = getArguments().getIntArray("titles");
        int[] drawables = getArguments().getIntArray("drawables");
        for (int itr = 0; itr < titles.length; itr++) {
            list.add(new IconListItem(titles[itr], drawables[itr]));
        }
        IconListAdapter adapter = new IconListAdapter(getContext(), list);
        iconList.setAdapter(adapter);
        iconList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (listener != null) {
                    listener.onItemClick(position);
                    dismiss();
                }
            }
        });
        if (ThemeEngine.isDark()) {
            v.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.holo_dark_action_mode));
            title.setTextColor(Color.WHITE);
        }
        return v;
    }

    public IconsBottomSheetFragment setItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
        return this;
    }

    public interface OnItemClickListener {
        void onItemClick(int id);
    }
}

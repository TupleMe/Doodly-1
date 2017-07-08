package tuple.me.lily.views.bs;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

import tuple.me.lily.Contexter;
import tuple.me.lily.R;
import tuple.me.lily.ThemeEngine;
import tuple.me.lily.core.Pair;
import tuple.me.lily.views.CustomTextView;

public class PropertiesBottomSheet extends BottomSheetDialogFragment {

    private String titleText;

    @NonNull
    public static PropertiesBottomSheet newInstance(ArrayList<Pair<String, String>> properties) {
        PropertiesBottomSheet fragment = new PropertiesBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable("PROPS", properties);
        fragment.setArguments(args);
        return fragment;
    }

    public PropertiesBottomSheet setTitle(String title) {
        titleText = title;
        return this;
    }

    public PropertiesBottomSheet setTitle(@StringRes int title) {
        titleText = Contexter.getString(title);
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
        ArrayList<Pair<String, String>> props = (ArrayList<Pair<String, String>>) getArguments().getSerializable("PROPS");
        if (props != null) {
            PropertiesListAdapter adapter = new PropertiesListAdapter(getContext(), props);
            iconList.setAdapter(adapter);
        }

        if (ThemeEngine.isDark()) {
            v.setBackgroundColor(ThemeEngine.primaryBackGround);
            title.setTextColor(Color.WHITE);
        }
        return v;
    }

}

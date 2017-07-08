/*
 * ******************************************************************************
 *   Copyright (c) 2013-2015 Gabriele Mariotti.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  *****************************************************************************
 */

package tuple.me.lily.views.changelog.internal;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import tuple.me.lily.R;
import tuple.me.lily.ThemeEngine;
import tuple.me.lily.views.changelog.Constants;

public class ChangeLogRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ROW = 0;
    private static final int TYPE_HEADER = 1;

    private final Context mContext;
    private int mStringVersionHeader = Constants.mStringVersionHeader;

    @Nullable
    private List<ChangeLogRow> items;

    public ChangeLogRecyclerViewAdapter(Context mContext, @Nullable List<ChangeLogRow> items) {
        this.mContext = mContext;
        if (items == null)
            items = new ArrayList<>();
        this.items = items;
    }

    public void add(@NonNull LinkedList<ChangeLogRow> rows) {
        int originalPosition = items.size();
        items.addAll(rows);
        notifyItemRangeInserted(originalPosition, originalPosition + rows.size());
    }



    public static class ViewHolderHeader extends RecyclerView.ViewHolder {
        public TextView versionHeader;
        public TextView dateHeader;

        public ViewHolderHeader(@NonNull View itemView) {
            super(itemView);
            //VersionName text
            versionHeader = (TextView) itemView.findViewById(R.id.chg_headerVersion);
            //ChangeData text
            dateHeader = (TextView) itemView.findViewById(R.id.chg_headerDate);
        }
    }

    public static class ViewHolderRow extends RecyclerView.ViewHolder {
        public TextView textRow;
        public TextView bulletRow;

        public ViewHolderRow(@NonNull View itemView) {
            super(itemView);
            textRow = (TextView) itemView.findViewById(R.id.chg_text);
            bulletRow = (TextView) itemView.findViewById(R.id.chg_textbullet);
        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            final View viewHeader = LayoutInflater.from(parent.getContext()).inflate(R.layout.changelog_row_header, parent, false);
            return new ViewHolderHeader(viewHeader);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.changelog_row, parent, false);
            return new ViewHolderRow(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        if (isHeader(position)) {
            populateViewHolderHeader((ViewHolderHeader) viewHolder, position);
        } else {
            populateViewHolderRow((ViewHolderRow) viewHolder, position);
        }
    }

    private void populateViewHolderRow(@NonNull ViewHolderRow viewHolder, int position) {
        ChangeLogRow item = getItem(position);
        if (item != null) {
            if (viewHolder.textRow != null) {
                viewHolder.textRow.setText(Html.fromHtml(item.getChangeText(mContext)));
                viewHolder.textRow.setMovementMethod(LinkMovementMethod.getInstance());
            }
            if (viewHolder.bulletRow != null) {
                if (item.isBulletedList()) {
                    viewHolder.bulletRow.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.bulletRow.setVisibility(View.GONE);
                }
            }
        }
    }

    private void populateViewHolderHeader(@NonNull ViewHolderHeader viewHolder, int position) {
        ChangeLogRow item = getItem(position);
        if (item != null) {
            if (viewHolder.versionHeader != null) {
                StringBuilder sb = new StringBuilder();
                if (item.versionName != null) {
                    //String resource for Version
                    String versionHeaderString = mContext.getString(mStringVersionHeader);
                    sb.append(versionHeaderString);
                    //VersionName text
                    sb.append(item.versionName);

                    viewHolder.versionHeader.setText(sb.toString());
                    viewHolder.versionHeader.setTextColor(ThemeEngine.primary);

                } else {
                    viewHolder.versionHeader.setVisibility(View.GONE);
                }
            }

            //ChangeData text
            if (viewHolder.dateHeader != null) {
                //Check if exists

                if (item.changeDate != null) {
                    viewHolder.dateHeader.setText(item.changeDate);
                    viewHolder.dateHeader.setVisibility(View.VISIBLE);
                    viewHolder.dateHeader.setTextColor(ThemeEngine.primary);
                } else {
                    //If item hasn't changedata, hide TextView
                    viewHolder.dateHeader.setText("");
                    viewHolder.dateHeader.setVisibility(View.GONE);
                }
            }
        }
    }


    private boolean isHeader(int position) {
        return getItem(position).isHeader();
    }

    private ChangeLogRow getItem(int position) {
        return items.get(position);
    }


    @Override
    public int getItemViewType(int position) {
        if (isHeader(position))
            return TYPE_HEADER;
        return TYPE_ROW;
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

}

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

package tuple.me.lily.views.changelog.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import tuple.me.lily.R;
import tuple.me.lily.views.changelog.Constants;
import tuple.me.lily.views.changelog.internal.ChangeLog;
import tuple.me.lily.views.changelog.internal.ChangeLogRecyclerViewAdapter;
import tuple.me.lily.views.changelog.parser.XmlParser;

/**
 * RecyclerView for ChangeLog
 *
 * @author Gabriele Mariotti (gabri.mariotti@gmail.com)
 */
public class ChangeLogRecyclerView extends RecyclerView {

    protected int mRowLayoutId = Constants.mRowLayoutId;
    protected int mRowHeaderLayoutId;
    public int mChangeLogFileResourceId;
    @Nullable
    protected String mChangeLogFileResourceUrl = null;

    @NonNull
    protected static String TAG = "ChangeLogRecyclerView";

    protected ChangeLogRecyclerViewAdapter mAdapter;

    public ChangeLogRecyclerView(Context context) {
        this(context, null);
    }

    public ChangeLogRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChangeLogRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void init(AttributeSet attrs, int defStyle) {
        initAttrs(attrs, defStyle);
        initLayoutManager();
    }

    public void setChangeLogXml(@RawRes int xmlid) {
        this.mChangeLogFileResourceId = xmlid;
        initAdapter();
    }

    protected void initLayoutManager() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        this.setLayoutManager(layoutManager);
    }

    protected void initAttrs(AttributeSet attrs, int defStyle) {
        TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs, R.styleable.ChangeLogListView, defStyle, defStyle);

        try {
            mRowLayoutId = a.getResourceId(R.styleable.ChangeLogListView_rowLayoutId, mRowLayoutId);
            mRowHeaderLayoutId = a.getResourceId(R.styleable.ChangeLogListView_rowHeaderLayoutId, mRowHeaderLayoutId);
            mChangeLogFileResourceId = a.getResourceId(R.styleable.ChangeLogListView_changeLogFileResourceId, mChangeLogFileResourceId);
            mChangeLogFileResourceUrl = a.getString(R.styleable.ChangeLogListView_changeLogFileResourceUrl);
        } finally {
            a.recycle();
        }
    }


    protected void initAdapter() {

        try {
            XmlParser parse = new XmlParser(getContext(), mChangeLogFileResourceId);
            ChangeLog chg = new ChangeLog();
            mAdapter = new ChangeLogRecyclerViewAdapter(getContext(), chg.getRows());
            new ParseAsyncTask(mAdapter, parse).execute();
            setAdapter(mAdapter);

        } catch (Exception ignored) {
        }

    }

    protected class ParseAsyncTask extends AsyncTask<Void, Void, ChangeLog> {

        private ChangeLogRecyclerViewAdapter mAdapter;
        private XmlParser mParse;

        public ParseAsyncTask(ChangeLogRecyclerViewAdapter adapter, XmlParser parse) {
            mAdapter = adapter;
            mParse = parse;
        }

        @Nullable
        @Override
        protected ChangeLog doInBackground(Void... params) {

            try {
                if (mParse != null) {
                    return mParse.readChangeLogFile();
                }
            } catch (Exception ignored) {
            }
            return null;
        }

        protected void onPostExecute(@Nullable ChangeLog chg) {
            if (chg != null) {
                mAdapter.add(chg.getRows());
            }
        }
    }
}

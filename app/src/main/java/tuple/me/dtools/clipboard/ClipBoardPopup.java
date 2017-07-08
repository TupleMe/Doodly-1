package tuple.me.dtools.clipboard;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.arasthel.asyncjob.AsyncJob;

import java.util.ArrayList;

import tuple.me.dtools.R;
import tuple.me.dtools.constants.Constants;
import tuple.me.dtools.sugarmodel.clipboard.ClipboardItem;
import tuple.me.lily.adapters.core.OnItemClickListener;
import tuple.me.lily.util.CommonUtil;
import tuple.me.lily.util.ViewUtils;
import tuple.me.lily.views.AbsTextWatcher;
import tuple.me.lily.views.toasty.Toasty;

/**
 * Created by gokul-4192 on 0012 12-May-17.
 */

public class ClipBoardPopup extends Activity {
    private RecyclerView list;
    private ClipBoardAdapter adapter;
    private View empty;
    private EditText search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clipboard_popup);
        LinearLayout cardView = (LinearLayout) findViewById(R.id.card_view);
        cardView.setLayoutParams(new FrameLayout.LayoutParams((int) (ViewUtils.getWindowWidth(this) * 0.8), (int) (ViewUtils.getWindowHeight(this) * 0.8)));
        search = (EditText) findViewById(R.id.edit_search);
        search.addTextChangedListener(new AbsTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setUpAdapter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        list = (RecyclerView) findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(this));
        empty = findViewById(R.id.empty);
    }

    public void setUpAdapter(final String searchText) {
        AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
            @Override
            public void doOnBackground() {
                final ArrayList<ClipboardItem> items = ClipboardItem.getAll(searchText);
                AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                    @Override
                    public void doInUIThread() {
                        if (adapter == null) {
                            adapter = new ClipBoardAdapter(ClipBoardPopup.this, items);
                            adapter.setMultiSelectMode(false);
                            adapter.setOnClickListener(new OnItemClickListener<ClipboardItem>() {
                                @Override
                                public void onItemClick(ClipboardItem object) {
                                    CommonUtil.copyToClipBoard(Constants.APP_NAME, object.text);
                                    Toasty.success(ClipBoardPopup.this, R.string.copy_success);
                                    finish();
                                }
                            });
                            list.setAdapter(adapter);
                        }
                        adapter.setDataSet(items);
                        empty.setVisibility(adapter.isEmpty() ? View.VISIBLE : View.INVISIBLE);
                        adapter.setDisabled(false);
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpAdapter(search.getText().toString());
    }
}

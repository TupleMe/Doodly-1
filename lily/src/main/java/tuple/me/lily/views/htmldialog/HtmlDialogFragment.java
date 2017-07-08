package tuple.me.lily.views.htmldialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import tuple.me.lily.R;


/**
 * Created by gokul-pt749 on 06/11/2016.
 */
public class HtmlDialogFragment extends DialogFragment {
    private static final String TITLE = "title";
    public static final String HTML_RES_ID = "HtmlResID";
    private static final String TAG = "Html Dialog Fragment";
    private static final String POS_TEXT = "Positive text";
    private static final String NEG_TEXT = "Negative Text";
    public static final String TAG_HTML_DIALOG_FRAGMENT = "tagHtmlDialogFragment";


    private WebView mWebView;
    private ProgressBar mProgressBar;
    static HtmlDialog.onPositivePressed onPositivePressed;
    static HtmlDialog.onNegativePressed onNegativePressed;
    static HtmlDialog.onCancelled onCancelled;
    @Nullable
    String mPosText;
    @Nullable
    String mNegText;
    @Nullable
    private AsyncTask<Void, Void, String> mHtmlLoader;
    int mHtmlResId;
    @Nullable
    String mTitle;

    @NonNull
    public static HtmlDialogFragment newInstance(
            String title,
            int htmlResId,
            String posText, HtmlDialog.onPositivePressed monPositivePressed,
            String negText, HtmlDialog.onNegativePressed monNegativePressed,
            HtmlDialog.onCancelled monCancelled) {
        onPositivePressed = monPositivePressed;
        onNegativePressed = monNegativePressed;
        onCancelled = monCancelled;
        Bundle bundle = new Bundle();
        bundle.putInt(HTML_RES_ID, htmlResId);
        bundle.putString(TITLE, title);
        bundle.putString(POS_TEXT, posText);
        bundle.putString(NEG_TEXT, negText);
        HtmlDialogFragment fragment = new HtmlDialogFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        mHtmlResId = args.getInt(HTML_RES_ID);
        mTitle = args.getString(TITLE);
        mNegText = args.getString(NEG_TEXT);
        mPosText = args.getString(POS_TEXT);

    }


    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setCancelable(true);

        View content = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_htmldialog, null);

        mWebView = (WebView) content.findViewById(R.id.webView);
        mProgressBar = (ProgressBar) content.findViewById(R.id.progress_bar);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (mTitle != null)
            builder.setTitle(mTitle);

        builder.setView(content);

        if (onNegativePressed != null) {
            builder.setNegativeButton(mNegText,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(@NonNull DialogInterface dialog, int which) {
                            onNegativePressed.onPress();
                            dialog.dismiss();
                        }
                    });
        }

        if (onPositivePressed != null) {
            builder.setPositiveButton(mPosText,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(@NonNull DialogInterface dialog, int which) {
                            onPositivePressed.onPress();
                            dialog.dismiss();
                        }
                    });
        }

        return builder.create();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHtmlLoader != null) {
            mHtmlLoader.cancel(true);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadHtml();
    }

    private void loadHtml() {
        mHtmlLoader = new AsyncTask<Void, Void, String>() {

            @NonNull
            @Override
            protected String doInBackground(Void... params) {
                InputStream is = getActivity().getResources().openRawResource(mHtmlResId);
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuilder sb = new StringBuilder();
                try {
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    br.close();
                } catch (IOException e) {
                    Log.d(TAG, "IOException caught in loadHtml()");
                }

                return sb.toString();
            }

            @Override
            protected void onPostExecute(String htmlString) {
                if (getActivity() == null || isCancelled()) {
                    return;
                }

                mProgressBar.setVisibility(View.INVISIBLE);
                mWebView.setVisibility(View.VISIBLE);
                mWebView.loadDataWithBaseURL(null, htmlString, "text/html", "utf-8", null);
                mHtmlLoader = null;
            }

        }.execute();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (onCancelled != null) {
            onCancelled.cancel();
        }
    }
}

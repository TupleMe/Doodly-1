package tuple.me.dtools.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;
import tuple.me.lily.util.DialogUtil;

public class ProgressJob<M> {

    private List<M> dataSet;
    private Context context;
    private ActionHandler<M> actionHandler;
    private List<M> failedItems = new ArrayList<>();
    private Disposable disposable;
    private MaterialDialog.Builder materialDialog;
    private MaterialDialog dialog;

    public ProgressJob(@NonNull Context context, @NonNull List<M> dataSet) {
        this.context = context;
        this.dataSet = dataSet;
        materialDialog = DialogUtil.getBlankDialogBuilder(context).progressNumberFormat("%1d/%2d").progress(false, dataSet.size(), true).onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                stop();
            }
        }).negativeText(tuple.me.lily.R.string.cancel);
    }


    public ProgressJob actionHandler(ActionHandler<M> actionHandler) {
        this.actionHandler = actionHandler;
        return this;
    }

    public ProgressJob setTitle(@StringRes int title) {
        materialDialog.title(title);
        return this;
    }

    public ProgressJob start() {
        final MaterialDialog dialog = this.dialog = materialDialog.show();
        disposable = Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@io.reactivex.annotations.NonNull FlowableEmitter<Integer> e) throws Exception {
                for (int i = 0; i < dataSet.size(); i++) {
                    M m = dataSet.get(i);
                    try {
                        if (e.isCancelled()) {
                            break;
                        }
                        actionHandler.doAction(m);
                    } catch (Exception exception) {
                        failedItems.add(m);
                    }
                    e.onNext(i);
                }
                e.onComplete();
            }
        }, BackpressureStrategy.LATEST).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribeWith(new DisposableSubscriber<Integer>() {
            @Override
            public void onNext(Integer integer) {
                dialog.setProgress(integer);
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {
                cleanup();
                actionHandler.onComplete();
            }
        });
        return this;
    }

    public void stop() {
        cleanup();
    }

    private void cleanup() {
        if (disposable != null && !disposable.isDisposed())
            disposable.dispose();
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public List<M> getFailedItems() {
        return failedItems;
    }

    public interface ActionHandler<MM> {
        boolean doAction(MM o) throws Exception;

        void onComplete();
    }
}

package tuple.me.lily.util.async;

import android.os.Handler;
import android.os.Looper;

import tuple.me.lily.core.Callback;

/**
 * Created by gokul-4192 on 0019 19-Feb-17.
 */
public class SimpleJob<R, P> {
    private static Handler uiHandler = new Handler(Looper.getMainLooper());
    private boolean isCancelled = false;
    private volatile doInBackground<R, P> mDoInBackground;
    private volatile onComplete<R> mOnComplete;
    private P[] parms;
    private volatile boolean isCompleted = true;
    private volatile Callback<Void> preExecuteCallback;
    private Thread thread;

    public SimpleJob() {
    }


    public void execute() {
        isCompleted = false;
        if (preExecuteCallback != null) {
            preExecuteCallback.call(null);
        }

        if (mDoInBackground != null) {
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (!isCancelled && mDoInBackground != null) {
                            final R result = mDoInBackground.doInBackground(parms);
                            if (mOnComplete != null && !isCancelled) {
                                uiHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!isCancelled && mOnComplete != null)
                                            mOnComplete.onSuccess(result);
                                    }
                                });
                            }
                        }

                    } catch (final Exception e) {
                        if (mOnComplete != null && !isCancelled) {
                            uiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (mOnComplete != null && !isCancelled)
                                        mOnComplete.onError(e);
                                }
                            });
                        }
                    }
                    isCompleted = true;
                }
            });
            thread.start();
        } else {
            isCompleted = true;
        }
    }

    public void cancel() {
        this.isCancelled = true;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void doAgain() {
        this.isCancelled = false;
        execute();
    }


    public static <T> void runOnUiThread(final Callback<T> callback, final T val) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    callback.call(val);
                } catch (Exception ignored) {
                }
            }
        });
    }

    public boolean doAgainIfCompleted() {
        if (isCompleted) {
            execute();
            return true;
        }
        return false;
    }

    public SimpleJob<R, P> register(Callback<Void> callback, doInBackground<R, P> mDoInBackground, onComplete<R> mOnComplete) {
        this.isCancelled = false;
        this.preExecuteCallback = callback;
        this.mDoInBackground = mDoInBackground;
        this.mOnComplete = mOnComplete;
        return this;
    }

    public SimpleJob<R, P> unregister() {
        this.isCancelled = true;
        this.preExecuteCallback = null;
        this.mDoInBackground = null;
        this.mOnComplete = null;
        return this;
    }

    public static <R, P> SimpleJob<R, P> simpleJob(doInBackground<R, P> mDoInBackground, onComplete<R> mOnComplete) {
        SimpleJob<R, P> asyncJob = new SimpleJob<>();
        asyncJob.register(null, mDoInBackground, mOnComplete).execute();
        return asyncJob;
    }
}

package tuple.me.dtools.clipboard;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.arasthel.asyncjob.AsyncJob;
import com.orm.query.Select;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tuple.me.dtools.R;
import tuple.me.dtools.activity.MainActivity;
import tuple.me.dtools.service.NotificationActionService;
import tuple.me.dtools.sugarmodel.clipboard.ClipboardItem;

public class ClipBoardMonitorService extends Service {
    private ExecutorService mThreadPool = Executors.newSingleThreadExecutor();
    private ClipboardManager mClipboardManager;
    public final IBinder mBinder = new LocalBinder();
    private NotificationManager mNotificationManager;
    public static boolean isServiceRunning;
    private Context context;
    private int notificationID = 335;
    public static HashMap<String, Long> clipVsId = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mClipboardManager =
                (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        mClipboardManager.addPrimaryClipChangedListener(
                mOnPrimaryClipChangedListener);
        if (clipVsId == null) {
            clipVsId = new HashMap<>();
            AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
                @Override
                public void doOnBackground() {
                    Iterator<ClipboardItem> itr = ClipboardItem.findAll(ClipboardItem.class);
                    while (itr.hasNext()) {
                        ClipboardItem item = itr.next();
                        clipVsId.put(item.text, item.getId());
                    }
                }
            });
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isServiceRunning = true;
        context = getApplicationContext();
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        dismissNotification();
        setupNotification();
        return Service.START_STICKY;
    }

    private void setupNotification() {
        Intent notificationIntent = new Intent(context, ClipBoardPopup.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.putExtra(MainActivity.FRAGMENT_TO_OPEN, R.string.clipboard_manager);
        PendingIntent intent = PendingIntent.getActivity(context, 0,
                notificationIntent,  PendingIntent.FLAG_UPDATE_CURRENT);
        RemoteViews smallNotification = new RemoteViews(getPackageName(), R.layout.closable_notification_small);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setCustomContentView(smallNotification)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentIntent(intent);
        Notification notification = mBuilder.build();
        smallNotification.setTextViewText(R.id.text, getString(R.string.clipboard_mg_bg));

        Intent stop = new Intent(context, NotificationActionService.class)
                .setAction(NotificationActionService.STOP_CLIP_BOARD_STOP);
        PendingIntent stopPI = PendingIntent.getService(this, 0, stop, 0);
        smallNotification.setOnClickPendingIntent(R.id.stop, stopPI);
        mNotificationManager.notify(notificationID, notification);
    }


    private void dismissNotification() {
        if(mNotificationManager!=null){
            mNotificationManager.cancel(notificationID);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mClipboardManager != null) {
            mClipboardManager.removePrimaryClipChangedListener(
                    mOnPrimaryClipChangedListener);
        }
        isServiceRunning = false;
        dismissNotification();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public ClipBoardMonitorService getServiceInstance() {
            return ClipBoardMonitorService.this;
        }
    }

    private ClipboardManager.OnPrimaryClipChangedListener mOnPrimaryClipChangedListener =
            new ClipboardManager.OnPrimaryClipChangedListener() {
                @Override
                public void onPrimaryClipChanged() {
                    ClipData clip = mClipboardManager.getPrimaryClip();
                    if (clip.getItemCount() > 0)
                        mThreadPool.execute(new WriteHistoryRunnable(
                                clip.getItemAt(0).getText()));
                }
            };

    private static class WriteHistoryRunnable implements Runnable {
        private final Long mNow;
        private final CharSequence mTextToWrite;

        public WriteHistoryRunnable(CharSequence text) {
            mNow = System.currentTimeMillis();
            mTextToWrite = text;
        }

        @Override
        public void run() {
            if (TextUtils.isEmpty(mTextToWrite)) {
                return;
            }
            if (clipVsId.containsKey(mTextToWrite.toString())) {
                ClipboardItem item = ClipboardItem.findById(ClipboardItem.class, clipVsId.get(mTextToWrite.toString()));
                if (item != null) {
                    item.delete();
                }
            }
            ClipboardItem newItem = new ClipboardItem(mNow, mTextToWrite.toString());
            newItem.save();
            clipVsId.put(mTextToWrite.toString(), newItem.getId());
            if (clipVsId.size() > 50) {
                List<ClipboardItem> max = Select.from(ClipboardItem.class).orderBy("TIME desc").limit(clipVsId.size() - 50 + "").list();
                for (int i = 0; i < max.size(); i++) {
                    clipVsId.remove(max.get(i).text);
                    max.get(i).delete();
                }
            }
        }
    }
}


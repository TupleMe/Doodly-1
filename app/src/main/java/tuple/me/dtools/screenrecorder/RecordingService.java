package tuple.me.dtools.screenrecorder;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import org.greenrobot.eventbus.EventBus;

import tuple.me.dtools.R;
import tuple.me.dtools.activity.MainActivity;
import tuple.me.dtools.events.ScreenRecorderChange;
import tuple.me.dtools.service.NotificationActionService;
import tuple.me.lily.views.toasty.Toasty;

public class RecordingService extends Service {

    public static final String PROJECTION_INDENT_CODE = "projectionIntentCode";
    public final IBinder mBinder = new LocalBinder();
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder mBuilder;
    private Context context;
    public int notificationID = 53434;
    public static final String PROJECTION_INDENT = "projectionIntent";
    private RecordingSession recordingSession;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = getApplicationContext();
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent data = intent.getParcelableExtra(PROJECTION_INDENT);
        int resultCode = intent.getIntExtra(PROJECTION_INDENT_CODE, 0);
        if (data == null || resultCode == 0) {
            Toasty.error(context, "Unable to start screen capturing service");
        } else {
            recordingSession = new RecordingSession(context, listener, resultCode, data, 100);
            recordingSession.startRecording();
        }
        return Service.START_NOT_STICKY;
    }

    public class LocalBinder extends Binder {
        @NonNull
        public RecordingService getServiceInstance() {
            return RecordingService.this;
        }
    }


    private void setupNotification() {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);
        RemoteViews smallNotification = new RemoteViews(getPackageName(), R.layout.closable_notification_small);
        mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setCustomContentView(smallNotification)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentIntent(intent);
        Notification notification = mBuilder.build();
        smallNotification.setTextViewText(R.id.text, getString(R.string.screen_recorder_bg));
        Intent stopIntent = new Intent(getApplicationContext(), NotificationActionService.class)
                .setAction(NotificationActionService.STOP_SCREEN_REC);
        PendingIntent stop = PendingIntent.getService(getApplicationContext(), 0, stopIntent, 0);
        smallNotification.setOnClickPendingIntent(R.id.stop, stop);
        mNotificationManager.notify(notificationID, notification);
    }

    @Override
    public void onDestroy() {
        recordingSession.stopRecording();
        super.onDestroy();
        stopForeground(true);
        stopSelf();
    }

    public static boolean isRecorderRunning;
    RecordingSession.Listener listener = new RecordingSession.Listener() {
        @Override
        public void onPrepare() {

        }

        @Override
        public void onStart() {
            isRecorderRunning = true;
            EventBus.getDefault().post(new ScreenRecorderChange());
            setupNotification();
        }

        @Override
        public void onStop() {
            isRecorderRunning = false;
            EventBus.getDefault().post(new ScreenRecorderChange());
            mNotificationManager.cancel(notificationID);
        }

        @Override
        public void onEnd() {

        }
    };
}

package tuple.me.dtools.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.arasthel.asyncjob.AsyncJob;

import org.greenrobot.eventbus.EventBus;

import tuple.me.dtools.bfilter.ScreenFilterService;
import tuple.me.dtools.clipboard.ClipBoardMonitorService;
import tuple.me.dtools.constants.PrefConstants;
import tuple.me.dtools.events.FileDelete;
import tuple.me.dtools.file.SystemFile;
import tuple.me.dtools.screenrecorder.RecordingService;
import tuple.me.dtools.screenrecorder.RecordingSession;
import tuple.me.lily.util.Prefs;

public class NotificationActionService extends IntentService {

    public static final String BLUE_LIGHT_STOP = "bluelight_stop";
    public static final String STOP_SCREEN_REC = "stop_screen_recording";
    public static final String STOP_CLIP_BOARD_STOP = "clipboard_stop";
    public static final String DELETE_FILE = "delete_file";

    public NotificationActionService() {
        super(NotificationActionService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case STOP_CLIP_BOARD_STOP:
                Intent clipService = new Intent(getApplicationContext(), ClipBoardMonitorService.class);
                stopService(clipService);
                break;
            case BLUE_LIGHT_STOP:
                Prefs.getInstance().set(PrefConstants.BLUE_LIGHT_ENABLED, false);
                Intent blueService = new Intent(getApplicationContext(), ScreenFilterService.class);
                Prefs.getInstance().set(PrefConstants.BLUE_LIGHT_ENABLED, false);
                stopService(blueService);
                break;
            case STOP_SCREEN_REC:
                Intent screenRecordingService = new Intent(getApplicationContext(), RecordingService.class);
                screenRecordingService.addCategory(RecordingService.class.getName());
                stopService(screenRecordingService);
                break;
            case DELETE_FILE:
                final Uri uri = intent.getData();
                final ContentResolver contentResolver = getApplication().getContentResolver();
                AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
                    @Override
                    public void doOnBackground() {
                        int rowsDeleted = contentResolver.delete(uri, null, null);
                        if (rowsDeleted == 1) {
                            AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                                @Override
                                public void doInUIThread() {
                                    EventBus.getDefault().post(new FileDelete(new SystemFile(uri.getPath())));
                                    NotificationManager mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                    mNotificationManager.cancel(RecordingSession.NOTIFICATION_ID);
                                }
                            });
                        }
                    }
                });
                break;
        }
    }
}



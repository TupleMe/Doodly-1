package tuple.me.dtools.screenrecorder;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.display.VirtualDisplay;
import android.media.CamcorderProfile;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;
import tuple.me.dtools.R;
import tuple.me.dtools.constants.Constants;
import tuple.me.dtools.events.FileDelete;
import tuple.me.dtools.file.SystemFile;
import tuple.me.dtools.service.NotificationActionService;
import tuple.me.lily.util.ViewUtils;

import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;
import static android.content.Context.MEDIA_PROJECTION_SERVICE;
import static android.content.Context.NOTIFICATION_SERVICE;
import static android.content.Context.WINDOW_SERVICE;
import static android.content.Intent.ACTION_SEND;
import static android.content.Intent.ACTION_VIEW;
import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION;
import static android.media.MediaRecorder.OutputFormat.MPEG_4;
import static android.media.MediaRecorder.VideoEncoder.H264;
import static android.media.MediaRecorder.VideoSource.SURFACE;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Environment.DIRECTORY_MOVIES;
import static android.widget.Toast.LENGTH_SHORT;

public final class RecordingSession {
    public static final int NOTIFICATION_ID = 522592;

    private static final String DISPLAY_NAME = Constants.APP_NAME;
    private static final String MIME_TYPE = "video/mp4";

    interface Listener {
        /**
         * Invoked before {@link #onStart()} to prepare UI before recording.
         */
        void onPrepare();

        /**
         * Invoked immediately prior to the start of recording.
         */
        void onStart();

        /**
         * Invoked immediately after the end of recording.
         */
        void onStop();

        /**
         * Invoked after all work for this session has completed.
         */
        void onEnd();
    }

    private final Handler mainThread = new Handler(Looper.getMainLooper());

    private final Context context;
    private final Listener listener;
    private final int resultCode;
    private final Intent data;

    private final Boolean showCountDown;
    private final Integer videoSizePercentage;

    private final File outputRoot;
    private final DateFormat fileFormat =
            new SimpleDateFormat("'" + Constants.APP_NAME + "'yyyy-MM-dd-HH-mm-ss'.mp4'", Locale.US);

    private final NotificationManager notificationManager;
    private final WindowManager windowManager;
    private final MediaProjectionManager projectionManager;

    private MediaRecorder recorder;
    private MediaProjection projection;
    private VirtualDisplay display;
    private String outputFile;
    private boolean running;
    private long recordingStartNanos;

    public RecordingSession(Context context, Listener listener, int resultCode, Intent data, int videoSizePercentage) {
        this.context = context;
        this.listener = listener;
        this.resultCode = resultCode;
        this.data = data;

        this.showCountDown = true;
        this.videoSizePercentage = videoSizePercentage;

        File picturesDir = Environment.getExternalStoragePublicDirectory(DIRECTORY_MOVIES);
        outputRoot = new File(picturesDir, Constants.APP_NAME);
        notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        projectionManager = (MediaProjectionManager) context.getSystemService(MEDIA_PROJECTION_SERVICE);
    }


    private void hideOverlay() {
        Timber.d("Removing overlay view from window.");
    }

    private void cancelOverlay() {
        hideOverlay();
        listener.onEnd();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private RecordingInfo getRecordingInfo() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        wm.getDefaultDisplay().getRealMetrics(displayMetrics);
        int displayWidth = displayMetrics.widthPixels;
        int displayHeight = displayMetrics.heightPixels;
        int displayDensity = displayMetrics.densityDpi;
        Timber.d("Display size: %s x %s @ %s" + displayWidth + displayHeight + displayDensity);

        Configuration configuration = context.getResources().getConfiguration();
        boolean isLandscape = configuration.orientation == ORIENTATION_LANDSCAPE;
        Timber.d("Display landscape: %s" + isLandscape);

        // Get the best camera profile available. We assume MediaRecorder supports the highest.
        CamcorderProfile camcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        int cameraWidth = camcorderProfile != null ? camcorderProfile.videoFrameWidth : -1;
        int cameraHeight = camcorderProfile != null ? camcorderProfile.videoFrameHeight : -1;
        int cameraFrameRate = camcorderProfile != null ? camcorderProfile.videoFrameRate : 30;
        Timber.d("Camera size: %s x %s framerate: %s" + cameraWidth + cameraHeight + cameraFrameRate);

        int sizePercentage = videoSizePercentage;
        Timber.d("Size percentage: %s" + sizePercentage);

        return calculateRecordingInfo(displayWidth, displayHeight, displayDensity, isLandscape,
                cameraWidth, cameraHeight, cameraFrameRate, sizePercentage);
    }

    @TargetApi(LOLLIPOP)
    public void startRecording() {
        Timber.d("Starting screen recording...");

        if (!outputRoot.exists() && !outputRoot.mkdirs()) {
            Timber.d("Unable to create output directory '%s'." + outputRoot.getAbsolutePath());
            Toast.makeText(context, "Unable to create output directory.\nCannot record screen.",
                    LENGTH_SHORT).show();
            return;
        }

        RecordingInfo recordingInfo = getRecordingInfo();
        Timber.d("Recording: %s x %s @ %s" + recordingInfo.width + recordingInfo.height +
                recordingInfo.density);

        recorder = new MediaRecorder();
        recorder.setVideoSource(SURFACE);
        recorder.setOutputFormat(MPEG_4);
        recorder.setVideoFrameRate(recordingInfo.frameRate);
        recorder.setVideoEncoder(H264);
        recorder.setVideoSize(recordingInfo.width, recordingInfo.height);
        recorder.setVideoEncodingBitRate(8 * 1000 * 1000);

        String outputName = fileFormat.format(new Date());
        outputFile = new File(outputRoot, outputName).getAbsolutePath();
        Timber.d("Output file '%s'." + outputFile);
        recorder.setOutputFile(outputFile);

        try {
            recorder.prepare();
        } catch (IOException e) {
            throw new RuntimeException("Unable to prepare MediaRecorder.", e);
        }

        projection = projectionManager.getMediaProjection(resultCode, data);

        Surface surface = recorder.getSurface();
        display =
                projection.createVirtualDisplay(DISPLAY_NAME, recordingInfo.width, recordingInfo.height,
                        recordingInfo.density, VIRTUAL_DISPLAY_FLAG_PRESENTATION, surface, null, null);

        recorder.start();
        running = true;
        recordingStartNanos = System.nanoTime();
        listener.onStart();
        Timber.d("Screen recording started.");
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void stopRecording() {
        Timber.d("Stopping screen recording...");

        if (!running) {
            throw new IllegalStateException("Not running.");
        }
        running = false;

        hideOverlay();

        boolean propagate = false;
        try {
            // Stop the projection in order to flush everything to the recorder.
            projection.stop();
            // Stop the recorder which writes the contents to the file.
            recorder.stop();

            propagate = true;
        } finally {
            try {
                // Ensure the listener can tear down its resources regardless if stopping crashes.
                listener.onStop();
            } catch (RuntimeException e) {
                if (propagate) {
                    //noinspection ThrowFromFinallyBlock
                    throw e; // Only allow listener exceptions to propagate if stopped successfully.
                }
            }
        }

        long recordingStopNanos = System.nanoTime();

        recorder.release();
        display.release();


        Timber.d("Screen recording stopped. Notifying media scanner of new video.");

        MediaScannerConnection.scanFile(context, new String[]{outputFile}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, final Uri uri) {
                        if (uri == null) throw new NullPointerException("uri == null");
                        Timber.d("Media scanner completed.");
                        mainThread.post(new Runnable() {
                            @Override
                            public void run() {
                                showNotification(uri, null);
                            }
                        });
                    }
                });
    }

    @TargetApi(LOLLIPOP)
    private void showNotification(final Uri uri, Bitmap bitmap) {
        Intent viewIntent = new Intent(ACTION_VIEW, uri);
        PendingIntent pendingViewIntent =
                PendingIntent.getActivity(context, 0, viewIntent, FLAG_CANCEL_CURRENT);

        Intent shareIntent = new Intent(ACTION_SEND);
        shareIntent.setType(MIME_TYPE);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent = Intent.createChooser(shareIntent, null);

        PendingIntent pendingShareIntent =
                PendingIntent.getActivity(context, 0, shareIntent, FLAG_CANCEL_CURRENT);

        Intent deleteIntent = new Intent(context, NotificationActionService.class);
        deleteIntent.setData(uri);
        deleteIntent.setAction(NotificationActionService.DELETE_FILE);
        PendingIntent pendingDeleteIntent = PendingIntent.getService(context, 0, deleteIntent, 0);

        CharSequence title = context.getString(R.string.captured);
        CharSequence subtitle = Constants.APP_NAME;
        CharSequence share = context.getString(R.string.share);
        CharSequence delete = context.getString(R.string.delete);
        Notification.Builder builder = new Notification.Builder(context) //
                .setContentTitle(title)
                .setContentText(subtitle)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true)
                .setSmallIcon(R.drawable.ic_videocam_white_24dp).setContentIntent(pendingViewIntent)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_share_white_24dp, share, pendingShareIntent)
                .addAction(R.drawable.ic_delete_white_24dp, delete, pendingDeleteIntent);

        if (bitmap != null) {
            builder.setLargeIcon(ViewUtils.createSquareBitmap(bitmap))
                    .setStyle(new Notification.BigPictureStyle() //
                            .setBigContentTitle(title) //
                            .setSummaryText(subtitle) //
                            .bigPicture(bitmap));
        }

        notificationManager.notify(NOTIFICATION_ID, builder.build());

        if (bitmap != null) {
            listener.onEnd();
            return;
        }
        EventBus.getDefault().post(new FileDelete(new SystemFile(outputFile)));
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(@NonNull Void... none) {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(context, uri);
                return retriever.getFrameAtTime();
            }

            @Override
            protected void onPostExecute(@Nullable Bitmap bitmap) {
                if (bitmap != null && !notificationDismissed()) {
                    showNotification(uri, bitmap);
                } else {
                    listener.onEnd();
                }
            }

            private boolean notificationDismissed() {
                return SDK_INT >= M && notificationManager.getActiveNotifications().length == 0;
            }
        }.execute();
    }

    static RecordingInfo calculateRecordingInfo(int displayWidth, int displayHeight,
                                                int displayDensity, boolean isLandscapeDevice, int cameraWidth, int cameraHeight,
                                                int cameraFrameRate, int sizePercentage) {
        // Scale the display size before any maximum size calculations.
        displayWidth = displayWidth * sizePercentage / 100;
        displayHeight = displayHeight * sizePercentage / 100;

        if (cameraWidth == -1 && cameraHeight == -1) {
            // No cameras. Fall back to the display size.
            return new RecordingInfo(displayWidth, displayHeight, cameraFrameRate, displayDensity);
        }

        int frameWidth = isLandscapeDevice ? cameraWidth : cameraHeight;
        int frameHeight = isLandscapeDevice ? cameraHeight : cameraWidth;
        if (frameWidth >= displayWidth && frameHeight >= displayHeight) {
            // Frame can hold the entire display. Use exact values.
            return new RecordingInfo(displayWidth, displayHeight, cameraFrameRate, displayDensity);
        }

        // Calculate new width or height to preserve aspect ratio.
        if (isLandscapeDevice) {
            frameWidth = displayWidth * frameHeight / displayHeight;
        } else {
            frameHeight = displayHeight * frameWidth / displayWidth;
        }
        return new RecordingInfo(frameWidth, frameHeight, cameraFrameRate, displayDensity);
    }

    static final class RecordingInfo {
        final int width;
        final int height;
        final int frameRate;
        final int density;

        RecordingInfo(int width, int height, int frameRate, int density) {
            this.width = width;
            this.height = height;
            this.frameRate = frameRate;
            this.density = density;
        }
    }


    void destroy() {
        if (running) {
            Timber.w("Destroyed while running!");
            stopRecording();
        }
    }
}

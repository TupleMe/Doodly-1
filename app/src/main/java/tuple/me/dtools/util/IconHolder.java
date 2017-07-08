package tuple.me.dtools.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class IconHolder {

    private static final int MAX_CACHE = 500;

    private static final int MSG_LOAD = 1;
    private static final int MSG_LOADED = 2;
    private static final int MSG_DESTROY = 3;

    private final Map<String, Bitmap> mIcons;     // Themes based
    private final Map<String, Bitmap> mAppIcons;  // App based

    private Map<String, Long> mAlbums;      // Media albums

    private Map<ImageView, String> mRequests;

    private final Context mContext;
    private final boolean mUseThumbs;
    private HandlerThread mWorkerThread;
    private Handler mWorkerHandler;
    boolean grid;

    private static class LoadResult {
        String fso;
        Bitmap result;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOADED:
                    processResult((LoadResult) msg.obj);
                    sendEmptyMessageDelayed(MSG_DESTROY, 3000);
                    break;
                case MSG_DESTROY:
                    shutdownWorker();
                    break;
            }
        }

        private void processResult(LoadResult result) {
            // Cache the new drawable
            final String filePath = (result.fso);
            mAppIcons.put(filePath, result.result);

            // find the request for it
            for (Map.Entry<ImageView, String> entry : mRequests.entrySet()) {
                final ImageView imageView = entry.getKey();
                final String fso = entry.getValue();
                if (fso == result.fso) {
                    imageView.setImageBitmap(result.result);
                    mRequests.remove(imageView);
                    break;
                }
            }
        }
    };

    /**
     * Constructor of <code>IconHolder</code>.
     *
     * @param useThumbs If thumbs of images, videos, apps, ... should be returned
     * instead of the default icon.
     */
    int px;

    public IconHolder(Context context, boolean useThumbs, boolean grid) {
        super();
        this.mContext = context;
        this.mUseThumbs = useThumbs;
        this.mRequests = new HashMap<>();
        this.mIcons = new HashMap<>();
        this.mAppIcons = new LinkedHashMap<String, Bitmap>(MAX_CACHE, .75F, true) {
            private static final long serialVersionUID = 1L;

            @Override
            protected boolean removeEldestEntry(Entry<String, Bitmap> eldest) {
                return size() > MAX_CACHE;
            }
        };
        this.mAlbums = new HashMap<>();
        this.grid = grid;
        Resources res = mContext.getResources();
        int dp = 50;
        if (grid) {
            dp = 150;
        }
        px = (int) (dp * (res.getDisplayMetrics().densityDpi / 160));

    }

    /**
     * Method that returns a drawable reference of a icon.
     *
     * @param resid The resource identifier
     * @return Drawable The drawable icon reference
     */

    /**
     * Method that returns a drawable reference of a FileSystemObject.
     *
     * @param iconView View to load the drawable into
     * @param fso      The FileSystemObject reference
     * @return Drawable The drawable reference
     */
    public void loadDrawable(ImageView iconView, final String fso) {
        if (!mUseThumbs) {
            return;
        }

        if (this.mAppIcons.containsKey(fso)) {
            iconView.setImageBitmap(this.mAppIcons.get(fso));
            return;
        }
        mRequests.put(iconView, fso);
        new Thread(new Runnable() {
            @Override
            public void run() {

                mHandler.removeMessages(MSG_DESTROY);
                if (mWorkerThread == null || mWorkerHandler == null) {
                    mWorkerThread = new HandlerThread("IconHolderLoader");
                    mWorkerThread.start();
                    mWorkerHandler = new WorkerHandler(mWorkerThread.getLooper());
                }
                Message msg = mWorkerHandler.obtainMessage(MSG_LOAD, fso);
                msg.sendToTarget();

            }
        }).start();
    }

    /**
     * Cancel loading of a drawable for a certain ImageView.
     */
    public void cancelLoad(ImageView view) {
        String fso = mRequests.get(view);
        if (fso != null && mWorkerHandler != null) {
            mWorkerHandler.removeMessages(MSG_LOAD, fso);
        }
        mRequests.remove(view);
    }

    private class WorkerHandler extends Handler {
        public WorkerHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOAD:
                    String fso = (String) msg.obj;
                    Bitmap d = loadDrawable(fso);
                    if (d != null) {
                        LoadResult result = new LoadResult();
                        result.fso = fso;
                        result.result = d;
                        mHandler.obtainMessage(MSG_LOADED, result).sendToTarget();
                    }
                    break;
            }
        }
    }

    private Bitmap loadDrawable(String fso) {

        try {
            if (Icons.isApk((fso))) {
                return getAppDrawable(fso);
            } else if (Icons.isPicture((fso))) {
                return loadImage(fso);
            } else if (Icons.isVideo((fso)))
                return getVideoDrawable(fso);
        } catch (OutOfMemoryError outOfMemoryError) {
            cleanup();
            shutdownWorker();
        }

        return null;
    }

    private Bitmap getVideoDrawable(String path) throws OutOfMemoryError {

        try {
            return ThumbnailUtils.createVideoThumbnail(path,
                    MediaStore.Images.Thumbnails.MINI_KIND);
        } catch (Exception e) {
            return null;
        }
    }

    private Bitmap getAppDrawable(String path) throws OutOfMemoryError {
        Bitmap bitsat;
        try {
            PackageManager pm = mContext.getPackageManager();
            PackageInfo pi = pm.getPackageArchiveInfo(path, 0);
            pi.applicationInfo.sourceDir = path;
            pi.applicationInfo.publicSourceDir = path;
            Drawable d = pi.applicationInfo.loadIcon(pm);
            bitsat = ((BitmapDrawable) d).getBitmap();
        } catch (Exception e) {
            return null;
        }
        return bitsat;
    }

    public Bitmap loadImage(String path) throws OutOfMemoryError {
        Bitmap bitsat;
        try {
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//            options.inJustDecodeBounds = true;
//            options.inSampleSize = calculateInSampleSize(options, px, px);
//            bitsat = BitmapFactory.decodeFile(path, options);

            Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(path),
                    px, px);
            return ThumbImage;
        } catch (Exception e) {
            return null;
        }
    }

    private void shutdownWorker() {
        if (mWorkerThread != null) {
            mWorkerThread.getLooper().quit();
            mWorkerHandler = null;
            mWorkerThread = null;
        }
    }

    public void cleanup() {
        this.mRequests.clear();
        this.mIcons.clear();
        this.mAppIcons.clear();
        shutdownWorker();
    }


    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}


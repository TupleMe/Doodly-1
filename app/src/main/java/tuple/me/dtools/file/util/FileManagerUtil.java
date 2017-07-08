package tuple.me.dtools.file.util;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;
import android.util.Log;

import com.arasthel.asyncjob.AsyncJob;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import tuple.me.dtools.R;
import tuple.me.dtools.apk.ApkUtils;
import tuple.me.dtools.events.FileDelete;
import tuple.me.dtools.file.SystemFile;
import tuple.me.dtools.file.util.share.ShareTask;
import tuple.me.dtools.util.Icons;
import tuple.me.lily.Contexter;
import tuple.me.lily.ThemeEngine;
import tuple.me.lily.core.CollectionUtils;
import tuple.me.lily.core.Pair;
import tuple.me.lily.model.Item;
import tuple.me.lily.util.CommonUtil;
import tuple.me.lily.util.FileUtils;
import tuple.me.lily.util.MimeTypes;
import tuple.me.lily.util.RangeUtils;
import tuple.me.lily.views.bs.IconsBottomSheetFragment;
import tuple.me.lily.views.bs.PropertiesBottomSheet;
import tuple.me.lily.views.toasty.Toasty;

public class FileManagerUtil {

    private static final String INTERNAL_VOLUME = "internal";
    public static final String EXTERNAL_VOLUME = "external";

    private static final String EMULATED_STORAGE_SOURCE = System.getenv("EMULATED_STORAGE_SOURCE");
    private static final String EMULATED_STORAGE_TARGET = System.getenv("EMULATED_STORAGE_TARGET");
    private static final String EXTERNAL_STORAGE = System.getenv("EXTERNAL_STORAGE");

    public static String normalizeMediaPath(String path) {
        // Retrieve all the paths and check that we have this environment vars
        if (TextUtils.isEmpty(EMULATED_STORAGE_SOURCE) ||
                TextUtils.isEmpty(EMULATED_STORAGE_TARGET) ||
                TextUtils.isEmpty(EXTERNAL_STORAGE)) {
            return path;
        }

        // We need to convert EMULATED_STORAGE_SOURCE -> EMULATED_STORAGE_TARGET
        if (path.startsWith(EMULATED_STORAGE_SOURCE)) {
            path = path.replace(EMULATED_STORAGE_SOURCE, EMULATED_STORAGE_TARGET);
        }
        return path;
    }

    public static Uri fileToContentUri(Context context, File file) {
        // Normalize the path to ensure media search
        final String normalizedPath = normalizeMediaPath(file.getAbsolutePath());

        // Check in external and internal storages
        Uri uri = fileToContentUri(context, normalizedPath, EXTERNAL_VOLUME);
        if (uri != null) {
            return uri;
        }
        uri = fileToContentUri(context, normalizedPath, INTERNAL_VOLUME);
        if (uri != null) {
            return uri;
        }
        return null;
    }

    private static Uri fileToContentUri(Context context, String path, String volume) {
        String[] projection = null;
        final String where = MediaStore.MediaColumns.DATA + " = ?";
        Uri baseUri = MediaStore.Files.getContentUri(volume);
        boolean isMimeTypeImage = false, isMimeTypeVideo = false, isMimeTypeAudio = false;
        isMimeTypeImage = Icons.isPicture(path);
        if (!isMimeTypeImage) {
            isMimeTypeVideo = Icons.isVideo(path);
            if (!isMimeTypeVideo) {
                isMimeTypeAudio = Icons.isVideo(path);
            }
        }
        if (isMimeTypeImage || isMimeTypeVideo || isMimeTypeAudio) {
            projection = new String[]{BaseColumns._ID};
            if (isMimeTypeImage) {
                baseUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else if (isMimeTypeVideo) {
                baseUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            } else if (isMimeTypeAudio) {
                baseUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            }
        } else {
            projection = new String[]{BaseColumns._ID, MediaStore.Files.FileColumns.MEDIA_TYPE};
        }
        ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(baseUri, projection, where, new String[]{path}, null);
        try {
            if (c != null && c.moveToNext()) {
                boolean isValid = false;
                if (isMimeTypeImage || isMimeTypeVideo || isMimeTypeAudio) {
                    isValid = true;
                } else {
                    int type = c.getInt(c.getColumnIndexOrThrow(
                            MediaStore.Files.FileColumns.MEDIA_TYPE));
                    isValid = type != 0;
                }

                if (isValid) {
                    // Do not force to use content uri for no media files
                    long id = c.getLong(c.getColumnIndexOrThrow(BaseColumns._ID));
                    return Uri.withAppendedPath(baseUri, String.valueOf(id));
                }
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return null;
    }

    public static void showFileOptions(SystemFile file, FragmentManager fm, Context context) {
        showFileOptions(file.file, fm, context);
    }

    public static void showFileOptions(final File file, final FragmentManager fm, final Context context) {
        final ArrayList<Integer> titlesArr = new ArrayList<>();
        ArrayList<Integer> iconsArr = new ArrayList<>();
        if (Icons.isApk(file)) {
            titlesArr.add(R.string.install);
            iconsArr.add(R.drawable.ic_android_mod);
        }
        final Integer[] titles = new Integer[]{tuple.me.lily.R.string.open, tuple.me.lily.R.string.share, tuple.me.lily.R.string.delete, R.string.properties};
        final Integer[] icons = new Integer[]{tuple.me.lily.R.drawable.ic_folder_open, tuple.me.lily.R.drawable.ic_share, tuple.me.lily.R.drawable.ic_delete, R.drawable.ic_alert_circle};
        CollectionUtils.addAll(titlesArr, titles);
        CollectionUtils.addAll(iconsArr, icons);

        final IconsBottomSheetFragment customBottomSheetFragment = IconsBottomSheetFragment.newInstance(CollectionUtils.toIntArray(titlesArr), CollectionUtils.toIntArray(iconsArr)).setTitle(file.getName()).setTitle(file.getName());
        customBottomSheetFragment.setItemClickListener(new IconsBottomSheetFragment.OnItemClickListener() {
            @Override
            public void onItemClick(int id) {
                switch (titlesArr.get(id)) {
                    case R.string.open:
                        openunknown(file, context, false);
                        break;
                    case R.string.share:
                        shareFiles(context, file);
                        break;
                    case R.string.delete:
                        if (deleteFile(file, context)) {
                            EventBus.getDefault().post(new FileDelete(new SystemFile(file)));
                        }
                        break;
                    case R.string.properties:
                        showFileProperties(file, context, fm);
                        break;
                    case R.string.install:
                        ApkUtils.getInstance().installApp(file);
                        break;
                }
                customBottomSheetFragment.dismiss();
            }

        }).show(fm, "File Details");
    }


    private static void showFileProperties(final File file, final Context context, final FragmentManager fragmentManager) {
        AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
            @Override
            public void doOnBackground() {
                final ArrayList<Pair<String, String>> props = new ArrayList<>();
                props.add(new Pair<>(context.getString(R.string.name), file.getName()));
                props.add(new Pair<>(context.getString(R.string.location), file.getPath()));
                if (file.getParent() != null) {
                    props.add(new Pair<>(context.getString(R.string.parent_folder), file.getParent()));
                }
                props.add(new Pair<>(context.getString(R.string.last_modified), CommonUtil.getDateString(file.lastModified())));
                if (file.isDirectory()) {
                    long[] details = new long[2];
                    FileUtils.getDirSizeWithItems(file, details);
                    props.add(new Pair<>(context.getString(R.string.size), FileUtils.getReadableFileSize(details[0])));
                    props.add(new Pair<>(context.getString(R.string.items_in_folder), details[1] + " items"));
                } else {
                    props.add(new Pair<>(context.getString(R.string.size), FileUtils.getReadableFileSize(file.length())));
                }
                AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                    @Override
                    public void doInUIThread() {
                        PropertiesBottomSheet.newInstance(props).setTitle(R.string.properties).show(fragmentManager, "PROPS");
                    }
                });
            }
        });
    }

    public static void openunknown(File f, Context c, boolean forcechooser) {
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        String type = MimeTypes.getMimeType(f);
        if (type != null && type.trim().length() != 0 && !type.equals("*/*")) {
            Uri uri = fileToContentUri(c, f);
            if (uri == null) uri = Uri.fromFile(f);
            intent.setDataAndType(uri, type);
            Intent startintent;
            if (forcechooser)
                startintent = Intent.createChooser(intent, c.getResources().getString(R.string.open_with));
            else startintent = intent;
            try {
                c.startActivity(startintent);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                Toasty.error(c, R.string.noappfound);
            }
        } else {
            Toasty.error(c, R.string.noappfound);
        }
    }

    public static boolean deleteFile(@Nullable File file, Context context) {
        // First try the normal deletion.
        if (file == null) return true;
        boolean fileDelete = FileUtils.deleteFilesInFolder(file);
        if (file.delete() || fileDelete)
            return true;

        // Try with Storage Access Framework.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isOnExtSdCard(file, context)) {

            DocumentFile document = getDocumentFile(file, false, context);
            if (document != null)
                return document.delete();
        }

        // Try the Kitkat workaround.
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            ContentResolver resolver = context.getContentResolver();

            try {
                Uri uri = MediaStoreHack.getUriFromFile(file.getAbsolutePath(), context);
                resolver.delete(uri, null, null);
                return !file.exists();
            } catch (Exception e) {
                Log.d("DTools", "Error when deleting file " + file.getAbsolutePath(), e);
                return false;
            }
        }

        return !file.exists();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean isOnExtSdCard(final File file, Context c) {
        return getExtSdCardFolder(file, c) != null;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getExtSdCardFolder(final File file, Context context) {
        String[] extSdPaths = FileUtils.getExtSdCardPaths(context);
        try {
            for (String extSdPath : extSdPaths) {
                if (file.getCanonicalPath().startsWith(extSdPath)) {
                    return extSdPath;
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }


    public static DocumentFile getDocumentFile(final File file, final boolean isDirectory, Context context) {
        String baseFolder = getExtSdCardFolder(file, context);
        boolean originalDirectory = false;
        if (baseFolder == null) {
            return null;
        }

        String relativePath = null;
        try {
            String fullPath = file.getCanonicalPath();
            if (!baseFolder.equals(fullPath))
                relativePath = fullPath.substring(baseFolder.length() + 1);
            else originalDirectory = true;
        } catch (IOException e) {
            return null;
        } catch (Exception f) {
            originalDirectory = true;
            //continue
        }
        String as = PreferenceManager.getDefaultSharedPreferences(context).getString("URI", null);

        Uri treeUri = null;
        if (as != null) treeUri = Uri.parse(as);
        if (treeUri == null) {
            return null;
        }

        DocumentFile document = DocumentFile.fromTreeUri(context, treeUri);
        if (originalDirectory) return document;
        String[] parts = relativePath.split("\\/");
        for (int i = 0; i < parts.length; i++) {
            DocumentFile nextDocument = document.findFile(parts[i]);

            if (nextDocument == null) {
                if ((i < parts.length - 1) || isDirectory) {
                    nextDocument = document.createDirectory(parts[i]);
                } else {
                    nextDocument = document.createFile("image", parts[i]);
                }
            }
            document = nextDocument;
        }
        return document;
    }

    public static Uri getUriForFile(String path) {
        return getUriForFile(new File(path));
    }

    public static Uri getUriForFile(File file) {
        return FileProvider.getUriForFile(Contexter.getAppContext(), Contexter.getAppContext().getPackageName() + ".provider", file);
    }

    public static void shareFiles(Context c, SystemFile... a) {
        File[] filesToShare = new File[a.length];
        for (int i = 0; i < a.length; i++) {
            filesToShare[i] = a[i].file;
        }
        shareFiles(c, filesToShare);
    }


    public static void shareFiles(Context context, List<Item> selectedItems) {
        List<File> filesToShare = new ArrayList<>();
        for (Item selectedItem : selectedItems) {
            if (selectedItem instanceof SystemFile) {
                filesToShare.add(((SystemFile) selectedItem).file);
            }
        }
        shareFiles(context, filesToShare.toArray(new File[filesToShare.size()]));
    }

    public static void shareFiles(Context c, File... a) {
        ArrayList<Uri> uris = new ArrayList<>();
        boolean b = true;
        for (File f : a) {
            uris.add(getUriForFile(f));
        }
        String mime = MimeTypes.getMimeType(a[0]);
        if (mime == null) {
            mime = "*/*";
        }
        if (a.length > 1)
            for (File f : a) {
                if (!mime.equals(MimeTypes.getMimeType(f))) {
                    b = false;
                }
            }

        if (!b)
            mime = "*/*";
        try {
            new ShareTask(c, uris, ThemeEngine.theme, ThemeEngine.primaryDark).execute(mime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean copyFile(Context context, File source, File destination, boolean overwrite) {
        try {
            boolean res = FileUtils.copyFile(source, destination, overwrite);
            if (res) {
                MediaScannerConnection.scanFile(context, new String[]{destination.getPath()}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            @Override
                            public void onScanCompleted(String path, final Uri uri) {

                            }
                        });
            }
            return res;
        } catch (Exception e) {
            return false;
        }
    }

    public static ArrayList<SystemFile> listRecentFiles(Context context) {
        ArrayList<SystemFile> recents = new ArrayList<>();
        final String[] projection = {MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.DATE_MODIFIED};
        long d = System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(30, TimeUnit.DAYS);
        Cursor cursor = context.getContentResolver().query(MediaStore.Files
                        .getContentUri("external"), projection,
                null,
                null, null);
        if (cursor == null) return recents;
        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            do {
                String path = cursor.getString(cursor.getColumnIndex
                        (MediaStore.Files.FileColumns.DATA));
                File f = new File(path);
                if (d < f.lastModified() && !f.isDirectory()) {
                    recents.add(new SystemFile(f));
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        Collections.sort(recents, new Comparator<SystemFile>() {
            @Override
            public int compare(SystemFile lhs, SystemFile rhs) {
                return RangeUtils.compare(lhs.file.lastModified(), rhs.file.lastModified());

            }
        });
        if (recents.size() > 150)
            for (int i = recents.size() - 1; i > 150; i--) {
                recents.remove(i);
            }
        return recents;
    }

    public static class StorageItem {
        public String title;
        public String path;
        public int icon;
        public File file;
        public long totalSize;
        public long usedSize;

        public StorageItem(String storage) {
            path = storage;
            file = new File(storage);
            if ("/storage/emulated/legacy".equals(storage) || "/storage/emulated/0".equals(storage)) {
                title = Contexter.getString(R.string.storage);
                icon = R.drawable.ic_sd_storage_white_56dp;
            } else if ("/storage/sdcard1".equals(storage)) {
                title = Contexter.getString(R.string.extstorage);
                icon = R.drawable.ic_sd_storage_white_56dp;
            } else if ("/".equals(storage)) {
                title = Contexter.getString(R.string.rootdirectory);
                icon = R.drawable.ic_drawer_root_white;
            } else {
                title = Contexter.getString(R.string.storage);
                icon = R.drawable.ic_sd_storage_white_56dp;
            }
            StatFs statFs = new StatFs(storage);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                totalSize = statFs.getTotalBytes();
                usedSize = totalSize - statFs.getFreeBytes();
            } else {
                //noinspection deprecation
                totalSize = statFs.getBlockCount() * statFs.getBlockSize();
                //noinspection deprecation
                usedSize = totalSize - (statFs.getAvailableBlocks() * statFs.getBlockSize());
            }
        }
    }
}

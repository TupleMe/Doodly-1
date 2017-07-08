package tuple.me.lily.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import tuple.me.lily.core.Objects;

/**
 * Created by gokul.
 */
@SuppressWarnings({"UnusedDeclaration"})
public class FileUtils {

    final static Pattern DIR_SEPARATOR = Pattern.compile("/");
    private Context context;

    public FileUtils(@NonNull Context context) {
        this.context = context;
    }

    @NonNull
    public static String getReadableFileSize(double size) {
        if (size <= 0) return "0 Bytes";
        final String[] units = new String[]{"Bytes", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static long getDirSize(@Nullable File dir) {
        if (dir == null) {
            return 0;
        }
        long size = 0;
        if (dir.canRead()) {
            if (dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.canRead()) {
                            if (file.isDirectory()) {
                                size += getDirSize(file);
                            } else {
                                size += file.length();
                            }
                        }
                    }
                }
                return size;
            } else {
                return dir.length();
            }
        }
        return 0;
    }

    public static long[] getDirSizeWithItems(@Nullable File dir, long[] stats) {
        if (dir == null) {
            return stats;
        }
        long size = 0;
        if (dir.canRead()) {
            if (dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    stats[1] += files.length;
                    for (File file : files) {
                        if (file.canRead()) {
                            if (file.isDirectory()) {
                                getDirSizeWithItems(file, stats);
                            } else {
                                size += file.length();
                            }
                        }
                    }
                }
                stats[0] += size;
                return stats;
            } else {
                stats[0] += dir.length();
                return stats;
            }
        }
        return stats;
    }

    public static boolean writeToStream(@NonNull FileInputStream inputStream, @NonNull FileOutputStream outputStream) {
        boolean result = true;
        try {
            Objects.checkNotNull(inputStream);
            Objects.checkNotNull(outputStream);
            byte[] buffer = new byte[8128];
            int read;
            while ((read = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
            result = false;
        } finally {
            SafeClose.safeClose(outputStream);
            SafeClose.safeClose(inputStream);
        }
        return result;
    }


    public static boolean copyFile(@NonNull File source, @NonNull File destination, boolean overWrite) throws Exception {
        if (source.isDirectory()) {
            throw new IllegalArgumentException("Source location is directory");
        }
        if (destination.isDirectory()) {
            throw new IllegalArgumentException("Destination location is directory");
        }
        if (!source.canRead()) {
            throw new IllegalArgumentException("Can't read source file");
        }
        if (destination.exists() && !overWrite) {
            throw new IllegalStateException("File " + destination.getName() + " Already Exists");
        } else {
            if (destination.exists()) {
                writeToStream(new FileInputStream(source), new FileOutputStream(destination, false));
            }
            File parent = new File(destination.getParent());
            if (!parent.exists() && parent.mkdir()) {
                throw new IOException("Directory doesn't exists");
            }
            if (!destination.createNewFile()) {
                throw new IOException("Failed to create new file");
            }
            writeToStream(new FileInputStream(source), new FileOutputStream(destination, false));
        }
        return true;
    }

    @NonNull
    public static String getFileExtension(@NonNull File file) {
        if (file.isFile()) {
            String fileName = file.getName();
            int dotIndex = fileName.lastIndexOf('.');
            return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }

    public static boolean fileExist(@NonNull String path) {
        File imgFile = new File(path);
        return imgFile.exists();
    }

    public static boolean isPathValid(@Nullable String path) {
        return path != null && fileExist(path);
    }

    public static void deleteContents(File fileToDelete) throws IOException {
        if (fileToDelete.isFile()) {
            if (!fileToDelete.delete()) {
                throw new IOException("failed to delete file: " + fileToDelete);
            }
            return;
        }
        File[] files = fileToDelete.listFiles();
        if (files == null) {
            throw new IllegalArgumentException("not a directory: " + fileToDelete);
        }
        for (File file : files) {
            if (file.isDirectory()) {
                deleteContents(file);
            }
            if (!file.delete()) {
                throw new IOException("failed to delete file: " + file);
            }
        }
    }


    @Nullable
    public static File[] fileFilterWithExt(String dirName, final String ext) {
        File dir = new File(dirName);
        if (!dir.exists() || dir.isFile()) {
            return null;
        }
        return dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String filename) {
                return filename.endsWith(ext);
            }
        });
    }

    public static List<String> getStorageDirectories(Context context) {
        final ArrayList<String> rv = new ArrayList<>();
        final String rawExternalStorage = System.getenv("EXTERNAL_STORAGE");
        final String rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE");
        final String rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");
        if (TextUtils.isEmpty(rawEmulatedStorageTarget)) {
            if (TextUtils.isEmpty(rawExternalStorage)) {
                rv.add("/storage/sdcard0");
            } else {
                rv.add(rawExternalStorage);
            }
        } else {
            final String rawUserId;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                rawUserId = "";
            } else {
                final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                final String[] folders = DIR_SEPARATOR.split(path);
                final String lastFolder = folders[folders.length - 1];
                boolean isDigit = false;
                try {
                    Integer.valueOf(lastFolder);
                    isDigit = true;
                } catch (NumberFormatException ignored) {
                }
                rawUserId = isDigit ? lastFolder : "";
            }
            if (TextUtils.isEmpty(rawUserId)) {
                rv.add(rawEmulatedStorageTarget);
            } else {
                rv.add(rawEmulatedStorageTarget + File.separator + rawUserId);
            }
        }
        if (!TextUtils.isEmpty(rawSecondaryStoragesStr)) {
            final String[] rawSecondaryStorages = rawSecondaryStoragesStr.split(File.pathSeparator);
            Collections.addAll(rv, rawSecondaryStorages);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            rv.clear();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String strings[] = getExtSdCardPaths(context);
            for (String s : strings) {
                File f = new File(s);
                if (!rv.contains(s) && canListFiles(f))
                    rv.add(s);
            }
        }
        File usb = getUsbDrive();
        if (usb != null && !rv.contains(usb.getPath())) rv.add(usb.getPath());
        return rv;
    }

    public static File getUsbDrive() {
        File parent;
        parent = new File("/storage");

        try {
            for (File f : parent.listFiles()) {
                if (f.exists() && f.getName().toLowerCase().contains("usb") && f.canExecute()) {
                    return f;
                }
            }
        } catch (Exception ignored) {
        }
        parent = new File("/mnt/sdcard/usbStorage");
        if (parent.exists() && parent.canExecute())
            return (parent);
        parent = new File("/mnt/sdcard/usb_storage");
        if (parent.exists() && parent.canExecute())
            return parent;

        return null;
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String[] getExtSdCardPaths(Context context) {
        List<String> paths = new ArrayList<String>();
        for (File file : context.getExternalFilesDirs("external")) {
            if (file != null) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index < 0) {
                    Log.d("FileUtils", "Unexpected external file dir: " + file.getAbsolutePath());
                } else {
                    String path = file.getAbsolutePath().substring(0, index);
                    try {
                        path = new File(path).getCanonicalPath();
                    } catch (IOException e) {
                        // Keep non-canonical path.
                    }
                    paths.add(path);
                }
            }
        }
        if (paths.isEmpty()) {
            paths.add("/storage/sdcard1");
        }
        return paths.toArray(new String[paths.size()]);
    }

    public static boolean canListFiles(File f) {
        try {
            return f != null && f.canRead() && f.isDirectory();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isEmptyFile(File file) {
        return (file.length() == 0);
    }


    public static boolean deleteFilesInFolder(File folder) {
        boolean totalSuccess = true;
        if (folder == null)
            return false;
        if (folder.isDirectory()) {
            for (File child : folder.listFiles()) {
                deleteFilesInFolder(child);
            }

            if (!folder.delete())
                totalSuccess = false;
        } else {

            if (!folder.delete())
                totalSuccess = false;
        }
        return totalSuccess;
    }
}

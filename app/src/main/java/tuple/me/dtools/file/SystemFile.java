package tuple.me.dtools.file;

import android.support.annotation.NonNull;

import java.io.File;

import tuple.me.dtools.R;
import tuple.me.dtools.util.Icons;
import tuple.me.lily.util.RangeUtils;
import tuple.me.lily.model.Item;

public class SystemFile extends Item implements Comparable<SystemFile> {
    public File file;
    public long size;
    public long childItemsCount;
    public boolean isDir;
    public long lastModified;
    public int iconId;
    public String name;

    public SystemFile(String path) {
        this(new File(path));
    }

    public SystemFile(File file) {
        this.file = file;
        this.isDir = file.isDirectory();
        if (!this.isDir) {
            this.size = file.length();
            this.iconId = Icons.getIconId(file);
        }else {
            this.iconId = R.drawable.ic_doc_folder;
        }
        this.lastModified = file.lastModified();
        this.name = file.getName();

    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof SystemFile && ((SystemFile) obj).file.equals(this.file);
    }

    @Override
    public int compareTo(@NonNull SystemFile o) {
        return file.compareTo(o.file);
    }

    public static int compare(SystemFile file1, SystemFile file2, int sortType) {
        switch (sortType) {
            case SORT_SIZE:
                if (file1.isDir && file2.isDir) {
                    return file1.name.compareToIgnoreCase(file2.name);
                }
                if (file1.isDir) {
                    return -1;
                }
                if (file2.isDir) {
                    return 1;
                }
                return RangeUtils.compare(file1.size, file2.size);
            case SORT_NAME:
                if (file1.isDir && file2.isDir) {
                    return file1.name.compareToIgnoreCase(file2.name);
                }
                if (file1.isDir) {
                    return -1;
                }
                if (file2.isDir) {
                    return 1;
                }
                return file1.name.compareToIgnoreCase(file2.name);
            case SORT_DATE:
                return RangeUtils.compare(file1.lastModified,file2.lastModified);
        }
        return file1.compareTo(file2);
    }

    public static final int SORT_SIZE = 0;
    public static final int SORT_NAME = 1;
    public static final int SORT_DATE  = 2;
}

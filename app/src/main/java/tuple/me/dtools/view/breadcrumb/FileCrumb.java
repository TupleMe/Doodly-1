package tuple.me.dtools.view.breadcrumb;

import tuple.me.dtools.file.SystemFile;

/**
 * Created by gokul-4192 on 0011 11-Mar-17.
 */
public class FileCrumb extends BaseCrumb {

    public SystemFile systemFile;

    public FileCrumb(String title) {
        super(title);
    }

    public FileCrumb(SystemFile file) {
        super(file.file.getName());
        this.systemFile = file;
    }

    public String getTitle() {
        return systemFile.file.getPath().equals("/") ? "root" : systemFile.file.getName();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof FileCrumb && ((FileCrumb) obj).systemFile.equals(this.systemFile);
    }
}

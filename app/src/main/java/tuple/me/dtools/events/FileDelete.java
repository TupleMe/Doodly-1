package tuple.me.dtools.events;

import tuple.me.dtools.file.SystemFile;

/**
 * Created by gokul-4192 on 0015 15-Apr-17.
 */
public class FileDelete {
    public SystemFile file;

    public FileDelete(SystemFile file) {
        this.file = file;
    }
}

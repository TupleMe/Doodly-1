package tuple.me.dtools.file.dup;

import tuple.me.lily.model.Item;

public class DuplicateFileHeader extends Item {
    public String header;

    public DuplicateFileHeader(String header) {
        this.header = header;
    }
}

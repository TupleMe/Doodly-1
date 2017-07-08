package tuple.me.lily.views.changelog.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.LinkedList;

public class ChangeLog {

    private LinkedList<ChangeLogRow> rows;

    private boolean bulletedList;


    public ChangeLog() {

        rows = new LinkedList<>();
    }

    public void addRow(@Nullable ChangeLogRow row) {
        if (row != null) {
            if (rows == null) rows = new LinkedList<>();
            rows.add(row);
        }
    }

    public void clearAllRows() {
        rows = new LinkedList<>();
    }


    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("bulletedList=").append(bulletedList);
        sb.append("\n");
        if (rows != null) {
            for (ChangeLogRow row : rows) {
                sb.append("row=[");
                sb.append(row.toString());
                sb.append("]\n");
            }
        } else {
            sb.append("rows:none");
        }
        return sb.toString();
    }

    //-----------------------------------------------------------------------

    public boolean isBulletedList() {
        return bulletedList;
    }

    public void setBulletedList(boolean bulletedList) {
        this.bulletedList = bulletedList;
    }

    public LinkedList<ChangeLogRow> getRows() {
        return rows;
    }

    public void setRows(LinkedList<ChangeLogRow> rows) {
        this.rows = rows;
    }
}

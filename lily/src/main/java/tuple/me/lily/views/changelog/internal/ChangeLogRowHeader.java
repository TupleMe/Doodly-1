package tuple.me.lily.views.changelog.internal;

import android.support.annotation.NonNull;

public class ChangeLogRowHeader extends ChangeLogRow{


    public ChangeLogRowHeader(){
        super();
        setHeader(true);
        setBulletedList(false);
        setChangeTextTitle(null);
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb=new StringBuilder();
        sb.append("header="+super.header);
        sb.append(",");
        sb.append("versionName="+versionName);
        sb.append(",");
        sb.append("changeDate="+changeDate);
        return sb.toString();
    }

}

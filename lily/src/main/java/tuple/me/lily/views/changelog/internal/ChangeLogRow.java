package tuple.me.lily.views.changelog.internal;

import android.content.Context;
import android.support.annotation.Nullable;

import tuple.me.lily.R;


public class ChangeLogRow {

    public static final int DEFAULT = 0;

    public static final int BUGFIX = 1;

    public static final int IMPROVEMENT = 2;

    protected boolean header;

    protected String versionName;

    protected int versionCode;

    protected String changeDate;

    private boolean bulletedList;

    private String changeTextTitle;

    private String changeText;

    private int type;

    public void parseChangeText(@Nullable String changeLogText) {
        if (changeLogText != null) {
            changeLogText = changeLogText.replaceAll("\\[", "<").replaceAll("\\]", ">");
        }
        setChangeText(changeLogText);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("header=" + header);
        sb.append(",");
        sb.append("versionName=" + versionName);
        sb.append(",");
        sb.append("versionCode=" + versionCode);
        sb.append(",");
        sb.append("bulletedList=" + bulletedList);
        sb.append(",");
        sb.append("changeText=" + changeText);
        return sb.toString();
    }


    public boolean isHeader() {
        return header;
    }

    public void setHeader(boolean header) {
        this.header = header;
    }

    public boolean isBulletedList() {
        return bulletedList;
    }

    public void setBulletedList(boolean bulletedList) {
        this.bulletedList = bulletedList;
    }

    public String getChangeText() {
        return changeText;
    }

    public String getChangeText(@Nullable Context context) {
        if (context == null)
            return getChangeText();

        String prefix = "";
        switch (type) {
            case BUGFIX:
                prefix = context.getResources().getString(R.string.changelog_row_prefix_bug);
                prefix = prefix.replaceAll("\\[", "<").replaceAll("\\]", ">");
                break;
            case IMPROVEMENT:
                prefix = context.getResources().getString(R.string.changelog_row_prefix_improvement);
                prefix = prefix.replaceAll("\\[", "<").replaceAll("\\]", ">");
                break;
        }
        return prefix + " " + changeText;
    }

    public void setChangeText(String changeText) {
        this.changeText = changeText;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    /**
     * @deprecated use custom tags as changelogbug or changelogimprovement
     */
    public String getChangeTextTitle() {
        return changeTextTitle;
    }

    public void setChangeTextTitle(String changeTextTitle) {
        this.changeTextTitle = changeTextTitle;
    }

    public String getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(String changeDate) {
        this.changeDate = changeDate;
    }

    public void setType(int type) {
        this.type = type;
    }

}

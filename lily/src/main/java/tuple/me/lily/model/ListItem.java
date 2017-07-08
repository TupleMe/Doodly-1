package tuple.me.lily.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

/**
 * Created by gokul-4192 on 0024 24-Dec-16.
 */
public class ListItem extends Item implements Parcelable {

    public int title;

    public ListItem(@StringRes int title) {
        this.title = title;
    }

    protected ListItem(@NonNull Parcel in) {
        title = in.readInt();
    }

    public static final Creator<ListItem> CREATOR = new Creator<ListItem>() {
        @NonNull
        @Override
        public ListItem createFromParcel(@NonNull Parcel in) {
            return new ListItem(in);
        }

        @NonNull
        @Override
        public ListItem[] newArray(int size) {
            return new ListItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(title);
    }
}

package tuple.me.lily.model;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import tuple.me.lily.Contexter;

/**
 * Created by gokul-4192 on 0007 07-Oct-16.
 */
public class IconListItem extends ListItem implements Parcelable{
    public int drawable;

    public IconListItem(@StringRes int title, @DrawableRes int drawable) {
        super(title);
        this.drawable = drawable;
    }

    protected IconListItem(@NonNull Parcel in) {
        super(in);
        drawable = in.readInt();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(drawable);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<IconListItem> CREATOR = new Creator<IconListItem>() {
        @NonNull
        @Override
        public IconListItem createFromParcel(@NonNull Parcel in) {
            return new IconListItem(in);
        }

        @NonNull
        @Override
        public IconListItem[] newArray(int size) {
            return new IconListItem[size];
        }
    };

    public Drawable getDrawable(){
        return Contexter.getDrawable(this.drawable);
    }
}

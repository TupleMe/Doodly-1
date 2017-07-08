package tuple.me.dtools.constants;

import java.util.ArrayList;

import tuple.me.dtools.R;
import tuple.me.lily.model.IconListItem;
import tuple.me.lily.model.Item;
import tuple.me.lily.model.ListItem;

/**
 * Created by gokul-4192 on 0019 19-Feb-17.
 */
public class Constants {


    public static final String APP_NAME = "Doodly";

    public static ArrayList<Item> getNavModel() {
        ArrayList<Item> model = new ArrayList<>();
        model.add(new IconListItem(R.string.over_view, R.drawable.ic_check));
        model.add(new ListItem(R.string.storage));
        model.add(new IconListItem(R.string.file_manager, R.drawable.ic_doc_folder));
        model.add(new IconListItem(R.string.duplicate_files, R.drawable.ic_duplicate));
        model.add(new IconListItem(R.string.storage_analyzer, R.drawable.ic_pie_chart));
        model.add(new IconListItem(R.string.large_files, R.drawable.ic_folder_remove));
        model.add(new IconListItem(R.string.empty_files_folders, R.drawable.ic_basket_fill));
        model.add(new ListItem(R.string.app));
        model.add(new IconListItem(R.string.apk_manager, R.drawable.ic_android_mod));
        model.add(new IconListItem(R.string.permissions, R.drawable.ic_alert_circle));
        model.add(new IconListItem(R.string.cache_cleaner, R.drawable.ic_android_mod));
        model.add(new ListItem(R.string.others));
        model.add(new IconListItem(R.string.clipboard_manager, R.drawable.ic_clipboard));
        model.add(new IconListItem(R.string.screen_filter, R.drawable.ic_eye));
        model.add(new IconListItem(R.string.qr_scanner, R.drawable.ic_qrcode));
        model.add(new IconListItem(R.string.screen_recorder, R.drawable.ic_camcorder));
        model.add(new IconListItem(R.string.about, R.drawable.ic_account_circle));
        return model;
    }
}

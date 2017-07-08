package tuple.me.dtools.sugarmodel.clipboard;

import android.support.annotation.NonNull;

import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import tuple.me.lily.core.StringUtil;
import tuple.me.lily.util.RangeUtils;

public class ClipboardItem extends SugarRecord implements Comparable<ClipboardItem> {
    public long time;
    public String text;

    public ClipboardItem() {

    }

    public ClipboardItem(long time, String text) {
        this.time = time;
        this.text = text;
    }

    public static ArrayList<ClipboardItem> getAll() {
        ArrayList<ClipboardItem> list = new ArrayList<>(listAll(ClipboardItem.class));
        Collections.sort(list, new Comparator<ClipboardItem>() {
            @Override
            public int compare(ClipboardItem o1, ClipboardItem o2) {
                long x = o2.time;
                long y = o1.time;
                return (x < y) ? -1 : ((x == y) ? 0 : 1);
            }
        });
        return list;
    }


    public static ArrayList<ClipboardItem> getAll(String query) {
        if (StringUtil.isEmpty(query)) {
            return getAll();
        } else {
            return (ArrayList<ClipboardItem>) Select.from(ClipboardItem.class).where(Condition.prop("text").like("%"+query+"%")).list();
        }
    }

    @Override
    public int compareTo(@NonNull ClipboardItem o) {
        return RangeUtils.compare(time, o.time);
    }
}

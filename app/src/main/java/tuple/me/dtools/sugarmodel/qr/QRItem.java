package tuple.me.dtools.sugarmodel.qr;

import com.orm.SugarRecord;

/**
 * Created by gokul-4192 on 0031 31-Dec-16.
 */
public class QRItem extends SugarRecord{
    public long time;
    public String text;
    public QRItem(){

    }
    public QRItem(long time, String text) {
        this.time = time;
        this.text = text;
    }
}

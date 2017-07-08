package tuple.me.lily.views.changelog.parser;
import android.content.Context;
import android.support.annotation.Nullable;
import tuple.me.lily.views.changelog.internal.ChangeLog;
public abstract class BaseParser {

    protected Context mContext;
    protected boolean bulletedList;

    public BaseParser(Context context){
        this.mContext=context;
    }
    @Nullable
    public abstract ChangeLog readChangeLogFile() throws Exception;
}

package tuple.me.lily.views;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Created by gokul-4192 on 0024 24-Dec-16.
 */

public abstract class AbsTextWatcher implements TextWatcher {

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    abstract public void afterTextChanged(Editable s);
}

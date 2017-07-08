package tuple.me.lily.adapters;

import android.support.annotation.MenuRes;

import com.afollestad.materialcab.MaterialCab;

import tuple.me.lily.adapters.core.OnMultiSelectChangeListener;
import tuple.me.lily.adapters.core.SelectionHolder;

/**
 * Created by gokul-4192 on 0026 26-Feb-17.
 */
public class MultiSelectCabListener implements OnMultiSelectChangeListener {

    MaterialCab.Callback callback;
    int menu;
    MaterialCab cab;

    public MultiSelectCabListener(NavHandler cabProvider, MaterialCab.Callback callback, @MenuRes int menu) {
        this.callback = callback;
        this.menu = menu;
        if (cabProvider != null) {
            cab = cabProvider.getCab();
        }
    }

    @Override
    public void onMultiSelectChange(boolean isMultiSelectOn) {
        if (cab != null) {
            if (isMultiSelectOn) {
                cab.setMenu(menu);
                if (!cab.isActive())
                    cab.start(callback);
            } else {
                if (cab.isActive())
                    cab.finish();
            }
        }
    }

    @Override
    public void onItemCountChange(int count) {
        if (cab != null) {
            cab.setTitle(count + "");
        }
    }

    @Override
    public void onSelectionChange(SelectionHolder selectionHolder) {

    }
}

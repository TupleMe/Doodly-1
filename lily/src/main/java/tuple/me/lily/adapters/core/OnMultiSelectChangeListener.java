package tuple.me.lily.adapters.core;

/**
 * Created by gokul-4192 on 0016 16-May-17.
 */
public interface OnMultiSelectChangeListener extends SelectionHolder.onSelectionChangeListener {
    void onMultiSelectChange(boolean isMultiSelectOn);
    //TODO: Remove method
    void onItemCountChange(int count);
}

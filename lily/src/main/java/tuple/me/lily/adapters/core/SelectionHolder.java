package tuple.me.lily.adapters.core;

import android.support.v7.util.SortedList;
import android.util.SparseBooleanArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import tuple.me.lily.core.Objects;

/**
 * Created by gokul-4192 on 0015 15-May-17.
 */

@SuppressWarnings({"UnusedDeclaration"})
public class SelectionHolder {
    private SparseBooleanArray checkedItems;
    private AtomicInteger checkedCount;
    private boolean isInitialized = false;
    private boolean throwIfNotInitialized;
    private onSelectionChangeListener listener;

    public SelectionHolder(boolean throwIfNotInitialized) {
        this.throwIfNotInitialized = throwIfNotInitialized;
    }

    public SelectionHolder init() {
        if(!isInitialized||checkedCount.get()!=0){
            checkedItems = new SparseBooleanArray();
            checkedCount = new AtomicInteger();
        }
        isInitialized = true;
        return this;
    }

    public SelectionHolder setSelectionChangeListener(onSelectionChangeListener listener) {
        this.listener = listener;
        return this;
    }

    public boolean resetSelection() {
        if (isInitialized) {
            checkedItems = new SparseBooleanArray();
            checkedCount.set(0);
            return true;
        } else {
            Objects.assertVal(!throwIfNotInitialized, "Not initialised");
        }
        return false;
    }

    public boolean selectAll(int size) {
        if (isInitialized) {
            boolean isModified = false;
            for (int i = 0; i < size; i++) {
                if (!isSelected(i)) {
                    checkedItems.put(i, true);
                    checkedCount.getAndIncrement();
                    isModified = true;
                }
            }
            return isModified;
        } else {
            Objects.assertVal(!throwIfNotInitialized, "Not initialised");
        }
        return false;
    }

    public int getSelectedItemsCount() {
        if (isInitialized) {
            return checkedCount.get();
        } else {
            Objects.assertVal(!throwIfNotInitialized, "Not initialised");
        }
        return 0;
    }

    public boolean isSelected(int position) {
        if (isInitialized) {
            return checkedItems.get(position);
        } else {
            Objects.assertVal(!throwIfNotInitialized, "Not initialised");
        }
        return false;
    }

    public boolean selectItem(int position) {
        if (isInitialized) {
            if (!isSelected(position)) {
                checkedCount.getAndIncrement();
                checkedItems.put(position, true);
                return true;
            }
        } else {
            Objects.assertVal(!throwIfNotInitialized, "Not initialised");
        }
        return false;
    }

    public boolean toggleSelection(int position) {
        if (isInitialized) {
            if (isSelected(position)) {
                checkedItems.put(position, false);
                checkedCount.getAndDecrement();
            } else {
                checkedItems.put(position, true);
                checkedCount.getAndIncrement();
            }
            return true;
        } else {
            Objects.assertVal(!throwIfNotInitialized, "Not initialised");
        }
        return false;
    }

    public boolean isEmpty() {
        return isInitialized && checkedCount.intValue() == 0;
    }

    public <E> List<E> filterSelected(List<E> listToFilter) {
        if (!isEmpty()) {
            List<E> result = new ArrayList<>(checkedCount.get());
            for (int itr = 0, size = listToFilter.size(); itr < size; itr++) {
                if(isSelected(itr)){
                    result.add(listToFilter.get(itr));
                }
            }
            return result;
        } else {
            return Collections.emptyList();
        }
    }

    public <E> List<E> filterSelected(SortedList<E> listToFilter) {
        if (!isEmpty()) {
            List<E> result = new ArrayList<>(checkedCount.get());
            for (int itr = 0, size = listToFilter.size(); itr < size; itr++) {
                if(isSelected(itr)){
                    result.add(listToFilter.get(itr));
                }
            }
            return result;
        } else {
            return Collections.emptyList();
        }
    }

    public interface onSelectionChangeListener {
        void onSelectionChange(SelectionHolder selectionHolder);
    }
}

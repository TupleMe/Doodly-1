package tuple.me.dtools.file.empty;

import android.content.Context;
import android.support.v7.util.SortedList;
import android.support.v7.widget.util.SortedListAdapterCallback;

import java.util.List;

import tuple.me.dtools.file.BaseFilesAdapter;
import tuple.me.dtools.file.SystemFile;
import tuple.me.lily.model.Item;

public class EmptyFilesAdapter extends BaseFilesAdapter {

    public EmptyFilesAdapter(Context context) {
        super(context);
        dataSet = new SortedList<>(Item.class, new SortedListAdapterCallback<Item>(this) {
            @Override
            public int compare(Item o1, Item o2) {
                if (o1 instanceof SystemFile && o2 instanceof SystemFile) {
                    return SystemFile.compare((SystemFile) o1, (SystemFile) o2, SystemFile.SORT_NAME);
                }
                return o1.hashCode() - o2.hashCode();
            }

            @Override
            public boolean areContentsTheSame(Item oldItem, Item newItem) {
                return oldItem.equals(newItem);
            }

            @Override
            public boolean areItemsTheSame(Item item1, Item item2) {
                return item1.equals(item2);
            }
        });
    }

    public void addAll(List<SystemFile> files) {
        dataSet.beginBatchedUpdates();
        for (SystemFile file : files) {
            dataSet.add(file);
        }
        dataSet.endBatchedUpdates();
    }
}

package tuple.me.lily.adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import tuple.me.lily.adapters.core.OnItemClickListener;
import tuple.me.lily.adapters.core.OnItemLongClickLister;
import tuple.me.lily.adapters.core.OnMultiSelectChangeListener;
import tuple.me.lily.adapters.core.SelectionHolder;


@SuppressWarnings({"UnusedDeclaration"})
public abstract class BaseSortedRecyclerAdapter<VH extends RecyclerView.ViewHolder, M> extends RecyclerView.Adapter<VH> {

    public Context context;
    private SelectionHolder selectionHolder = new SelectionHolder(false);
    public SortedList<M> dataSet;
    private OnItemClickListener<M> itemClickListener;
    private OnItemLongClickLister<M> itemLongClickListener;
    private OnMultiSelectChangeListener multiSelectChangeListener;
    private boolean isMultiSelectList = false;
    private boolean isDisabled = false;

    public BaseSortedRecyclerAdapter(Context context) {
        this.context = context;
    }


    public BaseSortedRecyclerAdapter(Context context, SortedList<M> dataSet) {
        this.context = context;
        this.dataSet = dataSet;
    }


    @Override
    abstract public VH onCreateViewHolder(ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(final VH holder, final int position) {
        onBindViewHolder(holder, position, true);
    }


    public void onBindViewHolder(final VH holder, int position, final boolean isClickable) {
        if (isClickable) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isDisabled) {
                        int adapterPosition = holder.getAdapterPosition();
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            if (isMultiSelectList && isInMultiSelectMode()) {
                                onLongClickRow(adapterPosition, false);
                            } else {
                                onClickRow(adapterPosition);
                                if (itemClickListener != null)
                                    itemClickListener.onItemClick(dataSet.get(holder.getAdapterPosition()));
                            }
                        }
                    }
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!isDisabled) {
                        int adapterPosition = holder.getAdapterPosition();
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            onLongClickRow(adapterPosition, true);
                            if (itemLongClickListener != null)
                                itemLongClickListener.onItemLongClick(dataSet.get(holder.getAdapterPosition()));
                        }
                    }
                    return true;
                }
            });
        }

        if (isDisabled) {
            holder.itemView.setClickable(false);
        }
    }

    public void setDataSet(SortedList<M> dataSet) {
        this.dataSet = dataSet;
        resetMultiSelect();
        notifyDataSetChanged();
        notifyMenu();
    }

    //multi select
    protected void notifyMenu() {
        if (multiSelectChangeListener != null) {
            multiSelectChangeListener.onItemCountChange(selectionHolder.getSelectedItemsCount());
            multiSelectChangeListener.onMultiSelectChange(!selectionHolder.isEmpty());
        }
    }

    public void setChecked(int position) {
        if (selectionHolder.selectItem(position)) {
            notifyItemChanged(position);
            notifyMenu();
        }
    }

    public List<M> getSelectedItems() {
        return selectionHolder.filterSelected(dataSet);
    }

    public boolean isChecked(int position) {
        return selectionHolder.isSelected(position);
    }


    public void toggleChecked(int position) {
        if (selectionHolder.toggleSelection(position)) {
            notifyItemChanged(position);
            notifyMenu();
        }
    }

    public boolean isInMultiSelectMode() {
        return !selectionHolder.isEmpty();
    }

    public void setMultiSelectMode(boolean isMultiSelectList) {
        this.isMultiSelectList = isMultiSelectList;
        resetMultiSelect();
    }

    public void resetMultiSelect() {
        if (isMultiSelectList) {
            selectionHolder.init();
            notifyDataSetChanged();
        }
    }


    public void selectAll() {
        if (isMultiSelectList) {
            int size = dataSet.size();
            selectionHolder.selectAll(size);
            notifyDataSetChanged();
            notifyMenu();
        }
    }


    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public void onLongClickRow(int position, boolean isFromLongClick) {
        if (isMultiSelectList) {
            toggleChecked(position);
        }
    }

    public void onClickRow(int position) {
    }


    public void add(M object) {
        dataSet.add(object);
        resetMultiSelect();
    }

    public void addAll(@Nullable Collection<M> collection) {
        if (collection != null) {
            int oldSize = dataSet.size();
            dataSet.addAll(collection);
            resetMultiSelect();
            notifyItemRangeInserted(oldSize + 1, dataSet.size());
        }
    }

    @SafeVarargs
    public final void addAll(M... items) {
        addAll(Arrays.asList(items));
    }

    public boolean isEmpty() {
        return dataSet == null || dataSet.size() == 0;
    }

    public void clear() {
        dataSet.clear();
        notifyDataSetChanged();
        notifyMenu();
    }

    public void remove(M object) {
        dataSet.remove(object);
        notifyDataSetChanged();
    }

    public void resetDataSet(SortedList<M> dataSet) {
        this.dataSet = dataSet;
        resetMultiSelect();
        notifyDataSetChanged();
    }

    public M getItem(int position) {
        return dataSet.get(position);
    }

    //listener setters
    public void setOnClickListener(OnItemClickListener<M> listener) {
        this.itemClickListener = listener;
    }

    public void setOnLongClickListener(OnItemLongClickLister<M> listener) {
        this.itemLongClickListener = listener;
    }

    public void setMultiSelectChangeListener(OnMultiSelectChangeListener multiSelectChangeListener) {
        setMultiSelectMode(true);
        this.multiSelectChangeListener = multiSelectChangeListener;
    }
    public void setDisabled(boolean isDisabled) {
        this.isDisabled = isDisabled;
    }
}

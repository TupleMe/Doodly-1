package tuple.me.lily.util;

import android.support.annotation.NonNull;

import java.util.ArrayList;


@SuppressWarnings({"UnusedDeclaration"})
public class BackButton {

    @NonNull
    private ArrayList<OnBackClickListener> listeners = new ArrayList<>();

    public BackButton() {

    }

    public void addBackClickListener(OnBackClickListener listener) {
        if (getIndex(listener) == -1) {
            listeners.add(listener);
        }
    }

    public void removeBackClickListener(OnBackClickListener listener) {
        int index = getIndex(listener);
        if (index != -1) {
            listeners.remove(index);
        }
    }

    public int getIndex(OnBackClickListener listener) {
        int index = -1;
        for (int itr = 0; itr < listeners.size(); itr++) {
            if (listeners.get(itr) == listener) {
                index = itr;
                break;
            }
        }
        return index;
    }

    public interface OnBackClickListener {
        boolean onClick();
    }

    public interface BackButtonHandler {
        BackButton getBackButton();
    }

    public boolean isClickObserved() {
        for (int itr = 0; itr < listeners.size(); itr++) {
            OnBackClickListener listener = listeners.get(itr);
            if (listener != null && listener.onClick()) {
                return true;
            }
        }
        return false;
    }
}

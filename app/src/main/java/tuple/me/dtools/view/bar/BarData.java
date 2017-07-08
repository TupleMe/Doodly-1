package tuple.me.dtools.view.bar;

import android.support.annotation.ColorRes;

import java.util.ArrayList;
import java.util.List;

import tuple.me.lily.Contexter;

public class BarData {
    public float totalCap;
    List<Bar> bars = new ArrayList<>();

    public BarData(float totalCap) {
        this.totalCap = totalCap;
    }

    public BarData add(Bar bar) {
        bars.add(bar);
        return this;
    }

    public static class Bar {
        public float cap;
        public int color;

        public Bar(float cap) {
            this.cap = cap;
        }

        public Bar setColorResource(@ColorRes int color) {
            this.color = Contexter.getColor(color);
            return this;
        }

        public Bar setColor(int color) {
            this.color = color;
            return this;
        }
    }
}
package tuple.me.lily.adapters;


import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;
import java.util.List;

import tuple.me.lily.Contexter;

/**
 * Created by goku on 19/5/16.
 */
@SuppressWarnings({"UnusedDeclaration"})
public class CustomPagerAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener{
    private List<String> titles;
    private List<Fragment> fragments;

    public CustomPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
        fragments = new ArrayList<>(3);
        titles = new ArrayList<>(3);
    }

    public CustomPagerAdapter(FragmentManager fm, @IntRange(from = 1) int count) {
        super(fm);
        fragments = new ArrayList<>(count);
        titles = new ArrayList<>(count);
    }

    @Override
    public int getCount() {
        return titles.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public void addFragment(String title, Fragment fragment) {
        titles.add(title);
        fragments.add(fragment);
    }


    public void addFragment(@StringRes int title, Fragment fragment) {
        titles.add(Contexter.getString(title));
        fragments.add(fragment);
    }


    public void removeAll(@NonNull FragmentManager fm) {
        for (int i = 0; i < fragments.size(); i++) {
            titles.remove(i);
            fm.beginTransaction().remove(fragments.get(i)).commit();
            fragments.remove(i);
        }
    }

    @Nullable
    public Fragment getFragment(int position) {
        if (fragments != null && fragments.size() > position) {
            return fragments.get(position);
        }
        return null;
    }

    public void setVisibleFragment(int position) {
        if (fragments != null && fragments.size() > position) {
            for (int i = 0; i < fragments.size(); i++) {
                if (position != i)
                    fragments.get(i).setUserVisibleHint(false);
                else
                    fragments.get(i).setUserVisibleHint(true);
            }
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        setVisibleFragment(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}

package tuple.me.dtools.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tuple.me.dtools.R;
import tuple.me.lily.adapters.CustomPagerAdapter;

public abstract class SimpleViewPagerFragment extends Fragment implements ViewPager.OnPageChangeListener {


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.view_pager_fragment, container, false);
        ViewPager viewPager = (ViewPager) rootView.findViewById(R.id.pager);
        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.tabs);
        CustomPagerAdapter adapter = new CustomPagerAdapter(getChildFragmentManager());
        init();
        setTabs(adapter);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(adapter);
        viewPager.addOnPageChangeListener(this);
        tabLayout.setupWithViewPager(viewPager);
        return rootView;
    }

    protected abstract void init();

    public abstract void setTabs(CustomPagerAdapter adapter);

    public abstract void onFragmentSelected(int position);

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        onFragmentSelected(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}

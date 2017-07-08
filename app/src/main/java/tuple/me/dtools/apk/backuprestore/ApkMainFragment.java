package tuple.me.dtools.apk.backuprestore;

import tuple.me.dtools.R;
import tuple.me.dtools.activity.MainActivity;
import tuple.me.dtools.base.SimpleViewPagerFragment;
import tuple.me.lily.adapters.CustomPagerAdapter;

public class ApkMainFragment extends SimpleViewPagerFragment {
    @Override
    protected void init() {

    }

    @Override
    public void setTabs(CustomPagerAdapter adapter) {
        adapter.addFragment(R.string.back_up, new ApkBackUpFragment());
        adapter.addFragment(R.string.restore, new ApkRestoreFragment());
    }

    @Override
    public void onFragmentSelected(int position) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).getCab().finish();
        }
    }
}

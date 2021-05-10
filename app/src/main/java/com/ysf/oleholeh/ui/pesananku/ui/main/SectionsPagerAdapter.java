package com.ysf.oleholeh.ui.pesananku.ui.main;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.ysf.oleholeh.R;

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_bayar, R.string.tab_proses, R.string.tab_kirim, R.string.tab_terima, R.string.tab_selesai};
    private final Context mContext;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position){
            case 0:
                fragment = new BelumBayarFragment();
                break;
            case 1:
                fragment = new DiProsesFragment();
                break;
            case 2:
                fragment = new DiKirimFragment();
                break;
            case 3:
                fragment = new DiTerimaFragment();
                break;
            case 4:
                fragment = new SelesaiFragment();
                break;
        }
        return fragment;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        return 5;
    }
}
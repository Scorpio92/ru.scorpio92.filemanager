package ru.scorpio92.filemanager.Main.Adapters;

/**
 * Created by scorpio92 on 17.04.16.
 */
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import ru.scorpio92.filemanager.Main.UI.AboutTab;
import ru.scorpio92.filemanager.Main.UI.CapabilitiesTab;


public class AboutTabPagerAdapter extends FragmentStatePagerAdapter {
    public AboutTabPagerAdapter(FragmentManager fm) {
        super(fm);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                return new AboutTab();
            case 1:
                return new CapabilitiesTab();
        }
        return null;

    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return 2; //No of Tabs
    }

}
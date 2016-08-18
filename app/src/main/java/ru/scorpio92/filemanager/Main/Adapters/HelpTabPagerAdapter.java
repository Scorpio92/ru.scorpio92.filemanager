package ru.scorpio92.filemanager.Main.Adapters;

/**
 * Created by scorpio92 on 17.04.16.
 */
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import ru.scorpio92.filemanager.Main.UI.AboutTab;
import ru.scorpio92.filemanager.Main.UI.CapabilitiesTab;
import ru.scorpio92.filemanager.Main.UI.Intro.Help0;
import ru.scorpio92.filemanager.Main.UI.Intro.Help1;
import ru.scorpio92.filemanager.Main.UI.Intro.Help2;
import ru.scorpio92.filemanager.Main.UI.Intro.Help3;
import ru.scorpio92.filemanager.Main.UI.Intro.Help4;
import ru.scorpio92.filemanager.Main.UI.Intro.Help5;
import ru.scorpio92.filemanager.Main.UI.Intro.Help6;
import ru.scorpio92.filemanager.Main.UI.Intro.Help7;
import ru.scorpio92.filemanager.Main.UI.Intro.Help8;
import ru.scorpio92.filemanager.Main.UI.Intro.Help9;


public class HelpTabPagerAdapter extends FragmentStatePagerAdapter {
    public HelpTabPagerAdapter(FragmentManager fm) {
        super(fm);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                return new Help0();
            case 1:
                return new Help1();
            case 2:
                return new Help2();
            case 3:
                return new Help3();
            case 4:
                return new Help4();
            case 5:
                return new Help5();
            case 6:
                return new Help6();
            case 7:
                return new Help7();
            case 8:
                return new Help8();
            case 9:
                return new Help9();
        }
        return null;

    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return 10; //No of Tabs
    }

}
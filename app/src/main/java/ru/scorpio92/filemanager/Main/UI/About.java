package ru.scorpio92.filemanager.Main.UI;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;

import ru.scorpio92.filemanager.R;
import ru.scorpio92.filemanager.Main.Adapters.AboutTabPagerAdapter;


public class About extends FragmentActivity {

    private ActionBar actionBar;
    private ViewPager vp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        AboutTabPagerAdapter tabAdapter = new AboutTabPagerAdapter(getSupportFragmentManager());
        vp = (ViewPager) findViewById(R.id.pager);
        vp.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {

                        actionBar = getActionBar();
                        actionBar.setSelectedNavigationItem(position);
                    }
                });

        actionBar = getActionBar();
        //Enable Tabs on Action Bar
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ActionBar.TabListener tabListener = new ActionBar.TabListener(){

            @Override
            public void onTabReselected(android.app.ActionBar.Tab tab,
                                        FragmentTransaction ft) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                vp.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(android.app.ActionBar.Tab tab,
                                        FragmentTransaction ft) {
                // TODO Auto-generated method stub

            }};
        vp.setAdapter(tabAdapter);

        actionBar.addTab(actionBar.newTab().setText(getString(R.string.AboutTab)).setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText(getString(R.string.CapabilitiesTab)).setTabListener(tabListener));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        setResult(RESULT_OK, null);
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        setResult(RESULT_OK, null);
        super.onDestroy();
    }
}

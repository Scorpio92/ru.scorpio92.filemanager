package ru.scorpio92.filemanager.Main.UI.Intro;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;

import ru.scorpio92.filemanager.Main.Adapters.HelpTabPagerAdapter;
import ru.scorpio92.filemanager.Main.UI.MainUI;
import ru.scorpio92.filemanager.Main.Utils.SecondUsageUtils;
import ru.scorpio92.filemanager.Main.Variables.Constants;
import ru.scorpio92.filemanager.Main.Variables.VarStore;
import ru.scorpio92.filemanager.R;

/**
 * Created by scorpio92 on 13.08.16.
 */
public class Help extends FragmentActivity {

    private ActionBar actionBar;
    private ViewPager vp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);

        /*try {
            getActionBar().hide();
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        HelpTabPagerAdapter tabAdapter = new HelpTabPagerAdapter(getSupportFragmentManager());
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

        actionBar.addTab(actionBar.newTab().setText(getString(R.string.help0)).setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText(getString(R.string.help1)).setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText(getString(R.string.help2)).setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText(getString(R.string.help3)).setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText(getString(R.string.help4)).setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText(getString(R.string.help5)).setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText(getString(R.string.help6)).setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText(getString(R.string.help7)).setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText(getString(R.string.help8)).setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText(getString(R.string.help9)).setTabListener(tabListener));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    protected void onDestroy() {
        //Log.w("Help", "onDestroy");
        if(SecondUsageUtils.checkFirstRun()) {
            startActivityForResult(new Intent(this, MainUI.class), Constants.MAINUI_ACTIVITY_EXIT_CODE);
        }
        SecondUsageUtils.setRepeatedStart();
        super.onDestroy();
    }
}
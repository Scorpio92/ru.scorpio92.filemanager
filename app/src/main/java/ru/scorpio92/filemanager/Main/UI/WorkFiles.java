package ru.scorpio92.filemanager.Main.UI;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import ru.scorpio92.filemanager.R;
import ru.scorpio92.filemanager.Main.Adapters.FileListAdapter;
import ru.scorpio92.filemanager.Main.Adapters.WorkFilesTabPagerAdapter;
import ru.scorpio92.filemanager.Main.Utils.FileUtils;
import ru.scorpio92.filemanager.Main.Variables.Constants;
import ru.scorpio92.filemanager.Main.Variables.VarStore;

/**
 * Created by scorpio92 on 21.07.16.
 */
public class WorkFiles extends FragmentActivity {
    private ViewPager vp;
    private WorkFilesTabPagerAdapter TabAdapter;
    private ActionBar actionBar;
    private ActionBar.TabListener tabListener;

    public static FileListAdapter fla0 = null; //адаптер первой вкладки. устанавливается при создании фрагмента
    public static FileListAdapter fla1 = null; //адаптер второй вкладки. устанавливается при создании фрагмента
    private static int selectedItem=0; //номер текущей(выбранной) вкладки
    private int mode = WorkFilesTabPagerAdapter.ARCH_MODE; //текущий режим просмотра: Архивы по умолчанию
    private String old_dir; //директория из котороый была вызвана активити и которой нужно будет вернуться после

    private ViewPager.SimpleOnPageChangeListener tabSwipeListener; //слушатель свайпа вкладок

    private boolean finish = false;


    private VarStore getVarStore() { return (VarStore) VarStore.getAppContext(); }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //прогресс бар в ActionBar
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.work_files);

        old_dir =  getVarStore().getCurrentDir().getPath();

        TabAdapter = new WorkFilesTabPagerAdapter(getSupportFragmentManager());
        vp = (ViewPager) findViewById(R.id.pager);
        tabSwipeListener = new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {

                actionBar = getActionBar();
                actionBar.setSelectedNavigationItem(position);
                Log.w("test", "setOnPageChangeListener " + Integer.toString(position));
                //vp.setAdapter(new WorkFilesTabPagerAdapter(getSupportFragmentManager()));
                selectedItem = position;
                if(mode == WorkFilesTabPagerAdapter.ARCH_MODE) {
                    if (position == 0) {
                         getVarStore().getCurrentDir().setNewContent(Constants.ARCHIVE_DIR_PACKED);
                    }
                    if (position == 1) {
                         getVarStore().getCurrentDir().setNewContent(Constants.ARCHIVE_DIR_UNPACKED);
                    }
                }
                if(mode == WorkFilesTabPagerAdapter.CRYPTO_MODE) {
                    if (position == 0) {
                         getVarStore().getCurrentDir().setNewContent(Constants.CRYPTO_DIR_ENCRYPTED);
                    }
                    if (position == 1) {
                         getVarStore().getCurrentDir().setNewContent(Constants.CRYPTO_DIR_DECRYPTED);
                    }
                }
                setProgressBarIndeterminateVisibility(true);
                refreshCurrentDir();
                setProgressBarIndeterminateVisibility(false);
            }
        };
        vp.setOnPageChangeListener(tabSwipeListener);
        vp.setAdapter(TabAdapter);
        actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        tabListener = new ActionBar.TabListener(){

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
        //по умолчанию открываются архивы
        actionBar.addTab(actionBar.newTab().setText(getString(R.string.archPackedTab)).setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText(getString(R.string.archUnpackedTab)).setTabListener(tabListener));

        setProgressBarIndeterminateVisibility(false);
        tabSwipeListener.onPageSelected(0); //вызываем слушатель чтобы инициализировать первую вкладку
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.work_files_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //в ActionBar вынесена отдельная кнопка "Панель" по щелчку на которую показывается/скрывается панель с функциональными кнопками (поиск, обновление и т.д.)
        if (id == R.id.work_files_menu_acrh) {
            actionBar.removeAllTabs();
            TabAdapter = new WorkFilesTabPagerAdapter(getSupportFragmentManager());
            TabAdapter.setMode(WorkFilesTabPagerAdapter.ARCH_MODE);
            mode = WorkFilesTabPagerAdapter.ARCH_MODE;
            vp.setAdapter(TabAdapter);
            actionBar.addTab(actionBar.newTab().setText(getString(R.string.archPackedTab)).setTabListener(tabListener));
            actionBar.addTab(actionBar.newTab().setText(getString(R.string.archUnpackedTab)).setTabListener(tabListener));
            tabSwipeListener.onPageSelected(0); //вызываем слушатель чтобы инициализировать первую вкладку
            return true;
        }

        if (id == R.id.work_files_menu_crypto) {
            actionBar.removeAllTabs();
            TabAdapter = new WorkFilesTabPagerAdapter(getSupportFragmentManager());
            TabAdapter.setMode(WorkFilesTabPagerAdapter.CRYPTO_MODE);
            mode = WorkFilesTabPagerAdapter.CRYPTO_MODE;
            vp.setAdapter(TabAdapter);
            actionBar.addTab(actionBar.newTab().setText(getString(R.string.encryptedTab)).setTabListener(tabListener));
            actionBar.addTab(actionBar.newTab().setText(getString(R.string.decryptedTab)).setTabListener(tabListener));
            tabSwipeListener.onPageSelected(0); //вызываем слушатель чтобы инициализировать первую вкладку
            return true;
        }



        /*setProgressBarIndeterminateVisibility(true);
        refreshCurrentDir();
        setProgressBarIndeterminateVisibility(false);*/

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

            VarStore varStore = (VarStore) VarStore.getAppContext();
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                if (varStore.getCurrentDir().getPath().equals(Constants.ARCHIVE_DIR_PACKED) ||
                        varStore.getCurrentDir().getPath().equals(Constants.ARCHIVE_DIR_UNPACKED) ||
                        varStore.getCurrentDir().getPath().equals(Constants.CRYPTO_DIR_ENCRYPTED) ||
                        varStore.getCurrentDir().getPath().equals(Constants.CRYPTO_DIR_DECRYPTED)) {
                    //Toast.makeText(getApplicationContext(), getString(R.string.it_is_root_directory), Toast.LENGTH_SHORT).show();
                    //updateFileList(Constants.ROOT_PATH);
                    finish = true;
                    getVarStore().getCurrentDir().setSelectAll(false);
                    getVarStore().getCurrentDir().getSelectedObjectsIDs().clear();
                    finish();
                } else {
                    if(!finish) {
                        varStore.getCurrentDir().setNewContent(varStore.getMainOperationsTools().getParentDirectory(varStore.getCurrentDir().getPath()));
                        refreshCurrentDir();
                    }
                }
                return true;
            }

        return super.onKeyDown(keyCode, event);
    }

    //на выходе возвращаемся в предыдущую директорию
    @Override
    protected void onDestroy() {
        // getVarStore().getCurrentDir().setNewContent(old_dir, FileUtils.getAndSortObjectsList(old_dir, "", false));
        /* getVarStore().getCurrentDir().clear();
         getVarStore().getFLA().notifyDataSetChanged();*/
         //getVarStore().getCurrentDir().returnObjectsFromBuffer();
        //Log.w("rrr", getVarStore().getCurrentDir().getObjects().get(0).path);
         //getVarStore().getFLA().notifyDataSetChanged();
        super.onDestroy();

    }

    @Override
    protected void onPause() {
        //Log.w("onPause", "!!!");
        if(finish) {
            getVarStore().getCurrentDir().returnObjectsFromBuffer();
            //Log.w("rrr", getVarStore().getCurrentDir().getObjects().get(0).path);
        }
        super.onPause();
    }

    //обновление списка файлов текущей директории
    public static void refreshCurrentDir() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    VarStore varStore = (VarStore) VarStore.getAppContext();
                    varStore.getCurrentDir().setNewContent(varStore.getCurrentDir().getPath(), FileUtils.getAndSortObjectsList(varStore.getCurrentDir().getPath(), "", false));
                    //varStore.getFLA().notifyDataSetChanged();
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (selectedItem == 0) {
                                if (fla0 != null)
                                    fla0.notifyDataSetChanged();
                            }
                            if (selectedItem == 1) {
                                if (fla1 != null)
                                    fla1.notifyDataSetChanged();
                            }
                        }
                    });

                    //очищаем массив выделенных файлов
                    //varStore.getBuffer().clear();
                } catch (Exception e) {
                    Log.e("refreshCurrentDir", null, e);
                }
            }
        };
        thread.start();
    }

    public static void showCompleteWorkFilesDialog() {
        refreshCurrentDir();
    }

}

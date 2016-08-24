package ru.scorpio92.filemanager.Main.UI;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

import ru.scorpio92.filemanager.Main.Types.MainDB;
import ru.scorpio92.filemanager.Main.UI.Intro.Intro;
import ru.scorpio92.filemanager.Main.Utils.DBUtils;
import ru.scorpio92.filemanager.Main.Utils.SecondUsageUtils;
import ru.scorpio92.filemanager.Main.Variables.Constants;
import ru.scorpio92.filemanager.Main.Variables.VarStore;
import ru.scorpio92.filemanager.R;

/**
 * Created by scorpio92 on 05.08.16.
 */
public class Init extends Activity {


    private VarStore getVarStore() { return (VarStore) this.getApplication(); }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.w("Init", "onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        init();
    }

    //обработка результата при закрытии активности Intro (первичный запуск)
    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case Constants.INTRO_ACTIVITY_EXIT_CODE:
                if (resultCode == RESULT_OK) {
                    SecondUsageUtils.setRepeatedStart();
                    finish();
                    startActivityForResult(new Intent(this, MainUI.class), Constants.MAINUI_ACTIVITY_EXIT_CODE);
                }
                break;
        }
    }*/



    //проверочные методы и начальная инициализация. стартуют в onPostCreate
    private void init() {
        //записываем текущий контекст приложения в хранилище
        getVarStore().setContext(getApplicationContext());
        //проверка бинарника BusyBox и создание папок хранилища (если их нет)
        checkAppComponents();
        //инициализация БД и занесение в нее начальных значений (если БД еще не создана)
        initDB();
        //проверка на первый запуск
        checkFirstRun();
    }

    private void checkAppComponents() {
        try {
            if(!SecondUsageUtils.checkAppComponents()) {
                Toast.makeText(getApplicationContext(),
                        getString(R.string.no_needed_app_components),
                        Toast.LENGTH_SHORT).show();
                finish();
            }

        } catch (Exception e) {
            Log.e("checkAppComponents", null, e);
            Toast.makeText(getApplicationContext(),
                    getString(R.string.no_needed_app_components),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    //ининциализация БД
    private void initDB() {
        if(!new File(Constants.DATA_APP_PATH + "/" + Constants.BD_DIR + "/" + MainDB.MAIN_DATABASE_NAME).exists()) {
            MainDB mAccountDB = new MainDB(this);
            SQLiteDatabase sdb = mAccountDB.getReadableDatabase();
            sdb.close();

            ContentValues newValues = new ContentValues();

            //записываем настройку проверки на первый запуск
            newValues.put(MainDB.GENERAL_TABLE_SETTING_NAME_COLUMN, Constants.IS_FIRST_RUN_ROW);
            newValues.put(MainDB.GENERAL_TABLE_SETTING_VALUE_COLUMN, Constants.IS_FIRST_RUN);
            DBUtils.insert_update_delete(MainDB.MAIN_DATABASE_NAME, MainDB.GENERAL_TABLE, newValues, null, DBUtils.ACTION_INSERT);
            newValues.clear();

            //записываем настройку проверки Root прав, по умолчанию -1 (проверки не было)
            newValues.put(MainDB.GENERAL_TABLE_SETTING_NAME_COLUMN, Constants.ROOT_CHECK_RESULT_ROW);
            newValues.put(MainDB.GENERAL_TABLE_SETTING_VALUE_COLUMN, Constants.ROOT_NEED_TO_CHECK);
            DBUtils.insert_update_delete(MainDB.MAIN_DATABASE_NAME, MainDB.GENERAL_TABLE, newValues, null, DBUtils.ACTION_INSERT);
            newValues.clear();

            //записываем текущий путь
            newValues.put(MainDB.GENERAL_TABLE_SETTING_NAME_COLUMN, Constants.GENERAL_SETTING_SAVE_LAST_PATH_KEY);
            newValues.put(MainDB.GENERAL_TABLE_SETTING_VALUE_COLUMN, Constants.DEFAULT_HOME_PATH);
            DBUtils.insert_update_delete(MainDB.MAIN_DATABASE_NAME, MainDB.GENERAL_TABLE, newValues, null, DBUtils.ACTION_INSERT);
            newValues.clear();

            //записываем SDCARD в первую очередь, чтобы при загрузке показывалась внешняя КП
            newValues.put(MainDB.FAVORITE_PATHS_TABLE_ALIAS_COLUMN, getString(R.string.favorite_dialog_default_path_alias_sdcard));
            newValues.put(MainDB.FAVORITE_PATHS_TABLE_PATH_COLUMN, System.getenv("EXTERNAL_STORAGE"));
            DBUtils.insert_update_delete(MainDB.MAIN_DATABASE_NAME, MainDB.FAVORITE_PATHS_TABLE, newValues, null, DBUtils.ACTION_INSERT);
            newValues.clear();
            newValues.put(MainDB.FAVORITE_PATHS_TABLE_ALIAS_COLUMN, getString(R.string.favorite_dialog_app_store_alias_sdcard));
            newValues.put(MainDB.FAVORITE_PATHS_TABLE_PATH_COLUMN, Constants.APP_EXTERNAL_DIR);
            DBUtils.insert_update_delete(MainDB.MAIN_DATABASE_NAME, MainDB.FAVORITE_PATHS_TABLE, newValues, null, DBUtils.ACTION_INSERT);
            newValues.clear();
            newValues.put(MainDB.FAVORITE_PATHS_TABLE_ALIAS_COLUMN, getString(R.string.favorite_dialog_default_path_alias_root));
            newValues.put(MainDB.FAVORITE_PATHS_TABLE_PATH_COLUMN, "/");
            DBUtils.insert_update_delete(MainDB.MAIN_DATABASE_NAME, MainDB.FAVORITE_PATHS_TABLE, newValues, null, DBUtils.ACTION_INSERT);
            newValues.clear();
            newValues.put(MainDB.FAVORITE_PATHS_TABLE_ALIAS_COLUMN, getString(R.string.favorite_dialog_default_path_alias_data));
            newValues.put(MainDB.FAVORITE_PATHS_TABLE_PATH_COLUMN, "/data");
            DBUtils.insert_update_delete(MainDB.MAIN_DATABASE_NAME, MainDB.FAVORITE_PATHS_TABLE, newValues, null, DBUtils.ACTION_INSERT);
            newValues.clear();
            newValues.put(MainDB.FAVORITE_PATHS_TABLE_ALIAS_COLUMN, getString(R.string.favorite_dialog_default_path_alias_system));
            newValues.put(MainDB.FAVORITE_PATHS_TABLE_PATH_COLUMN, "/system");
            DBUtils.insert_update_delete(MainDB.MAIN_DATABASE_NAME, MainDB.FAVORITE_PATHS_TABLE, newValues, null, DBUtils.ACTION_INSERT);
            newValues.clear();
        }
    }

    //проверка на первый запуск: нужно ли показывать справку и проверять рут права
    private void checkFirstRun() {
        try {
            Boolean b = SecondUsageUtils.checkFirstRun();
            if(b != null) {
                if (b) {
                    //finish();
                    startActivityForResult(new Intent(this, Intro.class), Constants.INTRO_ACTIVITY_EXIT_CODE);
                    finish();
                } else {
                    //finish();
                    startActivityForResult(new Intent(this, MainUI.class), Constants.MAINUI_ACTIVITY_EXIT_CODE);
                    finish();
                }
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.problems_with_app_db), Toast.LENGTH_SHORT).show();
                finish();
            }

            //для теста интро
            //startActivityForResult(new Intent(this, Intro.class), Constants.INTRO_ACTIVITY_EXIT_CODE);
        } catch (Exception e) {
            Log.e("checkFirstRun", null, e);
        }
    }

}
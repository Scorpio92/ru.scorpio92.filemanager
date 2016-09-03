package ru.scorpio92.filemanager.Main.Utils;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.scorpio92.filemanager.R;
import ru.scorpio92.filemanager.Textviewer.TextViewer;
import ru.scorpio92.filemanager.Main.Types.MainDB;
import ru.scorpio92.filemanager.Main.Variables.Constants;
import ru.scorpio92.filemanager.Main.Variables.VarStore;
import ru.scorpio92.io.MainOperationsTools;

/**
 * Created by scorpio92 on 28.07.16.
 */
public class SecondUsageUtils {

    //проверяем наличие папки приложения на внешней КП
    //проверка бинарников BusyBox и mksh
    public static Boolean checkAppComponents() {
        try {
            //VarStore varStore = (VarStore) VarStore.getAppContext();
            //проверяем наличие бинарников sh и busybox
            File f;

            f = new File(Constants.BUSYBOX_PATH);

            if (!f.exists()) {
                Log.w("checkAppComponents", "no busybox. copyIO");
                return new MainOperationsTools(Constants.BUSYBOX_PATH).copyIO(VarStore.getAppContext().getResources().openRawResource(R.raw.busybox), new FileOutputStream(Constants.BUSYBOX_PATH)) && f.setExecutable(true, true);
            }

            //проверяем наличие папок приложения на внешней КП
            if(new File(Constants.ARCHIVE_DIR_PACKED).mkdirs()) {
                Log.w("checkAppComponents", "no ARCHIVE_DIR_PACKED. create");
            }
            if(new File(Constants.ARCHIVE_DIR_UNPACKED).mkdirs()) {
                Log.w("checkAppComponents", "no ARCHIVE_DIR_UNPACKED. create");
            }
            if(new File(Constants.CRYPTO_DIR_ENCRYPTED).mkdirs()) {
                Log.w("checkAppComponents", "no CRYPTO_DIR_ENCRYPTED. create");
            }
            if(new File(Constants.CRYPTO_DIR_DECRYPTED).mkdirs()) {
                Log.w("checkAppComponents", "no CRYPTO_DIR_DECRYPTED. create");
            }

            return true;

        } catch (Exception e) {
            Log.e("checkAppComponents", null, e);
        }
        return false;
    }

    //проверяем что это первый запуск
    public static Boolean checkFirstRun() {
        ArrayList<String> als = new ArrayList<String>();
        try {
            als.add(MainDB.GENERAL_TABLE_SETTING_VALUE_COLUMN);
            ArrayList<String> result = DBUtils.select_from_db(MainDB.MAIN_DATABASE_NAME, "SELECT * " +
                    "FROM " + MainDB.GENERAL_TABLE +
                    " WHERE " + MainDB.GENERAL_TABLE_SETTING_NAME_COLUMN + "=" + "'" + Constants.IS_FIRST_RUN_ROW + "'", als, false);
            return result.get(0).equals(Constants.IS_FIRST_RUN);
        } catch (Exception e) {
            Log.e("checkFirstRun", null, e);
            return null;
        }
        //return true;
    }

    public static Boolean setRepeatedStart() {
        try {
            ContentValues newValues = new ContentValues();
            String where = MainDB.GENERAL_TABLE_SETTING_NAME_COLUMN + "=" + "'" + Constants.IS_FIRST_RUN_ROW + "'";
            newValues.put(MainDB.GENERAL_TABLE_SETTING_VALUE_COLUMN, "0");
            DBUtils.insert_update_delete(MainDB.MAIN_DATABASE_NAME, MainDB.GENERAL_TABLE, newValues, where, DBUtils.ACTION_UPDATE);
            newValues.clear();
            return true;
        } catch (Exception e) {
            Log.e("setRepeatedStart", null, e);
        }
        return false;
    }

    public static String checkRootAvailableParameter() {
        try {
            ArrayList<String> als = new ArrayList<String>();
            als.add(MainDB.GENERAL_TABLE_SETTING_VALUE_COLUMN);
            ArrayList<String> result = DBUtils.select_from_db(MainDB.MAIN_DATABASE_NAME, "SELECT * " +
                    "FROM " + MainDB.GENERAL_TABLE +
                    " WHERE " + MainDB.GENERAL_TABLE_SETTING_NAME_COLUMN + "=" + "'" + Constants.ROOT_CHECK_RESULT_ROW + "'", als, false);
            return result.get(0);
        } catch (Exception e) {
            Log.e("checkRootAvailableParam", null, e);
        }
        return Constants.ROOT_NEED_TO_CHECK; //проверяем
    }

    public static Boolean getAndSetRootStatus() {
        try {
            VarStore varStore = (VarStore) VarStore.getAppContext();
            ContentValues newValues = new ContentValues();
            String where = MainDB.GENERAL_TABLE_SETTING_NAME_COLUMN + "=" + "'" + Constants.ROOT_CHECK_RESULT_ROW + "'";
            if(varStore.getMainOperationsTools().checkRoot()) {
                newValues.put(MainDB.GENERAL_TABLE_SETTING_VALUE_COLUMN, Constants.ROOT_AVAILABLE);
                DBUtils.insert_update_delete(MainDB.MAIN_DATABASE_NAME, MainDB.GENERAL_TABLE, newValues, where, DBUtils.ACTION_UPDATE);
                newValues.clear();
                return true;
            } else {
                newValues.put(MainDB.GENERAL_TABLE_SETTING_VALUE_COLUMN, Constants.ROOT_NOT_AVAILABLE);
                DBUtils.insert_update_delete(MainDB.MAIN_DATABASE_NAME, MainDB.GENERAL_TABLE, newValues, where, DBUtils.ACTION_UPDATE);
                newValues.clear();
                varStore.getMainOperationsTools().setAllowSU(false);
            }
        } catch (Exception e) {
            Log.e("setRootStatus", null, e);
        }
        return false;
    }

    public static String getLastPath() {
        try {
            ArrayList<String> als = new ArrayList<String>();
            als.add(MainDB.GENERAL_TABLE_SETTING_VALUE_COLUMN);
            ArrayList<String> result = DBUtils.select_from_db(MainDB.MAIN_DATABASE_NAME, "SELECT * " +
                    "FROM " + MainDB.GENERAL_TABLE +
                    " WHERE " + MainDB.GENERAL_TABLE_SETTING_NAME_COLUMN + "=" + "'" + Constants.GENERAL_SETTING_SAVE_LAST_PATH_KEY + "'", als, false);
            return result.get(0);
        } catch (Exception e) {
            Log.e("getLastPath", null, e);
        }
        return Constants.DEFAULT_HOME_PATH;
    }

    public static void setLastPath(String path) {
        try {
            ContentValues newValues = new ContentValues();
            String where = MainDB.GENERAL_TABLE_SETTING_NAME_COLUMN + "=" + "'" + Constants.GENERAL_SETTING_SAVE_LAST_PATH_KEY + "'";
            newValues.put(MainDB.GENERAL_TABLE_SETTING_VALUE_COLUMN, path);
            DBUtils.insert_update_delete(MainDB.MAIN_DATABASE_NAME, MainDB.GENERAL_TABLE, newValues, where, DBUtils.ACTION_UPDATE);
            newValues.clear();
        } catch (Exception e) {
            Log.e("setLastPath", null, e);
        }
    }

    //открытие текстовых файлов !нужно протестировать открытие файлов со сложными именами: со скобками и пр.
    public static Boolean openTextFile(Context activityContext, String path, Boolean forEdit) {
        try {
            Intent intent = new Intent(activityContext, TextViewer.class);
            intent.putExtra(Constants.TEXTVIEWER_DATA, FileUtils.openTextFile(path));
            intent.putExtra(Constants.TEXTVIEWER_FILE, path);
            intent.putExtra(Constants.TEXTVIEWER_EDITABLE, forEdit);
            activityContext.startActivity(intent);
            return true;

        } catch (Exception e) {
            Log.e("openTextFile", null, e);
        }
        return false;
    }

    public static List<ResolveInfo> getLauncherCategoryApps(PackageManager pm) {
        List<ResolveInfo> appList = new ArrayList<ResolveInfo>();
        try {
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            appList = pm.queryIntentActivities(mainIntent, 0);
            Collections.sort(appList, new ResolveInfo.DisplayNameComparator(pm));

        } catch (Exception e) {
            Log.e("getLauncherCategoryApps", null, e);
        }
        return appList;
    }

    //открытие файлов
    public static Boolean openFile(Context activityContext,String path) {
        try {
            MimeTypeMap myMime = MimeTypeMap.getSingleton();
            Intent newIntent = new Intent(Intent.ACTION_VIEW);
            String mimeType = myMime.getMimeTypeFromExtension(((VarStore) VarStore.getAppContext()).getMainOperationsTools().getFileExt(path));
            newIntent.setDataAndType(Uri.fromFile(new File(path)), mimeType);
            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                activityContext.startActivity(newIntent);
                return true;
            } catch (ActivityNotFoundException e) {
                Log.e("openFile", null, e);
            }
        } catch (Exception e) {
            Log.e("openFile", null, e);
        }
        return false;
    }

    public static Boolean openFileWithPackage(Context activityContext,String path, String packageName) {
        try {
            Intent intent = new Intent();
            intent.setPackage(packageName);
            //MimeTypeMap myMime = MimeTypeMap.getSingleton();
            //String mimeType = myMime.getMimeTypeFromExtension(((VarStore) VarStore.getAppContext()).getMainOperationsTools().getFileExt(path));
            intent.setDataAndType(Uri.fromFile(new File(path)), "*/*");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                activityContext.startActivity(intent);
                return true;
            } catch (Exception e) {
                Log.e("openFile with", null, e);
            }
        } catch (Exception e) {
            Log.e("openFile with", null, e);
        }
        return false;
    }

    public static void makeAppBinding(String extension, String packageName, String activity) {
        try {

            ContentValues newValues = new ContentValues();
            if (getPackageForExtension(extension) == null) {
                newValues.put(MainDB.APP_BINDING_TABLE_EXTENSION_COLUMN, extension);
                newValues.put(MainDB.APP_BINDING_TABLE_PACKAGE_NAME_COLUMN, packageName);
                newValues.put(MainDB.APP_BINDING_TABLE_ACTIVITY_COLUMN, activity);
                DBUtils.insert_update_delete(MainDB.MAIN_DATABASE_NAME, MainDB.APP_BINDING_TABLE, newValues, null, DBUtils.ACTION_INSERT);
            } else {
                String where = MainDB.APP_BINDING_TABLE_EXTENSION_COLUMN + "=" + "'" + extension + "'";
                newValues.put(MainDB.APP_BINDING_TABLE_PACKAGE_NAME_COLUMN, packageName);
                newValues.put(MainDB.APP_BINDING_TABLE_ACTIVITY_COLUMN, activity);
                DBUtils.insert_update_delete(MainDB.MAIN_DATABASE_NAME, MainDB.APP_BINDING_TABLE, newValues, where, DBUtils.ACTION_UPDATE);
            }
        } catch (Exception e) {
            Log.e("makeAppBinding", null, e);
        }
    }

    public static String getPackageForExtension(String extension) {
        try {
            ArrayList<String> als = new ArrayList<String>();
            als.add(MainDB.APP_BINDING_TABLE_PACKAGE_NAME_COLUMN);
            ArrayList<String> result = DBUtils.select_from_db(MainDB.MAIN_DATABASE_NAME, "SELECT * " +
                    "FROM " + MainDB.APP_BINDING_TABLE +
                    " WHERE " + MainDB.APP_BINDING_TABLE_EXTENSION_COLUMN + "=" + "'" + extension + "'", als, false);
            if (!result.isEmpty()) {
                return result.get(0);
            }
        } catch (Exception e) {
            Log.e("getPackageForExtension", null, e);
        }
        return null;
    }

    public static ArrayList<String> getFavoritePaths () {
        ArrayList<String> result = new ArrayList<String>();
        try {
            ArrayList<String> alias = new ArrayList<String>();
            alias.add(MainDB.FAVORITE_PATHS_TABLE_ALIAS_COLUMN);
            result = DBUtils.select_from_db(MainDB.MAIN_DATABASE_NAME, "SELECT * FROM " + MainDB.FAVORITE_PATHS_TABLE, alias, true);
        } catch (Exception e) {
            Log.e("getFavoritePaths", null,e);
        }
        return result;
    }

    public static ArrayList<String> getFavoritePathsByAlias (String alias) {
        ArrayList<String> result = new ArrayList<String>();
        try {
            ArrayList<String> als = new ArrayList<String>();
            als.add(MainDB.FAVORITE_PATHS_TABLE_PATH_COLUMN);
            result = DBUtils.select_from_db(MainDB.MAIN_DATABASE_NAME, "SELECT * FROM " + MainDB.FAVORITE_PATHS_TABLE + " WHERE " + MainDB.FAVORITE_PATHS_TABLE_ALIAS_COLUMN + "=" + "'" + alias + "'", als, false);
        } catch (Exception e) {
            Log.e("getFavoritePaths", null,e);
        }
        return result;
    }

    public static Boolean checkPathInFavorites(String path) {
        try {
            ArrayList<String> als = new ArrayList<String>();
            als.add(MainDB.FAVORITE_PATHS_TABLE_ALIAS_COLUMN);
            ArrayList<String> result = DBUtils.select_from_db(MainDB.MAIN_DATABASE_NAME, "SELECT * " +
                    "FROM " + MainDB.FAVORITE_PATHS_TABLE +
                    " WHERE " + MainDB.FAVORITE_PATHS_TABLE_PATH_COLUMN + "=" + "'" + path + "'", als, false);
            return !result.isEmpty();
        } catch (Exception e) {
            Log.e("checkPathInFavorites", null, e);
        }
        return true;
    }

    public static Boolean checkAliasInFavorites(String alias) {
        try {
            ArrayList<String> als = new ArrayList<String>();
            als.add(MainDB.FAVORITE_PATHS_TABLE_ALIAS_COLUMN);
            ArrayList<String> result = DBUtils.select_from_db(MainDB.MAIN_DATABASE_NAME, "SELECT * FROM " + MainDB.FAVORITE_PATHS_TABLE + " WHERE " + MainDB.FAVORITE_PATHS_TABLE_ALIAS_COLUMN + "=" + "'" + alias + "'", als, false);
            return !result.isEmpty();
        } catch (Exception e) {
            Log.e("checkPathInFavorites", null, e);
        }
        return true;
    }

    public static void addPathInFavorites(String alias, String path) {
        try {
            ContentValues newValues = new ContentValues();
            newValues.put(MainDB.FAVORITE_PATHS_TABLE_ALIAS_COLUMN, alias);
            newValues.put(MainDB.FAVORITE_PATHS_TABLE_PATH_COLUMN, path);
            DBUtils.insert_update_delete(MainDB.MAIN_DATABASE_NAME, MainDB.FAVORITE_PATHS_TABLE, newValues, null, DBUtils.ACTION_INSERT);
        } catch (Exception e) {
            Log.e("addPathInFavorites", null, e);
        }
    }

    public static void deletePathFromFavorites(String path) {
        try {
            String where = MainDB.FAVORITE_PATHS_TABLE_PATH_COLUMN + "=" + "'" + path + "'";
            DBUtils.insert_update_delete(MainDB.MAIN_DATABASE_NAME, MainDB.FAVORITE_PATHS_TABLE, null, where, DBUtils.ACTION_DELETE);
        } catch (Exception e) {
            Log.e("addPathInFavorites", null, e);
        }
    }


    //скриываем виртуальную клавиатуру
    public static void hideVirtualKeyboard(View v) {
        try {
            InputMethodManager imm = (InputMethodManager) VarStore.getAppContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        } catch (Exception e) {
            Log.e("hideVirtualKeyboard", null, e);
        }
    }

    //копирование в буффер полного пути или имени объекта
    public static Boolean copyStrToBuffer(String text) {
        try {
            //String name = ((VarStore) VarStore.getAppContext()).getMainOperationsTools().getLastPathComponent(filePath);
            ClipboardManager clipboard = (ClipboardManager) VarStore.getAppContext().getSystemService(Context.CLIPBOARD_SERVICE);
            /*if (copyName)
                clipboard.setText(name);
            else
                clipboard.setText(filePath);*/
            clipboard.setText(text);
            return true;
        } catch (Exception e) {
            Log.e("copyNameToBuffer", null, e);
        }
        return false;
    }

}

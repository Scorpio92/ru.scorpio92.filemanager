package ru.scorpio92.filemanager.Main.Types;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Created by scorpio92 on 28.03.16.
 */
public class MainDB extends SQLiteOpenHelper implements BaseColumns {

    public static String MAIN_DATABASE_NAME="main.db";
    private static final int MAIN_DATABASE_VERSION = 1;
    public static final String GENERAL_TABLE="general";
    public static final String GENERAL_TABLE_SETTING_NAME_COLUMN="setting";
    public static final String GENERAL_TABLE_SETTING_VALUE_COLUMN="value";
    public static final String FAVORITE_PATHS_TABLE="favorite_paths";
    public static final String FAVORITE_PATHS_TABLE_ALIAS_COLUMN="alias";
    public static final String FAVORITE_PATHS_TABLE_PATH_COLUMN="path";
    public static final String APP_BINDING_TABLE="app_binding";
    public static final String APP_BINDING_TABLE_EXTENSION_COLUMN="extension";
    public static final String APP_BINDING_TABLE_PACKAGE_NAME_COLUMN="package";
    public static final String APP_BINDING_TABLE_ACTIVITY_COLUMN="activity";

    private static final String GENERAL_TABLE_CREATE_SCRIPT = "create table "
            + GENERAL_TABLE + " (" + BaseColumns._ID
            + " integer primary key autoincrement, " + GENERAL_TABLE_SETTING_NAME_COLUMN
            + " text not null, " + GENERAL_TABLE_SETTING_VALUE_COLUMN + " text not null);";

    private static final String FAVORITE_PATHS_TABLE_CREATE_SCRIPT = "create table "
            + FAVORITE_PATHS_TABLE + " (" + BaseColumns._ID
            + " integer primary key autoincrement, " + FAVORITE_PATHS_TABLE_ALIAS_COLUMN
            + " text not null, " + FAVORITE_PATHS_TABLE_PATH_COLUMN + " text not null);";

    private static final String APP_BINDING_TABLE_CREATE_SCRIPT = "create table "
            + APP_BINDING_TABLE + " (" + BaseColumns._ID
            + " integer primary key autoincrement, " + APP_BINDING_TABLE_EXTENSION_COLUMN
            + " text not null, " + APP_BINDING_TABLE_PACKAGE_NAME_COLUMN
            + " text not null, " + APP_BINDING_TABLE_ACTIVITY_COLUMN + " text not null);";

    public MainDB(Context context) {
        super(context, MAIN_DATABASE_NAME, null, MAIN_DATABASE_VERSION);
    }

    public MainDB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(GENERAL_TABLE_CREATE_SCRIPT);
        sqLiteDatabase.execSQL(FAVORITE_PATHS_TABLE_CREATE_SCRIPT);
        sqLiteDatabase.execSQL(APP_BINDING_TABLE_CREATE_SCRIPT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
// Запишем в журнал
        Log.w("SQLite", "Обновляемся с версии " + i + " на версию " + i1);
        sqLiteDatabase.execSQL("DROP TABLE IF IT EXISTS " + GENERAL_TABLE);
        sqLiteDatabase.execSQL("DROP TABLE IF IT EXISTS " + FAVORITE_PATHS_TABLE);
        sqLiteDatabase.execSQL("DROP TABLE IF IT EXISTS " + APP_BINDING_TABLE);
        onCreate(sqLiteDatabase);
    }
}

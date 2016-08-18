package ru.scorpio92.filemanager.Main.Variables;

/**
 * Created by scorpio92 on 26.02.16.
 */
public class Constants  {

    public static final String APP_NAME = "ru.scorpio92.filemanager";
    public static final String DATA_APP_PATH = "/data/data" + "/" + APP_NAME;
    public static final String ROOT_PATH = "/";
    public static final String DEFAULT_HOME_PATH = System.getenv("EXTERNAL_STORAGE");

    //shell
    public static final String BUSYBOX_PATH = DATA_APP_PATH + "/busybox";
    public static final String SH_PATH = "/system/bin/sh";

    //служебные директории
    public static final String BD_DIR = "databases";
    public static final String APP_EXTERNAL_DIR = System.getenv("EXTERNAL_STORAGE") + "/" + APP_NAME;
    //архивация
    public static final String ARCHIVE_DIR = APP_EXTERNAL_DIR + "/" + "arch";
    public static final String ARCHIVE_DIR_PACKED = ARCHIVE_DIR + "/" + "packed";
    public static final String ARCHIVE_DIR_UNPACKED = ARCHIVE_DIR + "/" + "unpacked";
    //шифрование
    public static final String CRYPTO_DIR = APP_EXTERNAL_DIR + "/" + "crypto";
    public static final String CRYPTO_DIR_ENCRYPTED = CRYPTO_DIR + "/" + "encrypted";
    public static final String CRYPTO_DIR_DECRYPTED = CRYPTO_DIR + "/" + "decrypted";


    //Настройки
    //когда запускаем активность настроек, в основном классе слушаем возврат данного кода и проверяем результат -
    //если что поменялось в настройках представления - обновляем список директорий и файлов
    public static final int SETTINGS_ACTIVITY_REQUEST_CODE = 0; //когда запускаем активность настроек
    public static final String NEED_REFRESH_AFTER_SETTINGS_CHANGE="need_refresh"; //когда запускаем активность настроек
    //ключ для настройки сохранения последнего пути
    public static final String GENEREAL_SETTING_SAVE_LAST_PATH_KEY="settings_general_save_last_path";
    //копирование папок в самих себя
    public static final String GENEREAL_SETTING_SELF_COPY="settings_general_allow_self_copy";
    //представление и сортировка
    public static final String SORT_SETTING_KEY="settings_view_sort";
    public static final int SORT_BY_NAME=0;
    public static final int SORT_BY_NAME_FOLDER_FIRST=1;
    public static final int SORT_BY_NAME_FILE_FIRST=2;
    public static final int SORT_BY_CHANGE_TIME_OLD_FIRST=3;
    public static final int SORT_BY_CHANGE_TIME_NEW_FIRST=4;
    public static final int SORT_BY_SIZE_SMALL_FIRST=5;
    public static final int SORT_BY_SIZE_BIG_FIRST=6;

    public static final String VIEW_SHOW_DIR_SIZE_KEY="settings_view_show_dirs_size";
    public static final String VIEW_SHOW_FILE_SIZE_KEY="settings_view_show_files_size";
    public static final String VIEW_SHOW_CHANGE_TIME_KEY="settings_view_show_change_time";
    public static final String VIEW_SHOW_SELECTED_FILES_SIZE_KEY="settings_view_show_selected_files_size";
    //шифрование
    public static final String CRYPTO_KEY_LENGHT_KEY = "settings_crypto_key_lenght";
    public static final String CRYPTO_BUFFER_SIZE_KEY = "settings_crypto_buffer";
    //textviewer
    public static final String TEXTVIEWER_FONT_SIZE_KEY = "settings_textviewer_font_size";
    public static final String TEXTVIEWER_LINE_BREAK_KEY = "settings_textviewer_line_break";
    public static final String TEXTVIEWER_LINE_NUMS_KEY = "settings_textviewer_line_nums";
    public static final String TEXTEDITOR_FILES_BACKUP_KEY = "settings_texteditor_allow_bak_files";
    //терминал
    public static final String TERMINAL_FONT_SIZE_KEY = "settings_terminal_font_size";
    public static final String TERMINAL_LINE_BREAK_KEY = "settings_terminal_line_break";
    public static final String TERMINAL_LINE_NUMS_KEY = "settings_terminal_line_nums";
    //root
    public static final String ROOT_TIME="settings_root_test_root_time";


    //типы объектов
    public static final String OBJECT_TYPE_DIR="d";
    public static final String OBJECT_TYPE_SYMLINK_DIR="ld";
    public static final String OBJECT_TYPE_FILE="f";
    public static final String OBJECT_TYPE_SYMLINK_FILE="lf";
    public static final String OBJECT_TYPE_DIR_PROPERTY="directory";


    //startActivityForResult codes
    public static final int INTRO_ACTIVITY_EXIT_CODE = 0;
    public static final int MAINUI_ACTIVITY_EXIT_CODE = 1;
    public static final int HELP_ACTIVITY_EXIT_CODE = 2;


    public static final String ZIP_EXT = "zip";


    //БД
    public static final String IS_FIRST_RUN = "1";
    public static final String IS_FIRST_RUN_ROW = "is_first_run"; //запись в таблице определяющая - первый запуск программы или нет
    //проверка Root прав
    public static final String ROOT_CHECK_RESULT_ROW = "is_rooted_device";
    public static final String ROOT_AVAILABLE = "1";
    public static final String ROOT_NOT_AVAILABLE = "0";
    public static final String ROOT_NEED_TO_CHECK = "-1";

    //константы для TextViewer и TextEditor
    public static final String[] TEXT_EXT_MASSIVE = {"txt", "sh", "xml", "conf", "prop", "cfg", "ini", "log"};
    public static final String TEXTEDITOR_BACKUP_FILE_EXTENSION = ".bak";
    public static final String TEXTVIEWER_DATA = "data";
    public static final String TEXTVIEWER_FILE = "file";
    public static final String TEXTVIEWER_EDITABLE = "editable";
}

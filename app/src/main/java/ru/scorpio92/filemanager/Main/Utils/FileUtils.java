package ru.scorpio92.filemanager.Main.Utils;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import ru.scorpio92.filemanager.Main.Types.*;
import ru.scorpio92.filemanager.Main.Types.Object;
import ru.scorpio92.filemanager.R;
import ru.scorpio92.filemanager.Main.Variables.Constants;
import ru.scorpio92.filemanager.Main.Variables.VarStore;
import ru.scorpio92.io.MainOperationsTools;

/**
 * Created by scorpio92 on 28.02.16.
 */
public class FileUtils {

    private static final String CHMOD = "BB=BUSYBOX_FOR_REPLACE; $BB chmod MOD_FOR_REPLACE \"PATH_FOR_REPLACE\"";

    private static final String GET_OBJ_PROPERTIES = "BB=BUSYBOX_FOR_REPLACE; FILE=\"PATH_FOR_REPLACE\"; " + "$BB stat \"$FILE\" | $BB tr -s ' ' | $BB grep 'Change:' | $BB cut -d ' ' -f2,3 | $BB cut -d '.' -f1; $BB stat \"$FILE\" | $BB grep 'Uid:' | $BB sed 's/^.*Uid: (//;s/).*$//' | $BB cut -d '/' -f2 | $BB sed 's/^[ \\t]*//;s/[ \\t]*$//'; $BB stat \"$FILE\" | $BB grep 'Gid:' | $BB sed 's/^.*Gid: (//;s/).*$//' | $BB cut -d '/' -f2 | $BB sed 's/^[ \\t]*//;s/[ \\t]*$//'; $BB stat \"$FILE\" | $BB grep 'Access: (' | $BB sed 's/^.*Access: (//;s/).*$//' | $BB cut -d '/' -f2";  //формат вывода: change_time \n owner \n group \n permissions

    private static final String OPEN_TEXT_FILE = "BB=BUSYBOX_FOR_REPLACE; $BB cat \"PATH_FOR_REPLACE\"";

    private static final String SAVE_TEXT_FILE = "BB=BUSYBOX_FOR_REPLACE; $BB echo -e \"TEXT_FOR_REPLACE\" > \"PATH_FOR_REPLACE\"; $BB cat \"PATH_FOR_REPLACE\"";

    private static final String GET_ALL_OBJECTS_COUNT_IN_CURR_DIR = "BB=BUSYBOX_FOR_REPLACE; $BB find \"PATH_FOR_REPLACE\" -mindepth 1 -maxdepth 1 | $BB wc -l";

    private static final String GET_DIRS_COUNT_IN_CURR_DIR = "BB=BUSYBOX_FOR_REPLACE; $BB find \"PATH_FOR_REPLACE\" -mindepth 1 -maxdepth 1 -type d | $BB wc -l";

    private static final String GET_SPACE_USAGE_STAT = "BB=BUSYBOX_FOR_REPLACE; $BB df -Ph PATH_FOR_REPLACE | $BB sed 1d | $BB tr -s ' ' | $BB cut -d ' ' -f6,2,3,4"; //в названии точек монтирования пробелов нет, поэтом путь в кавычки не берем иначае не получим инфо по всем разделам

    private static final String GET_ALL_DIR_OBJECTS_AND_TYPES = "BB=BUSYBOX_FOR_REPLACE; OLDIFS=$IFS; IFS=\";\"; arr=(); arr+=($($BB printf '%s\\n' \"PATH_FOR_REPLACE\"/* | $BB grep \"GREP_STRING_FOR_REPLACE\" | $BB tr -s \"\\n\" \";\")); IFS=$OLDIFS; for obj in \"${arr[@]}\"; do detected=\"false\"; if [ -L \"$obj\" ];then if [ -d \"$obj\" ];then echo \"$obj;ld\"; detected=\"true\"; fi; if [ -f \"$obj\" ];then echo \"$obj;lf\"; detected=\"true\"; fi; else if [ -d \"$obj\" ];then echo \"$obj;d\"; detected=\"true\"; fi; if [ -f \"$obj\" ];then echo \"$obj;f\"; detected=\"true\"; fi; fi; if [[ \"$detected\" == \"false\" && -e \"$obj\" ]];then echo \"$obj;f\"; fi; done;";

    private static final String GET_ALL_DIR_OBJECTS_AND_TYPES_WITH_HIDDEN = "BB=BUSYBOX_FOR_REPLACE; PATH=\"PATH_FOR_REPLACE\"; OLDIFS=$IFS; IFS=\";\"; arr=(); arr+=($($BB ls -A \"$PATH\" | $BB grep \"GREP_STRING_FOR_REPLACE\" | $BB tr -s \"\\n\" \";\" )); IFS=$OLDIFS; for obj in \"${arr[@]}\"; do obj=\"$PATH/$obj\"; detected=\"false\"; if [ -L \"$obj\" ];then if [ -d \"$obj\" ];then echo \"$obj;ld\"; detected=\"true\"; fi; if [ -f \"$obj\" ];then echo \"$obj;lf\"; detected=\"true\"; fi; else if [ -d \"$obj\" ];then echo \"$obj;d\"; detected=\"true\"; fi; if [ -f \"$obj\" ];then echo \"$obj;f\"; detected=\"true\"; fi; fi; if [[ \"$detected\" == \"false\" && -e \"$obj\" ]];then echo \"$obj;f\"; fi; done;";

    private static final String GET_OBJECT_RAW_INFO = "BB=BUSYBOX_FOR_REPLACE; OLDIFS=$IFS; IFS=\";\"; arr=(); arr+=($($BB printf '%s\\n' \"PATH_FOR_REPLACE\" | $BB grep \"GREP_STRING_FOR_REPLACE\" | $BB tr -s \"\\n\" \";\")); IFS=$OLDIFS; for obj in \"${arr[@]}\"; do detected=\"false\"; if [ -L \"$obj\" ];then if [ -d \"$obj\" ];then echo \"$obj;ld\"; detected=\"true\"; fi; if [ -f \"$obj\" ];then echo \"$obj;lf\"; detected=\"true\"; fi; else if [ -d \"$obj\" ];then echo \"$obj;d\"; detected=\"true\"; fi; if [ -f \"$obj\" ];then echo \"$obj;f\"; detected=\"true\"; fi; fi; if [[ \"$detected\" == \"false\" && -e \"$obj\" ]];then echo \"$obj;f\"; fi; done;";


    private static VarStore getVarStore() {
        return (VarStore) VarStore.getAppContext();
    }

    private static MainOperationsTools getMainOperationsTools() {
        return getVarStore().getMainOperationsTools();
    }


    public static void getAndSortObjectsList(final String path, final String grepString, final boolean forOneObject, final Callback callback) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    getVarStore().getCurrentDir().setNewContent(path, getAndSortObjectsList(path,grepString,forOneObject));
                    callback.onListUpdateFinished();
                } catch (Exception e) {
                    Log.e("getAndSortObjectsList", null, e);
                }
            }
        };
        thread.start();
    }

    public static ArrayList<Object> getAndSortObjectsList(String path, String grepString, boolean forOneObject) {
        ArrayList<String> rawList = new ArrayList<String>();
        ArrayList<Object> objs = new ArrayList<Object>();
        int sortType = 0; //по умолчанию сортировка по имени (сначала папки)
        try {
            //получаем настройки сортировки
            sortType = Integer.parseInt(SettingsUtils.getStringSettings(VarStore.getAppContext(), Constants.SORT_SETTING_KEY));

            long startTime = System.nanoTime();
            if(forOneObject) {
                if (new File(path).getParentFile().listFiles() != null) {
                    rawList = getDirObjectsRawInfo(path, grepString, true);
                } else {
                    rawList = getPrivateDirObjectsRawInfo(path, grepString, true);
                }
            } else {
                if (new File(path).listFiles() != null) { //если прав хватает получаем список из под обычного пользователя
                    rawList = getDirObjectsRawInfo(path, grepString, false);
                } else {
                    rawList = getPrivateDirObjectsRawInfo(path, grepString, false);
                }
            }

            Dir dir = new Dir(path);

            boolean full_date_format = SettingsUtils.getBooleanSettings(VarStore.getAppContext(), Constants.VIEW_FULL_CHANGE_TIME_FORMAT_KEY);

            for (String s:rawList) {
                String[] o_mas = s.split(";");
                String o = o_mas[0];
                String type = o_mas[1];
                File f = new File(o);

                Boolean showDirSize = SettingsUtils.getBooleanSettings(VarStore.getAppContext(), Constants.VIEW_SHOW_DIR_SIZE_KEY);
                long size = 0;
                if(type.equals(Object.TYPE_DIR) || type.equals(Object.TYPE_SYMLINK_DIR)) {
                    if (showDirSize != null) {
                        if (showDirSize) {
                            size = getMainOperationsTools().getObjectSize(o, false);
                        }
                    }
                } else {
                    size = f.length();
                }

                Object object = new Object(o, type, new Date(f.lastModified()).toString(), size, full_date_format);
                dir.getObjects().add(object);
            }
            long endTime = System.nanoTime();
            //Log.w("getAndSortObjectsList", "duration: " + Long.toString((endTime - startTime)/1000000) + " msec");

            switch (sortType) {
                case Constants.SORT_BY_NAME:
                    break;
                case Constants.SORT_BY_NAME_FOLDER_FIRST:
                    dir.sortByName(true, false);
                    break;
                case Constants.SORT_BY_NAME_FILE_FIRST:
                    dir.sortByName(false, false);
                    break;
                case Constants.SORT_BY_CHANGE_TIME_OLD_FIRST:
                    dir.sortByDate(false);
                    break;
                case Constants.SORT_BY_CHANGE_TIME_NEW_FIRST:
                    dir.sortByDate(true);
                    break;
                case Constants.SORT_BY_SIZE_SMALL_FIRST:
                    dir.sortBySize(false);
                    break;
                case Constants.SORT_BY_SIZE_BIG_FIRST:
                    dir.sortBySize(true);
                    break;
            }

            objs = dir.getObjects();
        }
        catch (Exception e) {
            Log.e("getDirFileList", null, e);
        }
        //return rawList;
        return objs;
    }

    //получение списка файлов по номерам массива (без вложенных объектов)
    //передаем методам копирования и т.д. список самих объектов, полный список со всей структурой вложенности получаем уже в них самих
    //переносим данный функционал в Dir
    /*public static ArrayList<String> getFilesListUsingIndexMassive(ArrayList<Integer> idx_massive, ArrayList<Object> objects) {
        ArrayList<String> result = new ArrayList<String>();
        try {
            for (int i = 0; i < idx_massive.size(); i++) {
                /*String path = getMainOperationsTools().getRightPath(objects.get(idx_massive.get(i)).path); //если объект - симлинк - скопируется тот объект на который он ссылается

                //если нужно получить все объекты (включая поддиректории)
                if (getSubObjects) {
                    String fileType = objects.get(idx_massive.get(i)).type;
                    //если объект директория - получаем все ее подобъекты
                    if (fileType.equals(Constants.OBJECT_TYPE_DIR)) {
                        //добавляем все подобъекты данной директории и саму директорию
                        result.addAll(getVarStore().getMainOperationsTools().getAllObjectsInFolder(path));

                    } else { //если файл - просто добавляем
                        result.add(path);
                    }
                } else { //если нужно получить объекты только текущей директории (первый уровень вложенности)
                    result.add(path);
                }
                result.add(getMainOperationsTools().getRightPath(objects.get(idx_massive.get(i)).path));
            }
        } catch (Exception e) {
            Log.e("getFListUsingIdxMassive", null, e);
        }
        return result;
    }*/

    private static String getDirObjectsCommand(String path, String grepString, boolean forOneObject) {
        String command = null;
        try {
            if(forOneObject) {
                command = GET_OBJECT_RAW_INFO
                        .replace("PATH_FOR_REPLACE", path);
            } else {
                if (SettingsUtils.getBooleanSettings(VarStore.getAppContext(), Constants.VIEW_SHOW_HIDDEN_KEY)) {
                    command = GET_ALL_DIR_OBJECTS_AND_TYPES_WITH_HIDDEN;
                } else {
                    command = GET_ALL_DIR_OBJECTS_AND_TYPES;
                }
                if (path.equals("/")) {
                    command = command
                            .replace("PATH_FOR_REPLACE", "");
                } else {
                    command = command
                            .replace("PATH_FOR_REPLACE", path);
                }
            }

            //сортировка и т.д.
            command = command.replace("GREP_STRING_FOR_REPLACE", grepString);

        } catch (Exception e) {
            Log.e("getDirObjectsCommand", null, e);
        }

        return command;
    }

    //получаем список файлов и директорий
    public static ArrayList<String> getDirObjectsRawInfo(String path, String grepString, boolean forOneObject) {
        try {
            return getMainOperationsTools().runProcess(new String[]{Constants.SH_PATH, "-c", getDirObjectsCommand(path, grepString, forOneObject)});

        } catch (Exception e) {
            Log.e("getObjsNamesFromCurDir", null, e);
        }
        return new ArrayList<String>();
    }

    //получаем список файлов и директорий из приватных каталогов (например /data)
    public static ArrayList<String> getPrivateDirObjectsRawInfo(String path, String grepString, boolean forOneObject) {
        try {
            return getMainOperationsTools().runProcessFromSU(getDirObjectsCommand(path, grepString, forOneObject), true);
        } catch (Exception e) {
            Log.e("getObjsNamesFromCurtDir", null, e);
        }
        return new ArrayList<String>();
    }


    //получаем свойства объекта из вывода ls
    public static ObjectProperties getFileProperties(String path, String fileType) {
        ObjectProperties op = new ObjectProperties();
        try {
            //формат вывода: change_time \n owner \n group \n permissions \n size
            String command = GET_OBJ_PROPERTIES
                    .replace("PATH_FOR_REPLACE", path);

            //Log.w("test", command);

            ArrayList<String> out = getMainOperationsTools().runProcess(new String[]{Constants.SH_PATH, "-c", command});
            if (out.isEmpty()) { //если не хватает прав
                out = getMainOperationsTools().runProcessFromSU(command, true);
            }

            if (fileType.equals(Constants.OBJECT_TYPE_DIR) || fileType.equals(Constants.OBJECT_TYPE_SYMLINK_DIR)) {
                op = new ObjectProperties(
                        Constants.OBJECT_TYPE_DIR_PROPERTY,
                        getMainOperationsTools().getLastPathComponent(path),
                        out.get(0), out.get(1), out.get(2), out.get(3), "-", "-");

            } else {
                op = new ObjectProperties(
                        getMainOperationsTools().getMIMEType(path),
                        getMainOperationsTools().getLastPathComponent(path),
                        out.get(0), out.get(1), out.get(2), out.get(3), "-", "-");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return op;
    }

    //получение массива bool переменных из строки разрашений
    public static Boolean[] getBoolArrayFromPermissionString(String pString) {
        try {
            //-rw-rw-r--
            //drw-rw-r--
            Boolean[] bArray = new Boolean[9];
            for (int i = 1; i < pString.length(); i++) {
                if (pString.charAt(i) != '-') {
                    bArray[i - 1] = true;
                } else {
                    bArray[i - 1] = false;
                }
            }
            return bArray;
        } catch (Exception e) {
            Log.e("getFileProperties", null, e);
        }
        return null;
    }

    //получение числовых прав из строки вида: -rw-rw-r--
    public static String getNumberPermissionsFromString(String pString) {
        try {
            String binaryString = "";
            String octalString = "";
            for (int i = 1; i < pString.length(); i++) {
                if (pString.charAt(i) != '-') {
                    binaryString += "1";
                } else {
                    binaryString += "0";
                }
            }
            for (int i = 0; i < 3; i++) {
                long l = Long.parseLong(binaryString.substring(i * 3, i * 3 + 3), 2);
                octalString += Long.toOctalString(l);
            }
            return octalString;
        } catch (Exception e) {
            Log.e("getNumberPermFromString", null, e);
        }
        return VarStore.getAppContext().getString(R.string.empty_string);
    }

    //получение числовых прав из Boolean массива
    public static String getNumberPermissionsFromBooleanArray(Boolean[] bArray) {
        try {
            String binaryString = "";
            String octalString = "";
            for (int i = 0; i < bArray.length; i++) {
                if (bArray[i]) {
                    binaryString += "1";
                } else {
                    binaryString += "0";
                }
            }
            for (int i = 0; i < 3; i++) {
                long l = Long.parseLong(binaryString.substring(i * 3, i * 3 + 3), 2);
                octalString += Long.toOctalString(l);
            }
            return octalString;
        } catch (Exception e) {
            Log.e("getNumberPermFromString", null, e);
        }
        return VarStore.getAppContext().getString(R.string.empty_string);
    }

    //устанавливаем права на объект
    public static Boolean setMod(String path, String mod) {
        try {
            Log.w("test", path + " " + mod);
            String command = CHMOD
                    .replace("MOD_FOR_REPLACE", mod)
                    .replace("PATH_FOR_REPLACE", path);

            getMainOperationsTools().runProcessFromSU(command, false);
            ObjectProperties op = FileUtils.getFileProperties(path, Constants.OBJECT_TYPE_FILE); //не важно файл или папка берем только права
            if (FileUtils.getNumberPermissionsFromString(op.getPermissions()).equals(mod)) {
                return true;
            } else { //если пытаемся изменить права на объект, который находится на RO разделе
                getMainOperationsTools().remount(path, getMainOperationsTools().REMOUNT_RW);
                getMainOperationsTools().runProcessFromSU(command, false);
                getMainOperationsTools().remount(path, getMainOperationsTools().REMOUNT_RO);
                op = FileUtils.getFileProperties(path, Constants.OBJECT_TYPE_FILE);
                return FileUtils.getNumberPermissionsFromString(op.getPermissions()).equals(mod);
            }
        } catch (Exception e) {
            Log.e("setPermissions", null, e);
        }
        return false;
    }

    //получение статистики по распределению памяти
    public static ArrayList<String> getSpaceUsage(String path) {
        ArrayList<String> stat = new ArrayList<String>();
        try {
            if (path != null) {
                if (path.equals(System.getenv("EXTERNAL_STORAGE")) || path.contains(System.getenv("EXTERNAL_STORAGE"))) {
                    return getMainOperationsTools().runProcess(new String[]{Constants.SH_PATH, "-c", GET_SPACE_USAGE_STAT.replace("PATH_FOR_REPLACE", path)});
                } else {
                    stat = getMainOperationsTools().runProcess(new String[]{Constants.SH_PATH, "-c", GET_SPACE_USAGE_STAT.replace("PATH_FOR_REPLACE", path)}); //для остальных путей
                    /*if (stat.isEmpty()) { //если не хватает прав
                        stat = getMainOperationsTools().runProcessFromSU(GET_SPACE_USAGE_STAT.replace("PATH_FOR_REPLACE", path), true);
                    }*/
                }

                for (int i = 0; i < stat.size(); i++) {
                    String[] array = stat.get(i).split(" ");
                    if (!path.contains(array[3])) {
                        stat.remove(i); //удаляем несовпадающие разделы
                    }
                }
            } else {
                //stat = getMainOperationsTools().runProcessFromSU(GET_SPACE_USAGE_STAT.replace("PATH_FOR_REPLACE", ""), true);
                stat = getMainOperationsTools().runProcess(new String[]{Constants.SH_PATH, "-c", GET_SPACE_USAGE_STAT.replace("PATH_FOR_REPLACE", "")}); //для остальных путей
            }

        } catch (Exception e) {
            Log.e("getSpaceUsage", null, e);
        }
        return stat;
    }



    public static ArrayList<String> openTextFile(String path) {
        ArrayList<String> result = new ArrayList<String>();
        try {
            String command = OPEN_TEXT_FILE
                    .replace("PATH_FOR_REPLACE", path);
            if(new File(path).canRead()) {
                result = getMainOperationsTools().runProcess(new String[]{Constants.SH_PATH, "-c", command});
            } else {
                result = getMainOperationsTools().runProcessFromSU(command, true);
            }
        } catch (Exception e) {
            Log.e("openTextFile", null, e);
        }
        return result;
    }

    public static Boolean saveText(String text, String file) {
        try {
            text = text.replace("$", "\\$");
            text = text.replaceAll("\"", "\\\\\"");
            String command = SAVE_TEXT_FILE.replace("TEXT_FOR_REPLACE", text);
            command = command.replaceAll("PATH_FOR_REPLACE", file);
            //Log.w("save", command);
            if (new File(file).delete()) {
                String s = getMainOperationsTools().runProcess(new String[]{Constants.SH_PATH, "-c", command}).get(0);
                //Log.w("test", s);
                if (!s.isEmpty()) {
                    return true;
                }
            } else {
                if (getMainOperationsTools().delete(file)) {
                    if (!getMainOperationsTools().runProcessFromSU(command, false).isEmpty()) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            Log.e("save", null, e);
        }
        return false;
    }

    public static Integer getObjectsCountInDir(String path, boolean calcDirsCount) {
        try {
            String commandForDirs = GET_DIRS_COUNT_IN_CURR_DIR.replace("PATH_FOR_REPLACE", path);
            int dirsCount = Integer.parseInt(getMainOperationsTools().runProcess(new String[]{Constants.SH_PATH, "-c", commandForDirs}).get(0));
            if(calcDirsCount) {
                return dirsCount;
            } else {
                String commandForFiles = GET_ALL_OBJECTS_COUNT_IN_CURR_DIR.replace("PATH_FOR_REPLACE", path);
                int allObjectsCount = Integer.parseInt(getMainOperationsTools().runProcess(new String[]{Constants.SH_PATH, "-c", commandForFiles}).get(0));
                return allObjectsCount-dirsCount;
            }
        } catch (Exception e) {
            Log.e("getObjectsCountInDir", null, e);
        }
        return 0;
    }

    public static Integer getObjectsCountInDir(Dir dir, boolean calcDirsCount) {
        try {
            int dirs_count=0;
            int files_cont=0;
            for (Object o:dir.getObjects()) {
                if(o.type.equals(Object.TYPE_DIR) || o.type.equals(Object.TYPE_SYMLINK_DIR)) {
                    dirs_count++;
                }
                if(o.type.equals(Object.TYPE_FILE) || o.type.equals(Object.TYPE_SYMLINK_FILE)) {
                    files_cont++;
                }
            }
            if(calcDirsCount) {
                return dirs_count;
            } else {
                return files_cont;
            }
        } catch (Exception e) {
            Log.e("getObjectsCountInDir", null, e);
        }
        return 0;
    }
}

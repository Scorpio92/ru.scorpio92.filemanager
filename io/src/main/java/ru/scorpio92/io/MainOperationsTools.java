package ru.scorpio92.io;

import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 * Created by scorpio92 on 18.07.16.
 */
public class MainOperationsTools {

    private  final String SH_PATH = "/system/bin/sh";
    private  final String SYSTEM_MOUNT_BIN_PATH = "/system/bin/mount"; //через busybox в первый раз после запуска устройства /system почему то не монтируется
    private  final String BUSYBOX_DEFAULT_PATH = "busybox"; //должен быть такой путь
    private  String BUSYBOX_PATH; //должен быть такой путь
    private  final String SU_COMMAND = "su";
    private  Boolean allowSU = true; //по умолчанию можно использовать SU

    private  final String GET_MOUNT_POINTS_COMMAND = "BB=BUSYBOX_FOR_REPLACE; $BB mount | $BB cut -d ' ' -f3";
    private  final String GET_MOUNT_POINT_FREE_SPACE_COMMAND = "BB=BUSYBOX_FOR_REPLACE; $BB df -P \"PATH_FOR_REPLACE\" | $BB sed 1d | $BB tr -s ' ' | $BB cut -d ' ' -f4";
    private static final String GET_DIR_SIZE_IN_KB = "BB=BUSYBOX_FOR_REPLACE; $BB echo $($BB du -HLsk \"PATH_FOR_REPLACE\") | $BB cut -d ' ' -f1";
    private static final String GET_FILE_SIZE_IN_BYTES = "BB=BUSYBOX_FOR_REPLACE; FILE=\"PATH_FOR_REPLACE\"; $BB stat \"$FILE\" | $BB grep 'Size:' | $BB sed 's/^[ \\t]*//;s/[ \\t]*$//' | $BB cut -d ' ' -f2 | $BB sed s/[^0-9]//g";
    private  final String CHECK_FILE_EXISTS_COMMAND = "BB=BUSYBOX_FOR_REPLACE; $BB ls -ld \"PATH_FOR_REPLACE\"";
    private  final String CREATE_EMPTY_FILE_COMMAND = "BB=BUSYBOX_FOR_REPLACE; $BB echo '\n' > \"PATH_FOR_REPLACE\"";
    private  final String CREATE_NEW_DIR_COMMAND = "BB=BUSYBOX_FOR_REPLACE; $BB mkdir -p \"PATH_FOR_REPLACE\"";
    private  final String COPY_COMMAND = "BB=BUSYBOX_FOR_REPLACE; $BB cp \"SOURCE_PATH_FOR_REPLACE\" \"DIST_PATH_FOR_REPLACE\"";
    private  final String DELETE_COMMAND = "BB=BUSYBOX_FOR_REPLACE; $BB rm -rf \"PATH_FOR_REPLACE\"";
    private  final String _REMOUNT_RW_COMMAND = "BB=BUSYBOX_FOR_REPLACE; $BB mount -o remount,rw \"PATH_FOR_REPLACE\"";
    private  final String _REMOUNT_RO_COMMAND = "BB=BUSYBOX_FOR_REPLACE; $BB mount -o remount,ro \"PATH_FOR_REPLACE\"";
    private  final String REMOUNT_RW_COMMAND = SYSTEM_MOUNT_BIN_PATH + " -o remount,rw \"PATH_FOR_REPLACE\"";
    private  final String REMOUNT_RO_COMMAND = SYSTEM_MOUNT_BIN_PATH + " -o remount,ro \"PATH_FOR_REPLACE\"";
    private  final String CHECK_ROOT = "BB=BUSYBOX_FOR_REPLACE; $BB ls /data";
    private  final String GET_ALL_SUBDIR_OBJECTS = "BB=BUSYBOX_FOR_REPLACE; $BB find \"PATH_FOR_REPLACE\"";

    //операция перемонтирования, к этим константам разрешаем доступ извне
    public static final int REMOUNT_RW = 0;
    public static final int REMOUNT_RO = 1;

    private  final String OBJECT_TYPE_UNKNOW_PROPERTY = "unknown";
    private  final int DEFAULT_COPY_BUFFER_SIZE = 2048;


    public MainOperationsTools() {
        BUSYBOX_PATH=BUSYBOX_DEFAULT_PATH;
    }

    public MainOperationsTools(String busybox_path) {
        BUSYBOX_PATH = busybox_path;
    }


    //true - разрешаем использование SU
    public void setAllowSU(Boolean b) {
        allowSU = b;
    }


    //запускаем процессы и получаем их вывод
    public  ArrayList<String> runProcess(String[] command) {
        ArrayList<String> result = new ArrayList<String>();
        BufferedReader in = null;
        ProcessBuilder pb = null;
        Process p = null;
        try {
            //если задан кастомный путь для BB - меняем его в команде
            if(BUSYBOX_PATH != null) {
                //if (!BUSYBOX_PATH.equals("busybox")) {
                    command[command.length-1] = command[command.length-1].replace("BUSYBOX_FOR_REPLACE", BUSYBOX_PATH); //у последнего элемента в массиве(самой команды) меняем путь к BB
                    Log.w("BUSYBOX_PATH:", BUSYBOX_PATH);
                    Log.w("command:", command[command.length-1]);
                //}
            } else {
                command[command.length-1] = command[command.length-1].replace("BUSYBOX_FOR_REPLACE", BUSYBOX_DEFAULT_PATH); //у последнего элемента в массиве(самой команды) меняем путь к BB
                Log.w("BUSYBOX_DEFAULT_PATH:", BUSYBOX_DEFAULT_PATH);
                Log.w("command:", command[command.length-1]);
            }
            //Log.w("command2", command[command.length-1]);
            pb = new ProcessBuilder(command);
            p = pb.start();
            in = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;
            while ((line = in.readLine()) != null) {
                result.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (p != null) {
                p.destroy();
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e("runProcess", null, e);
                }
            }
        }
        return result;
    }

    //запускаем команды от имени root
    public  ArrayList<String> runProcessFromSU(String command, Boolean needResult) {
        ArrayList<String> result = new ArrayList<String>();
        try {
            if(allowSU == null) { //если переменная обнулена, то ставим запрет на использование SU
                allowSU = false;
            }
            if(allowSU) { //если разрешено использовать SU
                if (needResult) {
                    result = runProcess(new String[]{SU_COMMAND, "-c", command});
                } else {
                    runProcess(new String[]{SU_COMMAND, "-c", command});
                }
            } else {
                result.clear(); //если права суперпользователя не подтверждены - возвращаем пустой результат
            }
        } catch (Exception e) {
            Log.e("runProcessFromSU", command, e);
            result.clear();
        }
        return result;
    }

    //монтирование rw/ro
    public  void remount(String path, int action) {
        try {
            String mountPoint = null;
            //ArrayList<String> mountPoints = runProcessFromSU(GET_MOUNT_POINTS_COMMAND, true);
            ArrayList<String> mountPoints = runProcess(new String[]{SH_PATH, "-c", GET_MOUNT_POINTS_COMMAND});
            for (String point:mountPoints) {
                //Log.w("remount", mountPoints.get(i));
                if (path.contains(point) && !point.equals("/")) {
                    mountPoint = point;
                    //break;
                }
            }
            if (mountPoint == null) {
                mountPoint = "/";
            }
            switch (action) {
                case REMOUNT_RW:
                    runProcessFromSU(REMOUNT_RW_COMMAND.replace("PATH_FOR_REPLACE", mountPoint), false);
                    //Log.w("remount rw", REMOUNT_RW_COMMAND.replace("PATH_FOR_REPLACE", mountPoint));
                    break;

                case REMOUNT_RO:
                    runProcessFromSU(REMOUNT_RO_COMMAND.replace("PATH_FOR_REPLACE", mountPoint), false);
                    //Log.w("remount ro", mountPoint);
                    break;
            }
        } catch (Exception e) {
            Log.e("remount", null, e);
        }
    }



    //создание нового файла
    public  Boolean createNewFile(String path, String fileName) {
        try {
            File f = new File(path + "/" + fileName);
            FileWriter writer = new FileWriter(f);
            writer.append("\n");
            writer.flush();
            writer.close();
            return true;
        } catch (Exception e) {
            Log.e("createNewFile", "try su", e);
            try {
                remount(path, REMOUNT_RW);
                runProcessFromSU(CREATE_EMPTY_FILE_COMMAND .replace("PATH_FOR_REPLACE", path + "/" + fileName), false);
                remount(path, REMOUNT_RO);
                if (checkExists(path + "/" + fileName, true)) {
                    return true;
                }
            } catch (Exception e2) {
                Log.e("createNewFile", "su fail", e);
            }
        }
        return false;
    }

    //создание новой папки
    public  Boolean createNewDir(String path, String dirName) {
        try {
            String dir = path + "/" + dirName;
            if (new File(dir).mkdir()) {
                return true;
            } else {
                remount(path, REMOUNT_RW);
                runProcessFromSU(CREATE_NEW_DIR_COMMAND.replace("PATH_FOR_REPLACE", dir), false);
                Log.w("createNewDir", dir);
                remount(path, REMOUNT_RO);
                if (checkExists(dir, true)) {
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e("createNewDir", null, e);
        }
        return false;
    }

    //простое копирование файлов (Java.IO)
    public  Boolean copyIO(InputStream IS, OutputStream OS) {
        try {
            //Log.w("!!!!!!!!!!!!!!!!!!!!", SettingsUtils.getStringSettings(VarStore.getAppContext(), COPY_BUFFER_SIZE_KEY));
            //byte[] buf = new byte[Integer.parseInt(SettingsUtils.getStringSettings(VarStore.getAppContext(), COPY_BUFFER_SIZE_KEY))];
            byte[] buf = new byte[DEFAULT_COPY_BUFFER_SIZE];
            int len;
            while ((len = IS.read(buf)) > 0) {
                OS.write(buf, 0, len);
            }
            IS.close();
            OS.flush();
            OS.close();
            return true;
        } catch (Exception e) {
            Log.e("copyIO", null, e);
        }
        return false;
    }

    public  Boolean copyIO(String sourceFile, String distFile) {
        try {
            //Log.w("!!!!!!!!!!!!!!!!!!!!", SettingsUtils.getStringSettings(VarStore.getAppContext(), COPY_BUFFER_SIZE_KEY));
            //byte[] buf = new byte[Integer.parseInt(SettingsUtils.getStringSettings(VarStore.getAppContext(), COPY_BUFFER_SIZE_KEY))];
            byte[] buf = new byte[DEFAULT_COPY_BUFFER_SIZE];
            int len;
            FileInputStream IS = new FileInputStream(sourceFile);
            FileOutputStream OS = new FileOutputStream(distFile);
            while ((len = IS.read(buf)) > 0) {
                OS.write(buf, 0, len);
            }
            IS.close();
            OS.flush();
            OS.close();
            return true;
        } catch (Exception e) {
            Log.e("copyIO", null, e);
        }
        return false;
    }

    //быстрое копирование с использованием java.nio. Для файлов < 2ГБ
    public  Boolean copyNIO(String sourceFile, String distFile) {
        try {
            Log.w("copyNIO", "try fast NIO copyIO");
            //Log.w("IOcopy", "source file: " + sourcePath);
            FileInputStream IS = new FileInputStream(sourceFile);
            FileOutputStream OS = new FileOutputStream(distFile);
            FileChannel inChannel = IS.getChannel();
            FileChannel outChannel = OS.getChannel();
            //т.к. при копировании объекты помещаются в оперативную память,то тут может возникнуть Out of memory
            inChannel.transferTo(0, inChannel.size(), outChannel);
            IS.close();
            OS.close();
            //Log.w("IOcopy", "dist file: " + distPath);
            return true;
        } catch (Exception e) {
            Log.e("MainOperations.copyNIO", null, e);
        }
        return false;
    }

    public  Boolean copyNIO(FileInputStream IS, FileOutputStream OS) {
        try {
            Log.w("copyNIO", "try fast NIO copyIO");
            //Log.w("IOcopy", "source file: " + sourcePath);
            //FileInputStream inStream = new FileInputStream(sourcePath);
            //FileOutputStream outStream = new FileOutputStream(distPath);
            FileChannel inChannel = IS.getChannel();
            FileChannel outChannel = OS.getChannel();
            //т.к. при копировании объекты помещаются в оперативную память,то тут может возникнуть Out of memory
            inChannel.transferTo(0, inChannel.size(), outChannel);
            IS.close();
            OS.close();
            //Log.w("IOcopy", "dist file: " + distPath);
            return true;
        } catch (Exception e) {
            Log.e("MainOperations.copyNIO", null, e);
        }
        return false;
    }

    //копирование из-под SU
    public  Boolean copySU(String sourceFile, String distFile, boolean doRemount) {
        try {
            if(doRemount)
                remount(distFile, REMOUNT_RW);
            //если копируемый объект директория - создаем ее
            if (new File(sourceFile).isDirectory()) {
                runProcessFromSU(CREATE_NEW_DIR_COMMAND.replace("PATH_FOR_REPLACE", distFile), false);
            } else { //если файл - копируем
                runProcessFromSU(COPY_COMMAND.replace("SOURCE_PATH_FOR_REPLACE", sourceFile).replace("DIST_PATH_FOR_REPLACE", distFile), false);
            }

            if (doRemount)
                remount(distFile, REMOUNT_RO);

            //проверяем
            if (checkExists(distFile, true)) {
                return true;
            }

        } catch (Exception e) {
            Log.e("copySU", null, e);
        }
        return false;
    }

    public  Boolean delete(String path) {
        try {
            return new File(path).delete(); //пытаемся удалить стандартными средствами

        } catch (Exception e) {
            Log.w("delete fail", path);
        }
        return false;
    }

    public  Boolean deleteSU(String path, boolean doRemount) {
        try {
            if (new File(path).delete()) { //пытаемся удалить стандартными средствами
                return true;
            } else { //если не получилось - удаляем из под рута
                //Log.w("delete su", path);
                if (doRemount)
                    remount(path, REMOUNT_RW);
                runProcessFromSU(DELETE_COMMAND.replace("PATH_FOR_REPLACE", path), false);
                if (doRemount)
                    remount(path, REMOUNT_RO);
                if (!checkExists(path, true)) {
                    return true;
                }
            }
        } catch (Exception e) {
            Log.w("delete fail", path);
        }
        return false;
    }



    //передаем путь, на выходе получаем массив путей (для отображения в верхней панели)
    public  ArrayList<String> getAllPossiblePaths(String path) {
        ArrayList<String> paths = new ArrayList<String>();
        try {
            String[] parsed = path.split("/");
            paths.add("/");
            String fullPath="/";
            for (int i=0; i< parsed.length; i++) {
                if(!parsed[i].equals("")) {
                    paths.add(fullPath + parsed[i]);
                    fullPath = fullPath + parsed[i] + "/";
                }
            }
        } catch (Exception e) {
            Log.e("getAllPossiblePaths", null, e);
        }
        return paths;
    }

    //получаем родительскую директрию
    public  String getParentDirectory(String path) {
        try {
            if (path.equals("/")) {
                return "/";
            } else {
                return new File(path).getParent();
            }
        } catch (Exception e) {
            Log.e("getParentDirectory", null, e);
        }
        return null;
    }

    //проверка существования файла
    public  Boolean checkExists(String path, Boolean runAsSU) {
        try {
            if (runAsSU) {
                return !runProcessFromSU(CHECK_FILE_EXISTS_COMMAND.replace("PATH_FOR_REPLACE", path), true).isEmpty();
            } else {
                //return !runProcess(new String[]{SH_PATH, "-c", CHECK_FILE_EXISTS_COMMAND.replace("PATH_FOR_REPLACE", path)}).isEmpty();
                return new File(path).exists();
            }
        } catch (Exception e) {
            Log.e("getMountStatus", null, e);
        }
        return false;
    }

    //проверка объекта на то ялвялется ли он симлинком или нет
    //если является - возвращаем истинный путь
    public  String getRightPath(String path) {
        try {
            return new File(path).getCanonicalPath();
        } catch (Exception e) {
            Log.e("getRightPath", null, e);
        }
        return path;
    }

    //получение имени файла из его абсолютного пути
    public  String getLastPathComponent(String filePath) {
        try {
            if (filePath.equals("/")) {
                return "";
            }
            if (filePath.substring(filePath.length() - 1, filePath.length()).equals("/")) { //если папка пустая и путь оканчивается на /
                filePath = filePath.substring(0, filePath.length() - 1);
            }
            String[] segments = filePath.split("/");
            return segments[segments.length - 1];
        } catch (Exception e) {
            Log.e("getLastPathComponent", null, e);
        }
        return "";
    }

    //получаем расширение файла
    public  String getFileExt(String path) {
        try {
            String tmp = path.substring(path.lastIndexOf(".") + 1, path.length());
            if (!tmp.equals("") && tmp != path) {
                return tmp;
            }
            return "";
        } catch (Exception e) {
            Log.e("getFileExt", null, e);
        }
        return "";
    }

    //получаем из float размер в байтах, килобайтах, мегабайтах или гигабайтах
    public  String getRightSize(long size) {
        try {
            if (size < 1024) {
                return Long.toString(size) + " B"; //байты
            }
            if ((size >= 1024) && (size < 1024 * 1024)) {
                return String.format("%.2f", (float) size / 1024) + " KB"; //КБ
            }
            if ((size >= 1024 * 1024) && (size < 1024 * 1024 * 1024)) {
                return String.format("%.2f", (float) size / 1024 / 1024) + " MB"; //МБ
            }
            if (size >= 1024 * 1024 * 1024) {
                return String.format("%.2f", (float) size / 1024 / 1024 / 1024) + " GB"; //ГБ
            }
        } catch (Exception e) {
            Log.e("getObjectSize", null, e);
        }
        return "0 Bytes";
    }

    //получение MIME типа файла
    public  String getMIMEType(String path) {
        try {
            String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(getFileExt(path));
            if (type != null)
                return type;

        } catch (Exception e) {
            Log.e("getMIMEType", null, e);
        }
        return OBJECT_TYPE_UNKNOW_PROPERTY;
    }

    public Boolean checkRoot() {
        try {
            if(!runProcess(new String[]{SU_COMMAND, "-c", CHECK_ROOT}).isEmpty()) {
                return true;
            }
        } catch (Exception e) {
            Log.e("checkRoot", null, e);
        }
        return false;
    }

    public long getFreeSpaceOnMountPoint(String path) {
        try {
            String mountPoint = null;
            ArrayList<String> mountPoints = runProcess(new String[]{SH_PATH, "-c", GET_MOUNT_POINTS_COMMAND});
            for (String point:mountPoints) {
                if (path.contains(point) && !point.equals("/")) {
                    mountPoint = point;
                    //break;
                }
            }
            if(mountPoint!=null) {
                return Long.parseLong(runProcess(new String[]{SH_PATH, "-c", GET_MOUNT_POINT_FREE_SPACE_COMMAND.replace("PATH_FOR_REPLACE", mountPoint)}).get(0)) * 1024; //в байтах

            }
        } catch (Exception e) {
            Log.e("getFreeSpaceOnMountPoint", null, e);
        }
        return 0;
    }


    //размер файла: String
    //du -HLsk /data
    //если второй параметр false - узнаем размер директории, true - размер файла
    public long getObjectSize(String path, Boolean isFile) {
        String command="";
        try {
            if(isFile) {
                command = GET_FILE_SIZE_IN_BYTES
                        .replace("PATH_FOR_REPLACE", path);
            } else {
                command = GET_DIR_SIZE_IN_KB
                        .replace("PATH_FOR_REPLACE", path);
            }
            if(isFile) {
                return Long.parseLong(runProcess(new String[]{SH_PATH, "-c", command}).get(0));
            } else {
                return 1024 * Long.parseLong(runProcess(new String[]{SH_PATH, "-c", command}).get(0));
            }
        } catch (Exception e) {
            Log.e("getObjectSize", null, e);
            try {
                if(isFile) {
                    return Long.parseLong(runProcessFromSU(command, true).get(0));
                } else {
                    return 1024 * Long.parseLong(runProcessFromSU(command, true).get(0));
                }
            } catch (Exception e2) {
                Log.e("getObjectSize", null, e2);
            }
        }
        return 0;
    }

    //получаем список всех подобъектов данной директории (включая саму директорию)
    public ArrayList<String> getAllObjectsInFolder(String dirPath) {
        ArrayList<String> resultList = new ArrayList<String>();
        try {
            String command = GET_ALL_SUBDIR_OBJECTS
                    .replace("PATH_FOR_REPLACE", dirPath);

            if (new File(dirPath).listFiles() != null) {//если хватает прав
                resultList.addAll(runProcess(new String[]{SH_PATH, "-c", command}));
            } else {
                resultList.addAll(runProcessFromSU(command, true));
            }
        } catch (Exception e) {
            Log.e("getAllObjectsInFolder", null, e);
        }
        return resultList;
    }

    //получение полного списка объектов(включая подобъекты) по списку средствами java.io
    public ArrayList<String> getAllObjectsFromList(ArrayList<String> list) {
        ArrayList<String> objects = new ArrayList<String>();
        try {
            for(String obj:list) {
                if(new File(obj).isDirectory()) {
                    objects.addAll(getAllObjectsInFolder(obj));
                } else {
                    objects.add(obj);
                }
            }
        } catch (Exception e) {
            Log.w("getAllObjectsFromList", null, e);
        }
        return objects;
    }
}

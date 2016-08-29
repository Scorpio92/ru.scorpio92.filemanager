package ru.scorpio92.io;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by scorpio92 on 01.03.16.
 */
public class MainOperations {

    //ограничение которое учитывается при копировании больших файлов
    //если размер файла < 2ГБ копируем его с помощью java.nio
    private final long NIO_COPY_FILESIZE_LIMIT = 2147000000l; //2147483648=2048MB
    private final int PROGRESS_UPDATE_INTERVAL = 20; //мсек
    private final String FILE_OPERATIONS_DUPLICATE_APPENDIX = "-copy";

    private MainOperationsParams mainOperationsParams;
    private MainOperationsTools mainOperationsTools;
    private Thread thread;
    private int status;
    private int progress; //счетчик для прогресс бара
    //private int itemsCount; //счетчик объектов над которыми выполняется операция (например удаление 2-х файлов)
    private int maxProgress;
    private Boolean stop;

    public MainOperations(MainOperationsParams mainOperationsParams) {
        this.mainOperationsParams=mainOperationsParams;
        status =MainOperationsConstants.IN_PROGRESS;
        progress =0;
        //itemsCount=0;
        stop=false;
        //MainOperationsTools.BUSYBOX_PATH = Constants.BUSYBOX_PATH;
        if(mainOperationsParams.getMainOperationsTools()==null) {
            mainOperationsTools = new MainOperationsTools();
        } else {
            mainOperationsTools = mainOperationsParams.getMainOperationsTools();
        }
    }

    public MainOperationsParams getMainOperationsParams() {return mainOperationsParams;}

    //метод запуска
    public void start() {
        if(mainOperationsParams.getRunInThread()) { //если требуется запуск в отдельном потоке
            thread = new Thread() {
                @Override
                public void run() {
                    try {
                        startOperation();
                    } catch (Exception e) {
                        Log.e("MainOper.thread.start", null, e);
                    }
                }
            };
            thread.start();
        } else { //запуск в основном потоке
            startOperation();
        }
    }

    //выбор и запуск нужной операции
    private void startOperation() {
        switch (mainOperationsParams.getOperation()) {
            case MainOperationsConstants.FILE_OPERATION_RENAME:
                showProgressDialog();
                rename(mainOperationsParams.getSourceFile(), mainOperationsParams.getFileName());
                break;
            case MainOperationsConstants.FILE_OPERATION_DELETE:
                maxProgress = mainOperationsParams.getPaths().size();
                showProgressDialog();
                delete(mainOperationsParams.getPaths());
                break;
            case MainOperationsConstants.FILE_OPERATION_COPY:
                maxProgress = mainOperationsParams.getPaths().size();
                showProgressDialog();
                copy_move(mainOperationsParams.getPaths(), mainOperationsParams.getDistFile(), MainOperationsConstants.FILE_OPERATION_COPY);
                break;
            case MainOperationsConstants.FILE_OPERATION_MOVE:
                maxProgress = mainOperationsParams.getPaths().size() * 2; //т.к. сначала копируем, а потом удаляем родительские объекты
                showProgressDialog();
                copy_move(mainOperationsParams.getPaths(), mainOperationsParams.getDistFile(), MainOperationsConstants.FILE_OPERATION_MOVE);
                break;
            /*case MainOperationsConstants.FILE_OPERATION_SEARCH:
                showProgressDialog();
                search(mainOperationsParams.getSourceFile(), mainOperationsParams.getFileName());
                break;*/
        }
    }

    //остановка путем нажатия на кнопку Отмены в диалоге операции
    public void stop() {
        try {
            stop=true;
            thread.interrupt();
        } catch (Exception e) {
            Log.e("MainOper.thread.stop", null, e);
        }
    }

    //статус операции
    public int getStatus() {
        return status;
    }

    //прогресс операции
    public int getProgress() {
        return progress;
    }

    private void showProgressDialog() {
        try {
            if(mainOperationsParams.getMainOperationsDialogParams()==null) {
                return;
            }

            final MainOperationsDialogParams mainOperationsDialogParams = mainOperationsParams.getMainOperationsDialogParams();
            final AlertDialog.Builder alertDialog = mainOperationsDialogParams.alertDialog;

            alertDialog.setNegativeButton(mainOperationsDialogParams.getNegativeButtonText(),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            stop();
                            //выполняем метод переданный сюда извне. В данном случае это обновление текущей директории
                            /*Method method = mainOperationsDialogParams.method;
                            if(method !=null) {
                                try {
                                    method.invoke(mainOperationsDialogParams.context);
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();
                                }
                            }*/
                            dialog.cancel();
                        }
                    });

            if(mainOperationsDialogParams.progressBar!=null) {
                mainOperationsDialogParams.progressBar.setMax(maxProgress);
            }

            final AlertDialog[] alert = {null};
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    alert[0] = alertDialog.create();
                }
            });

            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        int i = 0;
                        while(true) { //ждем обновление статуса
                            final float sec = (float) i;
                            if(getStatus()!=MainOperationsConstants.IN_PROGRESS) {
                                break;
                            }
                            //если задействованы оба элемента - обновляем их сразу. так наиболее оптимально
                            if(mainOperationsDialogParams.timer!=null && mainOperationsDialogParams.progressBar!=null) {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mainOperationsDialogParams.timer.setText(String.format("%.2f", (float) progress * 100 / maxProgress) + "%" + "/" + String.format("%.3f", sec / 1000) + " sec");
                                        mainOperationsDialogParams.progressBar.setProgress(progress);
                                    }
                                });
                            } else {
                                //таймер операции
                                if (mainOperationsDialogParams.timer != null) {
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mainOperationsDialogParams.timer.setText(String.format("%.2f", (float) progress * 100 / maxProgress) + "%" + "/" + String.format("%.3f", sec / 1000) + " sec");
                                        }
                                    });
                                }
                                //прогресс операции
                                if (mainOperationsDialogParams.progressBar != null) {
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mainOperationsDialogParams.progressBar.setProgress(progress);
                                        }
                                    });
                                }
                            }
                            i+=PROGRESS_UPDATE_INTERVAL;
                            sleep(PROGRESS_UPDATE_INTERVAL);
                        }
                        switch (getStatus()) {
                            case MainOperationsConstants.COMPLETE:
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(!stop) { //если операция не была прервана пользователем
                                            //выполняем метод переданный сюда извне. В данном случае это обновление текущей директории
                                            Method method = mainOperationsDialogParams.method;
                                            if (method != null) {
                                                try {
                                                    method.invoke(mainOperationsDialogParams.context);
                                                } catch (IllegalAccessException e) {
                                                    e.printStackTrace();
                                                } catch (InvocationTargetException e) {
                                                    e.printStackTrace();
                                                } catch (IllegalArgumentException e) {
                                                    e.printStackTrace();
                                                }
                                            }

                                            Toast.makeText(mainOperationsDialogParams.context, mainOperationsDialogParams.successText, Toast.LENGTH_SHORT).show();

                                        }
                                        alert[0].dismiss();
                                    }
                                });
                                break;
                            case MainOperationsConstants.ERROR:
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(mainOperationsDialogParams.context, mainOperationsDialogParams.failText, Toast.LENGTH_SHORT).show();
                                        alert[0].dismiss();
                                    }
                                });
                                break;
                            case MainOperationsConstants.NO_SPACE_IN_TARGET:
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(mainOperationsDialogParams.context, mainOperationsDialogParams.noFreeSpaceText, Toast.LENGTH_SHORT).show();
                                        alert[0].dismiss();
                                    }
                                });
                                break;
                        }
                    } catch (Exception e) {
                        Log.e("MainOper.showProgDialog", null, e);
                    }
                }
            };
            thread.start();

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    alert[0].show();
                }
            });

        } catch (Exception e) {
            Log.e("MainOper.showProgDialog", null, e);
        }
    }
    //////////////////////////////////////////////////////////////////////////////////

    //переименование файлов и директорий
    private Boolean rename(String file, String newFileName) {
        try {
            String pathToNewObj = mainOperationsTools.getParentDirectory(file) + "/" + newFileName;
            //если такой файл уже есть - возвращаем ошибку
            if(mainOperationsTools.checkExists(pathToNewObj, false)) {
                status = MainOperationsConstants.ERROR;
                return false;
            }
            File newFile=new File(pathToNewObj);
            if(new File(file).renameTo(newFile)) {
                status = MainOperationsConstants.COMPLETE;
                return true;
            } else {
                //если такой файл уже есть - возвращаем ошибку
                if(mainOperationsTools.checkExists(pathToNewObj, true)) {
                    status = MainOperationsConstants.ERROR;
                    return false;
                }
                mainOperationsTools.remount(file, MainOperationsTools.REMOUNT_RW);
                mainOperationsTools.runProcessFromSU("rename " + file.replaceAll(" ", "\\\\ ") + " " + pathToNewObj.replaceAll(" ", "\\\\ "), false);
                mainOperationsTools.remount(file, MainOperationsTools.REMOUNT_RO);
                if(!mainOperationsTools.checkExists(pathToNewObj, true)) {
                    status = MainOperationsConstants.ERROR;
                    return false;
                } else {
                    status = MainOperationsConstants.COMPLETE;
                    return true;
                }
            }

        } catch (Exception e) {
            status = MainOperationsConstants.ERROR;
            Log.e("MainOperations.rename",null,e);
        }
        return false;
    }

    //удаление файлов. на вход подается массив путей. на выходе - int массив:
    //1 элемент - общее кол-во файлов, 2 элемент - кол-во успешно удаленный файлов
    private void delete(ArrayList<String> paths) {
        boolean wasRemount = false;
        String remountPath = null;
        try {
            while (!paths.isEmpty()) {
                if(stop) {
                    status = MainOperationsConstants.COMPLETE;
                    return;
                }
                String path = paths.get(paths.size()-1);
                Log.w("delete", path);
                try {
                    if(mainOperationsTools.delete(path)) {
                        progress++;
                    } else {
                        if(!wasRemount) {
                            mainOperationsTools.remount(path, MainOperationsTools.REMOUNT_RW);
                            wasRemount = true;
                            remountPath = path;
                        }
                        if(mainOperationsTools.deleteSU(path, false)) {
                            progress++;
                        }
                    }
                } catch (Exception e) {
                    Log.w("delete fail", path);
                }
                finally {
                    paths.remove(paths.size()-1); //даже при возникновении сбоя удаляем из списка проблемный файл и продолжаем удаление
                }
            }
            if(paths.isEmpty() && progress==maxProgress) {
                status = MainOperationsConstants.COMPLETE;
            } else {
                status = MainOperationsConstants.ERROR;
            }
            //return result;
        } catch (Exception e) {
            status = MainOperationsConstants.ERROR;
            Log.e("MainOperations.delete",null,e);
        } finally {
            if(wasRemount) {
                try {
                    mainOperationsTools.remount(remountPath, MainOperationsTools.REMOUNT_RO);
                } catch (Exception e2) {
                    Log.e("MainOperations.delete",null,e2);
                }

            }
        }
    }

    //копирование и перемещение
    private void copy_move(ArrayList<String> paths, String distPath, int action) {
        boolean wasRemount = false;
        String remountPath = null;

        String objOldPath = null;
        String oldBase = null;
        String objNewPath = null;
        //ArrayList<String> forRename = null; //массив переименованных путей
        ArrayList<String> forDelete = null; //массив объектов на удаление
        Map<String, String> forRename = new HashMap<String, String>();

        try {
            //вычисляем размер копируемых объектов и сравниваем его с оствшимся местом в distPath
            long totalSize=0;
            long freeSpace =0;

            for(String path:paths) {
                if(new File(path).isFile()) {
                    totalSize+=mainOperationsTools.getObjectSize(path, true);
                } else {
                    totalSize+=mainOperationsTools.getObjectSize(path, false);
                }
            }

            Log.w("totalSize", Long.toString(totalSize));

            freeSpace = mainOperationsTools.getFreeSpaceOnMountPoint(distPath);
            Log.w("totalSize", Long.toString(freeSpace));

            if(totalSize>= freeSpace) {
                status = MainOperationsConstants.NO_SPACE_IN_TARGET;
                return;
            }


            //forRename = new ArrayList<String>();
            forDelete = new ArrayList<String>();

            //директория откуда копируем, корневая часть
            //oldBase = mainOperationsTools.getParentDirectory(paths.get(0));
            oldBase = mainOperationsParams.getSourceFile();
            Log.w("oldBase", oldBase);
            Log.w("distPath", distPath);

            //начинаем копирование по списку
            while (!paths.isEmpty()) {
                try {
                    //получаем объект для копирования
                    objOldPath = paths.get(0);
                    Log.w("objOldPath", objOldPath);

                    //меняем основную часть пути на ту куда копируем
                    objNewPath = objOldPath.replace(oldBase, distPath);
                    Log.w("objNewPath", objNewPath);

                    //проверяем: если данный объект уже есть по соответствующему пути, делаем замену в пути (на путь с аппендиксом, -copyX)
                    for (Map.Entry entry : forRename.entrySet()) {
                        if(objNewPath.contains(entry.getKey().toString())) {
                            //Log.w("forRename", "entry.getKey().toString(): "+ entry.getKey().toString() + " entry.getValue().toString(): " + entry.getValue().toString());
                            objNewPath=objNewPath.replace(entry.getKey().toString(), entry.getValue().toString());
                            Log.w("replace from HM", objNewPath);
                        }
                    }

                    //проверяем: если данный объект уже есть по соответствующему пути, добавляем аппендикс к имени
                    //и если это директория, заносим в хэш мап
                    if (mainOperationsTools.checkExists(objNewPath, false)) {
                        Log.w("checkExists", objNewPath);
                        int i = 1;
                        String objNewPath_before_replace=objNewPath;
                        objNewPath += FILE_OPERATIONS_DUPLICATE_APPENDIX;
                        String tmpPath = objNewPath;
                        objNewPath = objNewPath + Integer.toString(i);
                        while (mainOperationsTools.checkExists(objNewPath, false)) {
                            objNewPath = tmpPath + Integer.toString(i);
                            i++;
                        }

                        //добавляем в хэш мап только если это директория. ключ - старый путь, значение - новый путь
                        if(new File(objOldPath).isDirectory()) {
                            forRename.put(objNewPath_before_replace, objNewPath);
                            Log.w("put to HM", "objNewPath_before_replace: " + objNewPath_before_replace + "  objNewPath: " + objNewPath);
                        }

                        Log.w("checkExists objNewPath", objNewPath);
                    }

                    //если не удалось выполнить операцию стандартными средствами - выполняем из-под рута
                    Boolean OK;

                    if(new File(objOldPath).isDirectory()) {
                        OK = new File(objNewPath).mkdir();
                    } else {
                        if(new File(objOldPath).length() < NIO_COPY_FILESIZE_LIMIT) {
                            OK = mainOperationsTools.copyNIO(objOldPath, objNewPath); //если файл меньше 2ГБ копируем его с помощью файловых каналов
                            //при копировании больших объектов в оперативке может не хватить свободной памяти
                            //и тогда возникнет exception
                            //+ предотвращаем др. возможные случаи
                            if(!OK) {
                                //OK = IOcopy(objOldPath, objNewPath);
                                OK = mainOperationsTools.copyIO(objOldPath, objNewPath);
                            }
                        } else {
                            //OK = IOcopy(objOldPath, objNewPath);
                            OK = mainOperationsTools.copyIO(objOldPath, objNewPath);
                        }
                    }

                    if(OK) {
                        //если это операция перемещения - удаляем исходный файл
                        if (action == MainOperationsConstants.FILE_OPERATION_MOVE) {
                            forDelete.add(objOldPath);
                        }

                        //операция копирования объекта успешно выполнилась
                        progress++;
                    } else { //пробуем копировать из под рута, если стандартным способом не получилось
                        Log.w("", "MainOperations.copyIO  fail, try su");

                        if(!wasRemount) {
                            mainOperationsTools.remount(objNewPath, MainOperationsTools.REMOUNT_RW);
                            wasRemount = true;
                            remountPath = objNewPath;
                        }

                        //начинаем копирование из под рута
                        if(mainOperationsTools.copySU(objOldPath, objNewPath, false)) {
                            //если это операция перемещения - удаляем исходный файл
                            if (action == MainOperationsConstants.FILE_OPERATION_MOVE) {
                                forDelete.add(objOldPath);
                            }
                            //операция копирования объекта успешно выполнилась
                            progress++;
                        }
                    }

                } catch (Exception e) {
                    Log.w("MainOperations.cp fail", objOldPath);
                }
                finally {
                    paths.remove(0); //даже при возникновении сбоя удаляем из списка проблемный файл и продолжаем удаление
                    //progress++;
                }
            }

            //если выполняется операция премещения - удаляем исходниые объекты
            if(action == MainOperationsConstants.FILE_OPERATION_MOVE) {
                //int deleteResult = 0;
                try {
                    if(stop) {
                        status =MainOperationsConstants.COMPLETE;
                        return;
                    }
                    delete(forDelete);
                } catch (Exception e) {
                    Log.w("move.delete", objOldPath);
                }
            }

            if(paths.isEmpty() && progress==maxProgress) {
                status = MainOperationsConstants.COMPLETE;
            } else {
                status = MainOperationsConstants.ERROR;
            }

        } catch (Exception e) {
            status = MainOperationsConstants.ERROR;
            Log.e("MainOperations.copyIO",null,e);
        } finally {
            if(wasRemount) {
                try {
                    mainOperationsTools.remount(remountPath, MainOperationsTools.REMOUNT_RO);
                } catch (Exception e2) {
                    Log.e("MainOperations.copyIO",null,e2);
                }

            }
        }
    }
}

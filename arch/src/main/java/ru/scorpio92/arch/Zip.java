package ru.scorpio92.arch;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.progress.ProgressMonitor;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by scorpio92 on 14.06.16.
 */
public class Zip {

    private ZipParams zipParams;

    private int progress;
    private int maxProgress;
    private int status;

    private Thread thread;
    private Boolean stop;



    public Zip(ZipParams zipParams) {
        this.zipParams=zipParams;
        progress=0;
        status=ZipConstants.IN_PROGRESS;
        stop=false;
    }

    //метод запуска
    //запускаем всегда в доп. потоке
    public void start() {
        //if(zipParams.getRunInThread()) { //если требуется запуск в отдельном потоке
        thread = new Thread() {
            @Override
            public void run() {
                try {
                    //startOperation();
                    switch (zipParams.getOperation()) {
                        case ZipConstants.OPERATION_ZIP:
                            zip();
                            break;
                        case ZipConstants.OPERATION_UNZIP:
                            unzip();
                            break;
                    }
                } catch (Exception e) {
                    Log.e("Zip.thread.start", null, e);
                }
            }
        };
        thread.start();
        Log.w("zip.start", "started");
        //} else { //запуск в основном потоке
        //    startOperation();
        //}
    }

    public void stop() {
        try {
            stop=true;
            thread.interrupt();
        } catch (Exception e) {
            Log.e("MainOper.thread.stop", null, e);
        }
    }

    //архивация
    private void zip() {
        try {
            if(zipParams.getPartSize()==ZipConstants.DEFAULT_PART_SIZE) { //если архивируем без разбиения на части
                //устанавливаем величину progress bar
                maxProgress=100 * zipParams.getObjects().size(); //по 100% для каждого файла
                showProgressDialog();
                //Log.w("maxProgress", Integer.toString(maxProgress));
                zipNoSplit(true);
            } else { //если архив нужно разбить по частям, то сначала архивируем с выбранными параметрами, а затем разбиваем полученный архив на части путем повторной архивации с разбиением без сжатия
                //maxProgress=100 * zipParams.getObjects().size() + 100; //по 100% для каждого файла + 100% для создания архива с разбиением
                maxProgress=100 * zipParams.getObjects().size(); //по 100% для каждого файла
                showProgressDialog();
                //Log.w("maxProgress", "set progress for split" + Integer.toString(maxProgress));
                if (zipNoSplit(false)) { //если сжатие архива прошло успешно
                    zipWithSplit();
                }
            }

        } catch (Exception e) {
            status=ZipConstants.ERROR;
            Log.e("Zip.zip", null, e);
        }
    }

    private Boolean zipNoSplit(Boolean setCompete) { //если setComplete=false, значит метод был вызван из другого метода с целью продолжения операции и устанавливать статус Завершено не нужно
        try {

            //int generalResult=0;//общий статус архивации

            ZipFile zipFile = new ZipFile(zipParams.getDistPath());

            zipFile.setRunInThread(true);

            ZipParameters parameters = new ZipParameters();

            if(zipParams.getCompressLevel() == ZipConstants.DEFAULT_COMPRESS_LEVEL) {
                parameters.setCompressionMethod(Zip4jConstants.COMP_STORE); //без сжатия
            } else {
                parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            }

            parameters.setCompressionLevel(zipParams.getCompressLevel());

            //устанавливаем пароль и метод шифрования в зависимовсти от длины ключа
            if(zipParams.getNeedEncrypt()) {
                parameters.setEncryptFiles(true);
                switch (zipParams.getKeyLenght()) {
                    case 0:
                        parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
                        break;
                    case 128:
                        parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
                        parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_128);
                        break;
                    case 256:
                        parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
                        parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
                        break;
                    default:
                        status=ZipConstants.ERROR;
                        return false; //если передан некорректный параметр - возвращаем ошибку
                }
                //parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
                //Log.w("zip", "setPassword: " + zipParams.getPassword());
                parameters.setPassword(zipParams.getPassword());
            }


            for (String p:zipParams.getObjects()) {
                if(stop) { //прервано пользователем
                    status=ZipConstants.COMPLETE;
                    zipFile.getProgressMonitor().cancelAllTasks();
                    break;
                }
                //Log.w("curr obj", p);
                if (new File(p).isDirectory()) {
                    zipFile.addFolder(p, parameters);
                    updateZipProgress(zipFile); //статус архивации отдельной директории
                } else {
                    zipFile.addFile(new File(p), parameters);
                    updateZipProgress(zipFile); //статус архивации отдельного файла
                }
                if(status!=ZipConstants.IN_PROGRESS) {
                    break;
                }
            }

            if(setCompete) {
                if (status == ZipConstants.IN_PROGRESS) {
                    status = ZipConstants.COMPLETE;
                    return true;
                }
            } else {
                if (status == ZipConstants.IN_PROGRESS) {
                    return true;
                }
            }

        } catch (Exception e) {
            Log.e("Zip.zipNoSplit", null, e);
            status=ZipConstants.ERROR;
        }
        return false;
    }

    //тут архивируется уже конечный архив(с сжатием и шифрованием, если было задано)
    //сам разделенный архив для упрощения не шифруется и не сжимается
    private void zipWithSplit() {
        try {
            //разделенный архив будет с префиксом "_splitted"
            ZipFile zipFile = new ZipFile(zipParams.getDistPath() + ZipConstants.SPLITTED_ARCH_PREFIX);

            zipFile.setRunInThread(true); //

            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_STORE);
            parameters.setCompressionLevel(ZipConstants.DEFAULT_COMPRESS_LEVEL); //архивировать буем без сжатия

            //устанавливаем пароль и метод шифрования в зависимовсти от длины ключа
            if(zipParams.getNeedEncrypt()) {
                parameters.setEncryptFiles(true);
                switch (zipParams.getKeyLenght()) {
                    case 0:
                        parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
                        break;
                    case 128:
                        parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
                        parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_128);
                        break;
                    case 256:
                        parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
                        parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
                        break;
                    default:
                        status=ZipConstants.ERROR;
                        return; //если передан некорректный параметр - возвращаем ошибку
                }
                //parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
                //Log.w("zip", "setPassword: " + zipParams.getPassword());
                parameters.setPassword(zipParams.getPassword());
            }

            //добавляем только что полученный архив zipParams.getDistPath()
            zipFile.createZipFile(new File(zipParams.getDistPath()), parameters, true, zipParams.getPartSize());
            updateZipProgress(zipFile);

            if(status!=ZipConstants.IN_PROGRESS) { //проверяем - если неудачно создался архив - выходим
                return;
            }
            //удаляем исходный одиночный архив, а затем переименовываем обратно
            //ArrayList<String> forDelete = new ArrayList<String>();
            //forDelete.add(zipParams.getDistPath() + "/" + zipParams.getArchiveName());
            //return (MainOperations.delete(forDelete, false) == 1) && (MainOperations.rename(zipParams.getDistPath() + "/" + zipParams.getArchiveName() + "_splitted", zipParams.getArchiveName()));
            //делаем средствами явы

            if((new File(zipParams.getDistPath()).delete()) && (new File(zipParams.getDistPath() + ZipConstants.SPLITTED_ARCH_PREFIX).renameTo(new File(zipParams.getDistPath())))) {
                status=ZipConstants.COMPLETE;
            }

        } catch (Exception e) {
            status=ZipConstants.ERROR;
            Log.e("zipWithSplit", null, e);
        }
        // return false;
    }


    //распаковка
    //в текущей реализации можно передавать на распаковку только один архив
    //в дальнейшем сделаю распаковку нескольких архивов
    private void unzip() {
        try {
            //если архив - разделен на части - соединяем
            if(new ZipFile(zipParams.getObjects().get(0)).isSplitArchive()) {
                maxProgress=200; //надо распаковать 2 раза: сначала соединить + распаковать сжатый без сжатия архив без пароля (100%), а затем распаковать уже внутренний архив (еще 100%)
                showProgressDialog();
                unzipSplitted();
            } else { //если архив не разбит на части
                Log.w("unzip", "its not splitted archive");
                maxProgress=100; //просто распаковать 1 архив
                showProgressDialog();
                unzipNonSplitted(zipParams, true); // true - означает что это окончательная операция и нужно в случае успеха поменять статус
            }
        } catch (Exception e) {
            status=ZipConstants.ERROR;
            Log.e("Zip.unzip", null, e);
        }
        //return false;
    }

    //распаковка не разбитого на части архива
    private Boolean unzipNonSplitted(ZipParams zipParams, Boolean setCompete) { //если setComplete=false, значит метод был вызван из другого метода с целью продолжения операции и устанавливать статус Завершено не нужно
        try {
            Log.w("unzipNonSplitted", "file for unzip: " + zipParams.getDistPath());
            ZipFile zipFile = new ZipFile(zipParams.getObjects().get(0)); //берем первый архив списке на распаковку

            zipFile.setRunInThread(true);

            //если архив зашифрован - ставим пароль на распаковку
            if (zipFile.isEncrypted()) {
                Log.w("zip", "setPassword: " + zipParams.getPassword());
                zipFile.setPassword(zipParams.getPassword());
            }

            if (setCompete) { //если это распаковка отдельного не рахделенного архива - распаковывем в отдельную директорию (расщирение .zip из названия убираем)
                String archDir = zipParams.getDistPath() + "/" + zipFile.getFile().getName().substring(0, zipFile.getFile().getName().length()-4);
                if(!new File(archDir).mkdir()) {
                    status = ZipConstants.ERROR;
                    return false;
                }
                zipFile.extractAll(archDir); //распаковываем в указанную папку + имя архива
            } else {
                zipFile.extractAll(zipParams.getDistPath()); //распаковываем в указанную папку
            }

            updateZipProgress(zipFile);

            if(setCompete) { //если это не разделенный архив - то это конечная операция, ставим статус завершено если нет ошибок и выходим
                if (status == ZipConstants.IN_PROGRESS) {
                    status = ZipConstants.COMPLETE;
                    return true;
                }
            } else {
                if (status == ZipConstants.IN_PROGRESS) {
                    return true;
                }
            }

        } catch (Exception e) {
            status = ZipConstants.ERROR;
            Log.e("Zip.unzipNonSplitted", null, e);
        }

        return false;
    }

    //распаковка разбитого на части архива
    private void unzipSplitted() {
        try {
            //String sourceArchiveName = FileUtils.getLastPathComponent(zipParams.getObjects().get(0));
            String sourceArchiveName = new File(zipParams.getObjects().get(0)).getName(); //получаем имя архива на распаковку
            String mergedArchiveName = ZipConstants.MERGED_ARCH_PREFIX + sourceArchiveName; //имя смерженного архива
            //String mergedArchive = FileUtils.getParentDirectory(zipParams.getDistPath()) + "/" + mergedArchiveName;
            //путь где будет лежать смерженный архив (абослютный) + имя архива
            //String mergedArchive = new File(zipParams.getObjects().get(0)).getParentFile().getAbsolutePath() + "/" + mergedArchiveName;
            String mergedArchive = zipParams.getDistPath() + "/" + mergedArchiveName;
            //String mergedArchiveUnpackDir = zipParams.getDistPath() + "/" + mergedArchiveName;
            //String mergedArchiveUnpackDir = zipParams.getDistPath();
            String insideArchive = zipParams.getDistPath() + "/" + sourceArchiveName;
            String insideArchiveUnpackDir = zipParams.getDistPath() + "/" + sourceArchiveName.substring(0, sourceArchiveName.length()-4);

            //соединяем части архива в один архив
            ZipFile zipFile = new ZipFile(zipParams.getObjects().get(0));
            //zipFile.setRunInThread(true);
            try {
                //Log.w("unzip", "merge start");
                zipFile.mergeSplitFiles(new File(mergedArchive));
                //Log.w("unzip", "merge post");
            } catch (Exception e) {
                status=ZipConstants.ERROR;
                Log.e("unzip.merge", null, e);
                return;
            }
            //Log.w("unzip", "merge ok");

            //теперь нужно распаковать архив
            ArrayList<String> objects = new ArrayList<String>();
            objects.add(mergedArchive);
            //ZipParams zipParamsNew = new ZipParams(ZipConstants.OPERATION_UNZIP, objects, zipParams.getDistPath());
            ZipParams zipParamsNew = new ZipParams(ZipConstants.OPERATION_UNZIP, objects, zipParams.getDistPath());

            if(zipParams.getNeedEncrypt()) {
                zipParamsNew.setNeedEncrypt(true);
                zipParamsNew.setPassword(zipParams.getPassword());
                zipParamsNew.setKeyLenght(zipParams.getKeyLenght());
            }

            if(unzipNonSplitted(zipParamsNew, false)) { //распаковываем смерженный архив
                //Log.w("unzip", "merge archive unpack ok");
                //удаляем смерженый архив
                //MainOperations.delete(forDelete, false);
                if(!new File(mergedArchive).delete()) {
                    status=ZipConstants.ERROR;
                    return;
                }
                //Log.w("test", "unpack merged arch and delete ok");
                //Log.w("unzip", "delete merge archive ok");
                //получаем архив: zipParams.getDistPath()/<merged_...>/FileUtils.getLastPathComponent(zipParams.getArchiveName()
                //устанавливаем параметры для окончательной распаковки архива
                objects.clear();
                //добавляем архив распакованный из смерженного архива
                objects.add(insideArchive);
                zipParamsNew = new ZipParams(ZipConstants.OPERATION_UNZIP, objects, insideArchiveUnpackDir);
                zipParamsNew.setNeedEncrypt(zipParams.getNeedEncrypt());
                if(zipParams.getNeedEncrypt()) {
                    zipParamsNew.setPassword(zipParams.getPassword());
                    zipParamsNew.setKeyLenght(zipParams.getKeyLenght());
                }

                //перед распаковкой - создаем папку куда положим арспакованные из конечного архива данные
                //Log.w("test", "mkdir for inside arch: " + insideArchiveUnpackDir);
                if(!new File(insideArchiveUnpackDir).mkdir()) {
                    //Log.w("test", "mkdir for inside arch fail");
                    new File(insideArchive).delete();
                    status=ZipConstants.ERROR;
                    return;
                }
                //Log.w("test", "mkdir for inside arch ok");

                if(unzipNonSplitted(zipParamsNew, false)) {
                    //Log.w("test", "unpack inside arch ok");
                    //удаляем смерженный архив и архив распакованный из смерженного
                    //return (new File(zipParams.getDistPath() + "/" + mergedArchiveName).delete()) && (new File(zipParams.getDistPath() + "/" + mergedArchiveName + "/" + sourceArchiveName).delete());
                    if(new File(insideArchive).delete()) {
                        //Log.w("test", "del inside arch ok");
                        status=ZipConstants.COMPLETE;
                        //return;
                    } else {
                        //Log.w("test", "unpack inside arch fail");
                        status=ZipConstants.ERROR;
                        //return;
                    }
                } else {
                    new File(insideArchive).delete();
                    new File(insideArchiveUnpackDir).delete();
                }
            } else {
                //если распаковка прошла неудачно - удаляем смерженый архив
                //MainOperations.delete(forDelete, false);
                /*if(!new File(mergedArchive).delete()) {
                    status=ZipConstants.ERROR;
                    //return;
                }*/
                new File(mergedArchive).delete();
            }
        } catch (Exception e) {
            status=ZipConstants.ERROR;
            Log.e("unzipSplitted", null, e);
        }
        //return false;
    }

    ////////////////////////////////////////////////////
    private void showProgressDialog() {
        try {
            if(zipParams.getZipProgressDialogParams()==null) { //проверка: если не был задан диалог - выходим
                return;
            }
            final ZipProgressDialogParams zipProgressDialogParams = zipParams.getZipProgressDialogParams();
            final AlertDialog.Builder alertDialog = zipProgressDialogParams.getAlertDialog();

            //кнопка отмены - прерываем текущий поток


            alertDialog.setNegativeButton(zipProgressDialogParams.getNegativeButtonText(),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            stop();
                            //выполняем метод переданный сюда извне. В данном случае это обновление текущей директории
                            /*Method method = zipProgressDialogParams.getMethod();
                            if (method != null) {
                                try {
                                    method.invoke(zipProgressDialogParams.getContext());
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            }*/
                            dialog.cancel();
                        }
                    });

            if(zipProgressDialogParams.getProgressBar()!=null) {
                //Log.w("showProgressDialog", "set max progrees bar: " + Integer.toString(maxProgress));
                zipProgressDialogParams.getProgressBar().setMax(maxProgress);
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
                            //final String sec = String.valueOf(i);
                            final float sec = (float) i;
                            if(getStatus()!= ZipConstants.IN_PROGRESS) {
                                break;
                            }
                            //если задействованы оба элемента - обновляем их сразу. так наиболее оптимально
                            if(zipProgressDialogParams.getTimer()!=null && zipProgressDialogParams.getProgressBar()!=null) {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        zipProgressDialogParams.getTimer().setText(String.format("%.2f", (float) progress * 100 / maxProgress) + "%" + "/" + String.format("%.3f", sec / 1000) + " sec");
                                        //Log.w("Percent Done dialog: ", Integer.toString(progress));
                                        zipProgressDialogParams.getProgressBar().setProgress(progress);
                                    }
                                });
                            } else {
                                //таймер операции
                                if (zipProgressDialogParams.getTimer() != null) {
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            zipProgressDialogParams.getTimer().setText(String.format("%.2f", (float) progress * 100/maxProgress) + "%" + "/" + String.format("%.3f", sec / 1000) + " sec");
                                        }
                                    });
                                }
                                //прогресс операции
                                if (zipProgressDialogParams.getProgressBar() != null) {
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            zipProgressDialogParams.getProgressBar().setProgress(progress);
                                        }
                                    });
                                }
                            }
                            i+=ZipConstants.PROGRESS_UPDATE_INTERVAL;
                            sleep(ZipConstants.PROGRESS_UPDATE_INTERVAL);
                        }
                        switch (getStatus()) {
                            case ZipConstants.COMPLETE:
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(!stop) { //если операция не была прервана пользователем - показываем сообщение об успешном окончании
                                            //выполняем метод переданный сюда извне. В данном случае это обновление текущей директории
                                            Method method = zipProgressDialogParams.getMethod();
                                            if(method !=null) {
                                                try {
                                                    method.invoke(zipProgressDialogParams.getContext());
                                                } catch (IllegalAccessException e) {
                                                    e.printStackTrace();
                                                } catch (InvocationTargetException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            Toast.makeText(zipProgressDialogParams.getContext(), zipProgressDialogParams.getSuccessText(), Toast.LENGTH_SHORT).show();
                                        }
                                        alert[0].dismiss();
                                    }
                                });
                                break;
                            case ZipConstants.ERROR:
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(zipProgressDialogParams.getContext(), zipProgressDialogParams.getFailText(), Toast.LENGTH_SHORT).show();
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

    //получение прогресса в процентах для каждого отдельного файла/папки (от 0 до 100)
    private void updateZipProgress(ZipFile zipFile) {
        try {
            ProgressMonitor progressMonitor = zipFile.getProgressMonitor();
            int currProgress=0;
            while (progressMonitor.getState() == ProgressMonitor.STATE_BUSY) {

                //проверяем, если поток был остановлен по кнопке Отмена - завершаем текущую операцию
                //if(Thread.currentThread().isInterrupted()) {
                if(stop) {
                    status=ZipConstants.COMPLETE;
                    zipFile.getProgressMonitor().cancelAllTasks();
                } else {
                    currProgress = progressMonitor.getPercentDone();
                    if (currProgress > progress) {
                        progress = currProgress;
                        //Log.w("Percent Done: ", Integer.toString(progress));
                        //Log.w("File: ", progressMonitor.getFileName());
                    }
                }
            }

            if (progressMonitor.getResult() == ProgressMonitor.RESULT_ERROR) {
                // Any exception can be retrieved as below:
                if (progressMonitor.getException() != null) {
                    progressMonitor.getException().printStackTrace();
                } else {
                    Log.w("zip","An error occurred without any exception");
                }
                status=ZipConstants.ERROR;
            }

            //Log.w("Result: ", Integer.toString(progressMonitor.getResult()));

        } catch (Exception e) {
            Log.e("updateZipProgress", null, e);
            status=ZipConstants.ERROR;
        }
    }

    public int getProgress() {
        return progress;
    }

    //статус операции
    public int getStatus() {
        return status;
    }


    //проверка - запаролен ли архив
    public static Boolean checkArchEncrypt(String path) {
        try {
            return (new ZipFile(path)).isEncrypted();
        } catch (Exception e) {
            Log.e("checkArchEncrypt", null, e);
        }
        return false;
    }
}

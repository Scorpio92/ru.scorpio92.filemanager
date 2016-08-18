package ru.scorpio92.security.SHA1;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.UUID;


/**
 * Created by scorpio92 on 15.06.16.
 */
public class SHA1 {

    private int action;
    private String object;
    private Boolean runInThread;

    private Thread thread;
    private Boolean stop;
    private int status;
    private String hash;
    private Boolean hardCalc; //если будет недостаточно прав для вычисления хеш-суммы, будет использована утилита busybox sha1sum из-под рута

    private String BUSYBOX_DEFAULT_PATH = "busybox";
    private String BUSYBOX_PATH;
    private final String SU_COMMAND = "su";
    private final String SHA1_INSTANCE_NAME ="SHA1";
    private final int FILE_BUFFER=2048;
    private final String UNKNOWN_HASH="???";
    private final String STRING_HASH_ENCODING="iso-8859-1";
    private final String SHA1SUM_COMMAND="BB=BUSYBOX_FOR_REPLACE; $BB sha1sum \"FILE_FOR_REPLACE\" | $BB awk '{print $1}'";


    public SHA1 (int action, String object) {
        this.action=action;
        this.object=object;
        runInThread=false;
        BUSYBOX_PATH = BUSYBOX_DEFAULT_PATH;
        status=SHA1Constants.IN_PROGRESS;
        stop=false;

    }

    //если на устройстве не установлен BB или он левый
    public void setBBPath(String bbPath) {
        BUSYBOX_PATH=bbPath;
    }

    public void setRunInThread(Boolean b) {
        runInThread=b;
    }

    public void setHardCalc(Boolean b) {
        hardCalc=b;
    }

    //метод запуска
    public SHA1 start() {
        if(runInThread) { //если требуется запуск в отдельном потоке
            thread = new Thread() {
                @Override
                public void run() {
                    try {
                        startOperation();
                    } catch (Exception e) {
                        Log.e("SHA1.thread.start", null, e);
                    }
                }
            };
            thread.start();
        } else { //запуск в основном потоке
            startOperation();
        }
        return this;
    }

    //остановка путем нажатия на кнопку Отмены в диалоге операции
    public void stop() {
        try {
            stop=true;
            thread.interrupt();
        } catch (Exception e) {
            Log.e("SHA1.thread.stop", null, e);
        }
    }

    //статус операции
    public int getStatus() {
        return status;
    }

    public String getHash() {
        return hash;
    }

    //выбор и запуск нужной операции
    private void startOperation() {
        switch (action) {
            case SHA1Constants.CALC_STRING_HASH:
                hash=getStringHash();
                break;
            case SHA1Constants.CALC_FILE_HASH:
                hash=getFileHash();
                break;
        }
    }


    private String getFileHash() {
        MessageDigest md = null;
        FileInputStream fis = null;
        int nread = 0;
        try {
            md = MessageDigest.getInstance(SHA1_INSTANCE_NAME);
            fis = new FileInputStream(object);
            byte[] dataBytes = new byte[FILE_BUFFER];
            while ((nread = fis.read(dataBytes)) != -1) {
                if(stop)
                    break;
                md.update(dataBytes, 0, nread);
            }
            if(stop) {
                fis.close();
                status = SHA1Constants.COMPLETE;
                return UNKNOWN_HASH;
            }

            byte[] mdbytes = md.digest();
            //convert the byte to hex format
            StringBuffer sb = new StringBuffer("");
            for (int i = 0; i < mdbytes.length; i++) {
                if(stop)
                    break;
                sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            fis.close();
            status = SHA1Constants.COMPLETE;
            if(stop) {
                return UNKNOWN_HASH;
            }
            return sb.toString();
        } catch (FileNotFoundException fnfe) {
            Log.e("getFileHash", null, fnfe);
            if(hardCalc) {
                String s;
                try {
                    String command = SHA1SUM_COMMAND;
                    if(BUSYBOX_PATH != null) {
                        command = command.replace("BUSYBOX_FOR_REPLACE", BUSYBOX_PATH); //у последнего элемента в массиве(самой команды) меняем путь к BB
                        Log.w("BUSYBOX_PATH:", BUSYBOX_PATH);
                    } else {
                        command = command.replace("BUSYBOX_FOR_REPLACE", BUSYBOX_DEFAULT_PATH); //у последнего элемента в массиве(самой команды) меняем путь к BB
                        Log.w("BUSYBOX_DEFAULT_PATH:", BUSYBOX_DEFAULT_PATH);
                    }

                    command = command.replace("FILE_FOR_REPLACE", object);
                    Log.w("getFileHash", command);
                    s = runProcess(new String[]{SU_COMMAND, "-c", command}).get(0);
                    if(s.length()==40) {
                        status = SHA1Constants.COMPLETE;
                    } else {
                        status=SHA1Constants.ERROR;
                        s=UNKNOWN_HASH;
                    }
                    return s;
                } catch (Exception e) {
                    Log.e("getFileHash", "fail su", e);
                    status=SHA1Constants.ERROR;
                }
            } else {
                status=SHA1Constants.ERROR;
            }
        } catch (Exception e) {
            Log.e("getFileHash", null, e);
            status=SHA1Constants.ERROR;
        }

        return UNKNOWN_HASH;
    }

    //функция вычисления хеш-суммы строки
    private String getStringHash() {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(SHA1_INSTANCE_NAME);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            status=SHA1Constants.ERROR;
            return UNKNOWN_HASH;
        }

        try {
            md.update(object.getBytes(STRING_HASH_ENCODING), 0, object.length());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            status=SHA1Constants.ERROR;
            return UNKNOWN_HASH;
        }

        byte[] mdbytes = md.digest();
        StringBuffer sb = new StringBuffer("");
        for (int i = 0; i < mdbytes.length; i++) {
            sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        status=SHA1Constants.COMPLETE;

        return sb.toString();
    }


    public String getRandomHash() {
        object= UUID.randomUUID().toString();
        return getStringHash();
    }

    private ArrayList<String> runProcess(String[] command) {
        ArrayList<String> result = new ArrayList<String>();
        BufferedReader in = null;
        ProcessBuilder pb = null;
        Process p = null;
        try {
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
}

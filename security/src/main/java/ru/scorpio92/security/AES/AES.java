package ru.scorpio92.security.AES;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by scorpio92 on 13.06.16.
 */
public class AES {

    private AESParams aesParams;
    private Thread thread;
    private int operationStatus;
    private Boolean stop;


    public AES (AESParams aesParams) {
        this.aesParams=aesParams;
        operationStatus = AESConstants.IN_PROGRESS;
        stop=false;
    }

    //метод запуска операций шифрования/расшифровки
    public void start() {
        if(aesParams.getRunInThread()) { //если требуется запуск в отдельном потоке
            thread = new Thread() {
                @Override
                public void run() {
                    try {
                        startOperation();
                    } catch (Exception e) {
                        Log.e("AES.thread.start", null, e);
                    }
                }
            };
            thread.start();
        } else { //запуск в основном потоке
            startOperation();
        }
    }

    //остановка путем нажатия на кнопку Отмены в диалоге операции
    public void stop() {
        try {
            stop=true;
            thread.interrupt();
        } catch (Exception e) {
            Log.e("AES.thread.stop", null, e);
        }
    }

    //статус операции
    public int getStatus() {
        return operationStatus;
    }

    //выбор и запуск нужной операции
    private void startOperation() {
        switch (aesParams.getOperation()) {
            case AESConstants.ENCRYPT:
                showProgressDialog();
                encrypt();
                break;
            case AESConstants.DECRYPT:
                showProgressDialog();
                decrypt();
                break;
        }
    }

    //диалог прогресса
    private void showProgressDialog() {
        try {
            final AESProgressDialogParams aesProgressDialogParams = aesParams.getAesProgressDialogParams();
            if(aesProgressDialogParams==null) {
                return;
            }
            final AlertDialog.Builder alertDialog = aesProgressDialogParams.getAlertDialog();

            alertDialog.setNegativeButton(aesProgressDialogParams.getNegativeButtonText(),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            stop();
                            //выполняем метод переданный сюда извне. В данном случае это обновление текущей директории
                            /*Method method = aesProgressDialogParams.getMethod();
                            if (method != null) {
                                try {
                                    method.invoke(aesProgressDialogParams.getContext());
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            }*/
                            dialog.cancel();
                        }
                    });

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
                        while(true) { //ждем обновление статуса
                            if(getStatus()!=AESConstants.IN_PROGRESS) {
                                break;
                            }
                            sleep(AESConstants.PROGRESS_UPDATE_INTERVAL);
                        }
                        switch (getStatus()) {
                            case AESConstants.COMPLETE:
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                     @Override
                                     public void run() {
                                         if(!stop) { //если операция не была прервана пользователем
                                             //выполняем метод переданный сюда извне. В данном случае это обновление текущей директории
                                             Method method = aesProgressDialogParams.getMethod();
                                             if (method != null) {
                                                 try {
                                                     method.invoke(aesProgressDialogParams.getContext());
                                                 } catch (IllegalAccessException e) {
                                                     e.printStackTrace();
                                                 } catch (InvocationTargetException e) {
                                                     e.printStackTrace();
                                                 }
                                             }
                                             Toast.makeText(aesProgressDialogParams.getContext(), aesProgressDialogParams.getSuccessText(), Toast.LENGTH_SHORT).show();
                                         }
                                         alert[0].dismiss();
                                     }
                                 });
                                break;
                            case AESConstants.ERROR:
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(aesProgressDialogParams.getFailText()!=null) {
                                            Toast.makeText(aesProgressDialogParams.getContext(), aesProgressDialogParams.getFailText(), Toast.LENGTH_SHORT).show();
                                        }
                                        alert[0].dismiss();
                                    }
                                });
                                break;
                        }
                    } catch (Exception e) {
                        Log.e("AES.showProgressDialog", null, e);
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
            Log.e("AES.showProgressDialog", null, e);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    private byte[] generate256bitByteArray(String string) {
        try {
            final MessageDigest digest = MessageDigest.getInstance(AESConstants.HASH_ALGORITHM);
            byte[] bytes = string.getBytes("UTF-8");
            digest.update(bytes, 0, bytes.length);
            return digest.digest();
        } catch (Exception e) {
            Log.e("generate256bitString", null, e);
        }
        return null;
    }

    private byte[] generate128bitByteArray(String string) {
        try {
            byte[] array = generate256bitByteArray(string);
            byte[] tmp_array = new byte[array.length/2];
            System.arraycopy(array, 0, tmp_array, 0, array.length / 2);
            return tmp_array;
        } catch (Exception e) {
            Log.e("generate256bitString", null, e);
        }
        return null;
    }

    //получение ключа
    //по умолчанию генерируем 256 битный ключ с помощью алгоритма SHA-256
    private  SecretKeySpec generate256bitKey(final String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        return new SecretKeySpec(generate256bitByteArray(password), "AES");
    }

    private  SecretKeySpec generate128bitKey(final String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        byte[] key = generate256bitByteArray(password);

        //если задано шифрование AES 128 bit, копируем первую половину массива
        if (aesParams.getKeyLenght()==AESConstants.KEY_LENGHT_128) {
            byte[] tmp_key = new byte[key.length/2];
            System.arraycopy(key, 0, tmp_key, 0, key.length/2);
            key=tmp_key;
        }

        return new SecretKeySpec(key, "AES");
    }

    //генерируем вектори инициализация  получая хешу сумму SHA-256 от пароля + дефолтной соли и беря половину массива (128 бит)
    private IvParameterSpec generateIV() {
        try {
            return new IvParameterSpec(generate128bitByteArray(aesParams.getPassword()+AESConstants.DEFAULT_SALT));
        } catch (Exception e) {
            Log.e("generateIV", null, e);
        }
        return null;
    }

    //шифрование
    private void encrypt() {
        try {
            // Here you read the cleartext.
            FileInputStream fis = new FileInputStream(aesParams.getSourceFile());
            if(stop) {
                fis.close();
                return;
            }
            File distDir = new File(aesParams.getDistFile()).getParentFile();
            if(!distDir.exists()) {
                if(!distDir.mkdirs()) {
                    operationStatus=AESConstants.ERROR;
                    fis.close();
                    return;
                }
            }
            // This stream write the encrypted text. This stream will be wrapped by another stream.
            FileOutputStream fos = new FileOutputStream(aesParams.getDistFile());
            if(stop) {
                fis.close();
                fos.close();
                return;
            }
            // Length is 16 byte
            // Careful when taking user input!!! http://stackoverflow.com/a/3452620/1188357
            //SecretKeySpec sks = new SecretKeySpec(getKey(aesParams.getPassword()), "AES");
            SecretKeySpec sks=null;
            if (aesParams.getKeyLenght()==AESConstants.KEY_LENGHT_128) {
                sks = generate128bitKey(aesParams.getPassword());
            }
            if (aesParams.getKeyLenght()==AESConstants.KEY_LENGHT_256) {
                sks = generate256bitKey(aesParams.getPassword());
            }
            // Create cipher
            //Cipher cipher = Cipher.getInstance(AESConstants.AES_INSTANCE_NAME);
            Cipher cipher = Cipher.getInstance(AESConstants.AES_MODE);
            //cipher.init(Cipher.ENCRYPT_MODE, sks);
            cipher.init(Cipher.ENCRYPT_MODE, sks, generateIV());
            // Wrap the output stream
            CipherOutputStream cos = new CipherOutputStream(fos, cipher);
            if(stop) {
                cos.flush();
                cos.close();
                fis.close();
                fos.close();
                return;
            }
            // Write bytes
            int b;
            byte[] d = new byte[aesParams.getFileBuffer()];
            while ((b = fis.read(d)) != -1) {
                if(stop) {
                    break;
                }
                cos.write(d, 0, b);
            }
            // Flush and close streams.
            cos.flush();
            cos.close();
            fos.flush();
            fos.close();
            fis.close();
            if(!stop) { //если операция не была прервана пользвателем, устанавливаем статус успешно
                if(aesParams.getDeleteSource()) {
                    if(new File(aesParams.getSourceFile()).delete()) {
                        operationStatus = AESConstants.COMPLETE;
                    } else {
                        operationStatus=AESConstants.ERROR;
                    }
                } else {
                    operationStatus = AESConstants.COMPLETE;
                }
            }
            //return true;
        } catch (Exception e) {
            operationStatus=AESConstants.ERROR;
            Log.e("AES.encrypt", null, e);
        }
        //return false;
    }

    //расшифровка
    private void decrypt() {
        try {
            FileInputStream fis = new FileInputStream(aesParams.getSourceFile());
            if(stop) {
                fis.close();
                return;
            }
            File distDir = new File(aesParams.getDistFile()).getParentFile();
            if(!distDir.exists()) {
                if(!distDir.mkdirs()) {
                    operationStatus=AESConstants.ERROR;
                    fis.close();
                    return;
                }
            }
            FileOutputStream fos = new FileOutputStream(aesParams.getDistFile());
            if(stop) {
                fis.close();
                fos.close();
                return;
            }
            //SecretKeySpec sks = new SecretKeySpec(getKey(aesParams.getPassword()), "AES");
            SecretKeySpec sks=null;
            if (aesParams.getKeyLenght()==AESConstants.KEY_LENGHT_128) {
                sks = generate128bitKey(aesParams.getPassword());
            }
            if (aesParams.getKeyLenght()==AESConstants.KEY_LENGHT_256) {
                sks = generate256bitKey(aesParams.getPassword());
            }
            //Cipher cipher = Cipher.getInstance("AES");
            //cipher.init(Cipher.DECRYPT_MODE, sks);
            Cipher cipher = Cipher.getInstance(AESConstants.AES_MODE);
            //cipher.init(Cipher.ENCRYPT_MODE, sks);
            cipher.init(Cipher.DECRYPT_MODE, sks, generateIV());
            CipherInputStream cis = new CipherInputStream(fis, cipher);
            if(stop) {
                cis.close();
                fis.close();
                fos.close();
                return;
            }
            int b;
            byte[] d = new byte[aesParams.getFileBuffer()];
            while ((b = cis.read(d)) != -1) {
                if(stop) {
                    break;
                }
                fos.write(d, 0, b);
            }
            cis.close();
            fos.flush();
            fos.close();
            fis.close();
            if(!stop) { //если операция не была прервана пользвателем, устанавливаем статус успешно
                if(aesParams.getDeleteSource()) {
                    if(new File(aesParams.getSourceFile()).delete()) {
                        operationStatus = AESConstants.COMPLETE;
                    } else {
                        operationStatus=AESConstants.ERROR;
                    }
                } else {
                    operationStatus = AESConstants.COMPLETE;
                }
            }
            //return true;
        } catch (Exception e) {
            operationStatus=AESConstants.ERROR;
            Log.e("AES.decrypt", null, e);
        }
        //return false;
    }
}

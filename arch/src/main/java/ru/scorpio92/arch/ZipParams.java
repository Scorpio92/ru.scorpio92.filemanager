package ru.scorpio92.arch;

import java.util.ArrayList;

/**
 * Created by scorpio92 on 31.03.16.
 */
public class ZipParams {

    private int operation;
    private ArrayList<String> objects; //полные пути объектов для запаковки и распаковки
    //private String archiveName; //при запаковке передаем просто имя будущего файла архива, а при распаковке передаем путь до архива
    private int compressLevel;
    private int partSize;
    private Boolean needEncrypt;
    private String password;
    private int keyLenght; //длина ключа: для стандартного шифрования - 0, для AES 128 или 256
    private String distPath; //полный путь включая имя (при запаковке) и просто путь (при распаковке)
    private ZipProgressDialogParams zipProgressDialogParams;

    public ZipParams(int operation, ArrayList<String> objects, String distPath) {
        this.operation=operation;
        this.objects=objects;
        this.distPath=distPath;
        //this.archiveName =archiveName;
        compressLevel=ZipConstants.DEFAULT_COMPRESS_LEVEL;
        partSize=ZipConstants.DEFAULT_PART_SIZE;
        needEncrypt=false;
        keyLenght=ZipConstants.DEFAULT_KEY_LENGHT;
        zipProgressDialogParams=null;
    }


    //getters
    public int getOperation() {
        return operation;
    }

    public ArrayList<String> getObjects() {
        return objects;
    }

    /*public String getArchiveName() {
        return archiveName;
    }*/

    public int getCompressLevel() {
        return compressLevel;
    }

    public int getPartSize() {
        return partSize;
    }

    public String getPassword() {
        return password;
    }

    public String getDistPath() {
        return distPath;
    }

    public int getKeyLenght() {
        return keyLenght;
    }

    public Boolean getNeedEncrypt() {
        return needEncrypt;
    }


    //setters
    public void setCompressLevel(int i) {compressLevel=i;}

    public void setPartSize(int i) {partSize=i;}

    public void setNeedEncrypt(Boolean b) {needEncrypt=b;}

    public void setPassword(String s) {password=s;}

    public void setKeyLenght(int i) {keyLenght=i;}


    public void setZipProgressDialogParams(ZipProgressDialogParams zipProgressDialogParams) {
        this.zipProgressDialogParams = zipProgressDialogParams;
    }

    public ZipProgressDialogParams getZipProgressDialogParams() {
        return zipProgressDialogParams;
    }
}

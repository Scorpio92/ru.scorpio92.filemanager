package ru.scorpio92.security.AES;

/**
 * Created by scorpio92 on 13.06.16.
 */
public class AESParams {

    private int operation;
    private String sourceFile;
    private String distFile;
    private String password;
    private int keyLenght;
    private Boolean deleteSource;
    private int fileBuffer;
    private Boolean runInThread;
    private AESProgressDialogParams aesProgressDialogParams;


    public AESParams(int operation, String sourceFile, String distFile, String password) {
        this.operation=operation;
        this.sourceFile=sourceFile;
        this.distFile=distFile;
        this.password=password;
        keyLenght=AESConstants.KEY_LENGHT_128;
        deleteSource=false;
        fileBuffer=AESConstants.DEFAULT_BUFFER;
        runInThread=true;
        aesProgressDialogParams=null;
    }

    public int getOperation() {
        return operation;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public String getDistFile() {
        return distFile;
    }

    public String getPassword() {
        return password;
    }

    public int getKeyLenght() {
        return keyLenght;
    }

    public Boolean getDeleteSource() {
        return deleteSource;
    }

    public int getFileBuffer() {
        return fileBuffer;
    }

    public Boolean getRunInThread() {
        return runInThread;
    }

    public AESProgressDialogParams getAesProgressDialogParams() {
        return aesProgressDialogParams;
    }



    public void setKeyLenght(int keyLenght) {
        this.keyLenght = keyLenght;
    }

    public void setDeleteSource(Boolean deleteSource) {
        this.deleteSource = deleteSource;
    }

    public void setFileBuffer(int fileBuffer) {
        this.fileBuffer = fileBuffer;
    }

    public void setRunInThread(Boolean runInThread) {
        this.runInThread = runInThread;
    }

    public void setAesProgressDialogParams(AESProgressDialogParams aesProgressDialogParams) {
        this.aesProgressDialogParams = aesProgressDialogParams;
    }
}

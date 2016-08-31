package ru.scorpio92.io;

import java.util.ArrayList;
import ru.scorpio92.io.Types.Object;

/**
 * Created by scorpio92 on 15.06.16.
 */
public class MainOperationsParams {

    private Integer operation;
    private String sourceFile;
    private String distFile;
    private String fileName;
    private ArrayList<String> paths = null;
    private ArrayList<Object> objects = null;
    private boolean runInThread;
    private boolean allowInselfCopy;
    private MainOperationsDialogParams mainOperationsDialogParams=null;
    private MainOperationsTools mainOperationsTools=null;


    //setters
    public void setOperation(Integer operation) {
        this.operation=operation;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile=sourceFile;
    }

    public void setDistFile(String distFile) {
        this.distFile=distFile;
    }

    public void setFileName(String fileName) {
        this.fileName=fileName;
    }

    public void setPaths(ArrayList<String> paths) {
        this.paths=paths;
    }

    public void setObjects(ArrayList<Object> objects) {
        this.objects = objects;
    }

    public void setRunInThread(Boolean runInThread) {
        this.runInThread=runInThread;
    }

    public void setAllowInselfCopy(boolean allowInselfCopy) {
        this.allowInselfCopy = allowInselfCopy;
    }

    public void setMainOperationsDialogParams(MainOperationsDialogParams mainOperationsDialogParams) {
        this.mainOperationsDialogParams = mainOperationsDialogParams;
    }

    public void setMainOperationsTools(MainOperationsTools mainOperationsTools) {
        this.mainOperationsTools = mainOperationsTools;
    }


    //getters
    public Integer getOperation() {
        return operation;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public String getDistFile() {
        return distFile;
    }

    public String getFileName() {
        return fileName;
    }

    public ArrayList<String> getPaths() {
        return paths;
    }

    public ArrayList<Object> getObjects() {
        return objects;
    }

    public Boolean getRunInThread() {
        return runInThread;
    }

    public MainOperationsDialogParams getMainOperationsDialogParams() {
        return mainOperationsDialogParams;
    }


    public MainOperationsTools getMainOperationsTools() {
        return mainOperationsTools;
    }

    public boolean isAllowInselfCopy() {
        return allowInselfCopy;
    }
}

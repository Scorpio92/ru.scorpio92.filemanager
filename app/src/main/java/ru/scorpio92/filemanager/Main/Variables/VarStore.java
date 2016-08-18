package ru.scorpio92.filemanager.Main.Variables;

import android.app.Application;
import android.content.Context;

import java.util.ArrayList;

import ru.scorpio92.arch.Zip;
import ru.scorpio92.filemanager.Main.Adapters.FileListAdapter;
import ru.scorpio92.filemanager.Main.Types.*;
import ru.scorpio92.filemanager.Main.Types.Object;
import ru.scorpio92.filemanager.Main.UI.DialogPresenter;
import ru.scorpio92.filemanager.Main.Utils.SearchUtils;
import ru.scorpio92.io.MainOperations;
import ru.scorpio92.io.MainOperationsTools;
import ru.scorpio92.security.AES.AES;

/**
 * Created by scorpio92 on 26.02.16.
 */
public class VarStore extends Application {

    //контекст приложения
    private static Context context;

    public void setContext (Context c) {context=c; }

    public static Context getAppContext() {
        return context;
    }


    //адаптер для листа
    private FileListAdapter FLA;

    public void setFLA (FileListAdapter FLA) { this.FLA = FLA; }

    public FileListAdapter getFLA () { return FLA; }



    //текущая директория
    private Dir currentDir;

    public void setCurrentDir(Dir currentDir) {this.currentDir = currentDir;}

    public Dir getCurrentDir() {return currentDir;}


    private Dir currentWorkFilesDir;

    public void setCurrentWorkFilesDir(Dir currentWorkFilesDir) {this.currentWorkFilesDir = currentWorkFilesDir;}

    public Dir getCurrentWorkFilesDir() {return currentWorkFilesDir;}



    /*private CurrentDirectory CurrentDirectory;

    public void setCurrentDirectory(CurrentDirectory buf) {this.CurrentDirectory = buf;}

    public CurrentDirectory getCurrentDirectory() {return CurrentDirectory;}*/


    //путь для истории. чтобы иметь возможность переходить не только назад но и вперед
    private String historyPath;

    public void setHistoryPath(String historyPath) {
        this.historyPath = historyPath;
    }

    public String getHistoryPath() {
        return historyPath;
    }


    //поиск файлов
    private ArrayList<Object> searchResults;

    public ArrayList<Object> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(ArrayList<Object> searchResults) {
        this.searchResults = searchResults;
    }


    //интерфейс
    private SearchUtils searchUtils;

    public void setSearchUtils(SearchUtils searchUtils) {
        this.searchUtils = searchUtils;
    }

    public SearchUtils getSearchUtils() {
        return searchUtils;
    }

    private DialogPresenter dialogPresenter;

    public void setDialogPresenter(DialogPresenter dialogPresenter) {
        this.dialogPresenter = dialogPresenter;
    }

    public DialogPresenter getDialogPresenter() {
        return dialogPresenter;
    }



    //инстансы классов операций
    private MainOperationsTools mainOperationsTools;

    public void setMainOperationsTools(MainOperationsTools mainOperationsTools) {
        this.mainOperationsTools = mainOperationsTools;
    }

    public MainOperationsTools getMainOperationsTools() {
        return mainOperationsTools;
    }

    private MainOperations mainOperationsInstance;

    public MainOperations getMainOperationsInstance() {return mainOperationsInstance;}

    public void setMainOperationsInstance(MainOperations mainOperationsInstance) {this.mainOperationsInstance=mainOperationsInstance;}


    private Zip zipInstance;

    public Zip getZipInstance() {return zipInstance;}

    public void setZipInstance(Zip zipInstance) {this.zipInstance=zipInstance;}


    private AES aesInstance;

    public AES getAesInstance() {return aesInstance;}

    public void setAesInstance(AES aes) {aesInstance=aes;}

}

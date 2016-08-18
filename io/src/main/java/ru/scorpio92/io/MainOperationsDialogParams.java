package ru.scorpio92.io;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.reflect.Method;

/**
 * Created by scorpio92 on 15.06.16.
 */
public class MainOperationsDialogParams {

    //AretDialog
    private static final String DEFAULT_NAGATIVE_BUTTON_TEXT = "Cancel";
    private static final String DEFAULT_SUCCESS_TEXT = "OK";
    private static final String DEFAULT_FAIL_TEXT = "Error";
    private static final String DEFAULT_NO_FREE_SPACE_TEXT = "No free space for this operation";

    Context context;
    AlertDialog.Builder alertDialog;
    ProgressBar progressBar;
    TextView timer;
    String negativeButtonText;
    String successText;
    String failText;
    String noFreeSpaceText;
    Method method; //метод который нужно выполнить по окончанию операции

    public MainOperationsDialogParams (Context context, AlertDialog.Builder alertDialog) {
        this.context=context;
        this.alertDialog=alertDialog;
        progressBar=null;
        timer=null;
        negativeButtonText = DEFAULT_NAGATIVE_BUTTON_TEXT;
        successText = DEFAULT_SUCCESS_TEXT;
        failText = DEFAULT_FAIL_TEXT;
        noFreeSpaceText = DEFAULT_NO_FREE_SPACE_TEXT;
        method=null;
    }

    //getters
    public Context getContext() {
        return context;
    }

    public AlertDialog.Builder getAlertDialog() {
        return alertDialog;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public TextView getTimer() {
        return timer;
    }

    public String getNegativeButtonText() {
        return negativeButtonText;
    }

    public String getSuccessText() {
        return successText;
    }

    public String getFailText() {
        return failText;
    }

    public String getNoFreeSpaceText() {
        return noFreeSpaceText;
    }

    public Method getMethod() {
        return method;
    }


    //setters
    public void setProgressBar(ProgressBar p) {progressBar=p;}

    public void setTimer(TextView t) {timer=t;}

    public void setNegativeButtonText(String s) {negativeButtonText=s;}

    public void setSuccessText(String s) {successText=s;}

    public void setFailText(String s) {failText=s;}

    public void setNoFreeSpaceText(String s) {noFreeSpaceText=s;}

    public void setMethod(Method m) {method=m;}
}

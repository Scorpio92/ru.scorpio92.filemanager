package ru.scorpio92.arch;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.reflect.Method;

/**
 * Created by scorpio92 on 16.06.16.
 */
public class ZipProgressDialogParams {

    private Context context;
    private AlertDialog.Builder alertDialog;
    private ProgressBar progressBar;
    private TextView timer;
    private String negativeButtonText;
    private String successText;
    private String failText;
    private Method method; //метод который нужно выполнить по окончанию операции

    public ZipProgressDialogParams (Context context, AlertDialog.Builder alertDialog) {
        this.context=context;
        this.alertDialog=alertDialog;
        progressBar=null;
        timer=null;
        negativeButtonText=ZipConstants.DEFAULT_NAGATIVE_BUTTON_TEXT;
        successText=ZipConstants.DEFAULT_SUCCESS_TEXT;
        failText=ZipConstants.DEFAULT_FAIL_TEXT;
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

    public Method getMethod() {
        return method;
    }


    //setters
    public void setProgressBar(ProgressBar p) {progressBar=p;}

    public void setTimer(TextView t) {timer=t;}

    public void setNegativeButtonText(String s) {negativeButtonText=s;}

    public void setSuccessText(String s) {successText=s;}

    public void setFailText(String s) {failText=s;}

    public void setMethod(Method m) {method=m;}
}

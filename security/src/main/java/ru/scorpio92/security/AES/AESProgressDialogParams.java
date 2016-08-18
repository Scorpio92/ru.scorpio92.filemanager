package ru.scorpio92.security.AES;

import android.app.AlertDialog;
import android.content.Context;

import java.lang.reflect.Method;

/**
 * Created by scorpio92 on 14.06.16.
 */
public class AESProgressDialogParams {

    private Context context;
    private AlertDialog.Builder alertDialog;
    private String negativeButtonText;
    private String successText;
    private String failText;
    private Method method; //метод который нужно выполнить по окончанию операции


    public AESProgressDialogParams(Context context, AlertDialog.Builder alertDialog) {
        this.context=context;
        this.alertDialog=alertDialog;
        negativeButtonText=AESConstants.DEFAULT_NAGATIVE_BUTTON_TEXT;
        successText=AESConstants.DEFAULT_SUCCESS_TEXT;
        failText=AESConstants.DEFAULT_FAIL_TEXT;
        method=null;
    }

    //getters
    public Context getContext() {
        return context;
    }

    public AlertDialog.Builder getAlertDialog() {
        return alertDialog;
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
    public void setNegativeButtonText(String s) {negativeButtonText=s;}

    public void setSuccessText(String s) {successText=s;}

    public void setFailText(String s) {failText=s;}

    public void setMethod(Method m) {method=m;}
}

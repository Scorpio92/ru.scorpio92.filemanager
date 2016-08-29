package ru.scorpio92.filemanager.Main.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import ru.scorpio92.filemanager.Main.Types.Object;
import ru.scorpio92.filemanager.R;
import ru.scorpio92.filemanager.Main.Variables.VarStore;

/**
 * Created by scorpio92 on 01.08.16.
 */
public class SearchUtils {

    private Context context;
    private AlertDialog dialog;
    private String finishMethod;
    private String path;
    private String fileName;
    private Thread thread;

    public SearchUtils(Context context, AlertDialog dialog, String finishMethod, String path, String fileName) {
        this.context=context;
        this.dialog=dialog;
        this.finishMethod=finishMethod;
        this.path=path;
        this.fileName=fileName;
        thread = null;
    }

    private VarStore getVarStore() { return (VarStore) VarStore.getAppContext(); }

    public void search() {
        try {
            thread = new Thread() {
                @Override
                public void run() {
                    getVarStore().setSearchResults(new ArrayList<Object>());
                    try {
                        ArrayList<String> tmp = getVarStore().getMainOperationsTools().getAllObjectsInFolder(path);

                        if (tmp == null) {
                            dialog.dismiss();
                            ((Activity) context).setProgressBarIndeterminateVisibility(false);
                            Toast.makeText(context, context.getString(R.string.operation_result_fail), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        for (int i = 0; i < tmp.size(); i++) {
                            if (new File(tmp.get(i)).getName().contains(fileName)) {
                                //для совместимости с raw форматом: <сортировочный параметр>;<путь>;<тип файла>;<дата изменения>
                                /*String s = FileUtils.getObjectRawInfo(tmp.get(i));
                                if (s != null) {
                                    getVarStore().getSearchResults().add(s);
                                }*/
                                getVarStore().getSearchResults().add(FileUtils.getAndSortObjectsList(tmp.get(i), "", true).get(0));
                                if (Thread.currentThread().isInterrupted()) {
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            dialog.dismiss();
                                            ((Activity) context).setProgressBarIndeterminateVisibility(false);
                                            Toast.makeText(context, context.getString(R.string.operation_result_interrupted), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    break;
                                }
                            }
                        }
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                ((Activity) context).setProgressBarIndeterminateVisibility(false);
                                Toast.makeText(context, context.getString(R.string.operation_result_success), Toast.LENGTH_SHORT).show();
                                Method method = null;
                                try {
                                    method = context.getClass().getMethod(finishMethod);
                                } catch (NoSuchMethodException e) {
                                    e.printStackTrace();
                                }
                                if (method != null) {
                                    try {
                                        method.invoke(context);
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    } catch (InvocationTargetException e) {
                                        e.printStackTrace();
                                    } catch (IllegalArgumentException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                    } catch (Exception e) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                ((Activity) context).setProgressBarIndeterminateVisibility(false);
                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                                Toast.makeText(context, context.getString(R.string.operation_result_fail), Toast.LENGTH_SHORT).show();
                            }
                        });
                        Log.e("MainOperations.search", null, e);
                    }
                }
            };
            thread.start();
        } catch (Exception e) {
            Log.e("search", null, e);
        }
    }

    public void stopSearch() {
        try {
            if(thread!=null) {
                thread.interrupt();
            }
        } catch (Exception e) {
            Log.e("stopSearch", null, e);
        }
    }
}

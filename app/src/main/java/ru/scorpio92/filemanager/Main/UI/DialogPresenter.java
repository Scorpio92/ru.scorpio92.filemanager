package ru.scorpio92.filemanager.Main.UI;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.scorpio92.arch.Zip;
import ru.scorpio92.arch.ZipConstants;
import ru.scorpio92.arch.ZipParams;
import ru.scorpio92.arch.ZipProgressDialogParams;
import ru.scorpio92.filemanager.Main.Adapters.AppsListAdapter;
import ru.scorpio92.filemanager.R;
import ru.scorpio92.filemanager.Main.Types.ObjectProperties;
import ru.scorpio92.filemanager.Main.Utils.FileUtils;
import ru.scorpio92.filemanager.Main.Utils.SecondUsageUtils;
import ru.scorpio92.filemanager.Main.Utils.SettingsUtils;
import ru.scorpio92.filemanager.Main.Variables.Constants;
import ru.scorpio92.filemanager.Main.Variables.VarStore;
import ru.scorpio92.io.MainOperations;
import ru.scorpio92.io.MainOperationsConstants;
import ru.scorpio92.io.MainOperationsDialogParams;
import ru.scorpio92.io.MainOperationsParams;
import ru.scorpio92.security.AES.AES;
import ru.scorpio92.security.AES.AESConstants;
import ru.scorpio92.security.AES.AESParams;
import ru.scorpio92.security.AES.AESProgressDialogParams;
import ru.scorpio92.security.SHA1.SHA1;
import ru.scorpio92.security.SHA1.SHA1Constants;

/**
 * Created by scorpio92 on 29.07.16.
 */
public class DialogPresenter {

    private Context activityContext;
    private View activityView;
    private Boolean additionalPanelIsDisplayed; //переменная которая показывает, открыта ли доп панел (при копировании, перемещении), если открыта,

    public DialogPresenter(Context activityContext, View activityView) {
        this.activityContext = activityContext;
        this.activityView = activityView;
        additionalPanelIsDisplayed = false;
    }

    //получение хранилища переменных
    private VarStore getVarStore() { return (VarStore) VarStore.getAppContext(); }

    //показываем диалог переименования файла
    public void showRenameDialog(final int positionLongPressedFile) {
        try {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(activityContext);
            alertDialog.setTitle(activityContext.getString(R.string.rename_dialog_tittle));


            final String filePath = getVarStore().getCurrentDir().getObjects().get(positionLongPressedFile).path;
            final String oldName = getVarStore().getMainOperationsTools().getLastPathComponent(filePath);

            final EditText input = new EditText(activityContext);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);
            input.setText(oldName);
            alertDialog.setView(input);

            alertDialog.setPositiveButton(activityContext.getString(R.string.rename_dialog_positive_button),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Do nothing here because we override this button later to change the close behaviour.
                            //However, we still need this because on older versions of Android unless we
                            //pass a handler the button doesn't get instantiated
                        }
                    });


            alertDialog.setNegativeButton(activityContext.getString(R.string.negative_button),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (getVarStore().getMainOperationsInstance() != null) {
                                getVarStore().getMainOperationsInstance().stop();
                            }
                            dialog.cancel();
                        }
                    });

            final AlertDialog dialog = alertDialog.create();
            dialog.setCancelable(false);
            dialog.show();

            //переопределяем кнопку поиска чтобы окно не закрывалось если в поле ничего не ввели
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (input.getText().toString().trim().matches("")) { //если не задано имя файла
                            Toast.makeText(activityContext, activityContext.getString(R.string.empty_input_warning), Toast.LENGTH_SHORT).show();
                        } else {
                            if (oldName.equals(input.getText().toString().trim())) {
                                Toast.makeText(activityContext, activityContext.getString(R.string.rename_dialog_its_old_name), Toast.LENGTH_SHORT).show();
                            } else {

                                MainOperationsParams mainOperationsParams = new MainOperationsParams();
                                mainOperationsParams.setOperation(MainOperationsConstants.FILE_OPERATION_RENAME);
                                mainOperationsParams.setRunInThread(true); //операция переименования бует выполняться в основном потоке
                                mainOperationsParams.setSourceFile(filePath);
                                mainOperationsParams.setFileName(input.getText().toString().trim());
                                mainOperationsParams.setMainOperationsTools(getVarStore().getMainOperationsTools());

                                LinearLayout pview = getHorizontalPBView();

                                ProgressBar progressBar = (ProgressBar) pview.findViewById(R.id.OperationProgress);
                                TextView timer = (TextView) pview.findViewById(R.id.durationTime);

                                MainOperationsDialogParams mainOperationsDialogParams = new MainOperationsDialogParams(
                                        activityContext,
                                        getAlertDialog(activityContext, pview)
                                );

                                mainOperationsDialogParams.setProgressBar(progressBar);
                                mainOperationsDialogParams.setTimer(timer);
                                mainOperationsDialogParams.setNegativeButtonText(activityContext.getString(R.string.negative_button));
                                mainOperationsDialogParams.setSuccessText(activityContext.getString(R.string.operation_result_success));
                                mainOperationsDialogParams.setFailText(activityContext.getString(R.string.operation_result_fail));
                                mainOperationsDialogParams.setMethod(activityContext.getClass().getMethod("refreshCurrentDir"));
                                mainOperationsParams.setMainOperationsDialogParams(mainOperationsDialogParams);

                                final MainOperations mainOperations = new MainOperations(mainOperationsParams);

                                mainOperations.start();

                                dialog.dismiss();
                            }
                        }
                    } catch (Exception e) {
                        Log.e("showRenameDialog", null, e);
                    }
                }
            });
        } catch (Exception e) {
            Log.e("showRenameDialog", null, e);
        }
    }

    //показываем диалог удаления файлов
    public void showDeleteDialog(final int positionLongPressedFile) {
        try {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(activityContext);
            alertDialog.setTitle(activityContext.getString(R.string.delete_dialog_tittle));
            alertDialog.setMessage(activityContext.getString(R.string.delete_dialog_body));


            alertDialog.setPositiveButton(activityContext.getString(R.string.delete_dialog_positive_button),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                if (getVarStore().getCurrentDir().getSelectedObjectsIDs().isEmpty()) { //если не были выбраны файлы, удаляется тот файл/директория на котором было вызвано контесктное меню
                                    getVarStore().getCurrentDir().getSelectedObjectsIDs().add(positionLongPressedFile);
                                }
                                //записываем в буфер список текущих объектов для дальнейших операций с ними
                                //getVarStore().getBuffer().setTypesArray(getVarStore().getCurrentDirectory().getTypesArray());
                                //getVarStore().getBuffer().setFilesArray(getVarStore().getCurrentDirectory().getFilesArray());
                                //showHorizontalProgressDialog();
                                //getVarStore().getThreadWorker().execute(Constants.FILE_OPERATION_DELETE);


                                MainOperationsParams mainOperationsParams = new MainOperationsParams();
                                mainOperationsParams.setOperation(MainOperationsConstants.FILE_OPERATION_DELETE);
                                mainOperationsParams.setRunInThread(true);
                                mainOperationsParams.setObjects(getVarStore().getCurrentDir().getSelectedObjects());
                                mainOperationsParams.setMainOperationsTools(getVarStore().getMainOperationsTools());

                                LinearLayout pview = getHorizontalPBView();

                                ProgressBar progressBar = (ProgressBar) pview.findViewById(R.id.OperationProgress);
                                TextView timer = (TextView) pview.findViewById(R.id.durationTime);

                                MainOperationsDialogParams mainOperationsDialogParams = new MainOperationsDialogParams(
                                        activityContext,
                                        getAlertDialog(activityContext, pview)
                                );

                                mainOperationsDialogParams.setProgressBar(progressBar);
                                mainOperationsDialogParams.setTimer(timer);
                                mainOperationsDialogParams.setNegativeButtonText(activityContext.getString(R.string.negative_button));
                                mainOperationsDialogParams.setSuccessText(activityContext.getString(R.string.operation_result_success));
                                mainOperationsDialogParams.setFailText(activityContext.getString(R.string.operation_result_fail));
                                mainOperationsDialogParams.setMethod(activityContext.getClass().getMethod("refreshCurrentDir"));

                                //mainOperations.showProgressDialog(mainOperationsDialogParams);
                                mainOperationsParams.setMainOperationsDialogParams(mainOperationsDialogParams);

                                MainOperations mainOperations = new MainOperations(mainOperationsParams);

                                getVarStore().setMainOperationsInstance(mainOperations);

                                mainOperations.start();

                                dialog.dismiss();

                            } catch (Exception e) {
                                Log.e("delete.prerpare", null, e);
                            }
                        }
                    });

            alertDialog.setNegativeButton(activityContext.getString(R.string.negative_button),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (getVarStore().getMainOperationsInstance() != null) {
                                getVarStore().getMainOperationsInstance().stop();
                            }
                            dialog.cancel();
                        }
                    });

            alertDialog.setCancelable(false);
            alertDialog.show();
        } catch (Exception e) {
            Log.e("showDeleteDialog", null, e);
        }
    }

    //диалог для копирования и перемещения
    public void showCopyMoveDialog(final int positionLongPressedFile, int action) {
        try {
            if (getVarStore().getCurrentDir().getSelectedObjectsIDs().isEmpty()) { //если не были выбраны файлы, удаляется тот файл/директория на котором было вызвано контесктное меню
                getVarStore().getCurrentDir().getSelectedObjectsIDs().add(positionLongPressedFile);
            }

            Log.w("showCopyMoveDialog", Integer.toString(action));

            MainOperationsParams mainOperationsParams = new MainOperationsParams();
            mainOperationsParams.setOperation(action);
            mainOperationsParams.setSourceFile(getVarStore().getCurrentDir().getPath());
            mainOperationsParams.setObjects(getVarStore().getCurrentDir().getSelectedObjects());
            mainOperationsParams.setRunInThread(true);
            if(action == MainOperationsConstants.FILE_OPERATION_COPY) {
                mainOperationsParams.setAllowInselfCopy(SettingsUtils.getBooleanSettings(activityContext, Constants.GENERAL_SETTING_SELF_COPY_KEY));
            } else {
                mainOperationsParams.setAllowInselfCopy(false);
            }
            mainOperationsParams.setMainOperationsTools(getVarStore().getMainOperationsTools());
            getVarStore().setMainOperationsInstance(new MainOperations(mainOperationsParams));

            show_hide_additionalPanel(activityView, true, action);
        } catch (Exception e) {
            Log.e("showCopyMoveDialog", null, e);
        }
    }

    //свойства файла/директории
    public void showProperties(final int positionLongPressedFile) {
        try {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(activityContext);
            alertDialog.setTitle(activityContext.getString(R.string.properties));

            LayoutInflater inflater = (LayoutInflater) activityContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View dialoglayout = inflater.inflate(R.layout.object_properties, null);
            alertDialog.setView(dialoglayout);

            //getVarStore().setPropertiesDialog(dialoglayout);

            final String filePath = getVarStore().getCurrentDir().getObjects().get(positionLongPressedFile).path;
            final String fileType = getVarStore().getCurrentDir().getObjects().get(positionLongPressedFile).type;
            //String fileSize = FileUtils.getRightSize(Long.parseLong(getVarStore().getCurrentDirectory().getSizeArray().get(positionLongPressedFile)));
            //String fileSize = getVarStore().getCurrentDirectory().getSizeArray().get(positionLongPressedFile);
            //String fileCommonType = getVarStore().getCurrentDirectory().getTypesArray().get(positionLongPressedFile);
            //Log.w("positionLongPressedFile", Integer.toString(positionLongPressedFile) + " " + filePath);
            final ObjectProperties op = FileUtils.getFileProperties(filePath, fileType);

            //свойства
            //final TableLayout table = (TableLayout) dialoglayout.findViewById(R.id.properties_table);
            //table.setColumnShrinkable(1, true);
            //тип
            final TextView fileTypeTV = (TextView) dialoglayout.findViewById(R.id.properties_type);
            fileTypeTV.setText(op.getType());
            /*if(fileType.equals(Constants.OBJECT_TYPE_DIR) || fileType.equals(Constants.OBJECT_TYPE_SYMLINK_DIR)) { //если это директория или ссылка на директорию
                fileTypeTV.setText(Constants.OBJECT_TYPE_DIR_PROPERTY);
            } else { //если это файл
                fileTypeTV.setText(FileUtils.getMIMEType(filePath));
            }*/

            //полный путь
            final TextView name = (TextView) dialoglayout.findViewById(R.id.properties_name);
            name.setText(op.getName());
            name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(SecondUsageUtils.copyStrToBuffer(op.getName())) {
                        Toast.makeText(activityContext, activityContext.getString(R.string.successful_copy_to_cliboard), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            //absolutePath.setText(filePath);

            //время изменения
            final TextView changeTime = (TextView) dialoglayout.findViewById(R.id.properties_change_time);
            changeTime.setText(op.getChangeTime());
            //changeTime.setText(FileUtils.getChangeTime(filePath, fileType));

            //владелец
            final TextView owner = (TextView) dialoglayout.findViewById(R.id.properties_owner);
            owner.setText(op.getOwner());
            //owner.setText(FileUtils.getChangeTime(filePath));

            //группа
            final TextView group = (TextView) dialoglayout.findViewById(R.id.properties_group);
            group.setText(op.getGroup());

            //разрешения
            final TextView permissions = (TextView) dialoglayout.findViewById(R.id.properties_permissions);
            permissions.setText(op.getPermissions() + " (" + FileUtils.getNumberPermissionsFromString(op.getPermissions()) + ")");

            //размер
            final TextView size = (TextView) dialoglayout.findViewById(R.id.properties_size);
            Log.w("filePath", filePath);
            Log.w("fileType", fileType);
            if(fileType.equals(Constants.OBJECT_TYPE_DIR) || fileType.equals(Constants.OBJECT_TYPE_SYMLINK_DIR)) {
                size.setText(activityContext.getString(R.string.properties_calculating));
                //size.setText(FileUtils.getRightSize(FileUtils.getObjectSize(filePath, false)));
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            long size_in_kb = getVarStore().getMainOperationsTools().getObjectSize(filePath, false);
                            final String s =  getVarStore().getMainOperationsTools().getRightSize(size_in_kb) + " (" + Long.toString(size_in_kb) + " Bytes)";
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    size.setText(s);
                                }
                            });
                        } catch (Exception e) {
                            Log.e("wait rename", null, e);
                        }
                    }
                };
                thread.start();
            } else {
                long size_in_kb = getVarStore().getMainOperationsTools().getObjectSize(filePath, true);
                size.setText(getVarStore().getMainOperationsTools().getRightSize(size_in_kb) + " (" + Long.toString(size_in_kb) + " Bytes)");
            }
            //size.setText(fileSize);

            //хеш
            final SHA1 sha1 = new SHA1(SHA1Constants.CALC_FILE_HASH, getVarStore().getMainOperationsTools().getRightPath(filePath));
            sha1.setBBPath(Constants.BUSYBOX_PATH);
            final TextView hash = (TextView) dialoglayout.findViewById(R.id.properties_sha1);
            if (fileType.equals(Constants.OBJECT_TYPE_DIR) || fileType.equals(Constants.OBJECT_TYPE_SYMLINK_DIR)) {
                hash.setText(activityContext.getString(R.string.empty_string));
            } else {
                hash.setText(activityContext.getString(R.string.properties_calculating));
                //getVarStore().getBuffer().setTmpPath(FileUtils.getRightPath(filePath)); //передаем правильный путь в случае ссылки на файл
                //getVarStore().getThreadWorker().execute(Constants.FILE_OPERATION_GET_SHA1);

                sha1.setRunInThread(true);
                sha1.setHardCalc(true);
                sha1.start();

                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            while (true) {
                                if(sha1.getStatus()!=SHA1Constants.IN_PROGRESS) {
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            hash.setText(sha1.getHash());
                                        }
                                    });
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            Log.e("wait rename", null, e);
                        }
                    }
                };
                thread.start();
            }
            hash.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(SecondUsageUtils.copyStrToBuffer(hash.getText().toString())) {
                        Toast.makeText(activityContext, activityContext.getString(R.string.successful_copy_to_cliboard), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            //если объект является символьной ссылкой - показываем на кого ссылается
            //if(FileUtils.isSymlink(filePath)) {
            //final TextView symlink_label = (TextView) dialoglayout.findViewById(R.id.properties_symlink_label);
            //symlink_label.setVisibility(View.VISIBLE);
            final TextView symlink = (TextView) dialoglayout.findViewById(R.id.properties_symlink);
            symlink.setText(getVarStore().getMainOperationsTools().getRightPath(filePath));
            symlink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(SecondUsageUtils.copyStrToBuffer(symlink.getText().toString())) {
                        Toast.makeText(activityContext, activityContext.getString(R.string.successful_copy_to_cliboard), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            //}

            alertDialog.setPositiveButton(activityContext.getString(R.string.ok_button),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //getVarStore().getThreadWorker().finish(null);
                            //очищаем массив выделенных файлов
                            //getVarStore().getBuffer().clear();
                            if(!fileType.equals(Constants.OBJECT_TYPE_DIR) && !fileType.equals(Constants.OBJECT_TYPE_SYMLINK_DIR)) {
                                sha1.stop();
                            }
                            dialog.cancel();
                        }
                    });

            final AlertDialog dialog = alertDialog.create();
            dialog.setCancelable(false);
            dialog.show();
        } catch (Exception e) {
            Log.e("showProperties", null, e);
        }
    }

    //окно редактирования разрешений
    public void showEditPermissions(final int positionLongPressedFile) {
        try {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(activityContext);
            alertDialog.setTitle(activityContext.getString(R.string.edit_permissions));

            LayoutInflater inflater = (LayoutInflater) activityContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View dialoglayout = inflater.inflate(R.layout.edit_permissions, null);
            alertDialog.setView(dialoglayout);

            alertDialog.setNegativeButton(activityContext.getString(R.string.negative_button),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            final String filePath = getVarStore().getCurrentDir().getObjects().get(positionLongPressedFile).path;
            //Log.w("positionLongPressedFile", Integer.toString(positionLongPressedFile) + " " + filePath);
            ObjectProperties op = FileUtils.getFileProperties(filePath, Constants.OBJECT_TYPE_FILE);
            Boolean[] permissions = FileUtils.getBoolArrayFromPermissionString(op.getPermissions());

            //инициализируем чекбоксы
            final CheckBox ownerRead = (CheckBox) dialoglayout.findViewById(R.id.edit_permissions_owner_read);
            final CheckBox ownerWrite = (CheckBox) dialoglayout.findViewById(R.id.edit_permissions_owner_write);
            final CheckBox ownerExecute = (CheckBox) dialoglayout.findViewById(R.id.edit_permissions_owner_execute);
            final CheckBox groupRead = (CheckBox) dialoglayout.findViewById(R.id.edit_permissions_group_read);
            final CheckBox groupWrite = (CheckBox) dialoglayout.findViewById(R.id.edit_permissions_group_write);
            final CheckBox groupExecute = (CheckBox) dialoglayout.findViewById(R.id.edit_permissions_group_execute);
            final CheckBox othersRead = (CheckBox) dialoglayout.findViewById(R.id.edit_permissions_others_read);
            final CheckBox othersWrite = (CheckBox) dialoglayout.findViewById(R.id.edit_permissions_others_write);
            final CheckBox othersExecute = (CheckBox) dialoglayout.findViewById(R.id.edit_permissions_others_execute);


            if (permissions != null) {
                //разрешения для владельцца
                ownerRead.setChecked(permissions[0]);
                ownerWrite.setChecked(permissions[1]);
                ownerExecute.setChecked(permissions[2]);
                //разрешения для группы
                groupRead.setChecked(permissions[3]);
                groupWrite.setChecked(permissions[4]);
                groupExecute.setChecked(permissions[5]);
                //разрешение для остальных
                othersRead.setChecked(permissions[6]);
                othersWrite.setChecked(permissions[7]);
                othersExecute.setChecked(permissions[8]);
            } else {
                //разрешения для владельцца
                ownerRead.setChecked(false);
                ownerWrite.setChecked(false);
                ownerExecute.setChecked(false);
                //разрешения для группы
                groupRead.setChecked(false);
                groupWrite.setChecked(false);
                groupExecute.setChecked(false);
                //разрешение для остальных
                othersRead.setChecked(false);
                othersWrite.setChecked(false);
                othersExecute.setChecked(false);
            }

            alertDialog.setPositiveButton(activityContext.getString(R.string.edit_button),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Boolean[] bArray = new Boolean[9];

                            bArray[0] = ownerRead.isChecked();
                            bArray[1] = ownerWrite.isChecked();
                            bArray[2] = ownerExecute.isChecked();

                            bArray[3] = groupRead.isChecked();
                            bArray[4] = groupWrite.isChecked();
                            bArray[5] = groupExecute.isChecked();

                            bArray[6] = othersRead.isChecked();
                            bArray[7] = othersWrite.isChecked();
                            bArray[8] = othersExecute.isChecked();

                            if (!FileUtils.setMod(filePath, FileUtils.getNumberPermissionsFromBooleanArray(bArray))) {
                                Toast.makeText(VarStore.getAppContext(),
                                        activityContext.getString(R.string.edit_permissions_fail),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

            final AlertDialog dialog = alertDialog.create();
            dialog.setCancelable(false);
            dialog.show();
        } catch (Exception e) {
            Log.e("showEditPermissions", null, e);
        }
    }

    //диалог архивации
    public void showZipDialog(final int positionLongPressedFile) {
        try {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(activityContext);
            alertDialog.setTitle(activityContext.getString(R.string.zip_dialog_tittle));
            LayoutInflater inflater = (LayoutInflater) activityContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View dialoglayout = inflater.inflate(R.layout.zip, null);
            alertDialog.setView(dialoglayout);

            final EditText zipName = (EditText) dialoglayout.findViewById(R.id.zipName);
            final Spinner compLevel = (Spinner) dialoglayout.findViewById(R.id.compressLevel);
            final CheckBox setPass = (CheckBox) dialoglayout.findViewById(R.id.setPassword);
            final TextView passwordLabel = (TextView) dialoglayout.findViewById(R.id.passwordLabel);
            final EditText password = (EditText) dialoglayout.findViewById(R.id.passwordValue);
            final TextView encMethodLabel = (TextView) dialoglayout.findViewById(R.id.encryptMethodLabel);
            final Spinner encMethod = (Spinner) dialoglayout.findViewById(R.id.zipEncryptMethod);
            final Spinner split = (Spinner) dialoglayout.findViewById(R.id.zipSplit);

            //скрывам ввод пароля или отображаем
            setPass.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (setPass.isChecked()) {
                        passwordLabel.setVisibility(View.VISIBLE);
                        password.setVisibility(View.VISIBLE);
                        encMethodLabel.setVisibility(View.VISIBLE);
                        encMethod.setVisibility(View.VISIBLE);
                    } else {
                        passwordLabel.setVisibility(View.INVISIBLE);
                        password.setVisibility(View.INVISIBLE);
                        encMethodLabel.setVisibility(View.INVISIBLE);
                        encMethod.setVisibility(View.INVISIBLE);
                    }
                }
            });

            alertDialog.setPositiveButton(activityContext.getString(R.string.zip_dialog_positive_button),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Do nothing here because we override this button later to change the close behaviour.
                            //However, we still need this because on older versions of Android unless we
                            //pass a handler the button doesn't get instantiated
                        }
                    });


            alertDialog.setNegativeButton(activityContext.getString(R.string.negative_button),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //очищаем массив выделенных файлов
                            //getVarStore().getBuffer().clear();
                            if (getVarStore().getZipInstance() != null) {
                                getVarStore().getZipInstance().stop();
                            }
                            dialog.cancel();
                        }
                    });

            final AlertDialog dialog = alertDialog.create();
            dialog.setCancelable(false);
            dialog.show();

            //переопределяем кнопку чтобы окно не закрывалось если в поле ничего не ввели
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (zipName.getText().toString().trim().matches("")) { //если не задано имя файла
                        Toast.makeText(activityContext, activityContext.getString(R.string.empty_input_warning), Toast.LENGTH_SHORT).show();
                    } else {
                        //проверяем что задано уникальное имя для архива
                        String archiveName = zipName.getText().toString().trim() + ".zip";
                        if (new File(Constants.ARCHIVE_DIR_PACKED + "/" + archiveName).exists()) {
                            Toast.makeText(activityContext, activityContext.getString(R.string.zip_dialog_file_exists), Toast.LENGTH_SHORT).show();
                        } else {

                            if (getVarStore().getCurrentDir().getSelectedObjectsIDs().isEmpty()) { //если не были выбраны файлы, удаляется тот файл/директория на котором было вызвано контесктное меню
                                getVarStore().getCurrentDir().getSelectedObjectsIDs().add(positionLongPressedFile);
                            }

                            int spinner_pos;
                            int[] values;
                            //уровень сжатия
                            spinner_pos = compLevel.getSelectedItemPosition();
                            values = activityContext.getResources().getIntArray(R.array.zipCompressValues);
                            int compLevelVal = values[spinner_pos];

                            //разбиение на части
                            spinner_pos = split.getSelectedItemPosition();
                            values = activityContext.getResources().getIntArray(R.array.zipSplitValues);
                            int splitVal = values[spinner_pos];

                            //метод шифрования
                            spinner_pos = encMethod.getSelectedItemPosition();
                            values = activityContext.getResources().getIntArray(R.array.encryptMethodsValues);
                            int encMeth = values[spinner_pos];

                            //если устанавливается пароль - проверяем введен ли пароль
                            Boolean passChecked = false;

                            ZipParams zipParams = new ZipParams(
                                    ZipConstants.OPERATION_ZIP,
                                    getVarStore().getCurrentDir().getSelectedObjectsPaths(),
                                    Constants.ARCHIVE_DIR_PACKED + "/" + archiveName
                            );
                            zipParams.setCompressLevel(compLevelVal);
                            zipParams.setPartSize(splitVal);

                            if (setPass.isChecked()) {
                                if (password.getText().toString().trim().matches("")) {
                                    Toast.makeText(activityContext, activityContext.getString(R.string.empty_input_warning), Toast.LENGTH_SHORT).show();
                                } else {
                                    passChecked = true;

                                    zipParams.setNeedEncrypt(setPass.isChecked());
                                    zipParams.setPassword(password.getText().toString().trim());
                                    zipParams.setKeyLenght(encMeth);
                                }
                            } else {
                                passChecked = true;
                            }

                            if (passChecked) {
                                LinearLayout pview = getHorizontalPBView();

                                ProgressBar progressBar = (ProgressBar) pview.findViewById(R.id.OperationProgress);
                                TextView timer = (TextView) pview.findViewById(R.id.durationTime);

                                ZipProgressDialogParams zipProgressDialogParams = new ZipProgressDialogParams(activityContext, getAlertDialog(activityContext, pview));
                                zipProgressDialogParams.setProgressBar(progressBar);
                                zipProgressDialogParams.setTimer(timer);
                                zipProgressDialogParams.setSuccessText(activityContext.getString(R.string.operation_result_success));
                                zipProgressDialogParams.setFailText(activityContext.getString(R.string.operation_result_fail));
                                zipProgressDialogParams.setNegativeButtonText(activityContext.getString(R.string.negative_button));
                                try {
                                    zipProgressDialogParams.setMethod(activityContext.getClass().getMethod("showCompleteWorkFilesDialog"));
                                } catch (NoSuchMethodException e) {
                                    e.printStackTrace();
                                }

                                zipParams.setZipProgressDialogParams(zipProgressDialogParams);

                                Zip zip = new Zip(zipParams);
                                getVarStore().setZipInstance(zip);
                                zip.start();
                                dialog.dismiss();
                            }
                        }

                    }
                }
            });
        } catch (Exception e) {
            Log.e("showZipDialog", null, e);
        }
    }

    //диалог распаквки архива
    public void showUnzipDialog(final int positionLongPressedFile) {
        try {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(activityContext);
            alertDialog.setTitle(activityContext.getString(R.string.unzip_dialog_tittle));

            LayoutInflater inflater = (LayoutInflater) activityContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View dialoglayout = inflater.inflate(R.layout.unzip, null);
            alertDialog.setView(dialoglayout);

            final TextView unzpipMessage = (TextView) dialoglayout.findViewById(R.id.unzipMessage);
            final EditText inputPassword = (EditText) dialoglayout.findViewById(R.id.passwordForUnzip);


            final String archive = getVarStore().getCurrentDir().getObjects().get(positionLongPressedFile).path;
            final String archiveName = new File(archive).getName().substring(0, new File(archive).getName().length() - 4); //только имя файла без .zip

            unzpipMessage.setText(activityContext.getString(R.string.unzip_dialog_body) + " " + Constants.ARCHIVE_DIR_UNPACKED + "/" + archiveName);

            //если архив защищен паролем - разблокируем поле ввода пароля
            if(Zip.checkArchEncrypt(archive)) {
                inputPassword.setVisibility(View.VISIBLE);
            } else {
                inputPassword.setVisibility(View.INVISIBLE);
            }

            alertDialog.setPositiveButton(activityContext.getString(R.string.unzip_dialog_positive_button),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Do nothing here because we override this button later to change the close behaviour.
                            //However, we still need this because on older versions of Android unless we
                            //pass a handler the button doesn't get instantiated
                        }
                    });


            alertDialog.setNegativeButton(activityContext.getString(R.string.negative_button),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (getVarStore().getZipInstance() != null) {
                                getVarStore().getZipInstance().stop();
                            }
                            dialog.cancel();
                        }
                    });

            final AlertDialog dialog = alertDialog.create();
            dialog.setCancelable(false);
            dialog.show();

            //переопределяем кнопку чтобы окно не закрывалось если в поле ничего не ввели
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //проверяем что задано уникальное имя для архива
                    //String archiveName = FileUtils.getLastPathComponent(getVarStore().getCurrentDir().getObjects().get(positionLongPressedFile).path);
                    if (new File(Constants.ARCHIVE_DIR_UNPACKED + "/" + archiveName).exists()) {
                        Toast.makeText(activityContext, activityContext.getString(R.string.zip_dialog_file_exists), Toast.LENGTH_SHORT).show();
                    } else {

                        ArrayList<String> objects = new ArrayList<String>();
                        objects.add(archive);
                        ZipParams zipParams = new ZipParams(ZipConstants.OPERATION_UNZIP, objects, Constants.ARCHIVE_DIR_UNPACKED);

                        Boolean passChecked = false;
                        //если архив защищен паролем - показано поле ввода пароля. если показано поле ввода пароля - проверяем заполнено ли оно
                        //String password = null;
                        //если устанавливается пароль - проверяем введен ли пароль
                        if (inputPassword.isShown()) {
                            if (inputPassword.getText().toString().trim().matches("")) {
                                Toast.makeText(activityContext, activityContext.getString(R.string.empty_input_warning), Toast.LENGTH_SHORT).show();
                            } else {
                                zipParams.setNeedEncrypt(true);
                                zipParams.setPassword(inputPassword.getText().toString().trim());

                                passChecked = true;
                            }
                        } else {
                            passChecked = true;
                        }

                        if (passChecked) {
                            LinearLayout pview = getHorizontalPBView();

                            ProgressBar progressBar = (ProgressBar) pview.findViewById(R.id.OperationProgress);
                            TextView timer = (TextView) pview.findViewById(R.id.durationTime);

                            ZipProgressDialogParams zipProgressDialogParams = new ZipProgressDialogParams(activityContext, getAlertDialog(activityContext, pview));
                            zipProgressDialogParams.setProgressBar(progressBar);
                            zipProgressDialogParams.setTimer(timer);
                            zipProgressDialogParams.setSuccessText(activityContext.getString(R.string.operation_result_success));
                            zipProgressDialogParams.setFailText(activityContext.getString(R.string.operation_result_fail));
                            zipProgressDialogParams.setNegativeButtonText(activityContext.getString(R.string.negative_button));
                            try {
                                zipProgressDialogParams.setMethod(activityContext.getClass().getMethod("showCompleteWorkFilesDialog"));
                            } catch (NoSuchMethodException e) {
                                e.printStackTrace();
                            }

                            zipParams.setZipProgressDialogParams(zipProgressDialogParams);

                            Zip zip = new Zip(zipParams);
                            getVarStore().setZipInstance(zip);
                            zip.start();
                            dialog.dismiss();
                        }
                    }
                }
            });
        } catch (Exception e) {
            Log.e("showZipDialog", null, e);
        }
    }

    //шифрование файла
    public void showEncryptDialog(final int positionLongPressedFile) {
        try {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(activityContext);
            alertDialog.setTitle(activityContext.getString(R.string.encrypt_dialog_tittle));
            LayoutInflater inflater = (LayoutInflater) activityContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View dialoglayout = inflater.inflate(R.layout.encrypt, null);
            alertDialog.setView(dialoglayout);

            final EditText passwordField = (EditText) dialoglayout.findViewById(R.id.encrypt_password);
            final CheckBox deleteSource = (CheckBox) dialoglayout.findViewById(R.id.encrypt_delete_source);

            alertDialog.setPositiveButton(activityContext.getString(R.string.encrypt_dialog_positive_button),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Do nothing here because we override this button later to change the close behaviour.
                            //However, we still need this because on older versions of Android unless we
                            //pass a handler the button doesn't get instantiated
                        }
                    });


            alertDialog.setNegativeButton(activityContext.getString(R.string.negative_button),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if(getVarStore().getAesInstance()!=null) {
                                getVarStore().getAesInstance().stop();
                            }
                            dialog.cancel();
                        }
                    });

            final AlertDialog dialog = alertDialog.create();
            dialog.setCancelable(false);
            dialog.show();

            //переопределяем кнопку чтобы окно не закрывалось если в поле ничего не ввели
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (passwordField.getText().toString().trim().matches("")) { //если не задан пароль
                        Toast.makeText(activityContext, activityContext.getString(R.string.empty_input_warning), Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            AESParams aesParams = new AESParams(
                                    AESConstants.ENCRYPT,
                                    getVarStore().getCurrentDir().getObjects().get(positionLongPressedFile).path,
                                    Constants.CRYPTO_DIR_ENCRYPTED + "/" + new File(getVarStore().getCurrentDir().getObjects().get(positionLongPressedFile).path).getName(),
                                    passwordField.getText().toString().trim()
                            );

                            aesParams.setKeyLenght(Integer.parseInt(SettingsUtils.getStringSettings(activityContext, Constants.CRYPTO_KEY_LENGHT_KEY)));
                            Log.w("setKeyLenght", SettingsUtils.getStringSettings(activityContext, Constants.CRYPTO_KEY_LENGHT_KEY));
                            aesParams.setDeleteSource(deleteSource.isChecked());
                            aesParams.setFileBuffer(AESConstants.DEFAULT_BUFFER);
                            aesParams.setRunInThread(true);

                            //диалог прогресса
                            LinearLayout pview = getCirclePBView();

                            AESProgressDialogParams aesProgressDialogParams = new AESProgressDialogParams(
                                    activityContext,
                                    getAlertDialog(activityContext, pview)
                            );
                            aesProgressDialogParams.setNegativeButtonText(activityContext.getString(R.string.negative_button));
                            aesProgressDialogParams.setSuccessText(activityContext.getString(R.string.operation_result_success));
                            aesProgressDialogParams.setFailText(activityContext.getString(R.string.operation_result_fail));
                            aesProgressDialogParams.setMethod(activityContext.getClass().getMethod("showCompleteWorkFilesDialog"));
                            aesParams.setAesProgressDialogParams(aesProgressDialogParams);

                            AES aes = new AES(aesParams);

                            getVarStore().setAesInstance(aes);

                            aes.start();

                            SecondUsageUtils.hideVirtualKeyboard(passwordField);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        dialog.dismiss();

                    }
                }
            });


        } catch (Exception e) {
            Log.e("showEncryptDialog", null, e);
        }
    }

    //расшифровка
    public void showDecryptDialog(final int positionLongPressedFile) {
        try {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(activityContext);
            alertDialog.setTitle(activityContext.getString(R.string.decrypt_dialog_tittle));
            LayoutInflater inflater = (LayoutInflater) activityContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View dialoglayout = inflater.inflate(R.layout.decrypt, null);
            alertDialog.setView(dialoglayout);

            final EditText passwordField = (EditText) dialoglayout.findViewById(R.id.decrypt_password);
            final CheckBox deleteSource = (CheckBox) dialoglayout.findViewById(R.id.decrypt_delete_source);

            alertDialog.setPositiveButton(activityContext.getString(R.string.decrypt_dialog_positive_button),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Do nothing here because we override this button later to change the close behaviour.
                            //However, we still need this because on older versions of Android unless we
                            //pass a handler the button doesn't get instantiated
                        }
                    });


            alertDialog.setNegativeButton(activityContext.getString(R.string.negative_button),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if(getVarStore().getAesInstance()!=null) {
                                getVarStore().getAesInstance().stop();
                            }
                            dialog.cancel();
                        }
                    });

            final AlertDialog dialog = alertDialog.create();
            dialog.setCancelable(false);
            dialog.show();

            //переопределяем кнопку чтобы окно не закрывалось если в поле ничего не ввели
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (passwordField.getText().toString().trim().matches("")) { //если не задан пароль
                        Toast.makeText(activityContext, activityContext.getString(R.string.empty_input_warning), Toast.LENGTH_SHORT).show();
                    } else {
                        try {

                            AESParams aesParams = new AESParams(
                                    AESConstants.DECRYPT,
                                    getVarStore().getCurrentDir().getObjects().get(positionLongPressedFile).path,
                                    Constants.CRYPTO_DIR_DECRYPTED + "/" + new File(getVarStore().getCurrentDir().getObjects().get(positionLongPressedFile).path).getName(),
                                    passwordField.getText().toString().trim()
                            );

                            aesParams.setKeyLenght(Integer.parseInt(SettingsUtils.getStringSettings(activityContext, Constants.CRYPTO_KEY_LENGHT_KEY)));
                            aesParams.setDeleteSource(deleteSource.isChecked());
                            aesParams.setFileBuffer(AESConstants.DEFAULT_BUFFER);
                            aesParams.setRunInThread(true);

                            //диалог прогресса
                            LinearLayout pview = getCirclePBView();

                            AESProgressDialogParams aesProgressDialogParams = new AESProgressDialogParams(
                                    activityContext,
                                    getAlertDialog(activityContext, pview)
                            );
                            aesProgressDialogParams.setNegativeButtonText(activityContext.getString(R.string.negative_button));
                            aesProgressDialogParams.setSuccessText(activityContext.getString(R.string.operation_result_success));
                            aesProgressDialogParams.setFailText(activityContext.getString(R.string.operation_result_fail));
                            aesProgressDialogParams.setMethod(activityContext.getClass().getMethod("showCompleteWorkFilesDialog"));
                            aesParams.setAesProgressDialogParams(aesProgressDialogParams);

                            AES aes = new AES(aesParams);

                            getVarStore().setAesInstance(aes);

                            aes.start();

                            SecondUsageUtils.hideVirtualKeyboard(passwordField);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        dialog.dismiss();

                    }
                }
            });
        } catch (Exception e) {
            Log.e("showEncryptDialog", null, e);
        }
    }

    public void showOpenWithDialog(final int positionLongPressedFile) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activityContext);
        alertDialog.setTitle(activityContext.getString(R.string.apps_list_dialog_tittle));

        LayoutInflater inflater = (LayoutInflater) activityContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialoglayout = inflater.inflate(R.layout.all_apps, null);

        ListView listView = (ListView) dialoglayout.findViewById(R.id.apps_list);

        alertDialog.setView(dialoglayout);

        final ArrayList<String> appsNames=new ArrayList<String>(); //apps labels
        final ArrayList<String> packagesNames=new ArrayList<String>();
        final ArrayList<String> appsActivities=new ArrayList<String>();
        ArrayList<Drawable> appsIcons=new ArrayList<Drawable>();

        final PackageManager pm = activityContext.getPackageManager();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> appList = pm.queryIntentActivities(mainIntent, 0);
        Collections.sort(appList, new ResolveInfo.DisplayNameComparator(pm));

        for (ResolveInfo temp : appList) {
            appsNames.add(temp.loadLabel(pm).toString());
            packagesNames.add(temp.activityInfo.packageName);
            appsActivities.add(temp.activityInfo.name);
            try {
                appsIcons.add(pm.getApplicationIcon(temp.activityInfo.packageName));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        AppsListAdapter adapter = new AppsListAdapter(activityContext, appsNames, appsIcons);
        listView.setAdapter(adapter);

        final AlertDialog dialog = alertDialog.create();
        dialog.show();

        adapter.notifyDataSetChanged();

        final String filePath = getVarStore().getCurrentDir().getObjects().get(positionLongPressedFile).path;

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    if (!SecondUsageUtils.openFileWithPackage(activityContext, filePath, packagesNames.get(i))) {
                        Toast.makeText(activityContext, activityContext.getString(R.string.openWithException), Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Log.e("openFile with", null, e);
                } finally {
                    dialog.dismiss();
                }
            }
        });
    }

    //диалог открытия текстового файла
    public void showOpenTextDialog(final String path) {
        try {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(activityContext);
            alertDialog.setTitle(activityContext.getString(R.string.opentext_dialog_tittle));
            alertDialog.setMessage(activityContext.getString(R.string.opentext_dialog_body));

            alertDialog.setPositiveButton(activityContext.getString(R.string.ok_button),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if(!SecondUsageUtils.openTextFile(activityContext, path, false)) {
                                Toast.makeText(activityContext, activityContext.getString(R.string.openTextFileException), Toast.LENGTH_LONG).show();
                            }
                            dialog.dismiss();
                        }
                    });

            alertDialog.setNegativeButton(activityContext.getString(R.string.negative_button),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            /*if(!SecondUsageUtils.openFile(MainUI.this, path)) {
                                Toast.makeText(getApplicationContext(), getString(R.string.activityNotFoundException), Toast.LENGTH_LONG).show();
                            }*/
                            dialog.dismiss();
                        }
                    });

            final AlertDialog dialog = alertDialog.create();
            dialog.setCancelable(false);
            dialog.show();

        } catch (Exception e) {
            Log.e("showOpenTextDialog", null, e);
        }
    }


    /////////////////////////////Доп методы//////////////////////////////////
    //скрываем/показываем дополнительную панель (при копировании/перемещении)
    public void show_hide_additionalPanel(View v, Boolean show, int action) {
        LinearLayout panel = (LinearLayout) v.findViewById(R.id.additionalPanel);
        if(show) {
            additionalPanelIsDisplayed=true;
            panel.setVisibility(View.VISIBLE);
            Button button = (Button)panel.findViewById(R.id.paste_moveButton);
            if (action == MainOperationsConstants.FILE_OPERATION_COPY) {
                button.setText(activityContext.getString(R.string.additional_panel_paste));
            }
            if (action == MainOperationsConstants.FILE_OPERATION_MOVE) {
                button.setText(activityContext.getString(R.string.additional_panel_move));
            }
        } else {
            additionalPanelIsDisplayed=false;
            panel.setVisibility(View.GONE);
        }
    }

    public Boolean getAdditionalPanelIsDisplayed() {
        return additionalPanelIsDisplayed;
    }

    //получаем вью ProgressBar
    public LinearLayout getCirclePBView() {
        LayoutInflater inflater = (LayoutInflater) activityContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return (LinearLayout) inflater.inflate(R.layout.operation_progress_circle, null);
    }

    //получаем вью HorizontalProgressBar
    public LinearLayout getHorizontalPBView() {
        LayoutInflater inflater = (LayoutInflater) activityContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return (LinearLayout) inflater.inflate(R.layout.operation_progress_horizontal, null);
    }

    //получаем алерт диалог (для операций копирования, архивации и т.д.)
    public AlertDialog.Builder getAlertDialog(Context c, LinearLayout view) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(c);
        alertDialog.setTitle(activityContext.getString(R.string.horizontal_progress_dialog_tittle));
        //alertDialog.setMessage(activityContext.getString(R.string.horizontal_progress_dialog_body));
        alertDialog.setMessage("\n");
        alertDialog.setView(view);
        alertDialog.setCancelable(false);
        return alertDialog;
    }
}

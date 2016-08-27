package ru.scorpio92.filemanager.Main.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


import ru.scorpio92.filemanager.R;
import ru.scorpio92.filemanager.Main.UI.MainUI;
import ru.scorpio92.filemanager.Main.Utils.SettingsUtils;
import ru.scorpio92.filemanager.Main.Variables.Constants;
import ru.scorpio92.filemanager.Main.Variables.VarStore;

/**
 * Created by scorpio92 on 29.02.16.
 */
public class FileListAdapter extends BaseAdapter {
    private Context context;
    private VarStore varStore;
    Map<String, Integer> map = new HashMap<String, Integer>();
    private Boolean showDirSize, showFileSize, showChangeTime;

    /*public boolean ready = false;
    Listener listener;

    public void setListener(Listener listener) {
        this.listener = listener;
    }*/


    public FileListAdapter(Context c)
    {
        // TODO Auto-generated method stub
        context = c;
        varStore = (VarStore) context.getApplicationContext();
        initIcons();
        clearShowParams();
    }

    private void initIcons() {
        map.put("file_icon", R.drawable.file);
        map.put("dir_icon", R.drawable.folder);
        map.put("symlink_file_icon", R.drawable.file_symlink);
        map.put("symlink_dir_icon", R.drawable.folder_symlink);
    }

    public void clearShowParams() {
        showDirSize = null;
        showFileSize = null;
        showChangeTime = null;
    }

    public int getCount() {
        // TODO Auto-generated method stub
        return varStore.getCurrentDir().getObjects().size();
    }

    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        if (convertView == null) {
            convertView = inflater.inflate(R.layout.file_explorer_element, null);
        }

        try {
            final String strPath = varStore.getCurrentDir().getObjects().get(position).path;

            //чекбокс выбора файлов
            final CheckBox cb = (CheckBox) convertView.findViewById(R.id.objectSelector);

            if (varStore.getCurrentDir().isSelectAll()) { //если пользователь нажал Выбрать все
                cb.setChecked(true);

            } else { //если не нужно выбирать все, проверяем вхождение текущей позиуии в массив
                if(!varStore.getCurrentDir().getSelectedObjects().contains(position)) {
                    cb.setChecked(false); //снимаем флажок
                } else {
                    cb.setChecked(true);
                }
            }

            //слушатель нажатий на чекбоскс
            cb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    varStore.getCurrentDir().setSelectAll(false); //если нажали на какой то чекбокс

                    if (cb.isChecked()) { //выделям
                        if (!varStore.getCurrentDir().getSelectedObjects().contains(position)) {
                            varStore.getCurrentDir().getSelectedObjects().add(position); //добавляем в список выбранных файлов
                            showSelectedInfo();
                        }
                    } else { //снимаем выделение
                        if (varStore.getCurrentDir().getSelectedObjects().contains(position)) {
                            varStore.getCurrentDir().getSelectedObjects().remove((Object) position); //удаляем из списка выбранных файлов
                            showSelectedInfo();
                        }
                    }
                }
            });


            //иконка для директории/файла
            ImageView imageView = (ImageView) convertView.findViewById(R.id.fileIcon);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            String fileType = varStore.getCurrentDir().getObjects().get(position).type;
            if (fileType.equals(Constants.OBJECT_TYPE_DIR)) {
                imageView.setImageResource(map.get("dir_icon"));
            }
            if (fileType.equals(Constants.OBJECT_TYPE_SYMLINK_DIR)) {
                imageView.setImageResource(map.get("symlink_dir_icon"));
            }
            if (fileType.equals(Constants.OBJECT_TYPE_FILE)) {
                imageView.setImageResource(map.get("file_icon"));
            }
            if (fileType.equals(Constants.OBJECT_TYPE_SYMLINK_FILE)) {
                imageView.setImageResource(map.get("symlink_file_icon"));
            }

            // название файла/директории
            TextView file = (TextView) convertView.findViewById(R.id.fileName);
            file.setText(varStore.getMainOperationsTools().getLastPathComponent(strPath));
            //Log.w("adapter", varStore.getMainOperationsTools().getLastPathComponent(strPath));

            //свойства файлов и директорий
            if (showDirSize == null)
                showDirSize = SettingsUtils.getBooleanSettings(context, Constants.VIEW_SHOW_DIR_SIZE_KEY);

            if (showFileSize == null)
                showFileSize = SettingsUtils.getBooleanSettings(context, Constants.VIEW_SHOW_FILE_SIZE_KEY);

            if (showChangeTime == null)
                showChangeTime = SettingsUtils.getBooleanSettings(context, Constants.VIEW_SHOW_CHANGE_TIME_KEY);

            TextView fileProp = (TextView) convertView.findViewById(R.id.fileProp);
            String date = "";
            String size = "";
            String prop = "";

            if (showChangeTime) {
                date = context.getString(R.string.file_list_changed_prop) + " " + varStore.getCurrentDir().getObjects().get(position).date;
            }

            if ((fileType.equals(Constants.OBJECT_TYPE_DIR) && showDirSize) || (fileType.equals(Constants.OBJECT_TYPE_FILE) && showFileSize)) {
                size = context.getString(R.string.file_list_size_prop) + " " + varStore.getMainOperationsTools().getRightSize(varStore.getCurrentDir().getObjects().get(position).size);
            }

            prop = date + "  " + size;

            fileProp.setText(prop.trim());

        } catch (Exception e) {
            Log.e("getView", null, e);
        }

        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        //ready = false;
        clearShowParams(); //при каждом обновлении списка очищаем параметры отображения чтобы подхватились свежие данные из настроек
        //Log.w("notifyDataSetChanged", varStore.getCurrentDir().getObjects().get(0).path);
        super.notifyDataSetChanged();
        ///ready = true;
        /*if(listener != null) {
            listener.onListUpdateFinished();
        }*/
    }


    private void showSelectedInfo() {
        try {
            Method method= MainUI.class.getMethod("showSelectedInfo");
            try {
                //Log.w("test", "try invoke showSelectedInfo");
                method.invoke(context);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

}

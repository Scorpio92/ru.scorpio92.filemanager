package ru.scorpio92.filemanager.Main.UI;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import ru.scorpio92.filemanager.R;
import ru.scorpio92.filemanager.Main.Adapters.FileListAdapter;
import ru.scorpio92.filemanager.Main.Utils.SecondUsageUtils;
import ru.scorpio92.filemanager.Main.Variables.Constants;
import ru.scorpio92.filemanager.Main.Variables.VarStore;

/**
 * Created by scorpio92 on 21.07.16.
 */
public class ArchUnpacked extends Fragment {
    private ListView filesList;
    private View ac;

    private VarStore getVarStore() { return (VarStore) VarStore.getAppContext(); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ac = inflater.inflate(R.layout.activity_file_explorer, container, false);

        //скрываем полоски memory_usage_current_partition_layout и selected_info_layout
        ((LinearLayout) ac.findViewById(R.id.memory_usage_current_partition_layout)).setVisibility(View.GONE);
        ((LinearLayout) ac.findViewById(R.id.selected_info_layout)).setVisibility(View.GONE);
        ((LinearLayout) ac.findViewById(R.id.objects_count_layout)).setVisibility(View.GONE);
        ((View) ac.findViewById(R.id.separator0)).setVisibility(View.GONE);
        ((View) ac.findViewById(R.id.separator1)).setVisibility(View.GONE);
        ((View) ac.findViewById(R.id.separator2)).setVisibility(View.GONE);
        ((View) ac.findViewById(R.id.separator3)).setVisibility(View.GONE);

        filesList = (ListView) ac.findViewById(R.id.filesList);
        LinearLayout panel = (LinearLayout) ac.findViewById(R.id.additionalPanel);
        panel.setVisibility(View.GONE);//скрываем доп. панель, она не пригодится
        //используем адаптер для списка файлов
        FileListAdapter fla = new FileListAdapter(ArchUnpacked.this.getActivity());
        filesList.setAdapter(fla);
        WorkFiles.fla1= fla;
        initOnClickListeners(); //устанавливаем слушатели
        return ac;
    }

    private void initOnClickListeners() {
        filesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    String path = getVarStore().getMainOperationsTools().getRightPath(getVarStore().getCurrentDir().getObjects().get(i).path);
                    if (getVarStore().getCurrentDir().getObjects().get(i).type.equals(Constants.OBJECT_TYPE_DIR) ||
                            getVarStore().getCurrentDir().getObjects().get(i).type.equals(Constants.OBJECT_TYPE_SYMLINK_DIR)) {

                        getVarStore().getCurrentDir().setNewContent(path);
                        WorkFiles.refreshCurrentDir();
                        filesList.setSelection(0); //перелистываем в начало списка
                    } else {
                        if (!SecondUsageUtils.openFile(getActivity(), getVarStore().getMainOperationsTools().getRightPath(getVarStore().getCurrentDir().getObjects().get(i).path))) {
                            Toast.makeText(getActivity(), getString(R.string.activityNotFoundException), Toast.LENGTH_LONG).show();
                        }
                    }

                } catch (Exception e) {
                    Log.e("onItemClick", null, e);
                }
            }
        });

        //обработчик долгого нажатия, вызываем контекстное меню
        filesList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                showFileOperationsMenu(view, i);//показываем контекстное меню только если у нас не открыта доп панель
                return true;
            }
        });
    }

    private void showFileOperationsMenu(View v, final int positionLongPressedFile) {
        try {
            PopupMenu popupMenu = new PopupMenu(getActivity(), v);
            popupMenu.inflate(R.menu.work_files_arch_unpacked_operations);
            Menu menu = popupMenu.getMenu();

            //не даем открыть папку в TextViewer
            if (getVarStore().getCurrentDir().getObjects().get(positionLongPressedFile).type.equals(Constants.OBJECT_TYPE_DIR) ||
                    getVarStore().getCurrentDir().getObjects().get(positionLongPressedFile).type.equals(Constants.OBJECT_TYPE_SYMLINK_DIR)) {
                menu.findItem(R.id.file_operations_open_in_textviewer).setVisible(false);
                menu.findItem(R.id.file_operations_open_in_texteditor).setVisible(false);
                menu.findItem(R.id.file_operations_encrypt).setVisible(false);
                //menu.findItem(R.id.file_operations_decrypt).setVisible(false);
            }

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.file_operations_selector:
                            getVarStore().getCurrentDir().getSelectedObjects().clear();
                            if (!getVarStore().getCurrentDir().isSelectAll()) {
                                //добавляем всех файлы списка в массив
                                for (int i = 0; i < getVarStore().getCurrentDir().getObjects().size(); i++) {
                                    getVarStore().getCurrentDir().getSelectedObjects().add(i);
                                }
                                getVarStore().getCurrentDir().setSelectAll(true);
                            } else {
                                getVarStore().getCurrentDir().setSelectAll(false);
                            }
                            WorkFiles.fla1.notifyDataSetChanged();
                            return true;
                        case R.id.file_operations_open_in_textviewer:
                            if (!SecondUsageUtils.openTextFile(getActivity(), getVarStore().getCurrentDir().getObjects().get(positionLongPressedFile).path, false)) {
                                Toast.makeText(getActivity(), getString(R.string.openTextFileException), Toast.LENGTH_LONG).show();
                            }
                            return true;
                        case R.id.file_operations_delete:
                            new DialogPresenter(getActivity(), ac).showDeleteDialog(positionLongPressedFile);
                            return true;
                        default:
                            return false;
                    }
                }
            });
            popupMenu.show();

        } catch (Exception e) {
            Log.e("showFileOperationsMenu", null, e);
        }
    }


}

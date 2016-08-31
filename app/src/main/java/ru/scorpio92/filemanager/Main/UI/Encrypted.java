package ru.scorpio92.filemanager.Main.UI;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import ru.scorpio92.io.Types.Object;
import ru.scorpio92.filemanager.R;
import ru.scorpio92.filemanager.Main.Adapters.FileListAdapter;
import ru.scorpio92.filemanager.Main.Variables.VarStore;

/**
 * Created by scorpio92 on 21.07.16.
 */
public class Encrypted extends Fragment {
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
        FileListAdapter fla = new FileListAdapter(Encrypted.this.getActivity());
        filesList.setAdapter(fla);
        WorkFiles.fla0= fla;
        initOnClickListeners(); //устанавливаем слушатели
        return ac;
    }

    private void initOnClickListeners() {
        //открываем файл. только zip архивы
        filesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    String type = getVarStore().getCurrentDir().getObjects().get(i).type;
                    if(type.equals(Object.TYPE_FILE)) {
                        //SecondUsageUtils.openFile(getActivity(), getVarStore().getMainOperationsTools().getRightPath(getVarStore().getCurrentDir().getObjects().get(i).path));
                        new DialogPresenter(getActivity(), ac).showDecryptDialog(i);
                    } else {
                        Toast.makeText(getVarStore().getApplicationContext(), getString(R.string.it_is_not_file), Toast.LENGTH_SHORT).show();
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
            popupMenu.inflate(R.menu.work_files_encrypted_operations);
            //Menu menu = popupMenu.getMenu();

            /*if (getVarStore().getCurrentDir().getSelectedObjectsIDs().size() > 1) {
                menu.findItem(R.id.file_operations_decrypt).setVisible(false);
            }*/

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.file_operations_selector:
                            getVarStore().getCurrentDir().getSelectedObjectsIDs().clear();
                            if (!getVarStore().getCurrentDir().isSelectAll()) {
                                //добавляем всех файлы списка в массив
                                for (int i = 0; i < getVarStore().getCurrentDir().getObjects().size(); i++) {
                                    getVarStore().getCurrentDir().getSelectedObjectsIDs().add(i);
                                }
                                getVarStore().getCurrentDir().setSelectAll(true);
                            } else {
                                getVarStore().getCurrentDir().setSelectAll(false);
                            }
                            WorkFiles.fla0.notifyDataSetChanged();
                            return true;
                        /*case R.id.file_operations_decrypt:
                            new DialogPresenter(getActivity(), ac).showDecryptDialog(positionLongPressedFile);
                            return true;*/
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

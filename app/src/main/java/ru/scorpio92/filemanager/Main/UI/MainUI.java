package ru.scorpio92.filemanager.Main.UI;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import ru.scorpio92.filemanager.Main.Types.*;
import ru.scorpio92.io.Types.Object;
import ru.scorpio92.filemanager.Main.UI.Intro.Intro;
import ru.scorpio92.filemanager.R;
import ru.scorpio92.filemanager.Terminal.Terminal;
import ru.scorpio92.filemanager.Main.Adapters.FileListAdapter;
import ru.scorpio92.filemanager.Main.Utils.SearchUtils;
import ru.scorpio92.filemanager.Main.Utils.SecondUsageUtils;
import ru.scorpio92.filemanager.Main.Utils.FileUtils;
import ru.scorpio92.filemanager.Main.Utils.SettingsUtils;
import ru.scorpio92.filemanager.Main.Variables.Constants;
import ru.scorpio92.filemanager.Main.Variables.VarStore;
import ru.scorpio92.io.MainOperations;
import ru.scorpio92.io.MainOperationsConstants;
import ru.scorpio92.io.MainOperationsDialogParams;
import ru.scorpio92.io.MainOperationsParams;
import ru.scorpio92.io.MainOperationsTools;


public class MainUI extends Activity implements Callback {

    //получение хранилища переменных
    private VarStore getVarStore() { return (VarStore) this.getApplication(); }

    private CheckBox selector;
    private ImageButton refreshCurrenntDirButton;
    private EditText liveSearch;
    private ImageButton searchButton;
    private ImageButton sortButton;
    private ImageButton createButton;

    private LinearLayout memStatInCurrPartPanel;

    private ListView filesList;

    private LinearLayout objectsCountInCurrPartPanel;

    private boolean searchListDisplayed=false; //признак показано ли окно найденных результатов
    private boolean disableFastSearchMode = false; //если true - не передаем в метод обновления директории параметр grep для фильтрации



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //прогресс бар в ActionBar
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_file_explorer);

        memStatInCurrPartPanel = (LinearLayout) findViewById(R.id.memory_usage_current_partition_layout);
        show_hide_memStatInCurrPartition_panel();

        selector = (CheckBox) findViewById(R.id.selector);
        refreshCurrenntDirButton = (ImageButton) findViewById(R.id.refresh_button);
        liveSearch = (EditText) findViewById(R.id.liveSearch);
        searchButton = (ImageButton) findViewById(R.id.search_button);
        sortButton = (ImageButton) findViewById(R.id.sort_button);
        createButton = (ImageButton) findViewById(R.id.create_button);

        filesList = (ListView) findViewById(R.id.filesList);

        objectsCountInCurrPartPanel = (LinearLayout) findViewById(R.id.objects_count_layout);

        //устанавливаем настройки по умолчанию
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

        //инициализация переменных в хранилище переменных
        initVarStore();

        //скрываем дополнительную панель
        getVarStore().getDialogPresenter().show_hide_additionalPanel(this.findViewById(android.R.id.content), false, -1);

        //инициализация слушателей основных элементов
        initOnClickListeners();

        //проверяем Root права
        checkRoot(false);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_menu, menu);

        try {
            //в ActionBar вынесена отдельная кнопка "Закладки"
            ImageButton fp_button = (ImageButton) menu.findItem(R.id.action_favorite_path).getActionView();
            fp_button.setImageResource(R.drawable.ic_favorite_path_black);
            fp_button.setBackgroundColor(getResources().getColor(R.color.scorpio_yellow));
            fp_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showFavoritePathSelectDialog();
                }
            });
            fp_button.setOnLongClickListener(new AdapterView.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (!SecondUsageUtils.checkPathInFavorites(getVarStore().getCurrentDir().getPath())) {
                        showFavoritePathDialog(true);
                    } else {
                        showFavoritePathDialog(false);

                    }
                    return true;
                }
            });

            //в ActionBar вынесена отдельная кнопка "Панель" по щелчку на которую показывается/скрывается панель с функциональными кнопками (поиск, обновление и т.д.)
            ImageButton hp_button = (ImageButton) menu.findItem(R.id.action_menu_panel).getActionView();
            hp_button.setImageResource(R.drawable.ic_expand_more);
            hp_button.setBackgroundColor(getResources().getColor(R.color.scorpio_yellow));
            hp_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TableRow panelView = (TableRow) findViewById(R.id.actionPanel);
                    if (panelView.getVisibility() == View.VISIBLE) {
                        panelView.setVisibility(View.GONE);
                    } else {
                        panelView.setVisibility(View.VISIBLE);
                    }
                }
            });
            hp_button.setOnLongClickListener(new AdapterView.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    return true;
                }
            });
        } catch (Exception e) {
            Log.e("onCreateOptionsMenu", "custom buttons", e);
        }
        return true;
    }

    //меню приложения
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_menu_work_files) {
            getVarStore().getCurrentDir().setObjectsToBuffer();
            getVarStore().getCurrentDir().clear(); //перед вызовом меню чистим объект текущей директории чтобы активность загрузилась быстрее

            Intent intent = new Intent(this, WorkFiles.class);
            startActivity(intent);
            //startActivityForResult(intent, Constants.SETTINGS_ACTIVITY_REQUEST_CODE);
            return true;
        }

        if (id == R.id.action_menu_settings) {
            Intent intent = new Intent(this, Settings.class);
            startActivityForResult(intent, Constants.SETTINGS_ACTIVITY_REQUEST_CODE);
            return true;
        }

        //показываем диалог использования памяти на всех разделах (на не рутованном устройстве отображается только память на внешней КП)
        if (id == R.id.action_menu_mem_usage_stat) {
            showMemUsageDialog();
            return true;
        }

        //проверка прав рута
        //если в результате проверки прав нет или они не были предоставлены, права рута в дальнейшем не будут запрашиваться когда это необходимо
        if (id == R.id.action_menu_check_root) {
            checkRoot(true);
            return true;
        }

        //терминал
        if (id == R.id.action_menu_terminal) {
            Intent intent = new Intent(MainUI.this, Terminal.class);
            intent.putExtra("path", getVarStore().getCurrentDir().getPath());
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_menu_help) {
            Intent intent = new Intent(this, Intro.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_menu_about) {
            Intent intent = new Intent(this, About.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_menu_exit) {
            //finish();
            try {
                //записываем текущий путь в БД (используется когда настройка settings_general_save_last_path включена)
                SecondUsageUtils.setLastPath(getVarStore().getCurrentDir().getPath());
                //setResult(RESULT_OK, null);
            } catch (Exception e) {
                Log.e("setLastPath", null, e);
            }
            try {
                android.os.Process.killProcess(android.os.Process.myPid());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //обработка нажатия кнопки назад
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            //Log.w("(Back)CurrentPath: ", getVarStore().getCurrentDirectory().getCurrentPath());
            //когда отфильтровали при нажатии назад - показываем туже директорию без фильтра по имени файла
            if (liveSearch.getText().length() > 0) {
                //убираем фокус с живого поиска
                liveSearch.clearFocus();
                //при установке пустого текста автоматом сработает слушатель события изменения текста
                liveSearch.setText("");
                //liveSearch.setText(getString(R.string.live_search_default_text));
                //updateFileList(getVarStore().getCurrentDirectory().getCurrentPath());
                refreshCurrentDir();
            } else { //когда ничего не ищем в текущем представлении
                //очищаем строку живого поиска
                liveSearch.clearFocus();
                liveSearch.setText("");
                if (getVarStore().getCurrentDir().getPath().equals(Constants.ROOT_PATH)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.it_is_root_directory), Toast.LENGTH_SHORT).show();
                    //updateFileList(Constants.ROOT_PATH);
                } else {
                    //если показываются результаты поиска (!!!поиска не потекущему представлению!!!) - возвращаемся к папке из которой запустили поиск
                    if (searchListDisplayed) {
                        searchListDisplayed = false;
                        refreshCurrentDir();
                    } else { //иначе переходим в родительскую директорию
                        updateFileList(getVarStore().getMainOperationsTools().getParentDirectory(getVarStore().getCurrentDir().getPath()));
                        //updateFileList();
                    }
                }
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    //обработка результата при закрытии активности настроек
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case Constants.SETTINGS_ACTIVITY_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Bundle res = data.getExtras();
                    //если были изменены настройки представления, обновляем список чтобы увидеть изменения
                    //Log.w("view_settings_was_ch", String.valueOf(res.getBoolean(Constants.NEED_REFRESH_AFTER_SETTINGS_CHANGE)));
                    if(res.getBoolean(Constants.NEED_REFRESH_AFTER_SETTINGS_CHANGE)) {
                        show_hide_memStatInCurrPartition_panel();
                        updateFileList(getVarStore().getCurrentDir().getPath());
                    }
                }
                break;
            /*case Constants.INTRO_ACTIVITY_EXIT_CODE:
                if (resultCode == RESULT_OK) {
                    SecondUsageUtils.setRepeatedStart();
                    checkRoot(false);
                }
                break;*/
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            //записываем текущий путь в БД (используется когда настройка settings_general_save_last_path включена)
            SecondUsageUtils.setLastPath(getVarStore().getCurrentDir().getPath());
            //setResult(RESULT_OK, null);
        } catch (Exception e) {
            Log.e("onDestroy", null, e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        //MenuItem bedMenuItem = m.findItem(R.id.favorite_paths_item);

    }

    ///////////////////методы которые выполняются в первую очередь при запуске//////////////////////
    //запись значений переменных в VarStore
    private void initVarStore() {

        getVarStore().setCurrentDir(new Dir(Constants.DEFAULT_HOME_PATH));

        //адаптер листа
        getVarStore().setFLA(new FileListAdapter(MainUI.this));
        filesList.setAdapter(getVarStore().getFLA());

        //инстанс класса MainOperationsTools с кастомным бузибоксом
        getVarStore().setMainOperationsTools(new MainOperationsTools(Constants.BUSYBOX_PATH));

        //класс предоставляющий диалоги. передаем текущую активность и вьюху
        getVarStore().setDialogPresenter(new DialogPresenter(MainUI.this, this.findViewById(android.R.id.content)));
    }

    //инициализация обработчиков нажатий
    private void initOnClickListeners() {

        //устанавливаем слушатель на нажатие на кнопку список путей в ActionBar
        final int abTitleId = getResources().getIdentifier("action_bar_title", "id", "android");
        findViewById(abTitleId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPathListDialog();
            }
        });

        //обработчик нажатий на чекбокс выделить все
        selector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.w("selector.setOnClickListener", "!!!");
                getVarStore().getCurrentDir().getSelectedObjects().clear();
                if (selector.isChecked()) {
                    //добавляем всех файлы списка в массив
                    for (int i = 0; i < getVarStore().getCurrentDir().getObjects().size(); i++) {
                        getVarStore().getCurrentDir().getSelectedObjects().add(i);
                    }
                }
                getVarStore().getCurrentDir().setSelectAll(selector.isChecked());
                showSelectedInfo(); //показываем кол-во выделенных
                getVarStore().getFLA().notifyDataSetChanged();
            }
        });


        //обновить текущую директорию
        refreshCurrenntDirButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshCurrentDir();
            }
        });


        //живой поиск
        liveSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //обновляем текущее представление только если что-то ввели
                if (s.length() > 0) {
                    disableFastSearchMode = false;
                    refreshCurrentDir();
                }
            }
        });


        //глобальный поиск
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showGlobalSearchDialog();
            }
        });

        //сортировка
        sortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSortDialog();
            }
        });

        //создание файлов и директорий
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainUI.this);
                LayoutInflater inflater = getLayoutInflater();
                View dialoglayout = inflater.inflate(R.layout.create_actions, null);
                alertDialog.setView(dialoglayout);

                final ListView actionList = (ListView) dialoglayout.findViewById(R.id.createActionsListView);
                final AlertDialog dialog = alertDialog.create();
                dialog.show();

                //обработчик нажатий на список файлов
                actionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        try {
                            showCreateDialog(actionList.getItemAtPosition(i).toString());
                        } catch (Exception e) {
                            Log.e("onItemClick", null, e);
                        } finally {
                            dialog.dismiss();
                        }
                    }
                });
            }
        });



        //обработчик нажатий на список файлов
        filesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    String path = getVarStore().getMainOperationsTools().getRightPath(getVarStore().getCurrentDir().getObjects().get(i).path);
                    if (getVarStore().getCurrentDir().getObjects().get(i).type.equals(Constants.OBJECT_TYPE_DIR) ||
                            getVarStore().getCurrentDir().getObjects().get(i).type.equals(Constants.OBJECT_TYPE_SYMLINK_DIR)) {
                        getVarStore().setHistoryPath(path); //устанавливаем путь для истории
                        disableFastSearchMode = true;
                        updateFileList(path);
                        filesList.setSelection(0); //перелистываем в начало списка
                        //очищаем строку живого поиска
                        liveSearch.clearFocus();
                        liveSearch.setText("");
                        //liveSearch.setText(getString(R.string.live_search_default_text));
                    } else {
                        //пытаемся открыть файл используя расширение
                        Boolean isText = false;
                        //проверяем текстовый ли это файл
                        for (String s : Constants.TEXT_EXT_MASSIVE) {
                            if (getVarStore().getMainOperationsTools().getFileExt(path).equals(s)) {
                                isText = true;
                                showOpenTextDialog(path);
                            }
                        }
                        if (!isText)
                            if (!SecondUsageUtils.openFile(MainUI.this, path)) {
                                Toast.makeText(getApplicationContext(), getString(R.string.activityNotFoundException), Toast.LENGTH_LONG).show();
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
                if (!getVarStore().getDialogPresenter().getAdditionalPanelIsDisplayed()) {
                    showFileOperationsMenu(view, i);//показываем контекстное меню только если у нас не открыта доп панель
                }
                return true;
            }
        });




    }

    ////////////////////////////////////////////////////////////////////////////////////////////////


    //////////////////////////////////ActionBar/////////////////////////////////////////////////////
    //диалог добавлени/удаления путей в избранное
    private void showFavoritePathDialog(Boolean isAdd) {
        try {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

            if(isAdd) {
                alertDialog.setTitle(getString(R.string.favorite_dialog_add_tittle));
                alertDialog.setMessage(getString(R.string.favorite_dialog_add_body));
                //currPath.setText(getVarStore().getCurrentDirectory().getCurrentPath());
                //Log.w("test", getVarStore().getCurrentDirectory().getCurrentPath());
            } else {
                alertDialog.setTitle(getString(R.string.favorite_dialog_remove_tittle));
                alertDialog.setMessage(getString(R.string.favorite_dialog_remove_body) + "\n" + getVarStore().getCurrentDir().getPath());
                //currPath.setText(getVarStore().getCurrentDirectory().getCurrentPath());
            }

            final EditText input = new EditText(this);

            if(isAdd) {
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);

                alertDialog.setPositiveButton(getString(R.string.favorite_dialog_add_button),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Do nothing here because we override this button later to change the close behaviour.
                                //However, we still need this because on older versions of Android unless we
                                //pass a handler the button doesn't get instantiated
                            }
                        });

            } else {
                alertDialog.setPositiveButton(getString(R.string.favorite_dialog_remove_button),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Do nothing here because we override this button later to change the close behaviour.
                                //However, we still need this because on older versions of Android unless we
                                //pass a handler the button doesn't get instantiated
                            }
                        });

            }

            alertDialog.setNegativeButton(getString(R.string.negative_button),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });


            final AlertDialog dialog = alertDialog.create();
            dialog.setCancelable(false);
            dialog.show();

            //переопределяем кнопку поиска чтобы окно не закрывалось если в поле ничего не ввели
            if(isAdd) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (input.getText().toString().trim().matches("")) { //если не задано имя файла
                            Toast.makeText(getApplicationContext(), getString(R.string.empty_input_warning), Toast.LENGTH_SHORT).show();
                        } else {
                            //проверяем - нет ли записи с таким же алиасом в БД

                            if(!SecondUsageUtils.checkAliasInFavorites(input.getText().toString().trim())) {
                                //добавляем в БД
                                SecondUsageUtils.addPathInFavorites(input.getText().toString().trim(), getVarStore().getCurrentDir().getPath());
                                dialog.cancel();
                            } else {
                                Toast.makeText(getApplicationContext(), getString(R.string.favorite_dialog_alias_exists), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            } else {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //удаляем из БД
                        SecondUsageUtils.deletePathFromFavorites(getVarStore().getCurrentDir().getPath());
                        dialog.cancel();
                    }
                });
            }

        } catch (Exception e) {
            Log.e("showDeleteDialog", null, e);
        }
    }

    //показываем диалог избранных путей
    private void showFavoritePathSelectDialog() {
        try {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View dialoglayout = inflater.inflate(R.layout.favorite_paths_list, null);
            alertDialog.setView(dialoglayout);

            ArrayList<String> alias = new ArrayList<String>();
            ArrayList<String> result = SecondUsageUtils.getFavoritePaths();
            if(!result.isEmpty()) {
                for (int i=0;i<result.size();i++) {
                    alias.add(result.get(i));
                }
            }

            final ListView pathList = (ListView) dialoglayout.findViewById(R.id.favoritePathsListView);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, alias);
            pathList.setAdapter(adapter);
            final AlertDialog dialog = alertDialog.create();
            //dialog.setCancelable(false);
            dialog.show();

            pathList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    try {
                        ArrayList<String> result = SecondUsageUtils.getFavoritePathsByAlias(adapterView.getItemAtPosition(i).toString());
                        if (!result.isEmpty()) {
                            updateFileList(result.get(0));
                        }
                    } catch (Exception e) {
                        Log.e("onItemClick", null, e);
                    } finally {
                        dialog.dismiss();
                    }
                }
            });
        } catch (Exception e) {
            Log.e("showFavoritePathDialog", null, e);
        }
    }

    //диалог создания нового файла
    private void showCreateDialog(final String action) {
        try {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

            if(action.equals(getString(R.string.create_dialog_new_file))) {
                alertDialog.setTitle(getString(R.string.create_dialog_file_tittle));
                alertDialog.setMessage(getString(R.string.create_dialog_file_body));
            }
            if(action.equals(getString(R.string.create_dialog_new_dir))) {
                alertDialog.setTitle(getString(R.string.create_dialog_dir_tittle));
                alertDialog.setMessage(getString(R.string.create_dialog_dir_body));
            }

            final EditText input = new EditText(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);
            alertDialog.setView(input);

            alertDialog.setPositiveButton(getString(R.string.create_button),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Do nothing here because we override this button later to change the close behaviour.
                            //However, we still need this because on older versions of Android unless we
                            //pass a handler the button doesn't get instantiated
                        }
                    });


            alertDialog.setNegativeButton(getString(R.string.negative_button),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //очищаем массив выделенных файлов
                            //getVarStore().getBuffer().clear();
                            getVarStore().getCurrentDir().getSelectedObjects().clear();
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
                    if (input.getText().toString().trim().matches("")) { //если не задано имя файла
                        Toast.makeText(getApplicationContext(), getString(R.string.empty_input_warning), Toast.LENGTH_SHORT).show();
                    } else {
                        if (action.equals(getString(R.string.create_dialog_new_file))) {
                            if (getVarStore().getMainOperationsTools().createNewFile(getVarStore().getCurrentDir().getPath(), input.getText().toString().trim())) {
                                Toast.makeText(getApplicationContext(), getString(R.string.operation_result_success), Toast.LENGTH_SHORT).show();
                                refreshCurrentDir();
                            } else {
                                Toast.makeText(getApplicationContext(), getString(R.string.operation_result_fail), Toast.LENGTH_SHORT).show();
                            }
                        }
                        if (action.equals(getString(R.string.create_dialog_new_dir))) {
                            if (getVarStore().getMainOperationsTools().createNewDir(getVarStore().getCurrentDir().getPath(), input.getText().toString().trim())) {
                                Toast.makeText(getApplicationContext(), getString(R.string.operation_result_success), Toast.LENGTH_SHORT).show();
                                refreshCurrentDir();
                            } else {
                                Toast.makeText(getApplicationContext(), getString(R.string.operation_result_fail), Toast.LENGTH_SHORT).show();
                            }
                        }
                        dialog.cancel();
                    }
                }
            });
        } catch (Exception e) {
            Log.e("showRenameDialog", null, e);
        }

    }

    //обработчик кнопки поиска в верхней панели
    public void showGlobalSearchDialog() {


        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(getString(R.string.search_dialog_tittle));

        LayoutInflater inflater = getLayoutInflater();
        final View dialoglayout = inflater.inflate(R.layout.search, null);
        alertDialog.setView(dialoglayout);

        final EditText fileName = (EditText) dialoglayout.findViewById(R.id.searchFile);
        final CheckBox cb = (CheckBox) dialoglayout.findViewById(R.id.useCurrPathCheckBox);
        final TextView pathLabel = (TextView) dialoglayout.findViewById(R.id.searchPathLabel);
        final EditText path = (EditText) dialoglayout.findViewById(R.id.searchPath);

        //по-умолчанию ищем в текщей папке
        cb.setChecked(true);
        path.setVisibility(View.INVISIBLE);
        pathLabel.setVisibility(View.INVISIBLE);

        //скрывам ввод пути если ищем в текущей папке
        cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cb.isChecked()) {
                    pathLabel.setVisibility(View.INVISIBLE);
                    path.setVisibility(View.INVISIBLE);
                } else {
                    pathLabel.setVisibility(View.VISIBLE);
                    path.setVisibility(View.VISIBLE);
                }
            }
        });

        alertDialog.setPositiveButton(getString(R.string.search_dialog_positive_button),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Do nothing here because we override this button later to change the close behaviour.
                        //However, we still need this because on older versions of Android unless we
                        //pass a handler the button doesn't get instantiated
                    }
                });

        alertDialog.setNegativeButton(getString(R.string.negative_button),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        final AlertDialog dialog = alertDialog.create();
        dialog.setCancelable(false);
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if(getVarStore().getSearchUtils()!=null) {
                        getVarStore().getSearchUtils().stopSearch();
                    }
                    dialog.cancel();
                    setProgressBarIndeterminateVisibility(false);
                }
            });

        //переопределяем кнопку поиска чтобы окно не закрывалось если в поле ничего не ввели
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileName.getText().toString().trim().matches("")) { //если не задано имя файла
                    Toast.makeText(getApplicationContext(), getString(R.string.empty_input_warning), Toast.LENGTH_SHORT).show();
                } else {
                    Boolean pathChecked = false;
                    if (cb.isChecked()) { //если ищем в текущей папке
                        pathChecked=true;
                    } else {
                        if (path.getText().toString().trim().matches("")) { //если задаем путь и он пустой
                            Toast.makeText(getApplicationContext(), getString(R.string.empty_input_warning), Toast.LENGTH_SHORT).show();
                        } else {
                            if(new File(path.getText().toString().trim()).exists()) {
                                pathChecked=true;
                            } else {
                                Toast.makeText(getApplicationContext(), getString(R.string.search_dialog_incorrect_path), Toast.LENGTH_SHORT).show();
                            }

                        }
                    }

                    if(pathChecked) {
                        setProgressBarIndeterminateVisibility(true);
                        fileName.setEnabled(false);
                        cb.setEnabled(false);
                        path.setEnabled(false);
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                        //thread.start();
                        if (cb.isChecked()) { //если ищем в текущей папке
                            getVarStore().setSearchUtils(new SearchUtils(MainUI.this, dialog, "displaySearchResults", getVarStore().getCurrentDir().getPath(), fileName.getText().toString().trim()));
                            getVarStore().getSearchUtils().search();
                        } else {
                            getVarStore().setSearchUtils(new SearchUtils(MainUI.this, dialog, "displaySearchResults", path.getText().toString().trim(), fileName.getText().toString().trim()));
                            getVarStore().getSearchUtils().search();
                        }
                    }
                }
            }
        });
    }

    //сортировка
    public void showSortDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.sort_dialog, null);
        alertDialog.setView(dialoglayout);

        final ListView actionList = (ListView) dialoglayout.findViewById(R.id.sortListView);
        final AlertDialog dialog = alertDialog.create();
        //dialog.setCancelable(false);
        dialog.show();

        //обработчик нажатий на список файлов
        actionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(Constants.SORT_SETTING_KEY, Integer.toString(i));
                    editor.apply();
                    refreshCurrentDir();
                } catch (Exception e) {
                    Log.e("onItemClick", null, e);
                } finally {
                    dialog.dismiss();
                }
            }
        });
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////Контекстное меню. Операции контекстного меню////////////////////////////
    //показываем контекстное меню
    //в зависимости от типа файла, условий выбора неск объектов показываем соотв. пункты
    private void showFileOperationsMenu(View v, final int positionLongPressedFile) {
        try {
            PopupMenu popupMenu = new PopupMenu(this, v);
            popupMenu.inflate(R.menu.file_operations);
            Menu menu = popupMenu.getMenu();
            String filePath = getVarStore().getCurrentDir().getObjects().get(positionLongPressedFile).path;

            //пункты меню по архивации
            if(getVarStore().getMainOperationsTools().getFileExt(filePath).equals(Constants.ZIP_EXT)) {
                menu.findItem(R.id.file_operations_zip).setVisible(false);
            } else {
                menu.findItem(R.id.file_operations_unzip).setVisible(false);
            }

            //если были выбраны файлы (>1), то блокируем пункт переименовать, свойства, копировать имя, копировать путь, распаковать, разрешения, TextViewer, TextEditor
            if (getVarStore().getCurrentDir().getSelectedObjects().size() > 1) {
                menu.findItem(R.id.file_operations_rename).setVisible(false);
                menu.findItem(R.id.file_operations_properties).setVisible(false);
                //menu.findItem(R.id.file_operations_copy_name_to_buffer).setVisible(false);
                //menu.findItem(R.id.file_operations_copy_path_to_buffer).setVisible(false);
                menu.findItem(R.id.file_operations_unzip).setVisible(false);
                menu.findItem(R.id.file_operations_permissions).setVisible(false);
                menu.findItem(R.id.file_operations_open_in_textviewer).setVisible(false);
                menu.findItem(R.id.file_operations_open_in_texteditor).setVisible(false);
                menu.findItem(R.id.file_operations_encrypt).setVisible(false);
                //menu.findItem(R.id.file_operations_decrypt).setVisible(false);
            }

            //не даем открыть папку в TextViewer
            if (getVarStore().getCurrentDir().getObjects().get(positionLongPressedFile).type.equals(Constants.OBJECT_TYPE_DIR) ||
                    getVarStore().getCurrentDir().getObjects().get(positionLongPressedFile).type.equals(Constants.OBJECT_TYPE_SYMLINK_DIR)) {
                menu.findItem(R.id.file_operations_open_in_textviewer).setVisible(false);
                menu.findItem(R.id.file_operations_open_in_texteditor).setVisible(false);
                menu.findItem(R.id.file_operations_encrypt).setVisible(false);
                //menu.findItem(R.id.file_operations_decrypt).setVisible(false);
            }

            menu.findItem(R.id.file_operations_decrypt).setVisible(false); //пункт меню "Расшифровать" не показываем нигде кроме активности Рабочие файлы

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {

                        case R.id.file_operations_copy:
                            getVarStore().getDialogPresenter().showCopyMoveDialog(positionLongPressedFile, MainOperationsConstants.FILE_OPERATION_COPY);
                            return true;

                        case R.id.file_operations_move:
                            getVarStore().getDialogPresenter().showCopyMoveDialog(positionLongPressedFile, MainOperationsConstants.FILE_OPERATION_MOVE);
                            return true;

                        case R.id.file_operations_delete:
                            getVarStore().getDialogPresenter().showDeleteDialog(positionLongPressedFile);
                            return true;

                        case R.id.file_operations_open_in_textviewer:
                            if (!SecondUsageUtils.openTextFile(MainUI.this, getVarStore().getCurrentDir().getObjects().get(positionLongPressedFile).path, false)) {
                                Toast.makeText(getApplicationContext(), getString(R.string.openTextFileException), Toast.LENGTH_LONG).show();
                            }
                            return true;

                        case R.id.file_operations_open_in_texteditor:
                            if (!SecondUsageUtils.openTextFile(MainUI.this, getVarStore().getCurrentDir().getObjects().get(positionLongPressedFile).path, true)) {
                                Toast.makeText(getApplicationContext(), getString(R.string.openTextFileException), Toast.LENGTH_LONG).show();
                            }
                            return true;

                        case R.id.file_operations_rename:
                            getVarStore().getDialogPresenter().showRenameDialog(positionLongPressedFile);
                            return true;

                        case R.id.file_operations_properties:
                            getVarStore().getDialogPresenter().showProperties(positionLongPressedFile);
                            return true;

                        case R.id.file_operations_permissions:
                            getVarStore().getDialogPresenter().showEditPermissions(positionLongPressedFile);
                            return true;

                        /*case R.id.file_operations_copy_name_to_buffer:
                            SecondUsageUtils.copyStrToBuffer(getVarStore().getCurrentDirectory().getFilesArray().get(positionLongPressedFile), true);
                            return true;

                        case R.id.file_operations_copy_path_to_buffer:
                            SecondUsageUtils.copyStrToBuffer(getVarStore().getCurrentDirectory().getFilesArray().get(positionLongPressedFile), false);
                            return true;*/

                        case R.id.file_operations_zip:
                            getVarStore().getDialogPresenter().showZipDialog(positionLongPressedFile);
                            return true;

                        case R.id.file_operations_unzip:
                            getVarStore().getDialogPresenter().showUnzipDialog(positionLongPressedFile);
                            return true;

                        case R.id.file_operations_encrypt:
                            getVarStore().getDialogPresenter().showEncryptDialog(positionLongPressedFile);
                            return true;

                        case R.id.file_operations_decrypt:
                            getVarStore().getDialogPresenter().showDecryptDialog(positionLongPressedFile);
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

    //когда запакуем/распакуем или зашифруем/расшифруем файлы, выводим диалог пользователю - показать результат в окне рабочие файлы или нажать отмену
    //рекурсивно вызываем его из библиотек arch и security
    public void showCompleteWorkFilesDialog() {
        try {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainUI.this);
            alertDialog.setTitle(getString(R.string.open_work_files_tittle));
            alertDialog.setMessage(getString(R.string.open_work_files_body));

            alertDialog.setPositiveButton(getString(R.string.positive_button),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                getVarStore().getCurrentDir().clear(); //перед вызовом меню чистим объект текущей директории чтобы активность загрузилась быстрее
                                Intent intent = new Intent(MainUI.this, WorkFiles.class);
                                startActivity(intent);
                            } catch (Exception e) {

                            }
                        }
                    });

            alertDialog.setNegativeButton(getString(R.string.no_button),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //refreshCurrentDir();
                            getVarStore().getCurrentDir().getSelectedObjects().clear();
                            dialog.dismiss();
                        }
                    });

            alertDialog.setCancelable(false);
            alertDialog.show();

        } catch (Exception e) {
            Log.e("showCompleteWorkFilesDialog", null, e);
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////


    /////////////////Дополнительные методы взимодействия с интерфейсом//////////////////////////////
    //статистика использования памяти на всех разделах (или только на внешней КП если отсутствует рут)
    private void showMemUsageDialog() {
        try {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View dialoglayout = inflater.inflate(R.layout.memory_usage_dialog, null);
            alertDialog.setView(dialoglayout);

            TableLayout table = (TableLayout) dialoglayout.findViewById(R.id.memory_usage_table_layout);
            TableRow row;

            //получаем статистику по остальным разделам
            ArrayList<String> memStat = FileUtils.getSpaceUsage(null);

            if (!memStat.isEmpty()) {
                for (int i = 0; i < memStat.size(); i++) {
                    row = new TableRow(this);
                    String[] mas = memStat.get(i).split(" ");

                    TextView t = new TextView(this);
                    t.setText(mas[3] + "  ");
                    t.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.memory_usage_dialog_text_height));
                    t.setPadding(10,0,0,0);
                    row.addView(t);
                    for(int j=0; j< 3; j++) {
                        t = new TextView(this);
                        t.setText(mas[j] + "  ");
                        t.setGravity(Gravity.CENTER);
                        t.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.memory_usage_dialog_text_height));
                        t.setPadding(10,0,0,0);
                        row.addView(t);
                    }
                    table.addView(row, new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));
                }
            } else { //если информация о разделах не была получена
                TextView noInfo = (TextView) dialoglayout.findViewById(R.id.memory_usage_point_no_info);
                noInfo.setVisibility(View.VISIBLE);
            }

            final AlertDialog dialog = alertDialog.create();
            dialog.show();
        } catch (Exception e) {
            Log.e("showMemUsageDialog", null, e);
        }
    }

    //использование памяти в текущем разделе
    private void showMemoryUsage(String path) {
        try {
            TextView mTextView = (TextView) this.findViewById(R.id.memory_usage_current_partition);
            try {
                String[] mas = FileUtils.getSpaceUsage(path).get(0).split(" ");
                mTextView.setText(getString(R.string.memory_usage_point_size) + ": " + mas[0] + ",  "
                        + getString(R.string.memory_usage_point_usage) + ": " + mas[1] + ",  "
                        + getString(R.string.memory_usage_available) + ": " + mas[2]);
            } catch (Exception ee) {
                mTextView.setText("no info");
            }
        } catch (Exception e) {
            Log.e("showMemoryUsage", null, e);
        }
    }

    //диалог открытия текстового файла
    private void showOpenTextDialog(final String path) {
        try {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle(getString(R.string.opentext_dialog_tittle));
            alertDialog.setMessage(getString(R.string.opentext_dialog_body));

            alertDialog.setPositiveButton(getString(R.string.ok_button),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if(!SecondUsageUtils.openTextFile(MainUI.this, path, false)) {
                                Toast.makeText(getApplicationContext(), getString(R.string.openTextFileException), Toast.LENGTH_LONG).show();
                            }
                            dialog.dismiss();
                        }
                    });

            alertDialog.setNegativeButton(getString(R.string.negative_button),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if(!SecondUsageUtils.openFile(MainUI.this, path)) {
                                Toast.makeText(getApplicationContext(), getString(R.string.activityNotFoundException), Toast.LENGTH_LONG).show();
                            }
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

    //метод вызывается релфлексивно из SearchUtils
    public void displaySearchResults() { //этот метод вызывается извне при окончани операции поиска файлов
        try {
            if(!getVarStore().getSearchResults().isEmpty()) {
                //нужно очистить список файлов и передать пути найденных файлов
                getVarStore().getCurrentDir().setNewContent(
                        getVarStore().getCurrentDir().getPath(),
                        getVarStore().getSearchResults()
                );
                //обновляем список
                MainUI.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        selector.setChecked(false);//при обновлении списка убираем флажок
                        filesList.setSelection(0); //автоскролинг на перввый элемент
                        getVarStore().getFLA().notifyDataSetChanged(); //обновляем текущее представление
                    }
                });
                //устанавливаем признак того что отобразились результаты поиска
                //нужно для избежания проблем при вы полнении операции дублирования и переходу назад к той директории из кот был запущен поиск
                searchListDisplayed = true;
            }
        } catch (Exception e) {
            Log.e("displaySearchResults", null, e);
        }
    }

    //показываем список путей
    private void showPathListDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.history_path_list, null);
        alertDialog.setView(dialoglayout);

        ListView pathList = (ListView) dialoglayout.findViewById(R.id.historyListView);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                getVarStore().getMainOperationsTools().getAllPossiblePaths(getVarStore().getHistoryPath()));
        pathList.setAdapter(adapter);

        final AlertDialog dialog = alertDialog.create();
        dialog.show();

        //обработчик нажатий на список файлов
        pathList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    updateFileList(adapterView.getItemAtPosition(i).toString());
                } catch (Exception e) {
                    Log.e("onItemClick", null, e);
                } finally {
                    dialog.dismiss();
                }
            }
        });
    }

    //дополнительная строка: сколько объектов выделено и сколько места занимают
    public void showSelectedInfo() {
        try {
            final TextView mTextView = (TextView) this.findViewById(R.id.selected_info);
            if(getVarStore().getCurrentDir().getSelectedObjects().size() >0) {
                if(SettingsUtils.getBooleanSettings(this, Constants.VIEW_SHOW_SELECTED_FILES_SIZE_KEY)) { //если нужно показывать размер выделенных файлов
                    mTextView.setText(getString(R.string.selected) + ": " + Integer.toString(getVarStore().getCurrentDir().getSelectedObjects().size()) + ", " + getString(R.string.selected_full_size) + ": " +getString(R.string.selected_size_calculating));
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            try {
                                long tmp=0;
                                for(int i=0; i<getVarStore().getCurrentDir().getSelectedObjects().size();i++) {
                                    Object o = getVarStore().getCurrentDir().getObjects().get(getVarStore().getCurrentDir().getSelectedObjects().get(i));
                                    String path = o.path;
                                    String type = o.type;
                                    if(type.equals(Object.TYPE_DIR) || type.equals(Object.TYPE_SYMLINK_DIR)) {
                                        tmp += getVarStore().getMainOperationsTools().getObjectSize(path, false);
                                    } else {
                                        tmp += getVarStore().getMainOperationsTools().getObjectSize(path, true);
                                    }
                                }
                                for(String path:getVarStore().getCurrentDir().getSelectedObjectsPaths()) {
                                    Log.w("fdsfsdf", path);
                                }
                                final long final_size=tmp;
                                MainUI.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mTextView.setText(getString(R.string.selected) + ": " + Integer.toString(getVarStore().getCurrentDir().getSelectedObjects().size()) +", " + getString(R.string.selected_full_size) + ": " + getVarStore().getMainOperationsTools().getRightSize(final_size));
                                    }
                                });
                            } catch (Exception e) {
                                Log.e("showSelectedInfo.thread", null, e);
                            }
                        }
                    };
                    thread.start();

                } else {
                    mTextView.setText(getString(R.string.selected) + ": " + Integer.toString(getVarStore().getCurrentDir().getSelectedObjects().size()));
                }
                mTextView.setVisibility(View.VISIBLE);
            } else {
                mTextView.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e("showSelectedInfo", null, e);
        }
    }


    //Дополнительная панель
    //обработчики нажатий на дополнительное меню
    public void onCancelClick(View view) {
        getVarStore().getDialogPresenter().show_hide_additionalPanel(this.findViewById(android.R.id.content), false, -1);
        if(getVarStore().getMainOperationsInstance()!=null) {
            getVarStore().getMainOperationsInstance().stop();
        }
    }

    //копировать или переместить
    public void onPasteMoveClick(View view) {
        try {
            //проверяем, если пытаемся скопировать или переместить внутрь какой то выделенной директории - выдаем ошибку
            Boolean stop = false;
            String currPath = getVarStore().getCurrentDir().getPath(); //директория куда копируем
            MainOperationsParams mainOperationsParams = getVarStore().getMainOperationsInstance().getMainOperationsParams();

            mainOperationsParams.setDistFile(currPath);

            LinearLayout pview = getVarStore().getDialogPresenter().getHorizontalPBView();

            ProgressBar progressBar = (ProgressBar) pview.findViewById(R.id.OperationProgress);
            TextView timer = (TextView) pview.findViewById(R.id.durationTime);

            MainOperationsDialogParams mainOperationsDialogParams = new MainOperationsDialogParams(
                    MainUI.this,
                    getVarStore().getDialogPresenter().getAlertDialog(MainUI.this, pview)
            );

            mainOperationsDialogParams.setProgressBar(progressBar);
            mainOperationsDialogParams.setTimer(timer);
            mainOperationsDialogParams.setNegativeButtonText(getString(R.string.negative_button));
            mainOperationsDialogParams.setSuccessText(getString(R.string.operation_result_success));
            mainOperationsDialogParams.setFailText(getString(R.string.operation_result_fail));
            mainOperationsDialogParams.setMethod(MainUI.class.getMethod("refreshCurrentDir"));

            //mainOperations.showProgressDialog(mainOperationsDialogParams);
            mainOperationsParams.setMainOperationsDialogParams(mainOperationsDialogParams);

            MainOperations mainOperations = new MainOperations(mainOperationsParams);

            getVarStore().setMainOperationsInstance(mainOperations);

            mainOperations.start();

            getVarStore().getDialogPresenter().show_hide_additionalPanel(this.findViewById(android.R.id.content), false, -1);
        } catch (Exception e) {
            Log.e("onPasteMoveClick", null, e);
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////вспомогательные методы//////////////////////////////////////////
    //проверка наличия root прав
    private void checkRoot(Boolean forceCheck) {
        try {
            if (SecondUsageUtils.checkRootAvailableParameter().equals(Constants.ROOT_NEED_TO_CHECK) || forceCheck) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle(getString(R.string.checkRootDialogTittle));

                LayoutInflater inflater = getLayoutInflater();
                View dialoglayout = inflater.inflate(R.layout.root_check_dialog, null);
                alertDialog.setView(dialoglayout);

                final TextView time = (TextView) dialoglayout.findViewById(R.id.rootCheckDialogTime);
                time.setText("0");

                final AlertDialog dialog = alertDialog.create();
                dialog.setCancelable(false);
                dialog.show();

                final Thread thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            int i = 0;
                            while (i < Integer.parseInt(SettingsUtils.getStringSettings(getApplicationContext(), Constants.ROOT_TIME))) {
                                i++;
                                final String sec = String.valueOf(i);
                                MainUI.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        time.setText(sec);
                                    }
                                });
                                sleep(1000);
                            }

                            if(SecondUsageUtils.getAndSetRootStatus()) {
                                MainUI.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), getString(R.string.rootCheckDialogOKcheck), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                MainUI.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), getString(R.string.rootCheckDialogFAILcheck), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            dialog.cancel();
                        } catch (Exception e) {
                            Log.e("checkRoot thread ex", null, e);
                        } finally {
                            MainUI.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //открываем путь по-умолчанию или последний
                                    initStartPath();
                                }
                            });
                        }
                    }
                };
                thread.start();
            } else {
                //открываем путь по-умолчанию или последний
                initStartPath();
            }
        } catch (Exception e) {
            Log.e("checkRoot", null, e);
        }
        //return false;
    }

    //инициализация начального пути.
    //если указана настройка "Запоминать последний путь" - открываем последний
    private void initStartPath() {
        try {
            if(SettingsUtils.getBooleanSettings(this, Constants.GENERAL_SETTING_SAVE_LAST_PATH_KEY)) {
                getVarStore().setHistoryPath(SecondUsageUtils.getLastPath());
                updateFileList(SecondUsageUtils.getLastPath());
            } else {
                //по-умолчанию при старте переходим на внешнюю КП
                getVarStore().setHistoryPath(Constants.DEFAULT_HOME_PATH);
                updateFileList(Constants.DEFAULT_HOME_PATH);
            }
        } catch (Exception e) {
            Log.e("initStartPath", null, e);
            try {
                getVarStore().setHistoryPath(Constants.DEFAULT_HOME_PATH);
                updateFileList(Constants.DEFAULT_HOME_PATH);
            } catch (Exception e2) {
                Log.e("initStartPath", null, e2);
            }
        }
    }

    //обновление списка файлов директории
    private void updateFileList(final String path){
        try {
            setProgressBarIndeterminateVisibility(true);
            //устанавливаем заголовки
            getActionBar().setTitle(getVarStore().getMainOperationsTools().getLastPathComponent(path));
            getActionBar().setSubtitle(getVarStore().getMainOperationsTools().getParentDirectory(path));
            //очищаем список (во избежание нажатий)
            getVarStore().getCurrentDir().clear();
            showSelectedInfo(); //после вызова getVarStore().getCurrentDirectory().clear() не останется выделенных файлов - скрываем панель
            getVarStore().getFLA().notifyDataSetChanged(); //обновляем текущее представление

            /*Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        if(liveSearch.getText().length() == 0) { //если ничего не ввели в поисковую строку
                            getVarStore().getCurrentDir().setNewContent(path, FileUtils.getAndSortObjectsList(path, "", false));
                            Log.w("test", "after getting data");
                        } else {
                            getVarStore().getCurrentDir().setNewContent(path, FileUtils.getAndSortObjectsList(path, liveSearch.getText().toString(), false));
                        }

                        //выводим информацию по использованию памяти в текущем разделе
                        MainUI.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showMemoryUsage(path);
                            }
                        });

                    } catch (Exception e) {
                        Log.e("updFileList.setNewDir", null, e);
                    }
                    MainUI.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            selector.setChecked(false);//при обновлении списка убираем флажок
                            filesList.setSelection(0); //автоскролинг на перввый элемент
                            getVarStore().getFLA().notifyDataSetChanged(); //обновляем текущее представление
                            setProgressBarIndeterminateVisibility(false); //скрываем прогресс
                        }
                    });
                }
            };
            thread.start();*/

            //реализуем обновление списка через callback
            if(liveSearch.getText().length() == 0) { //если ничего не ввели в поисковую строку
                FileUtils.getAndSortObjectsList(path, "", false, MainUI.this);
            } else {
                if(!disableFastSearchMode) {
                    FileUtils.getAndSortObjectsList(path, liveSearch.getText().toString(), false, MainUI.this);
                } else {
                    FileUtils.getAndSortObjectsList(path, "", false, MainUI.this);
                }
            }

        } catch (Exception e) {
            Log.e("updateFileList", null, e);
        }
    }

    //обновление списка файлов текущей директории
    public void refreshCurrentDir() {
        updateFileList(getVarStore().getCurrentDir().getPath());
    }

    @Override
    public void onListUpdateFinished() {
        MainUI.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showMemoryUsage(getVarStore().getCurrentDir().getPath());
                show_hide_objCountInCurrentDir_panel();
                selector.setChecked(false);//при обновлении списка убираем флажок
                filesList.setSelection(0); //автоскролинг на перввый элемент
                getVarStore().getFLA().notifyDataSetChanged(); //обновляем текущее представление
                setProgressBarIndeterminateVisibility(false); //скрываем прогресс
            }
        });

    }

    private void show_hide_memStatInCurrPartition_panel() {
        try {
            if(SettingsUtils.getBooleanSettings(this, Constants.GENERAL_SETTING_SHOW_MEM_STAT_IN_CURR_PART_KEY)) {
                memStatInCurrPartPanel.setVisibility(View.VISIBLE);
            } else {
                memStatInCurrPartPanel.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            Log.e("show_hide_memStatInCurrPartition_panel", null, e);
        }
    }

    private void show_hide_objCountInCurrentDir_panel() {
        try {
            if(SettingsUtils.getBooleanSettings(this, Constants.GENERAL_SETTING_SHOW_OBJECTS_COUNT_IN_CURRENT_DIR_KEY)) {
                TextView tv = (TextView) objectsCountInCurrPartPanel.findViewById(R.id.objects_count);
                tv.setText(
                        getString(R.string.objects_count_dirs) + " " + String.valueOf(FileUtils.getObjectsCountInDir(getVarStore().getCurrentDir(), true)) + ", " +
                                getString(R.string.objects_count_files) + " " + String.valueOf(FileUtils.getObjectsCountInDir(getVarStore().getCurrentDir(), false))
                );
                objectsCountInCurrPartPanel.setVisibility(View.VISIBLE);
            } else {
                objectsCountInCurrPartPanel.setVisibility(View.GONE);
            }


        } catch (Exception e) {
            Log.e("show_hide_memStatInCurrPartition_panel", null, e);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

}

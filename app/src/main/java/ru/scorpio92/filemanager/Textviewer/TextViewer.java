package ru.scorpio92.filemanager.Textviewer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import ru.scorpio92.filemanager.R;
import ru.scorpio92.filemanager.Main.Utils.FileUtils;
import ru.scorpio92.filemanager.Main.Utils.SettingsUtils;
import ru.scorpio92.filemanager.Main.Variables.Constants;
import ru.scorpio92.filemanager.Main.Variables.VarStore;
import ru.scorpio92.io.MainOperationsTools;

/**
 * Created by scorpio92 on 10.05.16.
 */
public class TextViewer extends Activity {

    private String file;
    private TextViewerAdapter adapter;
    private Boolean stopThread;
    private ArrayList<String> data;
    private ArrayList<String> buffer;


    public TextViewer() {
        stopThread = false;
        buffer = new ArrayList<String>();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //прогресс бар в ActionBar
        //requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.textviewer);

        //чтобы не терялся фокус при редактировании
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        init();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        finishWork();
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //показываем меню только если открываем файл на редактирование
        if(getIntent().getExtras().getBoolean("editable")) {
            getMenuInflater().inflate(R.menu.texteditor_menu, menu);
        }
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.texteditor_menu_save) {
            if(FileUtils.saveText(getTextForSave(), file)) {
                //adapter.notifyDataSetChanged(); //обновляем. для случая когда добавляем новую строку, чтобы она отобразилась корректно
                Toast.makeText(getApplicationContext(), getString(R.string.texteditor_save_ok), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.texteditor_save_fail), Toast.LENGTH_LONG).show();
            }
            //Toast.makeText(getApplicationContext(), getTextForSave(), Toast.LENGTH_LONG).show();
            return true;
        }

        if (id == R.id.texteditor_menu_undo) {
            if(undo()) {
                Toast.makeText(getApplicationContext(), getString(R.string.texteditor_undo_ok), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.texteditor_undo_fail), Toast.LENGTH_LONG).show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    //инициализация TextViewer/TextEditor
    private void init() {
        try {
            //файл
            file = getIntent().getExtras().getString(Constants.TEXTVIEWER_FILE).toString();

            //заголовок активности - имя файла
            setTitle(new MainOperationsTools().getLastPathComponent(file));

            //получаем размер шрифта из настроек
            float fontSize = (float) Integer.parseInt(SettingsUtils.getStringSettings(this, Constants.TEXTVIEWER_FONT_SIZE_KEY));

            //получаем массив строк
            data = getIntent().getExtras().getStringArrayList(Constants.TEXTVIEWER_DATA);

            //бэкап данных делаем если открываем файл на редактирование
            if(getIntent().getExtras().getBoolean(Constants.TEXTVIEWER_EDITABLE)) {
                backup();
            }

            //Получаем максимальную строку (по длине) чтобы вычислить ширину ListView
            String max = getMaxLine();
            Log.w("max lenght", Integer.toString(max.length()));

            ViewGroup.LayoutParams params;

            ListView field;
            if(max.length() == 0) { //если файл пустой мы не сможем вычислить необх шиину по длине строки
                field = (ListView) findViewById(R.id.textviewerListLineBreak);
                params = field.getLayoutParams();
                params.width = getDisplayWidth();
            } else {
                //определяем нужный listview в зависимости от того включена ли функция переноса строки
                if (SettingsUtils.getBooleanSettings(this, Constants.TEXTVIEWER_LINE_BREAK_KEY)) {
                    field = (ListView) findViewById(R.id.textviewerListLineBreak);
                    params = field.getLayoutParams();
                    params.width = getDisplayWidth();
                    //params.width = params.WRAP_CONTENT;
                } else {
                    field = (ListView) findViewById(R.id.textviewerList);
                    params = field.getLayoutParams();
                    params.width = getListWidth(max, Math.round(fontSize));
                }
            }

            field.setLayoutParams(params);

            adapter = new TextViewerAdapter(this, data, fontSize, getIntent().getExtras().getBoolean(Constants.TEXTVIEWER_EDITABLE), SettingsUtils.getBooleanSettings(this, Constants.TEXTVIEWER_LINE_NUMS_KEY), SettingsUtils.getBooleanSettings(this, Constants.TEXTVIEWER_LINE_BREAK_KEY));
            field.setAdapter(adapter);
            adapter.notifyDataSetChanged();

        } catch (Exception e) {
            Log.e("text read", null, e);
        }
    }

    private String getMaxLine() {
        String max="";
        try {
            for (int i = 0; i < data.size(); i++) {
                if (data.get(i).length() > max.length()) {
                    max = data.get(i) + i + ". "; //прибавляем текущий номер строки и точку (если включено отображение номера строк)
                }
            }
            Log.w("data max", max);
        } catch (Exception e) {
            Log.e("getMaxLine", null, e);
        }
        return max;
    }

    private int getListWidth(String s, int fontSize) {
        try {
            Paint mPaint = new Paint();
            float densityMultiplier = this.getResources().getDisplayMetrics().density;
            //Log.w("densityMultiplier", Float.toString(densityMultiplier));
            mPaint.setTextSize(fontSize * densityMultiplier);
            int width = (int)mPaint.measureText(s, 0, s.length());
            Log.w("getListWidth", Integer.toString(width));
            return width;
        } catch (Exception e) {
            Log.e("getListWidth", null, e);
        }
        return 0;
    }

    private int getDisplayWidth() {
        try {
            WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);
            Log.w("getDisplayWidth", Integer.toString(metrics.widthPixels));
            return metrics.widthPixels;
        } catch (Exception e) {
            Log.e("getDisplayWidth", null, e);
        }
        return 0;
    }

    //резервное копирование исходного файла и копирование текущего массива данных в буфер
    private void backup() {
        try {
            if(SettingsUtils.getBooleanSettings(this, Constants.TEXTEDITOR_FILES_BACKUP_KEY)) {
                //ложим копию рядом с оригинальным файлом
                if(!((VarStore) VarStore.getAppContext()).getMainOperationsTools().copyIO(file, file + Constants.TEXTEDITOR_BACKUP_FILE_EXTENSION)) {
                    //в случае неудачи - пробуем из под суперпользователя
                    if(!((VarStore) VarStore.getAppContext()).getMainOperationsTools().copySU(file, file + Constants.TEXTEDITOR_BACKUP_FILE_EXTENSION, true)) {
                        Toast.makeText(getApplicationContext(), getString(R.string.texteditor_backup_create_fail), Toast.LENGTH_LONG).show();
                        //finishWork();
                    }
                }
            }
            buffer.addAll(data);
        } catch (Exception e) {
            Log.e("backup", null, e);
        }

    }

    private String getTextForSave() {
        String s="";
        try {
            for (int i=0; i<data.size(); i++) {
                if (!stopThread) {
                    if((i+1)==data.size()) {
                        s += data.get(i);
                    } else {
                        s += data.get(i) + "\n";
                    }
                }
            }
        } catch (Exception e) {
            Log.e("getTextForSave", null, e);
        }
        return s;
    }

    private Boolean undo() {
        try {
            data.clear();
            data.addAll(buffer);
            adapter.notifyDataSetChanged();
            return true;
        } catch (Exception e) {
            Log.e("save", null, e);
        }
        return false;
    }

    private void finishWork() {
        try {
            stopThread = true;
        } catch (Exception e) {
            Log.e("finishWork", null, e);
        }
        finish();
    }
}

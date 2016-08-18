package ru.scorpio92.filemanager.Terminal;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ListView;

import java.util.ArrayList;

import ru.scorpio92.filemanager.R;
import ru.scorpio92.filemanager.Main.Types.MainDB;
import ru.scorpio92.filemanager.Main.Utils.DBUtils;
import ru.scorpio92.filemanager.Main.Utils.SettingsUtils;
import ru.scorpio92.filemanager.Main.Variables.Constants;

public class Terminal extends Activity {

    private ListView field;
    private TerminalAdapter adapter;
    private ArrayList<String> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal);

        //чтобы не терялся фокус при редактировании
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        init();
    }

    void init() {
        try {
            //получаем размер шрифта из настроек
            float fontSize = (float) Integer.parseInt(SettingsUtils.getStringSettings(this, Constants.TERMINAL_FONT_SIZE_KEY));

            //перенос строк
            Boolean lineBreak = SettingsUtils.getBooleanSettings(this, Constants.TERMINAL_LINE_BREAK_KEY);
            //определяем нужный listview в зависимости от того включена ли функция переноса строки
            if(lineBreak) {
                field = (ListView) findViewById(R.id.terminalFieldLineBreak);
            } else {
                field = (ListView) findViewById(R.id.terminalField);
            }

            data = new ArrayList<String>();
            //добавляем текущий путь
            data.add("Current path: " + getIntent().getExtras().getString("path").toString());

            //показываем статус получения рут прав приложением
            ArrayList<String> als = new ArrayList<String>();
            als.add(MainDB.GENERAL_TABLE_SETTING_VALUE_COLUMN);
            ArrayList<String> result = DBUtils.select_from_db(MainDB.MAIN_DATABASE_NAME, "SELECT * " +
                    "FROM " + MainDB.GENERAL_TABLE +
                    " WHERE " + MainDB.GENERAL_TABLE_SETTING_NAME_COLUMN + "=" + "'" + Constants.ROOT_CHECK_RESULT_ROW + "'", als, false);
            if (result.get(0).equals("1")) {
                data.add("App root status: granted");
            } else  {
                data.add("App root status: not granted");
            }

            //добавляем строку для ввода команды
            data.add("");

            adapter = new TerminalAdapter(field, Terminal.this, data, getIntent().getExtras().getString("path").toString(), fontSize, SettingsUtils.getBooleanSettings(this, Constants.TERMINAL_LINE_NUMS_KEY), lineBreak);
            field.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e("start", null, e);
        }
    }
}

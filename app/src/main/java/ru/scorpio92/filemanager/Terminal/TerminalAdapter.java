package ru.scorpio92.filemanager.Terminal;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.ClipboardManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import ru.scorpio92.filemanager.R;
import ru.scorpio92.filemanager.Main.Variables.Constants;
import ru.scorpio92.filemanager.Main.Variables.VarStore;

/**
 * Created by scorpio92 on 10.05.16.
 */
public class TerminalAdapter extends BaseAdapter {

    private ListView field; //для динамического расширения лаяута

    private Context context;
    private ArrayList<String> data;
    private String currentPath;
    private float fontSize;
    private Boolean showLineNums;
    private Boolean lineBreak;
    private int maxLineLenght=0; //длина самой длиной строки. нужна для определения ширины ListView
    private int maxLinePosition=0; //позиция самой длиной строки в массиве data

    private String udi; //user@device info
    private String su_udi; //su@device info
    //private String full_udi; //udi + path. Example: su@LG-D801:/system>
    private ArrayList<CommandLine> commandLines; //массив номеров строк команд

    private Boolean isRootMode = false; //режим: рут или обычный

    class ViewHolder {
        TextView num_line;
        TextView type_line;
        EditText text_line;
    }

    //объект - тип командной строки
    class CommandLine {
        int position;
        Boolean type; //рутовая строка или нет

        public CommandLine(int position, Boolean type) {
            this.position=position;
            this.type=type;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof CommandLine) {
                CommandLine cm = (CommandLine) o;
                return cm.position == position && cm.type == type;
            }
            return false;
        }
    }

    public TerminalAdapter(ListView field, Context context, ArrayList<String> data, String path, float fontSize, Boolean showLineNums, Boolean lineBreak) {
        this.field = field;

        this.context = context;
        this.data = data;
        currentPath = path;
        this.fontSize = fontSize;
        this.showLineNums = showLineNums;
        this.lineBreak = lineBreak;
        udi = Integer.toString(context.getApplicationInfo().uid) + "@" + android.os.Build.MODEL + ">";
        su_udi = "su" + "@" + android.os.Build.MODEL + ">";
        //full_udi = udi + ":" + path + ">";
        //full_udi = udi + ">"; //пока без пути
        commandLines = new ArrayList<CommandLine>();
    }


    public int getCount() {
        // TODO Auto-generated method stub
        return data.size();
    }

    public Object getItem(int position) {
        // TODO Auto-generated method stub
        //Log.w("test", "getItem at position: " + Integer.toString(position));
        return position;
    }

    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    View initItemView() {
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return mInflater.inflate(R.layout.terminal_field_row, null);
    }

    ViewHolder initNewViewHolder(View itemView) {
        ViewHolder holder = new ViewHolder();
        holder.num_line = (TextView) itemView.findViewById(R.id.terminalFieldRowLineNum);
        holder.type_line = (TextView) itemView.findViewById(R.id.terminalFieldRowLineType);
        holder.text_line = (EditText) itemView.findViewById(R.id.terminalFieldRow);
        return holder;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub

        final ViewHolder holder;

        if(position!=data.size()-1) {
            if (convertView == null) {
                //Log.w("test", "convertView == null" + " position: " + Integer.toString(position));
                convertView=initItemView();
                holder=initNewViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                //Log.w("test", "convertView != null" + " position: " + Integer.toString(position));
                holder = (ViewHolder) convertView.getTag();
                convertView.setTag(initNewViewHolder(convertView));
            }
        } else { //если рисуем последний row - инициализируем все заново чтобы очистить данные
            convertView=initItemView();
            holder=initNewViewHolder(convertView);
            convertView.setTag(holder);
        }

        //номер строки
        if(showLineNums) {
            holder.num_line.setText(Integer.toString(position +1) + ". ");
            holder.num_line.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
            holder.num_line.setVisibility(View.VISIBLE);
        } else {
            holder.num_line.setVisibility(View.GONE);
        }
        //тескт строки (команда/вывод команды)
        holder.text_line.setText(data.get(position));
        holder.text_line.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);

        if(lineBreak) {
            //holder.text_line.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            //holder.text_line.setSingleLine(true);
            //holder.text_line.setImeOptions(EditorInfo.IME_ACTION_DONE);
            holder.text_line.setMaxLines(4);
            holder.text_line.setHorizontallyScrolling(false);
        }

        if(position!=data.size()-1) {//если это НЕ последний элемент в списке - делаем его НЕ доступным на редактирование (ввод комманд)
            holder.text_line.setFocusableInTouchMode(false);
            holder.text_line.setFocusable(false);
            holder.text_line.setCursorVisible(false);
            holder.text_line.setKeyListener(null);
            holder.text_line.setBackgroundColor(Color.TRANSPARENT);
            //Log.w("test", "data.size: " + Integer.toString(data.size()) + " position: " + Integer.toString(position));

            //если текущая строка - строка с командой, отображаем uid текущего приложения@device_name:current_path>
            //сначала скрываем тип строки. потом проверяем условия совпадения
            holder.type_line.setVisibility(View.GONE);
            if(commandLines.contains(new CommandLine(position, false))) { //текущая позиция + не рут
                holder.type_line.setText(udi);
                holder.type_line.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
                holder.type_line.setVisibility(View.VISIBLE);
            }
            if(commandLines.contains(new CommandLine(position, true))) { //текущая позиция + рут
                holder.type_line.setText(su_udi);
                holder.type_line.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
                holder.type_line.setVisibility(View.VISIBLE);
            }

            //контекстное меню
            setMenuListener(holder);

            //holder.text_line.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            //holder.text_line.setSingleLine(true);
            //holder.text_line.getLayoutParams().height=60;
            //holder.text_line.setImeOptions(EditorInfo.IME_ACTION_DONE);-

        } else { //последняя строчка (строка ввода команды)
            //Log.w("test2", "data.size: " + Integer.toString(data.size()) + " position: " + Integer.toString(position));;
            //устанавливаем знак ввода команды
            holder.type_line.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
            //uid текущего приложения@device_name:current_path>
            if(isRootMode) {
                holder.type_line.setText(su_udi);
            } else {
                holder.type_line.setText(udi);
            }
            holder.type_line.setVisibility(View.VISIBLE);

            setInputCommandListener(holder, position);
        }
        return convertView;
    }

    //слушатель ввода команд
    void setInputCommandListener(final ViewHolder holder, final int position) {
        //если строка для ввода команды - вешаем слушатель
        holder.text_line.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    //Log.w("OnKeyLis", "Enter");
                    if (!holder.text_line.getText().toString().equals("")) {
                        //Log.w("test", "num_line " + Integer.toString(holder.num_line.getWidth()) + " type_line " + Integer.toString(holder.type_line.getWidth()));
                        //Log.w("test", "text_line " + Integer.toString(holder.text_line.getWidth()));

                        //записываем номер строки в массив строк команд для того чтобы отображать тип строки в дальнейшем
                        //Log.w("commandLines.add", Integer.toString(position));
                        commandLines.add(new CommandLine(position, isRootMode));

                        //перезаписываем последний элемент массива - введенную команду
                        String command = holder.text_line.getText().toString();
                        data.set(data.size() - 1, command);
                        Log.w("OnKeyLis", "command: " + command);

                        //запускаем команду
                        commandMain(command);

                        //добавляем новую строку
                        data.add("");

                        //определяем ширину ListView
                        setListViewWidth(holder);

                        //автоскролинг на последнюю строку
                        field.setSelection(data.size() - 1);
                    }
                }
                return false;
            }
        });
    }

    //контекстные команды
    void setMenuListener(final ViewHolder holder) {
        holder.text_line.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //диалог выбора действий
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View dialoglayout = inflater.inflate(R.layout.empty_list, null);
                alertDialog.setView(dialoglayout);
                ListView list = (ListView) dialoglayout.findViewById(R.id.emptyListView);
                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1,
                        new String[]{context.getString(R.string.terminal_line_menu_copy), context.getString(R.string.terminal_line_menu_rerun)});
                list.setAdapter(adapter);

                final AlertDialog dialog = alertDialog.create();
                dialog.show();

                //обработчик нажатий на список действий
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        try {
                            String item = adapterView.getItemAtPosition(i).toString();
                            //копирование строки
                            if (item.equals(context.getString(R.string.terminal_line_menu_copy))) {
                                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                clipboard.setText(holder.text_line.getText().toString());
                            }
                            //выполнение команды повторно
                            if (item.equals(context.getString(R.string.terminal_line_menu_rerun))) {
                                commandLines.add(new CommandLine(data.size()-1, isRootMode));

                                //перезаписываем последний элемент массива - введенную команду
                                String command = holder.text_line.getText().toString();
                                data.set(data.size() - 1, command);
                                Log.w("rerun", "command: " + command);

                                //запускаем команду
                                commandMain(command);

                                //добавляем новую строку
                                data.add("");

                                //определяем ширину ListView
                                //setListViewWidth(holder);

                                //автоскролинг на последнюю строку
                                field.setSelection(data.size() - 1);
                            }
                        } catch (Exception e) {
                            Log.e("onItemClick", null, e);
                        } finally {
                            dialog.dismiss();
                        }
                    }
                });
            }
        });
    }

    //запуск команд
    ArrayList<String> runCommand(String command) {
        ArrayList<String> result = new ArrayList<String>();
        if(isRootMode) {
            result = ((VarStore) VarStore.getAppContext()).getMainOperationsTools().runProcessFromSU(command, true);
        } else {
            result = ((VarStore) VarStore.getAppContext()).getMainOperationsTools().runProcess(new String[]{Constants.SH_PATH, "-c", command});
            //result = FileUtils.runProcess(new String[]{"/system/bin/sh", "-c", command});
        }
        return result;
    }

    //парсинг команд
    void commandMain(String command) {
        if(command.trim().equals("su")) {
            isRootMode = true;
            data.add("enter to root mode");
            return;
        }
        if(command.trim().equals("exit")) {
            if(isRootMode) {
                isRootMode = false;
                data.add("exit from root mode");
            } else {
                ((Terminal) context).finish();
            }
            return;
        }
        if(command.contains("cd ")) {
            String path = command.split("cd ".replaceAll(" ", "\\\\ "))[1].trim(); //определяем путь записанный после cd
            //Log.w("cd", "path: " + path);
            if(path.equals("..")) {
                currentPath = new File(currentPath).getParent();
                data.add("Current path: " + currentPath);
                maxLineLenght = getMaxLineWidth(getMaxLine(data), Math.round(fontSize));
                return;
            }
            //Log.w("cd", "1 char of path: " + path.substring(0,1));
            if(path.substring(0,1).equals("/")) {
                ArrayList<String> result = runCommand("cd " + path + " | echo $?");
                if(result.size()==1) {
                    if(result.get(0).equals("0")) {
                        currentPath = path;
                        data.add("Current path: " + currentPath);
                        maxLineLenght = getMaxLineWidth(getMaxLine(data), Math.round(fontSize));
                        return;
                    }
                }
            } else {
                ArrayList<String> result = runCommand("cd " + currentPath + "/" + path + " | echo $?");
                if(result.size()==1) {
                    if(result.get(0).equals("0")) {
                        currentPath = currentPath + "/" + path;
                        data.add("Current path: " + currentPath);
                        maxLineLenght = getMaxLineWidth(getMaxLine(data), Math.round(fontSize));
                        return;
                    }
                }
            }
            data.add("bad cd command syntax");
            return;
        }
        //все остальные команды
        ArrayList<String> result = runCommand("cd " + currentPath + "; " + command);
        if (!result.isEmpty()) {
            //когда получили результат - сразу проверяем массив на наличие самой длиной строки
            maxLineLenght = getMaxLineWidth(getMaxLine(result), Math.round(fontSize));
            //добавляем все результаты в общий массив строк
            data.addAll(result);
        } else {
            data.add("command return empty result!");
        }

    }

    void setListViewWidth(final ViewHolder holder) {
        ViewGroup.LayoutParams params = field.getLayoutParams();
        if(lineBreak) {
            params.width = getDisplayWidth(); //если перенос строк включен - ширина ListView равна вычисленной ширине экрана
        } else { //иначе ширина ListView равна как минимум ширине экрана
                            /*if(Math.max(getDisplayWidth(), getListWidth(getMaxLine(), Math.round(fontSize)) + holder.num_line.getWidth() + holder.type_line.getWidth()) == getDisplayWidth()) {
                                params.width = getDisplayWidth();
                            } else {
                                params.width = getListWidth(getMaxLine(), Math.round(fontSize)) + holder.num_line.getWidth() + holder.type_line.getWidth();
                            }*/
            //int sum_width = holder.text_line.getWidth() + holder.num_line.getWidth() + holder.type_line.getWidth();
            int sum_width = maxLineLenght + holder.num_line.getWidth() + holder.type_line.getWidth();
            /*if(Math.max(getDisplayWidth(), Math.max(sum_width, maxLineLenght)) == getDisplayWidth()) {
                params.width = getDisplayWidth();
            } else {
                if(sum_width > maxLineLenght) {
                    params.width = holder.text_line.getWidth() + holder.num_line.getWidth() + holder.type_line.getWidth();
                    maxLineLenght = params.width;
                } else {
                    params.width = maxLineLenght;
                }
            }*/
            if(getDisplayWidth() > sum_width) {
                params.width = getDisplayWidth();
            } else {
                params.width = sum_width;
            }
        }
        notifyDataSetChanged();
    }

    private String getMaxLine(ArrayList<String> lines) {
        String max="";
        int position=0;
        try {
            //выбираем самую длинную строку в текущем массиве результата команды
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).length() > max.length()) {
                    max = lines.get(i); //прибавляем текущий номер строки и точку (если включено отображение номера строк)
                    position = i;
                }
            }
            //если вычисленная строка короче предыдущей, вовзращается предыдущая строка
            if(max.length() < data.get(maxLinePosition).length()) {
                return data.get(maxLinePosition);
            }
            maxLinePosition = data.size() + position; //обновляем позицию самой длиной строки в массиве data
            Log.w("data max", max + "pos: " + Integer.toString(maxLinePosition));
        } catch (Exception e) {
            Log.e("getMaxLine", null, e);
        }
        return max;
    }

    private int getMaxLineWidth(String s, int fontSize) {
        try {
            Paint mPaint = new Paint();
            float densityMultiplier = context.getResources().getDisplayMetrics().density;
            //Log.w("densityMultiplier", Float.toString(densityMultiplier));
            mPaint.setTextSize(fontSize * densityMultiplier);
            int width = (int)mPaint.measureText(s, 0, s.length());
            Log.w("getListWidth", Integer.toString(width));

            //если текущая ширина меньше старой, оставляем старую
            if(width < maxLineLenght) {
                return maxLineLenght;
            }
            return width;
        } catch (Exception e) {
            Log.e("getListWidth", null, e);
        }
        return 0;
    }

    private int getDisplayWidth() {
        try {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
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

}
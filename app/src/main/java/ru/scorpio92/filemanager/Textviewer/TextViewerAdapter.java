package ru.scorpio92.filemanager.Textviewer;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import ru.scorpio92.filemanager.R;

/**
 * Created by scorpio92 on 10.05.16.
 */
public class TextViewerAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> data;
    private float fontSize;
    private Boolean isEditable;
    private Boolean showLineNums;
    private Boolean lineBreak;

    private int selectedNum=-1; //номер строки на которой фокус

    public TextViewerAdapter(Context context, ArrayList<String> data, float fontSize, Boolean isEditable, Boolean showLineNums, Boolean lineBreak) {
        this.context = context;
        this.data = data;
        this.fontSize = fontSize;
        this.isEditable = isEditable;
        this.showLineNums = showLineNums;
        this.lineBreak = lineBreak;
    }


    public int getCount() {
        // TODO Auto-generated method stub
        return data.size();
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

        final ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.textviewer_row, null);
            holder.text_line = (EditText) convertView.findViewById(R.id.textviewer_et);
            holder.num_line =  (TextView) convertView.findViewById(R.id.textviewer_row_num);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            holder.id = position; //когда загружаем уже инициализированный холдер - обновляем у него порядковый номер
        }

        //номера строк
        if(showLineNums) {
            holder.num_line.setVisibility(View.VISIBLE);
            holder.num_line.setText(Integer.toString(position) + ".");
            holder.num_line.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        }

        //если режим просмотра - запрещаем редактирование
        if(!isEditable) {
            holder.text_line.setFocusableInTouchMode(false);
            holder.text_line.setFocusable(false);
            holder.text_line.setCursorVisible(false);
            holder.text_line.setKeyListener(null);
            holder.text_line.setBackgroundColor(Color.TRANSPARENT);
        }

        //устанавливаем параметры для текстовых строк
        if(lineBreak) {
            holder.text_line.setMaxLines(Integer.MAX_VALUE);
            holder.text_line.setHorizontallyScrolling(false);
        }
        //holder.text_line.setSingleLine(lineBreak);
        holder.text_line.setText(data.get(position));
        holder.text_line.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        holder.text_line.clearFocus();
        holder.id = position;

        //если режим редактирования - вешаем слушатели на номер строки и саму строку
        if(isEditable) {
            initListeners(holder);
        }

        return convertView;
    }

    void initListeners(final ViewHolder holder) {

        //вешаем слушатель на номер строки. по нажатию будет вызываться контекстное меню
        holder.num_line.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //диалог выбора действий
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View dialoglayout = inflater.inflate(R.layout.empty_list, null);
                alertDialog.setView(dialoglayout);
                ListView list = (ListView) dialoglayout.findViewById(R.id.emptyListView);
                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1,
                        new String[] {context.getString(R.string.texteditor_line_num_menu_delete), context.getString(R.string.texteditor_line_num_menu_copy)});
                list.setAdapter(adapter);

                final AlertDialog dialog = alertDialog.create();
                dialog.show();

                //обработчик нажатий на список действий
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        try {
                            String item = adapterView.getItemAtPosition(i).toString();
                            //удаление строки
                            if (item.equals(context.getString(R.string.texteditor_line_num_menu_delete))) {
                                Log.w("holder.num_line.setOnClickListener " + holder.id, "delete row, refresh content");
                                data.remove(holder.id);
                                notifyDataSetChanged();
                            }
                            //копирование строки
                            if (item.equals(context.getString(R.string.texteditor_line_num_menu_copy))) {
                                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                clipboard.setText(holder.text_line.getText().toString());
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

        //устанавливаем слушатель нажатий(фокуса) на каждую иницилизированную строку с EditText
        holder.text_line.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                try {
                    if (hasFocus) {
                        selectedNum = holder.id; //записываем номер выделенной строчки
                        Log.w("onFocusChange", "row " + Integer.toString(selectedNum) + " get focus");
                        Log.w("onFocusChange", "get focus text: " + holder.text_line.getText().toString());

                        //вешаем слушатель изменения текста на данное поле
                        initTextWatcher(holder, false);

                    } else {
                        Log.w("onFocusChange", "row " + Integer.toString(selectedNum) + " lost focus");
                        Log.w("onFocusChange", "lost focus text: " + data.get(selectedNum));

                        //вешаем пустой слушатель текста (отвзяываем)
                        initTextWatcher(holder, true);

                        //скрываем клавиатуру
                        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(holder.text_line.getWindowToken(), 0);
                    }
                } catch (Exception e) {
                    Log.e("setOnFocusChangeListener", null, e);
                }
            }
        });
    }

    void initTextWatcher(final ViewHolder holder, final Boolean setEmptyListener) {

        holder.text_line.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                // TODO Auto-generated method stub
                //Log.w("onTextChanged", "!!!");
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                // TODO Auto-generated method stub
                //Log.w("beforeTextChanged", "!!!");
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    // TODO Auto-generated method stub
                    if (!setEmptyListener) {
                        if (selectedNum == holder.id) {
                            Log.w("afterTextChanged " + holder.id, s.toString());
                            //если удалили последний символ - удаляем данную строку и обновляем список
                            if ((s.toString().equals("") || s.toString().length() == 0) && data.size() > 1) {
                                Log.w("afterTextChanged " + holder.id, "delete row, refresh content");
                                data.remove(selectedNum);
                                notifyDataSetChanged();
                            } else { //иначе просто обновляем ее в массиве
                                data.set(selectedNum, s.toString());
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e("afterTextChanged", null, e);
                }
            }
        });
    }

    class ViewHolder {
        TextView num_line;
        EditText text_line;
        int id;
    }

}
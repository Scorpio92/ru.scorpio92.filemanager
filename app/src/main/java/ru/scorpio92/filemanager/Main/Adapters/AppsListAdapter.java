package ru.scorpio92.filemanager.Main.Adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import ru.scorpio92.filemanager.R;

/**
 * Created by scorpio92 on 03.09.16.
 */
public class AppsListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> appsNames;
    private ArrayList<Drawable> appsIcons;

    public AppsListAdapter(Context context, ArrayList<String> appsNames, ArrayList<Drawable> appsIcons) {
        this.context=context;
        this.appsNames=appsNames;
        this.appsIcons=appsIcons;
    }

    @Override
    public int getCount() {
        return appsNames.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        if (view == null) {
            view = inflater.inflate(R.layout.all_apps_element, null);
        }

        ImageView imageView = (ImageView) view.findViewById(R.id.appIcon);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageDrawable(appsIcons.get(i));

        TextView file = (TextView) view.findViewById(R.id.appName);
        file.setText(appsNames.get(i));

        return view;
    }
}

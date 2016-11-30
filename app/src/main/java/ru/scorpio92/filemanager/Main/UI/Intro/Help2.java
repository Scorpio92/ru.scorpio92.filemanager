package ru.scorpio92.filemanager.Main.UI.Intro;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import ru.scorpio92.view.ImageViewScrLoader;
import ru.scorpio92.filemanager.R;

/**
 * Created by scorpio92 on 13.08.16.
 */
public class Help2 extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.w("Help2", "onCreateView");
        View v = inflater.inflate(R.layout.help2, container, false);
        new ImageViewScrLoader(getActivity(), (ImageView) v.findViewById(R.id.intro2_img), R.drawable.intro_2).loadScr();
        return v;
    }


}


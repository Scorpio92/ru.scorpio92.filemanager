package ru.scorpio92.filemanager.Main.UI.Intro;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import ru.scorpio92.filemanager.R;
import ru.scorpio92.view.ImageViewScrLoader;

/**
 * Created by scorpio92 on 13.08.16.
 */
public class Help3 extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.help3, container, false);
        new ImageViewScrLoader(getActivity(), (ImageView) v.findViewById(R.id.intro3_img), R.drawable.intro_3).loadScr();
        return v;
    }
}


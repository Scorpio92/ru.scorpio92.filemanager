package ru.scorpio92.filemanager.Main.UI.Intro;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import ru.scorpio92.filemanager.R;

/**
 * Created by scorpio92 on 13.08.16.
 */
public class Help9 extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.help9, container, false);
        Button gotto_app = (Button) v.findViewById(R.id.goto_app);
        gotto_app.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });

        return v;
    }
}


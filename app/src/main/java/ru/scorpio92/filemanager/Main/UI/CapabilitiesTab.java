package ru.scorpio92.filemanager.Main.UI;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.scorpio92.filemanager.R;

/**
 * Created by scorpio92 on 17.04.16.
 */
public class CapabilitiesTab extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.capabilities_tab, container, false);
    }
}

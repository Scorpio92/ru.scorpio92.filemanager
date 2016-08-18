package ru.scorpio92.filemanager.Main.UI;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.scorpio92.filemanager.BuildConfig;
import ru.scorpio92.filemanager.R;
import ru.scorpio92.filemanager.Main.Variables.VarStore;

/**
 * Created by scorpio92 on 17.04.16.
 */
public class AboutTab extends Fragment {
    private View ac;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ac = inflater.inflate(R.layout.about_tab, container, false);

        //app version
        TextView ver = (TextView) ac.findViewById(R.id.app_version);
        ver.setTypeface(Typeface.DEFAULT_BOLD);
        ver.setText(VarStore.getAppContext().getString(R.string.about_version_tittle) + ": " + BuildConfig.VERSION_NAME);

        return ac;
    }
}

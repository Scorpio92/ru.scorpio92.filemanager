package ru.scorpio92.filemanager.Main.UI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import ru.scorpio92.filemanager.R;
import ru.scorpio92.filemanager.Main.Variables.Constants;

/**
 * Created by scorpio92 on 08.03.16.
 */
public class Settings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    //private Boolean view_settings_was_changed=false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        setTheme(R.style.settings_theme);

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if(s.equals(Constants.SORT_SETTING_KEY) || s.equals(Constants.VIEW_SHOW_CHANGE_TIME_KEY) || s.equals(Constants.VIEW_SHOW_DIR_SIZE_KEY) || s.equals(Constants.VIEW_SHOW_FILE_SIZE_KEY)) {
            //view_settings_was_changed = true;
            setResult();
        }
    }

    private void setResult() {
        Bundle conData = new Bundle();
        conData.putBoolean(Constants.NEED_REFRESH_AFTER_SETTINGS_CHANGE, true);
        Intent intent = new Intent();
        intent.putExtras(conData);
        setResult(RESULT_OK, intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

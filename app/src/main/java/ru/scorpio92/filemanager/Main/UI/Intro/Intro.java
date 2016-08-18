package ru.scorpio92.filemanager.Main.UI.Intro;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import ru.scorpio92.filemanager.BuildConfig;
import ru.scorpio92.filemanager.Main.UI.MainUI;
import ru.scorpio92.filemanager.Main.Utils.SecondUsageUtils;
import ru.scorpio92.filemanager.Main.Variables.Constants;
import ru.scorpio92.filemanager.Main.Variables.VarStore;
import ru.scorpio92.filemanager.R;
import ru.scorpio92.io.MainOperationsTools;

/**
 * Created by scorpio92 on 05.08.16.
 */
public class Intro extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        try {
            getActionBar().hide();
        } catch (Exception e) {
            e.printStackTrace();
        }
        setContentView(R.layout.intro_start);

        Button header = (Button) findViewById(R.id.header);
        header.setText(getPackageName() + " " + BuildConfig.VERSION_NAME);

        final Intent intent = new Intent(this, Help.class);
        Button go = (Button) findViewById(R.id.go);
        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //finish();
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //setResult(RESULT_OK, null);
        //finish();
        if(SecondUsageUtils.checkFirstRun()) {
            startActivityForResult(new Intent(this, MainUI.class), Constants.MAINUI_ACTIVITY_EXIT_CODE);
        }
        SecondUsageUtils.setRepeatedStart();
        finish();
        return super.onKeyDown(keyCode, event);
    }
}

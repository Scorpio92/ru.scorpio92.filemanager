package ru.scorpio92.filemanager.Main.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import ru.scorpio92.filemanager.Main.UI.ArchPacked;
import ru.scorpio92.filemanager.Main.UI.ArchUnpacked;
import ru.scorpio92.filemanager.Main.UI.Decrypted;
import ru.scorpio92.filemanager.Main.UI.Encrypted;

/**
 * Created by scorpio92 on 21.07.16.
 */
public class WorkFilesTabPagerAdapter extends FragmentStatePagerAdapter {

    //режимы отображения (просмотр архивов или шифрованных файлов)
    public static final int ARCH_MODE=0;
    public static final int CRYPTO_MODE=1;
    //текущий режим. по умолчанию - просмотр архивов
    private int mode=ARCH_MODE;

    public WorkFilesTabPagerAdapter(FragmentManager fm) {
        super(fm);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Fragment getItem(int i) {
        switch (mode) {
            case ARCH_MODE:
                switch (i) {
                    case 0:
                        return new ArchPacked();
                    case 1:
                        return new ArchUnpacked();
                }
            case CRYPTO_MODE:
            switch (i) {
                case 0:
                    return new Encrypted();
                case 1:
                    return new Decrypted();
            }
        }

        return null;

    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return 2; //No of Tabs
    }

    public void setMode(int mode) {
        this.mode=mode;
    }
}

package ru.scorpio92.io.Types;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by scorpio92 on 11.08.16.
 */
public class Object {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
    private final SimpleDateFormat dateFormat_with_GMT = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z");

    public static final String TYPE_DIR = "d";
    public static final String TYPE_SYMLINK_DIR = "ld";
    public static final String TYPE_FILE = "f";
    public static final String TYPE_SYMLINK_FILE = "lf";

    public String path;
    public String type;
    public String date;
    public long size;

    public Object(String path, String type, String date, long size) {
        this.path = path;
        this.type = type;
        this.date = dateFormat.format(new Date(date));
        this.size = size;
    }

    public Object(String path, String type, String date, long size, boolean full_date) {
        this.path = path;
        this.type = type;
        if(full_date) {
            this.date = dateFormat_with_GMT.format(new Date(date));
        } else {
            this.date = dateFormat.format(new Date(date));
        }
        this.size = size;
    }


}

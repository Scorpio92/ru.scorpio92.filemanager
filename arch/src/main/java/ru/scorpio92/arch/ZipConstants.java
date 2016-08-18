package ru.scorpio92.arch;

/**
 * Created by scorpio92 on 16.06.16.
 */
public class ZipConstants {

    public static final int OPERATION_ZIP=0;
    public static final int OPERATION_UNZIP=1;

    public static final int DEFAULT_COMPRESS_LEVEL=0;
    public static final int DEFAULT_PART_SIZE=0;
    public static final int DEFAULT_KEY_LENGHT=0;

    //статусы операции
    public static final int IN_PROGRESS=0;
    public static final int ERROR=1;
    public static final int COMPLETE=2;

    //AretDialog
    public static final String DEFAULT_NAGATIVE_BUTTON_TEXT="Cancel";
    public static final String DEFAULT_SUCCESS_TEXT="OK";
    public static final String DEFAULT_FAIL_TEXT="Error";

    public static final int PROGRESS_UPDATE_INTERVAL=20; //мсек

    public static final String SPLITTED_ARCH_PREFIX="_splitted";
    public static final String MERGED_ARCH_PREFIX="merged_";
}

package ru.scorpio92.io;

/**
 * Created by scorpio92 on 15.06.16.
 */
public class MainOperationsConstants {

    //операции
    public static final int FILE_OPERATION_RENAME=0;
    public static final int FILE_OPERATION_COPY=1;
    public static final int FILE_OPERATION_DELETE=2;
    public static final int FILE_OPERATION_MOVE=3;
    //public static final int FILE_OPERATION_SEARCH=4;

    //статусы операции
    public static final int IN_PROGRESS=0;
    public static final int ERROR=1;
    public static final int NO_SPACE_IN_TARGET=2;
    public static final int COMPLETE=3;
}

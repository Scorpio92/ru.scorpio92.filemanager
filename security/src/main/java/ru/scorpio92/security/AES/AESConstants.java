package ru.scorpio92.security.AES;

/**
 * Created by scorpio92 on 13.06.16.
 */
public class AESConstants {

    //public static final String AES_INSTANCE_NAME="AES";

    public static final String AES_MODE = "AES/CBC/PKCS7Padding";

    public static final String HASH_ALGORITHM = "SHA-256";

    public static final byte[] DEFAULT_INIT_VECTOR = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    public static final String DEFAULT_SALT = "0000000000000000";

    //тип операции
    public static final int ENCRYPT=0;
    public static final int DECRYPT=1;

    //длина ключа
    public static final int KEY_LENGHT_128=128;
    public static final int KEY_LENGHT_192=192;
    public static final int KEY_LENGHT_256=256;

    //статусы операции
    public static final int IN_PROGRESS=0;
    public static final int ERROR=1;
    public static final int COMPLETE=2;

    public static final String DEFAULT_NAGATIVE_BUTTON_TEXT="Cancel";
    public static final String DEFAULT_SUCCESS_TEXT="OK";
    public static final String DEFAULT_FAIL_TEXT="Error";
    public static final int PROGRESS_UPDATE_INTERVAL=100; //мсек

    public static final int DEFAULT_BUFFER=2048;
}

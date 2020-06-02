package dictionaryes;

public class Dictionary {
    public static final String CHAR_SET             = "UTF-8";
    public static final String FILE_NAME_SEPARATOR  = "~#~";

    // common control bytes
    public static final byte   SERVER_FILE_LIST     = 31;
    public static final byte   AUTH_REG_NEW_USER    = 13;
    public static final byte   AUTH                 = 14;
    public static final byte   SUCCESSEFUL_AUTH     = 15;
    public static final byte   FAILED_AUTH          = 16;

    // server control bytes
    public static final byte   GET_FILE_FROM_CLIENT = 28;
    public static final byte   SEND_FILE_TO_CLIENT  = 30;

    // client control bytes
    public static final byte   GET_FILE_FROM_SERVER = 30;
    public static final byte   SEND_FILE_TO_SERVER  = 28;

    // length constants
    public static final int    BYTE_LENGTH          = 1;
    public static final int    INT_LENGTH           = 4;
    public static final int    LONG_LENGTH          = 8;

    // auth service constants
    public static final int    DUMMY_USER           = -1;
}

package controller;

public class Dictionary {
    public static final String CHAR_SET   = "UTF-8";

    // control bytes constants
    public static final byte   FILE_LIST   = 31;
    public static final byte   GET_FILE    = 30;
    public static final byte   SEND_FILE   = 28;
    public static final byte   AUTH        = 14;

    // length constants
    public static final int    INT_LENGTH  = 4;
    public static final int    LONG_LENGTH = 8;
    public static final long   ZERO_LENGTH = 0L;
}

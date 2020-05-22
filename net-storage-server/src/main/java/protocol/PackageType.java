package protocol;

public enum PackageType {
    AUTH("Authorisation", 0)
    , DATA_FILE("Data", 1)
    , FILE_LIST("Files list", 2)
    , UNKNOWON("Unknown", -1);

    private String     name;
    private int        code;
    private static int LENGTH =1;

    PackageType(String _name, int _code){
        this.name = _name;
        this.code = _code;
    }

    public int getLength(){
        return LENGTH;
    }

    public byte getCode(){
        return  (byte) this.code;
    }
}

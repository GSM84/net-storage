package dictionaryes;

public enum HandelerState {
    IDLE,
    WAITING_FOR_AUTH,
    AUHORIZED,
    GET_CREDENTIAL_LENGTH,
    GET_LOGIN,
    GET_PASS,
    PROCESS_FILE_LIST,
    PROCESS_INCOME_FILE
}

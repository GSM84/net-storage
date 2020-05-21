package services;

public class AuthService {
    private static UserList users = new UserList();

    public static boolean authorize(String _login, String _pass){
        return users.getUserList().get(_login).getPass().equals(_pass);
    }
}

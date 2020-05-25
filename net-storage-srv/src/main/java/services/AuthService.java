package services;

public class AuthService {
    private static UserList users = new UserList();

    private UserList.User currUser;

    public boolean authorize(String _login, String _pass){
        if (users.getUserList().get(_login).getPass().equals(_pass))
            this.currUser = users.getUserList().get(_login);

        return users.getUserList().get(_login).getPass().equals(_pass);
    }

    public UserList.User getCurrUser() {
        return currUser;
    }

    public int getUserId(){
        return currUser.getId();
    }
}

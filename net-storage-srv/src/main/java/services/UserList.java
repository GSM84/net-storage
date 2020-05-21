package services;

import java.util.HashMap;

public class UserList {
    private HashMap<String, User> userList;

    public HashMap<String, User> getUserList() {
        return userList;
    }

    public UserList(){
        this.userList = new HashMap<>();
        this.userList.put("User1", new User("User1", "pass1", 1));
    }

    class User{
        private String login;
        private String pass;
        private int    id;

        public User(String _login, String _pass, int _id){
            this.login = _login;
            this.pass  = _pass;
            this.id    = _id;
        }

        public String getLogin() {
            return login;
        }

        public String getPass() {
            return pass;
        }

        public void setPass(String pass) {
            this.pass = pass;
        }

        public int getId() {
            return id;
        }

    }
}

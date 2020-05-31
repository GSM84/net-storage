package services;

import dictionaryes.Dictionary;

import java.sql.*;

public class DBService {
    private static       Connection        connection;
    private static       int               authUserId = Dictionary.DUMMY_USER;
    private static       ResultSet         resultSet;
    private static       PreparedStatement preparedStatement;
    private static final String            authQuery  = "select user_id " +
                                                        "from users " +
                                                        "where login = ? " +
                                                        "and password = ?";

    private static final String            regQuery   = "insert into users(" +
                                                        "login, password) " +
                                                        "values(?, ?) ";

    private static final String URL = "jdbc:sqlite:usersDB.db";

    public static void connect() {
        try {
            System.out.println("DB connection establishing...");

            connection = DriverManager.getConnection(URL);

            System.out.println("Connected.");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Can't connect to DB.");
        }
    }

    public static void disconnect(){
        try {
            connection.close();
            System.out.println("DB connection was closed.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean authUser(String _login, String _password){
        try {
            preparedStatement = connection.prepareStatement(authQuery);
            preparedStatement.setString(1, _login);
            preparedStatement.setString(2, _password);
            resultSet = preparedStatement.executeQuery();

            if(resultSet.next()){
                authUserId = resultSet.getInt("user_id");
                preparedStatement.close();

                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean regUser(String _login, String _password){
        try {
            preparedStatement = connection.prepareStatement(regQuery, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, _login);
            preparedStatement.setString(2, _password);
            preparedStatement.executeUpdate();
            resultSet = preparedStatement.getGeneratedKeys();

            if(resultSet.next()){
                authUserId = resultSet.getInt(1);
                preparedStatement.close();

                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static int getAuthUserId() {
        return authUserId;
    }

    public static void unsetUserId(){
        authUserId = Dictionary.DUMMY_USER;
    }
}

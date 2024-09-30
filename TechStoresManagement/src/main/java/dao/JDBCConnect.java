package dao;

import config.IDBConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBCConnect {
    public  static Connection getJDBCConnection() {
        Connection con = null;
        String connectionUrl = "jdbc:mysql://" + IDBConfig.HOSTNAME
                + ":" + IDBConfig.PORT + "/"
                + IDBConfig.DBNAME ;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            System.err.println("Where is your MySQL JDBC Driver?");
            return con;
        }
//        System.out.println("MySQL JDBC Driver Registered!");
        
        try {
            con = DriverManager.getConnection(connectionUrl, IDBConfig.USERNAME, IDBConfig.PASSWORD);
        } catch (SQLException ex) {
            System.err.println("Connection Failed! Check output console");
            return con;
        }
        return con;
    }
    public static void main(String[] args) {
        System.out.println(JDBCConnect.getJDBCConnection());
    }
}

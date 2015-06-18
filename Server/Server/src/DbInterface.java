import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class DbInterface {
    
    private  Connection connection = null;

    public void start() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
       
        try {
            connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/bankaccounts", "postgres", "admin");
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Opened database successfully");
    }
    
    public void createAccount(String account) throws SQLException {

        Statement  stmt = connection.createStatement();
        String sql = "CREATE TABLE " + account +
                     "(TIME         BIGINT PRIMARY KEY     NOT NULL," +
                     " NAME         TEXT    NOT NULL, " +
                     " DEPOSITO     BOOLEAN NOT NULL, " +
                     " VALOR        INT     NOT NULL)";

        stmt.executeUpdate(sql);
        stmt.close();
    }
    
    public int saldo(String account) throws SQLException {

        Statement stmt = null;
        connection.setAutoCommit(false);
        stmt = connection.createStatement();
        
        ResultSet rs = stmt.executeQuery( "SELECT * FROM "+ account +";" );
        int saldo = 0;
        while ( rs.next() ) {
            int value = rs.getInt("valor");
            if(rs.getBoolean("deposito")) {
                saldo = saldo + Math.abs(value);
            } else {
                saldo = saldo - Math.abs(value);
            }
        }
        rs.close();
        stmt.close();
        return saldo;
    }

    public void insert(String account, String name, boolean deposit, int value) {
        try {

            long time = System.currentTimeMillis();

            connection.setAutoCommit(false);
            Statement  stmt = connection.createStatement();

            String sql = "INSERT INTO "+ account +" (TIME,NAME,DEPOSITO,VALOR) "
                    + "VALUES (" + time + ", '"+ name +"', "+ deposit + ", " + value + ");";
            
            System.out.printf("insert is %s\n", sql);
            stmt.executeUpdate(sql);

            stmt.close();
            connection.commit();

        } catch (Exception e) {
            System.err.println( e.getClass().getName()+": "+ e.getMessage() );
            System.exit(0);
        }
        System.out.println("Records created successfully");
    }

    public void deposit (String account, String name, int value) {
        insert(account, name, true, value);
    }
    
    public void debit (String account, String name, int value) {
        insert(account, name, false, value);
    }
    
    public boolean accountExists(String account) {
        DatabaseMetaData md;
        boolean res = false;
        try {
            md = connection.getMetaData();
            ResultSet rs = md.getTables(null, null, account, null);
            if (rs.next()) {
                res = true;
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return res;
    }
    
    public void close () {
        try {
            connection.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}

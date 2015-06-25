import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.dist.common.BusinessException;
import com.dist.common.Seat;


public class DbImplementation implements DbInterface {

    private Connection connection = null;

    public void start() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            connection = DriverManager.getConnection(
                    "jdbc:postgresql://127.0.0.1:5432/cinema", "postgres",
                    "admin");
            initializeDatabase();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Opened database successfully");
    }
    
    public boolean databaseInitialized() {
        DatabaseMetaData md;
        boolean res = false;
        try {
            md = connection.getMetaData();
            ResultSet rs = md.getTables(null, null, "assento", null);
            if (rs.next()) {
                res = true;
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return res;
    }

    private void initializeDatabase() throws SQLException {
        
        
        if(databaseInitialized())
            return;

        Statement stmt = connection.createStatement();
        StringBuilder sql = new StringBuilder("CREATE TABLE ASSENTO");
        sql.append("(ID         SERIAL PRIMARY KEY     NOT NULL,");
        sql.append(" FILEIRA         TEXT    NOT NULL, ");
        sql.append(" NUMERO     INT NOT NULL, ");
        sql.append(" RESERVADO        BOOLEAN     NOT NULL,");
        sql.append(" DONO TEXT NULL,");
        sql.append(" FILME TEXT NOT NULL)");

        stmt.executeUpdate(sql.toString());
        sql = new StringBuilder();
        char fileira = 'A';
        for (int i = 0; i < 4; i++, fileira++)
            for (int j = 1; j < 6; j++) {
                sql.append("INSERT INTO ASSENTO (FILEIRA,NUMERO,RESERVADO,DONO,FILME) VALUES (");
                sql.append("\'").append(fileira).append("\'");
                sql.append(", ").append(j);
                sql.append(", ").append(false);
                sql.append(", NULL");
                sql.append(", \'her\'").append(");");
            }
        stmt.executeUpdate(sql.toString());
        stmt.close();

    }

    public String getAvailableSeats(String movie) throws SQLException {

        PreparedStatement stmt = null;
        connection.setAutoCommit(false);
        stmt = connection.prepareStatement("SELECT * FROM ASSENTO WHERE FILME = ? AND RESERVADO = FALSE;");
        stmt.setString(1, movie);
        ResultSet rs = stmt.executeQuery();
        StringBuilder seats = new StringBuilder();
        while (rs.next()) {
            seats.append("[").append(rs.getString("fileira"))
            .append(" - ")
            .append(rs.getInt("numero")).append("] \n");
        }
        rs.close();
        stmt.close();
        return seats.toString();
    }
    
    public boolean isSeatTaken(String movie, Seat seat) throws SQLException, BusinessException{
        PreparedStatement stmt = null;
        connection.setAutoCommit(false);
        stmt = connection.prepareStatement("SELECT * FROM ASSENTO WHERE FILME = ? AND NUMERO = ? AND FILEIRA = ?;");
        stmt.setString(1, movie);
        stmt.setInt(2, seat.getNumber());
        stmt.setString(3, seat.getRow().toUpperCase());
        System.out.printf("query %s\n", stmt.toString());
        ResultSet rs = stmt.executeQuery();
        
        if(!rs.next())
            throw new BusinessException("Assento Nao Encontrado");
        boolean taken = rs.getBoolean("reservado");
        rs.close();
        stmt.close();
        return taken;
    }

    public void insert(String name, String movie, Seat seat) throws BusinessException  {
        
        try {
            if(isSeatTaken(movie, seat))
                throw new BusinessException("Assento Ocupado");
            connection.setAutoCommit(false);
            PreparedStatement stmt;
            
            stmt = connection.prepareStatement("UPDATE ASSENTO SET  DONO=? , RESERVADO = TRUE WHERE NUMERO = ? AND FILEIRA = ? AND FILME=?;");
            stmt.setString(1,name);
            stmt.setInt(2, seat.getNumber());
            stmt.setString(3, seat.getRow().toUpperCase());
            stmt.setString(4, movie);

            System.out.printf("insert is %s\n", stmt.toString());
            stmt.execute();

            stmt.close();
            connection.commit();

        } catch (SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Records created successfully");
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

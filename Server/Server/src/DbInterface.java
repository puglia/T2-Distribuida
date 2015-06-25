import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.dist.common.BusinessException;
import com.dist.common.Seat;

public interface DbInterface {
    
    public void insert(String name, String movie, Seat seat) throws BusinessException;
    
    public String getAvailableSeats(String movie) throws SQLException;
    
    public void start();
    
    public void close();

}

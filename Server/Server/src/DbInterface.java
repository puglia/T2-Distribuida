import java.sql.SQLException;

import com.dist.common.BusinessException;
import com.dist.common.Seat;

public interface DbInterface {

    
    public void insert(String name, String movie, Seat seat) throws BusinessException;
    
    public String getAvailableSeats(String movie) throws SQLException;
    
    public void start();
    
    public void close();

}

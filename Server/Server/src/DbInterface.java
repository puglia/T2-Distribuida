import java.sql.SQLException;

import com.dist.common.BusinessException;
import com.dist.common.Seat;

public interface DbInterface {

    
    public void insert(String name, String show, Seat seat, long time) throws BusinessException;
    
    public String getWaitingList(String show) throws SQLException;
    
    public void start();
    
    public void close();

}

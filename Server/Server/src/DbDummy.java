import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.dist.common.BusinessException;
import com.dist.common.Seat;

public class DbDummy implements DbInterface {
    Map<String, SeatAllocation[]> seats = new HashMap<>();

    @Override
    public void insert(String name, String movie, Seat seat)
            throws BusinessException {
        String row = seat.getRow().toUpperCase();
        if(seats.get(row)[seat.getNumber()] != null)
            throw new BusinessException("Seat Already Taken");
        
        seats.get(row)[seat.getNumber()] = new SeatAllocation(seat,name);

    }

    @Override
    public String getAvailableSeats(String movie) throws SQLException {
        StringBuilder builder = new StringBuilder();

        for (String row : seats.keySet())
            for (int i = 0; i < seats.get(row).length; i++)
                if (seats.get(row)[i] == null)
                    builder.append("[").append(row).append(" - ").append(i).append("]\n");
        
        return builder.toString();
    }

    @Override
    public void start() {
        char row = 'A';
        while (row != 'M')
            seats.put(Character.toString(row++), new SeatAllocation[20]);

    }

    @Override
    public void close() {
        return;
    }

    private class SeatAllocation extends Seat {
        String owner;
        String id;

        public SeatAllocation() {
            super();
        }

        public SeatAllocation(Seat seat, String owner) {
            super(seat.getNumber(), seat.getRow().toUpperCase(), seat.isReserved());
            this.id = seat.getRow() + seat.getNumber();
            this.owner = owner;
        }

        public SeatAllocation(Seat seat) {
            super(seat.getNumber(), seat.getRow(), seat.isReserved());
        }

        public String getOwner() {
            return owner;
        }

        public void setOwner(String owner) {
            this.owner = owner;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

    }

}

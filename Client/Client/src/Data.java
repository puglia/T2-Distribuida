import java.io.DataInput;
import java.io.DataOutput;

import org.jgroups.util.Streamable;
import org.jgroups.util.Util;

public class Data implements Streamable {

    private String movie;
    private String name;
    private Operation operation;
    private Seat reservedSeat;
    
    public Data() {}
    
    public Data(String movie, String name, Operation operation) {
        super();
        this.movie = movie;
        this.name = name;
        this.operation = operation;
    }
    
    public Data(String movie, String name, Operation operation,
            Seat reservedSeat) {
        super();
        this.movie = movie;
        this.name = name;
        this.operation = operation;
        this.reservedSeat = reservedSeat;
    }

    @Override
    public void readFrom(DataInput in) throws Exception {
        movie=(String)Util.objectFromStream(in);
        name=(String)Util.objectFromStream(in);
        operation=(Operation)Util.objectFromStream(in);
        reservedSeat=(Seat)Util.objectFromStream(in);
    }

    @Override
    public void writeTo(DataOutput out) throws Exception {
        Util.objectToStream(movie, out);
        Util.objectToStream(name, out);
        Util.objectToStream(operation, out);
        Util.objectToStream(reservedSeat, out);
    }
    
    public String getMovie() {
        return movie;
    }

    public String getName() {
        return name;
    }

    public Operation getOperation() {
        return operation;
    }
    
    public Seat getReservedSeat() {
        return reservedSeat;
    }

}

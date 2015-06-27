package com.dist.common;
import java.io.DataInput;
import java.io.DataOutput;

import org.jgroups.util.Streamable;
import org.jgroups.util.Util;

public class Data implements Streamable {

    private String artista;
    private String name;
    private Operation operation;
    private Seat reservedSeat;
    private long time;
    
    public Data() {}
    
    public Data(String artista, String name, Operation operation) {
        super();
        this.artista = artista;
        this.name = name;
        this.operation = operation;
        this.time = System.currentTimeMillis();
    }
    
    public Data(String artista, String name, Operation operation,
            Seat reservedSeat) {
        super();
        this.artista = artista;
        this.name = name;
        this.operation = operation;
        this.reservedSeat = reservedSeat;
        this.time = System.currentTimeMillis();
    }

    @Override
    public void readFrom(DataInput in) throws Exception {
        artista=(String)Util.objectFromStream(in);
        name=(String)Util.objectFromStream(in);
        operation=(Operation)Util.objectFromStream(in);
        reservedSeat=(Seat)Util.objectFromStream(in);
        time=(long)Util.objectFromStream(in);
    }

    @Override
    public void writeTo(DataOutput out) throws Exception {
        Util.objectToStream(artista, out);
        Util.objectToStream(name, out);
        Util.objectToStream(operation, out);
        Util.objectToStream(reservedSeat, out);
        Util.objectToStream(time, out);
    }
    
    public String getArtista() {
        return artista;
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
    
    public Long getTime() {
        return time;
    }

}

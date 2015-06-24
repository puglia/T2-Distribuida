package com.dist.common;
import java.io.DataInput;
import java.io.DataOutput;

import org.jgroups.util.Streamable;
import org.jgroups.util.Util;


public class Seat implements Streamable {

    
    private int number;
    private String row;
    private boolean reserved;
    
    public Seat() {
        super();
    }
    
    public Seat(int number, String row) {
        super();
        this.number = number;
        this.row = row;
    }
    
    public Seat(int number, String row, boolean reserved) {
        super();
        this.number = number;
        this.row = row;
        this.reserved = reserved;
    }
    public int getNumber() {
        return number;
    }
    public void setNumber(int number) {
        this.number = number;
    }
    public String getRow() {
        return row;
    }
    public void setRow(String row) {
        this.row = row;
    }
    public boolean isReserved() {
        return reserved;
    }
    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }

    @Override
    public void readFrom(DataInput in) throws Exception {
        number=(int)Util.objectFromStream(in);
        row=(String)Util.objectFromStream(in);
        reserved=(boolean)Util.objectFromStream(in);
    }

    @Override
    public void writeTo(DataOutput out) throws Exception {
        Util.objectToStream(number, out);
        Util.objectToStream(row, out);
        Util.objectToStream(reserved, out);
    }
    
    
}

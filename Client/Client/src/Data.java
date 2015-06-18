import java.io.DataInput;
import java.io.DataOutput;

import org.jgroups.util.Streamable;
import org.jgroups.util.Util;

public class Data implements Streamable {

    private String account;
    private String name;
    private int value;
    private boolean deposit;
    public Data() {}

    public Data(String account, String name, int value, boolean deposit) {
        this.account=account;
        this.name=name;
        this.value=value;
        this.deposit = deposit;
    }
    
    @Override
    public void readFrom(DataInput in) throws Exception {
//        account = in.readLine();
//        name = in.readLine();
//        value = in.readInt();
//        deposit = in.readBoolean();
        account=(String)Util.objectFromStream(in);
        name=(String)Util.objectFromStream(in);
        value=(int)Util.objectFromStream(in);
        deposit=(boolean)Util.objectFromStream(in);
    }

    @Override
    public void writeTo(DataOutput out) throws Exception {
//        out.writeChars(account);
//        out.writeChars(name);
//        out.write(value);
//        out.writeBoolean(deposit);
        
        Util.objectToStream(account, out);
        Util.objectToStream(name, out);
        Util.objectToStream(value, out);
        Util.objectToStream(deposit, out);
    }
    
    public String getAccount() {
        return account;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    public boolean isDeposit() {
        return deposit;
    }
}

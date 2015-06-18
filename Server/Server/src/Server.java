import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.SQLException;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.util.Util;

public class Server extends ReceiverAdapter {
    JChannel channel;
    String user_name=System.getProperty("user.name", "n/a");
    
    private DbInterface bank = null;

    public void receive(Message msg) {
        Data data = null;
        try {
            data = (Data)Util.streamableFromByteBuffer(Data.class, msg.getRawBuffer(), msg.getOffset(), msg.getLength());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.printf("Receive data from %s \n", data.getName());
        
        if (data.getName().equals("SALDO")) {
            int saldo = 0;
            try {
                saldo = bank.saldo(data.getAccount());
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.printf("Saldo de %s= %d \n", data.getAccount(), saldo);
        } else {
            bank.insert(data.getAccount(), data.getName(), data.isDeposit(), data.getValue());
        }
    }

    private void start() throws Exception {
    	System.setProperty("java.net.preferIPv4Stack", "true");
        channel=new JChannel();
        channel.setReceiver(this);
        

        channel.connect("bank");
        
        bank = new DbInterface();
        
        bank.start();
        String accountName = "conta1";
        if (!bank.accountExists(accountName)) {
            bank.createAccount(accountName);
        }
//        account.deposit(accountName, "Rafael", 1000);
//        
//        account.debit(accountName, "Thiago", 100);
//        
//        account.deposit(accountName, "Gianlucca", 500);
        
        waitAction();
        channel.close();
        

        
        
    }

    private void waitAction() {
        BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
        while(true) {
            try {
                System.out.print("> "); System.out.flush();
                String line=in.readLine().toLowerCase();
                if(line.startsWith("quit") || line.startsWith("exit")) {
                    break;
                }
                line="[" + user_name + "] -> " + line;
//                Message msg=new Message(null, null, line);
//                channel.send(msg);
            }
            catch(Exception e) {
            }
        }
    }

    public static void main(String[] args) throws Exception {
        new Server().start();
    }
}
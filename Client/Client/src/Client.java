import java.io.BufferedReader;
import java.io.InputStreamReader;



import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.util.Util;

public class Client extends ReceiverAdapter {
    JChannel channel;
    String user_name=System.getProperty("user.name", "n/a");

    public void receive(Message msg) {
        String line=msg.getSrc() + ": " + msg.getObject();
        System.out.println(line);
    }

    private void start() throws Exception {
    	System.setProperty("java.net.preferIPv4Stack", "true");
    	
        channel=new JChannel();
        //channel.setReceiver(this);
        

        channel.connect("bank");
        
        System.out.print("Client initialized\n");
        
        waitValue();
        
        channel.close();
        
    }
    

    private void waitValue() {
        BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
        while(true) {
            try {
                System.out.print("> "); System.out.flush();
                String line=in.readLine().toLowerCase();
                if(line.startsWith("quit") || line.startsWith("exit")) {
                    break;
                }
                Data data = null;
                if (line.startsWith("saldo")) {
                    data = new Data("conta1", "SALDO",  0, true);
                } else if (Integer.parseInt(line) < 0) {
                    data = new Data("conta1", "rafael",  Math.abs(Integer.parseInt(line)), false);
                    line="[" + user_name + "] -> " + line;
                } else {
                    data = new Data("conta1", "rafael",  Integer.parseInt(line), true);
                    line="[" + user_name + "] -> " + line;
                }
                byte[] buf=Util.streamableToByteBuffer(data);
                channel.send(new Message(null, buf));
                
            }
                catch(Exception e) {
            }
        }
    }

    public static void main(String[] args) throws Exception {
        new Client().start();
    }
}
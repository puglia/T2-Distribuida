import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.util.RspList;
import org.jgroups.util.Util;

public class Client extends ReceiverAdapter {
    JChannel channel;
    MessageDispatcher dispatcher;
    public static final long SERVER_TIMEOUT = 5000;
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
        dispatcher = new MessageDispatcher(channel, null, null);
        
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
                if (line.startsWith("consulta")) {
                    String movie = line.replace("consulta ", "").trim();
                    data = new Data(movie, InetAddress.getLocalHost().getHostAddress(),  Operation.CONSULTAR);
                } else if (line.startsWith("reserva")) {
                    String movie = line.replace("reserva ", "").trim();
                    
                    data = new Data(movie, InetAddress.getLocalHost().getHostAddress(),  Operation.RESERVAR);
                    line="[" + user_name + "] -> " + line;
                } 
                byte[] buf=Util.streamableToByteBuffer(data);
                
                
                RspList<Object> responses = dispatcher.castMessage(channel.getView().getMembers(), new Message(null, buf),options());
                processResponse(responses.getFirst());
                //channel.send(new Message(null, buf));
                
            }
                catch(Exception e) {
            }
        }
    }

    private void processResponse(Object object){
        if(object instanceof String)
            System.out.println(object);
    }
    
    private RequestOptions options(){
        return new RequestOptions(ResponseMode.GET_ALL, SERVER_TIMEOUT);
    }

    public static void main(String[] args) throws Exception {
        new Client().start();
    }
}
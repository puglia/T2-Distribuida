import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.protocols.DELAY;
import org.jgroups.protocols.SEQUENCER;
import org.jgroups.protocols.pbcast.NAKACK2;
import org.jgroups.stack.Protocol;
import org.jgroups.stack.ProtocolStack;
import org.jgroups.util.RspList;
import org.jgroups.util.Util;

import com.dist.common.Data;
import com.dist.common.Operation;
import com.dist.common.Seat;

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
        System.setProperty("log4j.configurationFile","/tmp/log4j2.xml");
        channel=new JChannel();
        channel.setReceiver(this);
        
        

        
        //******** protocols definition
        System.out.print("protocol stack initialization\n");
        ProtocolStack ps=channel.getProtocolStack();

        SEQUENCER sequencer=new SEQUENCER();
        
        ps.insertProtocol(sequencer,ProtocolStack.ABOVE,NAKACK2.class);
        channel.connect("bank");
        
        int i = 0;
        for (Address add : channel.getView().getMembers()) {
            System.out.printf(" addr: %s \n", add.toString() );
            if (add.equals(channel.getAddress()) && i == 2) {
                System.out.printf("I am the delayed one %s\n",  channel.getAddressAsString());
                DELAY delay=new DELAY();
                delay.setInDelay(5000);
                delay.setOutDelay(5000);
                try {
                    ps.insertProtocol(delay,ProtocolStack.ABOVE,SEQUENCER.class);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            i++;
        }
        
        for (Protocol p : ps.getProtocols()) {
            System.out.printf("get protocol %s\n", p.getName());
        }
        //***********  protocols definition
        

        if (sequencer.isCoordinator()) {
        	System.out.print("coordinator\n");
        } else {
        	System.out.print("not the coordinator\n");
        }
        dispatcher = new MessageDispatcher(channel, null, null);
        
        System.out.print("Client initialized\n");
        
        waitValue();

        channel.close();
        
    }
//    
//    public void viewAccepted(View view) {
//        int i=0;
//        System.out.printf("I am in the group size: %d",  view.size());
//        for (Address add : view.getMembers()) {
//            if (add == channel.getAddress() && i == 2) {
//                System.out.printf("I am the delayed one %s",  channel.getAddressAsString());
//                DELAY delay=new DELAY();
//                delay.setInDelay(1000);
//                delay.setOutDelay(1000);
//                ProtocolStack ps=channel.getProtocolStack();
//                try {
//                    ps.insertProtocol(delay,ProtocolStack.ABOVE,SEQUENCER.class);
//                } catch (Exception e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//            }
//            i++;
//        }
//    }
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
                    data = new Data(movie, channel.getAddressAsString(),  Operation.CONSULTAR);
                } else if (line.startsWith("reserva")) {
                    String movie = line.replace("reserva ", "").trim();
                    System.out.print("Informe um assento (fileira e numero):");
                    String param[]=in.readLine().toLowerCase().trim().split(" ", 2);
                    Seat seat = new Seat(Integer.parseInt(param[1]),param[0]);
                    data = new Data(movie, channel.getAddressAsString(), Operation.RESERVAR, seat);
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
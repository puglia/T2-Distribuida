import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.SecureRandom;
import java.util.Enumeration;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.protocols.SEQUENCER;
import org.jgroups.protocols.UNICAST3;
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
        try {
            Data data = (Data) Util.streamableFromByteBuffer(Data.class,
                    msg.getRawBuffer(), msg.getOffset(), msg.getLength());
            if (data.getOperation() == Operation.EXECUTAR_BATCH) {
                System.out.print("Batch execution request received!\n");
                castBatch();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    private void start() throws Exception {
    	System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("log4j.configurationFile","/log4j2.xml");
        channel=new JChannel();
        channel.setReceiver(this);
        
        //name the element
        Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
        NetworkInterface iface = n.nextElement();
        Enumeration<InetAddress> inet = iface.getInetAddresses();
        channel.setName("client_" + inet.nextElement().getHostAddress());


        // define the cluster connection
        BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
        System.out.printf("Digite o nome do show: ");
        System.out.flush();
        String artist=in.readLine().toLowerCase();

        ProtocolStack ps=channel.getProtocolStack();
        SEQUENCER sequencer=new SEQUENCER();
        if ( artist.equals("acdc")) {
            //******** protocols definition
            System.out.print("protocol stack initialization\n");
            ps.insertProtocol(sequencer,ProtocolStack.ABOVE,UNICAST3.class);
            
        }
        for (Protocol i : ps.getProtocols()) {
            System.out.printf("get protocol %s\n", i.getName());
        }
        channel.connect(artist);

        if ( artist.equals("acdc")) {
        System.out.print("coordinator?\n");
        if (sequencer.isCoordinator()) {
            System.out.print("coordinator\n");
        } else {
            System.out.print("not the coordinator\n");
        }
        }
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
                    String show = channel.getClusterName();
                    data = new Data(show, channel.getAddressAsString(),  Operation.CONSULTAR);
                } else if (line.startsWith("reserva")) {
                    String show = channel.getClusterName();
                    System.out.print("Informe um assento (fileira e numero):");
                    String param[]=in.readLine().toLowerCase().trim().split(" ", 2);
                    Seat seat = new Seat(Integer.parseInt(param[1]),param[0]);
                    data = new Data(show, channel.getAddressAsString(), Operation.RESERVAR, seat);
                    System.out.printf("time %d\n", data.getTime());
                    line="[" + user_name + "] -> " + line;
                } 
                else if(line.startsWith("batch")){
                    byte[] messageContent = Util.streamableToByteBuffer(new Data(null,null,Operation.EXECUTAR_BATCH));
                    channel.send( new Message(null, messageContent));
                    castBatch();
                    continue;
                    }
                else if(line.contains("cluster")){
                    for(Address addr: channel.getView().getMembers())
                        System.out.print(addr.toString()+"\n");
                }
                byte[] buf=Util.streamableToByteBuffer(data);
                
                
                RspList<Object> responses = dispatcher.castMessage(channel.getView().getMembers(), new Message(null, buf),options());
                processResponse(responses.getFirst());
                
            }
                catch(Exception e) {
            }
        }
    }
    
    public void castBatch() throws Exception{
        char row;
        int number;
        Thread.sleep(1000);
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(System.currentTimeMillis());
        for(int i =0; i <15; i++){
           number =  Math.abs(random.nextInt()%20);
           row = (char) ('A' + Math.abs(random.nextInt()%12)); 
           Seat seat = new Seat(number,Character.toString(row));

           Data data = new Data("titas", InetAddress.getLocalHost().getHostName(), Operation.RESERVAR, seat);

           byte[] buf=Util.streamableToByteBuffer(data);
           dispatcher.castMessage(channel.getView().getMembers(), new Message(null, buf),options());
         }
    }

    private void processResponse(Object object){
        if(object instanceof String)
            System.out.println(object);
    }
    
    private RequestOptions options(){
        return new RequestOptions(ResponseMode.GET_FIRST, SERVER_TIMEOUT);
    }

    public static void main(String[] args) throws Exception {
        new Client().start();
    }
}
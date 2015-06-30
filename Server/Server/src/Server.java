import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.sql.SQLException;
import java.util.Enumeration;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.protocols.DELAY;
import org.jgroups.protocols.SEQUENCER;
import org.jgroups.protocols.UDP;
import org.jgroups.protocols.UNICAST3;
import org.jgroups.stack.Protocol;
import org.jgroups.stack.ProtocolStack;
import org.jgroups.util.Util;

import com.dist.common.BusinessException;
import com.dist.common.Data;

public class Server implements RequestHandler {
    JChannel channel_seq;
    JChannel channel_nseq;
    MessageDispatcher dispatcher;
    String user_name = System.getProperty("user.name", "n/a");
    boolean _delay = false;
    boolean invert = false;
    String lero = "test";
    
    ProtocolStack ps_seq;
    ProtocolStack ps_nseq;
    
    DELAY delayDefault;

    private DbInterface dao = null;

    // handle the input messages
    public Object handle(Message msg) throws Exception {
        Data data = null;
        try {
            // recover the message from buffer
            data = (Data) Util.streamableFromByteBuffer(Data.class,
                    msg.getRawBuffer(), msg.getOffset(), msg.getLength());

            switch (data.getOperation()) {
            
            case CONSULTAR:
            System.out.printf("Receive data from %s \n", msg.getSrc());
                String seats = "";
                try {
                    // get the queue
                    seats = dao.getWaitingList(data.getArtista());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                System.out.printf("Assentos para %s= %s \n", data.getArtista(),
                        seats);
                return "Assentos para" + data.getArtista() + " = " + seats;
            case RESERVAR:
            	System.out.printf("Receive data from %s, %s: %s-%d\n", data.getName(), data.getOperation().getName(), data.getReservedSeat().getRow(), data.getReservedSeat().getNumber());
                // insert on queue
            	dao.insert(data.getName(), data.getArtista(),
                        data.getReservedSeat(), data.getTime());
            default:
                break;
            }
        }  catch (BusinessException e1) {
            return e1.getLocalizedMessage();
        }
        catch (Exception e1) {
            e1.printStackTrace();
        }
        
        /////////////// set delay
        if (_delay) {
            if(!invert) {
                System.out.printf("delay was on\n");
                delayDefault.setInDelay(5);
                invert = true;
            } else {
                System.out.printf("delay was off\n");
                delayDefault.setInDelay(6000);
                invert = false;
            }
        }
        /////////////// set delay

        return "Operacao efetuada com Sucesso!";

    }

    private void start() throws Exception {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("log4j.configurationFile","/log4j2.xml");
        // two channels, one for each show
        channel_seq = new JChannel();
        channel_nseq = new JChannel();

        //name the element
        Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
        NetworkInterface iface = n.nextElement();
        Enumeration<InetAddress> inet = iface.getInetAddresses();
        String Addr = inet.nextElement().getHostAddress();
        channel_seq.setName("server_" + Addr);
        channel_nseq.setName("server_" + Addr);
       
        //get protocol stack from channels
        ps_seq=channel_seq.getProtocolStack();
        ps_nseq=channel_nseq.getProtocolStack();


        //******** protocols definition
        System.out.print("protocol stack initialization\n");
        SEQUENCER sequencer=new SEQUENCER();
        ps_seq.insertProtocol(sequencer,ProtocolStack.ABOVE,UNICAST3.class);
        //***********  protocols definition
        
        // print the protocol stack
        for (Protocol i : ps_seq.getProtocols()) {
            System.out.printf("get protocol %s\n", i.getName());
        }
      
        // connecting to the cluster
        channel_seq.connect("acdc");
        channel_nseq.connect("pearl jam");
        
        
        //in sequencer cluster, check if we are the coordinator
        System.out.print("coordinator?\n");
        if (sequencer.isCoordinator()) {
            System.out.print("coordinator\n");
        } else {
            System.out.print("not the coordinator\n");
        }

        
        /////////////// set delay
        String delayedAddr = "server_192.168.85.105";
        if (delayedAddr.equals(channel_seq.getAddress().toString())) {
            _delay = true;
            System.out.printf("I am the delayed one %s\n",  channel_seq.getAddressAsString());
            delayDefault=new DELAY();
            delayDefault.setInDelay(6000);
            ps_seq.insertProtocol(delayDefault,ProtocolStack.ABOVE, UDP.class);
            //ps_nseq.insertProtocol(delayDefault,ProtocolStack.ABOVE, UDP.class);
        }
        /////////////// set delay
        
        // initializing object to synchronize messages
        dispatcher = new MessageDispatcher(channel_seq, null, null, this);
        dispatcher = new MessageDispatcher(channel_nseq, null, null, this);

        dao = new DbImplementation();

        dao.start();

        waitAction();
        channel_seq.close();
        channel_nseq.close();

    }

    
    private void waitAction() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                System.out.print("> ");
                System.out.flush();
                String line = in.readLine().toLowerCase();
                if (line.startsWith("quit") || line.startsWith("exit")) {
                    break;
                }
                line = "[" + user_name + "] -> " + line;
                // Message msg=new Message(null, null, line);
                // channel.send(msg);
            } catch (Exception e) {
            }
        }
    }

    public static void main(String[] args) throws Exception {
        new Server().start();
    }
}
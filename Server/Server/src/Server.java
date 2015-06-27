import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.sql.SQLException;
import java.util.Enumeration;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.protocols.DELAY;
import org.jgroups.protocols.PING;
import org.jgroups.protocols.SEQUENCER;
import org.jgroups.protocols.UFC;
import org.jgroups.stack.Protocol;
import org.jgroups.stack.ProtocolStack;
import org.jgroups.util.Util;

import com.dist.common.BusinessException;
import com.dist.common.Data;
import com.dist.common.helper;

public class Server implements RequestHandler {
    JChannel channel;
    MessageDispatcher dispatcher;
    String user_name = System.getProperty("user.name", "n/a");
    boolean _delay = false;
    boolean invert = false;
    String lero = "test";

    private DbInterface dao = null;

    public Object handle(Message msg) throws Exception {
        
        /////////////// set delay
        ProtocolStack ps=channel.getProtocolStack();
        if (_delay) {
            System.out.printf("I am the delayed one %s\n",  channel.getAddressAsString());
            if(!invert) {
                System.out.printf("false\n");
                ps.removeProtocol(UFC.class);
                invert = true;
            } else {
                System.out.printf("true\n");
                DELAY delay=new DELAY();
                delay.setInDelay(5000);
                ps.insertProtocol(delay,ProtocolStack.ABOVE,PING.class);
                invert = false;
            }
        }
        /////////////// set delay

        Data data = null;
        try {
            data = (Data) Util.streamableFromByteBuffer(Data.class,
                    msg.getRawBuffer(), msg.getOffset(), msg.getLength());

            switch (data.getOperation()) {
            
            case CONSULTAR:
            System.out.printf("Receive data from %s \n", msg.getSrc());
            
                String seats = "";
                try {
                    seats = dao.getWaitingList(data.getArtista());
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                System.out.printf("Assentos para %s= %s \n", data.getArtista(),
                        seats);
                return "Assentos para" + data.getArtista() + " = " + seats;
            case RESERVAR:
            	System.out.printf("Receive data from %s, %s: %s-%d\n", data.getName(), data.getOperation().getName(), data.getReservedSeat().getRow(), data.getReservedSeat().getNumber());
                dao.insert(data.getName(), data.getArtista(),
                        data.getReservedSeat(), data.getTime());
            default:
                break;
            }
        }  catch (BusinessException e1) {
            return e1.getLocalizedMessage();
        }
        catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        return "Operacao efetuada com Sucesso!";

    }
    
    public void delay(int pos, int usec) {
        int i = 0;
        ProtocolStack ps=channel.getProtocolStack();
        for (Address add : channel.getView().getMembers()) {
            System.out.printf(" addr: %s \n", add.toString() );
            if (add.equals(channel.getAddress()) && i == pos) {
                System.out.printf("I am the delayed one %s\n",  channel.getAddressAsString());
                DELAY delay=new DELAY();
                delay.setInDelay(usec);
                //delay.setOutDelay(usec);

                try {
                    ps.insertProtocol(delay,ProtocolStack.ABOVE,SEQUENCER.class);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            i++;
        }
    }
    
    private void start() throws Exception {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("log4j.configurationFile","/log4j2.xml");
        channel = new JChannel();

        //name the element
        Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
        NetworkInterface iface = n.nextElement();
        Enumeration<InetAddress> inet = iface.getInetAddresses();
        channel.setName("server_" + inet.nextElement().getHostAddress());
       
        
        helper h = new helper(channel);
        h.setUpProtocolStack();


        channel.connect("show");
        
        /////////////// set delay
        ProtocolStack ps=channel.getProtocolStack();

        String delayedAddr = "server_192.168.85.105";
        if (delayedAddr.equals(channel.getAddress().toString())) {
            _delay = true;
            System.out.printf("I am the delayed one %s\n",  channel.getAddressAsString());
            DELAY delay=new DELAY();
            delay.setInDelay(5000);
            ps.insertProtocol(delay,ProtocolStack.ABOVE,PING.class);
        }

        /////////////// set delay
        
        dispatcher = new MessageDispatcher(channel, null, null, this);

        dao = new DbImplementation();

        dao.start();

        waitAction();
        channel.close();

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
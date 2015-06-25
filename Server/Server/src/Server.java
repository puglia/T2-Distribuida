import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.SQLException;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.View;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.protocols.DELAY;
import org.jgroups.protocols.SEQUENCER;
import org.jgroups.protocols.pbcast.NAKACK2;
import org.jgroups.stack.Protocol;
import org.jgroups.stack.ProtocolStack;
import org.jgroups.util.Util;

import com.dist.common.BusinessException;
import com.dist.common.Data;
import com.dist.common.Operation;
import com.dist.common.Seat;

public class Server implements RequestHandler {
    JChannel channel;
    MessageDispatcher dispatcher;
    String user_name = System.getProperty("user.name", "n/a");

    private DbInterface dao = null;

    public Object handle(Message msg) {
        Data data = null;
        try {
            data = (Data) Util.streamableFromByteBuffer(Data.class,
                    msg.getRawBuffer(), msg.getOffset(), msg.getLength());

            switch (data.getOperation().getCode()) {
            case 2:
                String seats = "";
                try {
                    seats = dao.getAvailableSeats(data.getMovie());
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                System.out.printf("Assentos para %s= %s \n", data.getMovie(),
                        seats);
                return "Assentos para" + data.getMovie() + " = " + seats;
            case 1:
            	System.out.printf("Receive data from %s, %s: %s-%d\n", data.getName(), data.getOperation().getName(), data.getReservedSeat().getRow(), data.getReservedSeat().getNumber());
                dao.insert(data.getName(), data.getMovie(),
                        data.getReservedSeat());
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
    
    private void start() throws Exception {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("log4j.configurationFile","/tmp/log4j2.xml");
       
        channel = new JChannel();
        // channel.setReceiver(this);
        
        
        
        //******** protocols definition
        System.out.print("protocol stack initialization\n");
        ProtocolStack ps=channel.getProtocolStack();
        SEQUENCER sequencer=new SEQUENCER();
        
        ps.insertProtocol(sequencer,ProtocolStack.ABOVE,NAKACK2.class);
        System.out.print("Insert Sequencer \n");
        DELAY delay=new DELAY();
        delay.setInDelay(1);
        delay.setOutDelay(1);
        ps.insertProtocol(delay,ProtocolStack.ABOVE,SEQUENCER.class);
        System.out.print("Insert delay \n");

        //***********  protocols definition
        
        for (Protocol i : ps.getProtocols()) {
        	System.out.printf("get protocol %s\n", i.getName());
        }
        
        channel.connect("bank");
        System.out.print("coordinator?\n");
        if (sequencer.isCoordinator()) {
        	System.out.print("coordinator\n");
        } else {
        	System.out.print("not the coordinator\n");
        }
        


        dispatcher = new MessageDispatcher(channel, null, null, this);

        dao = new DbInterface();

        dao.start();

        Data data = new Data("jurassic world", null, Operation.CONSULTAR);
        byte[] buf = Util.streamableToByteBuffer(data);
        this.handle(new Message(null, buf));
        data = new Data("jurassic world", "192.168.1.4", Operation.RESERVAR,
                new Seat(2, "A"));
        buf = Util.streamableToByteBuffer(data);
        this.handle(new Message(null, buf));
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
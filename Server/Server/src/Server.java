import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.SQLException;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.util.Util;

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

            System.out.printf("Receive data from %s \n", data.getName());

            switch (data.getOperation()) {
            case CONSULTAR:
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
            case RESERVAR:
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

        return "Operação efetuada com Sucesso!";
    }

    private void start() throws Exception {
        System.setProperty("java.net.preferIPv4Stack", "true");
        channel = new JChannel();
        // channel.setReceiver(this);
        channel.connect("bank");

        dispatcher = new MessageDispatcher(channel, null, null, this);

        dao = new DbInterface();

        dao.start();
        String accountName = "conta1";
        // if (!bank.accountExists(accountName)) {
        // bank.createAccount(accountName);
        // }
        // account.deposit(accountName, "Rafael", 1000);
        //
        // account.debit(accountName, "Thiago", 100);
        //
        // account.deposit(accountName, "Gianlucca", 500);
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
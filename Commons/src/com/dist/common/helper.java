package com.dist.common;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.ReceiverAdapter;
import org.jgroups.protocols.DELAY;
import org.jgroups.protocols.FRAG2;
import org.jgroups.protocols.MFC;
import org.jgroups.protocols.PING;
import org.jgroups.protocols.RSVP;
import org.jgroups.protocols.SEQUENCER;
import org.jgroups.protocols.UFC;
import org.jgroups.protocols.UNICAST3;
import org.jgroups.stack.Protocol;
import org.jgroups.stack.ProtocolStack;

public class helper  extends ReceiverAdapter {
    
    protected JChannel channel;

    public helper(JChannel channel) {
        this.channel = channel;
    }
    // deprecated
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
                        ps.insertProtocol(delay,ProtocolStack.ABOVE,PING.class);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                i++;
            }

    }

    public void setUpProtocolStack() throws Exception{
      //******** protocols definition
        System.out.print("protocol stack initialization\n");
        ProtocolStack ps=channel.getProtocolStack();
        SEQUENCER sequencer=new SEQUENCER();
        ps.insertProtocol(sequencer,ProtocolStack.ABOVE,UNICAST3.class);

        //***********  protocols definition
        
        for (Protocol i : ps.getProtocols()) {
            System.out.printf("get protocol %s\n", i.getName());
        }
      
        System.out.print("coordinator?\n");
        if (sequencer.isCoordinator()) {
            System.out.print("coordinator\n");
        } else {
            System.out.print("not the coordinator\n");
        }

    }
}

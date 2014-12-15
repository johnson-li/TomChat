package sample;

import io.netty.buffer.ByteBuf;
import net.tomp2p.peers.Number160;

/**
 * Created by johnson on 12/14/14.
 */
public interface MessageReceiver {
    boolean checkMine(Number160 peerID);

    void onReceived(ByteBuf request);
}

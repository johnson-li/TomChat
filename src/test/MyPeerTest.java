import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import sample.MyPeer;
import sample.Utils;

import java.net.InetAddress;
import java.util.Random;

import static org.junit.Assert.*;

public class MyPeerTest {
    Logger logger = LogManager.getLogger();

    @Test
    public void testInitPeer() throws Exception {
//        logger.debug(MyPeer.initPeer("johnson", "10.141.251.33"));
//        if (true) return;
        Random r = new Random(43L);
        PeerDHT peer = new PeerBuilderDHT(new PeerBuilder(new Number160(r)).ports(Utils.CLIENT_PORT).start()).start();
        PeerAddress bootStrapServer = new PeerAddress(Number160.ZERO, InetAddress.getByName("10.141.251.33"), Utils.TARGET_PORT, Utils.TARGET_PORT);
        FutureDiscover fd = peer.peer().discover().peerAddress(bootStrapServer).start();
        System.out.println("About to wait...");
        fd.awaitUninterruptibly();
        if (fd.isSuccess()) {
            System.out.println("*** FOUND THAT MY OUTSIDE ADDRESS IS " + fd.peerAddress());
        } else {
            System.out.println("*** FAILED " + fd.failedReason());
        }
        bootStrapServer = fd.reporter();
        FutureBootstrap bootstrap = peer.peer().bootstrap().peerAddress(bootStrapServer).start();
        bootstrap.awaitUninterruptibly();
        assertTrue(bootstrap.isSuccess());
        logger.debug(peer.peer().peerID());
        logger.debug(peer.peer().peerAddress());
    }
}
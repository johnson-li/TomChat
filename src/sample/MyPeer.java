package sample;

import net.tomp2p.connection.Bindings;
import net.tomp2p.connection.PeerConnection;
import net.tomp2p.dht.*;
import net.tomp2p.futures.*;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;

/**
 * Created by johnson on 11/27/14.
 */
public class MyPeer {
    static Logger logger = LogManager.getLogger();
    static PeerDHT clientPeerDHT;
    static Bindings bindings = new Bindings();
    static NeighborPeers neighborPeers = new NeighborPeers();
    static BaseFutureAdapter<FutureGet> messageGetListener;

    public static void initMessageGetListener(BaseFutureAdapter<FutureGet> listener) {
        messageGetListener = listener;
    }

    public static boolean initPeer(String str, String ip) {
        PeerAddress serverPeerAddress;
        try {
            bindings.addAddress(InetAddress.getByName(ip));
            clientPeerDHT = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(str)).ports(Utils.CLIENT_PORT).behindFirewall().start()).start();
            serverPeerAddress = new PeerAddress(Number160.ZERO, InetAddress.getByName(ip), Utils.TARGET_PORT, Utils.TARGET_PORT);
        }
        catch (Exception e) {
            logger.catching(e);
            return false;
        }
        FutureDiscover futureDiscover = clientPeerDHT.peer().discover().peerAddress(serverPeerAddress).start();
        futureDiscover.awaitUninterruptibly();
        if (futureDiscover.isSuccess())
            logger.info("*** FOUND THAT MY OUTSIDE ADDRESS IS " + futureDiscover.peerAddress());
        else
            logger.warn("*** FAILED " + futureDiscover.failedReason());
        serverPeerAddress = futureDiscover.reporter();

        FutureBootstrap futureBootstrap = clientPeerDHT.peer().bootstrap().peerAddress(serverPeerAddress).start();
        futureBootstrap.awaitUninterruptibly();
        if (futureBootstrap.isSuccess()) {
            for (PeerAddress p: futureBootstrap.bootstrapTo())
                logger.info("Bootstrapped to: " + p);
            neighborPeers.addPeer(getIdentification(serverPeerAddress.peerId()));
            initPeerListener();
            return true;
        }
        else {
            logger.error(futureBootstrap.failedReason());
            logout();
            return false;
        }
    }

    static void initPeerListener() {
        FutureGet futureGet = clientPeerDHT.get(clientPeerDHT.peerID()).start();
        if (messageGetListener == null) initMessageGetListener();
        futureGet.addListener(messageGetListener);
    }

    public static PeerAddress getPeerAddress() {
        return clientPeerDHT.peerAddress();
    }

    static String getIdentification(Number160 peerID) {
        return "server";
    }

    public static boolean checkOnLine(Number160 number160) {
        return true;
    }

    public static void logout() {
        clientPeerDHT.shutdown();
        neighborPeers.clear();
    }

    static void initMessageGetListener() {
        initMessageGetListener(new BaseFutureAdapter<FutureGet>() {
            @Override
            public void operationComplete(FutureGet future) throws Exception {
                logger.info("received message: " + future.data());
            }
        });
    }

    public static void addPeer(String peerName) {
        neighborPeers.put(Number160.createHash(peerName), peerName);
    }
}

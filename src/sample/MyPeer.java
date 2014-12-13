package sample;

import net.tomp2p.connection.Bindings;
import net.tomp2p.connection.PeerConnection;
import net.tomp2p.dht.*;
import net.tomp2p.futures.*;
import net.tomp2p.nat.FutureNAT;
import net.tomp2p.nat.FutureRelayNAT;
import net.tomp2p.nat.PeerNAT;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
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



    public static boolean initPeer(String str, String ip) {
        PeerAddress serverPeerAddress;
        try {
            bindings.addAddress(InetAddress.getByName(ip));
            clientPeerDHT = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(str)).ports(Utils.CLIENT_PORT).start()).start();
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
        else {
            logger.warn("*** FAILED " + futureDiscover.failedReason());
            PeerNAT peerNAT = new PeerNAT(clientPeerDHT.peer(), null, null, null, 0, 0, 0, false);
            FutureNAT futureNAT = peerNAT.startSetupPortforwarding(futureDiscover);
            futureNAT.awaitUninterruptibly();
            if (futureNAT.isSuccess()) {
                logger.info("future nat success");
            }
            else {
                logger.warn(futureNAT.failedReason());
                FutureRelayNAT futureRelayNAT = peerNAT.startRelay(futureDiscover, futureNAT);
                futureRelayNAT.awaitUninterruptibly();
                if (futureRelayNAT.isSuccess()) {
                    logger.info("future relay nat success");
                }
                else {
                    logger.error(futureRelayNAT.failedReason());
                }
            }
        }
        serverPeerAddress = futureDiscover.reporter();

        FutureBootstrap futureBootstrap = clientPeerDHT.peer().bootstrap().peerAddress(serverPeerAddress).start();
        futureBootstrap.awaitUninterruptibly();
        if (futureBootstrap.isSuccess()) {
            for (PeerAddress p: futureBootstrap.bootstrapTo())
                logger.info("Bootstrapped to: " + p);
            neighborPeers.addPeer(getIdentification(serverPeerAddress.peerId()));
            initPeerListener();

            clientPeerDHT.peer().objectDataReply(new ObjectDataReply() {
                @Override
                public Object reply(PeerAddress sender, Object request) throws Exception {
                    logger.debug("received message from " + sender);
                    return "I received!";
                }
            });

            return true;
        }
        else {
            logger.error(futureBootstrap.failedReason());
            logout();
            return false;
        }
    }

    static void initPeerListener() {
        FutureGet futureGet = clientPeerDHT.get(clientPeerDHT.peerID()).all().start();
        if (messageGetListener == null) initMessageGetListener();
        futureGet.addListener(messageGetListener);
        new Thread() {
            @Override
            public void run() {
                for (;;) {
                    FutureGet futureGet = clientPeerDHT.get(clientPeerDHT.peerID()).all().start();
                    futureGet.awaitUninterruptibly();
                    Iterator<Data> iterator = futureGet.dataMap().values().iterator();
                    while (iterator.hasNext()) {
                        Data data = iterator.next();
                        logger.debug(data);
                    }
                    try {
                        Thread.sleep(500);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
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

    public static void initMessageGetListener(BaseFutureAdapter<FutureGet> listener) {
        messageGetListener = listener;
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
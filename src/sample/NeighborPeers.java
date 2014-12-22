package sample;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.peers.Number160;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by johnson on 12/6/14.
 */
public class NeighborPeers extends HashMap<Number160, String>{

    static Logger logger = LogManager.getLogger();
    HashSet<Number160> activePeers = new HashSet<Number160>();
    HashMap<Number160, Profile> profileHashMap = new HashMap<Number160, Profile>();

    public void addPeer(Number160 number160) {

    }

    public void addPeer(String identification) {
        Number160 peerID = Number160.createHash(identification);
        put(peerID, identification);
    }

    public boolean isActive(Number160 n) {
        return activePeers.contains(n);
    }

    public void setActive(Number160 n) {
        activePeers.add(n);
    }

    public void updateProfile(Number160 peerID) {
        FutureGet futureGet = MyPeer.clientPeerDHT.get(peerID).all().start();
        futureGet.awaitUninterruptibly();
        if (futureGet.isSuccess()) {
            try {
                Profile profile = (Profile)futureGet.data().object();
                profileHashMap.put(peerID, profile);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            logger.warn("no peer profile found: " + peerID);
        }
    }

    public void updateProfile(String peerName) {
        updateProfile(Number160.createHash(peerName));
    }

    public Profile getProfile(Number160 peerID) {
        return profileHashMap.get(peerID);
    }

    public Profile getProfile(String peerName) {
        return getProfile(Number160.createHash(peerName));
    }

    public void downloadHeadPic(Number160 number160, Number160 peerID) {
        FutureGet futureGet = MyPeer.clientPeerDHT.get(number160).all().start();
        futureGet.awaitUninterruptibly();
        if (futureGet.isSuccess()) {
            try {
                ByteBuffer byteBuffer = (ByteBuffer)futureGet.data().object();
                Path headPic = Paths.get(Utils.HEAD_PIC_PATH, peerID.toString());
                FileChannel fileChannel = FileChannel.open(headPic, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                fileChannel.read(byteBuffer);
                fileChannel.close();
                logger.info("download head pic succeeded: " + peerID);
            }
            catch (Exception e) {
                logger.catching(e);
            }
        }
    }
}

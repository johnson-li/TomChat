package sample;

import net.tomp2p.peers.Number160;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by johnson on 12/6/14.
 */
public class NeighborPeers extends HashMap<Number160, String>{

    HashSet<Number160> activePeers = new HashSet<Number160>();

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
}

package sample;

import net.tomp2p.peers.Number160;

import java.io.Serializable;

/**
 * Created by johnson on 12/19/14.
 */
public class Profile implements Serializable{
    Number160 headPic = Number160.ZERO;
    String city = "";
    int age = -1;
    String description = "";
}

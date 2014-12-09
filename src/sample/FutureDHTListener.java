package sample;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.futures.BaseFutureAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by johnson on 12/6/14.
 */
public abstract class FutureDHTListener extends BaseFutureAdapter<FutureGet> {
    static Logger logger = LogManager.getLogger();

    @Override
    public void exceptionCaught(Throwable t) throws Exception {
        logger.catching(t);
    }

}
package org.aksw.concurrency;

import org.aksw.index.IndexManager;
import org.apache.log4j.Logger;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class ShutdownThread extends Thread {

    Logger log = Logger.getLogger(ShutdownThread.class);
    
    /**
     * 
     */
    @Override public void run() {

        System.out.println("Received signal to shut down! Good bye!");
        this.log.info("Received signal to shut down! Good bye!");
        IndexManager.getInstance().closeIndex();
    }
}

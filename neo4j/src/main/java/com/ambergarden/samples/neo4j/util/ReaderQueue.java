package com.ambergarden.samples.neo4j.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: YKDZ5901703
 * Date: 2017/7/24
 * Time: 16:15
 * To change this template use File | Settings | File Templates.
 */
public class ReaderQueue implements Callable {
    private BlockingQueue queue;

    public Object call() throws Exception {
        if (queue != null) {
            return queue.poll();
        }
        return null;
    }


    public ReaderQueue(BlockingQueue queue) {
        this.queue = queue;
    }
}

package com.szty.h5xinfa;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Ly
 */
public class SingleThreadHandler {

    private ExecutorService executorService;
    private SingleThreadHandler(){
        if (executorService == null) {
            executorService = Executors.newSingleThreadExecutor();
        }
    }

    private static SingleThreadHandler singleThreadHandler;
    public static SingleThreadHandler getInstance(){
        if (singleThreadHandler == null) {
            synchronized (SingleThreadHandler.class){
                if (singleThreadHandler == null) {
                    singleThreadHandler = new SingleThreadHandler();
                }
            }
        }
        return singleThreadHandler;
    }


    public void handleSingleThread(Runnable runnable){
        executorService.execute(runnable);
    }
    public void shutdownRnnable(){
        executorService.shutdownNow();
    }

}

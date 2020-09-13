package com.llthunder.csdn;

import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Hello world!
 *
 */
public class AutoRefreshCSDN {
    public static void main( String[] args ) throws InterruptedException {
        String url = "https://blog.csdn.net/weixin_32820639";
        AutoRefreshService service = new AutoRefreshService(url);
        service.doRefresh(60L);
    }
}

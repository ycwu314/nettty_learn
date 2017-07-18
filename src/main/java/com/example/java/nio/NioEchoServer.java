package com.example.java.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by Administrator on 2017/7/18 0018.
 */
@Slf4j
public class NioEchoServer {

    public static final int PORT = 5400;

    public static void main(String[] args) throws IOException {
        ExecutorService acceptorPool = Executors.newCachedThreadPool();
        ExecutorService handlerPool = Executors.newCachedThreadPool();

        try (Selector selector = Selector.open();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        ) {
            log.info("server started");
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(PORT));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (selector.select() > 0) {
                Set<SelectionKey> selectionKeysSet = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeysSet.iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    if (selectionKey.isAcceptable()) {
                        log.info("found acceptable");
                        ServerSocketChannel ssc = (ServerSocketChannel) selectionKey.channel();
                        acceptorPool.submit(new NioEchoServerAcceptor(ssc.accept(), handlerPool));
                    }
                }
            }
        }
    }
}
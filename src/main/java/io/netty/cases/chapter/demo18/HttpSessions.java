package io.netty.cases.chapter.demo18;

import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HttpSessions {
    public static Map<String, NioSocketChannel> channelMap = new ConcurrentHashMap<>();
}

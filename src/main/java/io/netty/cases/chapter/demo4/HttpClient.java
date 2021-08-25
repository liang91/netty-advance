package io.netty.cases.chapter.demo4;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.concurrent.DefaultPromise;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

public class HttpClient {
    HttpClientHandler handler = new HttpClientHandler();
    private Channel channel;
    private static final byte[] msg = "hello I'm client".getBytes(StandardCharsets.UTF_8);

    public static void main(String[] args) throws Exception {
        HttpClient client = new HttpClient();
        client.connect();
        String url = "http://127.0.0.1/user?id=10&addr=NanJing";
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1,
                HttpMethod.GET,
                url,
                Unpooled.wrappedBuffer(msg)
        );
        client.blockSend(request);
    }

    private void connect() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup(1);
        Bootstrap b = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new HttpClientCodec());
                        pipeline.addLast(new HttpObjectAggregator(Short.MAX_VALUE));
                        pipeline.addLast(handler);
                    }
                });
        ChannelFuture f = b.connect(HttpServer.host, HttpServer.port).sync();
        channel = f.channel();
    }

    private void blockSend(FullHttpRequest request) throws InterruptedException, ExecutionException {
        DefaultPromise<HttpResponse> respPromise = new DefaultPromise<>(channel.eventLoop());
        handler.setRespPromise(respPromise);

        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
        channel.writeAndFlush(request);
        HttpResponse response = respPromise.get();
        if (response != null)
            System.out.print("server send:" + new String(response.body()));
    }
}

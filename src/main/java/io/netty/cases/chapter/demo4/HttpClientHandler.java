/**
 *
 */
package io.netty.cases.chapter.demo4;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.nio.charset.StandardCharsets;

public class HttpClientHandler extends SimpleChannelInboundHandler<FullHttpResponse> {
    private static final byte[] msg = "hello I'm client".getBytes(StandardCharsets.UTF_8);
    private static FullHttpRequest genReq() {
        DefaultFullHttpRequest req = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1,
                HttpMethod.GET,
                "http://127.0.0.1/user?id=10&addr=NanJing",
                ByteBufAllocator.DEFAULT.buffer().writeBytes(msg));
        req.headers().set(HttpHeaderNames.CONTENT_LENGTH, req.content().readableBytes());
        return req;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws InterruptedException {
        ctx.writeAndFlush(genReq()).sync();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
        if (msg.decoderResult().isFailure())
            throw new Exception("Decode HttpResponse error: " + msg.decoderResult().cause());
        ctx.writeAndFlush(genReq()).sync();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

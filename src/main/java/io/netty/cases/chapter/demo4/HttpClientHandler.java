/**
 *
 */
package io.netty.cases.chapter.demo4;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.concurrent.DefaultPromise;

public class HttpClientHandler extends SimpleChannelInboundHandler<FullHttpResponse> {
    DefaultPromise<HttpResponse> respPromise;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
        if (msg.decoderResult().isFailure())
            throw new Exception("Decode HttpResponse error: " + msg.decoderResult().cause());
        HttpResponse response = new HttpResponse(msg);
        respPromise.setSuccess(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    public void setRespPromise(DefaultPromise<HttpResponse> respPromise) {
        this.respPromise = respPromise;
    }
}

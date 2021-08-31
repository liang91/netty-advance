/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.cases.chapter.demo18;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handles a server-side channel.
 */
@ChannelHandler.Sharable
public class DiscardServerHandler extends SimpleChannelInboundHandler<Object> {
    static AtomicInteger counter = new AtomicInteger();

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) {
        // discard
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt == SslHandshakeCompletionEvent.SUCCESS) {
            System.out.println(counter.incrementAndGet());
            HttpSessions.channelMap.put(ctx.channel().id().toString(), (NioSocketChannel) ctx.channel());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

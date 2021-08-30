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
package io.netty.cases.chapter.demo16;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Sharable
public class TrafficShapingServerHandler extends ChannelInboundHandlerAdapter {
    private final ScheduledExecutorService es = Executors.newSingleThreadScheduledExecutor();
    private final AtomicInteger counter = new AtomicInteger(0);
    private Channel channel;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        channel = ctx.channel();
        es.scheduleAtFixedRate(() -> System.out.println("channel:" + channel + " read speed:[" + counter.getAndSet(0) + " bytes/s]"), 0, 1, TimeUnit.SECONDS);
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        String body = (String) msg;
        counter.addAndGet(body.getBytes().length);
        ByteBuf echo = ctx.alloc().buffer().writeBytes(body.getBytes());
        ctx.writeAndFlush(echo);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        es.shutdown();
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

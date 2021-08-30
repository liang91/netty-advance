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
package io.netty.cases.chapter.demo13;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.SingleThreadEventExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ServiceTraceServerHandlerV2 extends ChannelInboundHandlerAdapter {
    private static final AtomicInteger totalSendBytes = new AtomicInteger(0);
    private static volatile EventExecutorGroup executorGroup = null;
    private static volatile List<Channel> channels = new ArrayList<>();

    static {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            System.out.println("server write speed:" + totalSendBytes.getAndSet(0) + " bytes/s");
            if (executorGroup != null) {
                for (EventExecutor eventExecutor : executorGroup) {
                    SingleThreadEventExecutor executor = (SingleThreadEventExecutor) eventExecutor;
                    System.out.println(executor + " queued task:" + executor.pendingTasks());
                }
            }
            for (Channel channel : channels) {
                System.out.println(channel + " pending byte:" + channel.unsafe().outboundBuffer().totalPendingWriteBytes());
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        if (executorGroup == null) {
            executorGroup = ctx.executor().parent();
        }
        channels.add(ctx.channel());
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        int sendBytes = ((ByteBuf) msg).readableBytes();
        ChannelFuture writeFuture = ctx.write(msg);
        writeFuture.addListener((f) -> totalSendBytes.getAndAdd(sendBytes));
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

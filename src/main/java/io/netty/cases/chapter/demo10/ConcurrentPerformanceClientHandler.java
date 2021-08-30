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
package io.netty.cases.chapter.demo10;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentPerformanceClientHandler extends ChannelInboundHandlerAdapter {
    private static final AtomicInteger sender = new AtomicInteger();
    private static final AtomicInteger receiver = new AtomicInteger();
    private static final ArrayList<Long> times = new ArrayList<>();

    static {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            synchronized (times) {
                long max = 0, min = Integer.MAX_VALUE;
                for (Long time : times) {
                    if (max < time)
                        max = time;
                    if (min > time)
                        min = time;
                }
                long average = (long) times.stream().mapToLong(l -> l).average().getAsDouble();
                times.clear();
                System.out.printf("send:%d receive:%d average:%d max:%d min:%d\n", sender.getAndSet(0), receiver.getAndSet(0), average / 1000000, max / 1000000, min / 1000000);
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.executor().scheduleAtFixedRate(() -> {
            for (int i = 0; i < 45; i++) {
                if (ctx.channel().isWritable()) {
                    sender.incrementAndGet();
                    ByteBuf data = ctx.alloc().buffer();
                    data.writeLong(System.nanoTime());
                    ctx.writeAndFlush(data);
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        receiver.incrementAndGet();
        ByteBuf req = (ByteBuf) msg;
        synchronized (times) {
            times.add(System.nanoTime() - req.readLong());
        }
        ReferenceCountUtil.release(req);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

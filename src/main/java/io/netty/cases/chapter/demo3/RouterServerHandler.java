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
package io.netty.cases.chapter.demo3;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class RouterServerHandler extends ChannelInboundHandlerAdapter {
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final ScheduledExecutorService counterService = Executors.newSingleThreadScheduledExecutor();
    private static final AtomicInteger counter = new AtomicInteger();
    private static final Object lock = new Object();
    static {
        counterService.scheduleAtFixedRate(() -> {
            int count = counter.get();
            System.out.println(count);
            counter.set(0);

        }, 0, 1, TimeUnit.SECONDS);
    }
    private static final byte[] msg = "I'm Fine\n".getBytes(StandardCharsets.UTF_8);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ReferenceCountUtil.release(msg);
        if (counter.incrementAndGet() > 100000) {
            synchronized (lock) {
                System.out.println("entered");
                if (counter.get() > 100000) {
                    System.out.println("closed");
                    counter.set(0);
                    ctx.close();
                    return;
                }
            }
        }
        executorService.execute(() -> ctx.writeAndFlush(Unpooled.buffer(16).writeBytes(RouterServerHandler.msg)));
//        executorService.execute(() -> ctx.writeAndFlush(ctx.alloc().buffer(16).writeBytes(RouterServerHandler.msg)));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

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
package io.netty.cases.chapter.demo8;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class IotCarsDiscardServerHandler extends ChannelInboundHandlerAdapter {
    private final static AtomicInteger active = new AtomicInteger();
    private final static AtomicInteger flow = new AtomicInteger(0);
    private final static AtomicInteger flowTotal = new AtomicInteger(0);
    private final static ScheduledExecutorService counter = Executors.newSingleThreadScheduledExecutor();

    static {
        counter.scheduleAtFixedRate(() -> {
            int flowPerSecond = flow.get();
            flowTotal.addAndGet(flowPerSecond);
            System.out.printf("active:%d flow:%d flowTotal:%d\n", active.get(), flowPerSecond, flowTotal.get());
            flow.set(0);
        }, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        active.incrementAndGet();
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        flow.incrementAndGet();
        ReferenceCountUtil.release(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

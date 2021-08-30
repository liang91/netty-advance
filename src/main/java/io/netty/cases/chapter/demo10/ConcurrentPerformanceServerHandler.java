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

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentPerformanceServerHandler extends ChannelInboundHandlerAdapter {
    private static final AtomicInteger qps = new AtomicInteger();
    private static final AtomicInteger totalReq = new AtomicInteger();
    private static final AtomicInteger processSpeed = new AtomicInteger();
    private static final AtomicInteger processFinished = new AtomicInteger();

    static {
        Executors.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(() -> System.out.println(qps.getAndSet(0) + " " + processSpeed.getAndSet(0)), 0, 1, TimeUnit.SECONDS);
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        qps.incrementAndGet();
        ByteBuf req = (ByteBuf) msg;
        try {
            TimeUnit.MILLISECONDS.sleep(10);
            processSpeed.incrementAndGet();
            ctx.writeAndFlush(req);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private static String serviceStats() {
        int qps = ConcurrentPerformanceServerHandler.qps.getAndSet(0);
        int totalReqNum = totalReq.addAndGet(qps);
        int speed = processSpeed.getAndSet(0);
        int processed = processFinished.addAndGet(speed);
        int todo = totalReqNum - processed;
        return String.format("qps:%d todo:%d total:%d processed:%d speed:%d", qps, todo, totalReqNum, processed, speed);
    }
}

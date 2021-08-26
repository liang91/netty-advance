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
package io.netty.cases.chapter.demo6;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ApiGatewayServerHandler extends ChannelInboundHandlerAdapter {
    private static final AtomicInteger flow = new AtomicInteger();
    private static final AtomicInteger callerRun = new AtomicInteger();
    private static final ScheduledExecutorService counter = Executors.newSingleThreadScheduledExecutor();
    private static final ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(
            48,
            48,
            0,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(1000000),
            r -> {
                Thread t = new Thread(r);
                t.setPriority(Thread.MAX_PRIORITY);
                return t;
            },
            new ThreadPoolExecutor.AbortPolicy()
    );

    static {
        counter.scheduleAtFixedRate(()->{
            Map<String, Long> taskQueueStats = getTaskQueueStats();
            System.out.printf("flow:%d total:%d todo:%d completed:%d active:%d caller:%d\n", flow.get(), taskQueueStats.get("total"), taskQueueStats.get("todo"), taskQueueStats.get("completed"), taskQueueStats.get("active"), callerRun.get());
            flow.set(0);
        }, 0, 1, TimeUnit.SECONDS);
    }

    private static Map<String, Long> getTaskQueueStats() {
        long total = poolExecutor.getTaskCount();
        long completed = poolExecutor.getCompletedTaskCount();
        long active = poolExecutor.getActiveCount();
        long todo = total - completed - active - callerRun.get();
        Map<String, Long> res = new HashMap<>();
        res.put("total", total);
        res.put("completed", completed);
        res.put("active", active);
        res.put("todo", todo);
        return res;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        flow.incrementAndGet();
        ByteBuf req = (ByteBuf) msg;
        ByteBuf data = ctx.alloc().heapBuffer(req.readableBytes() + 1);
        poolExecutor.execute(() -> {
            sleep(1);
            data.writeByte(1);
            ReferenceCountUtil.release(data);
            if (Thread.currentThread().getName().contains("nio")) {
                callerRun.incrementAndGet();
            }
        });
        ctx.write(msg);
    }

//    public void channelRead(ChannelHandlerContext ctx, Object msg) {
//        ctx.write(msg);
//        char[] req = new char[((ByteBuf) msg).readableBytes()];
//        executorService.execute(() ->
//        {
//            char[] dispatchReq = req;
//            try
//            {
//                TimeUnit.MICROSECONDS.sleep(500);
//            }catch (Exception e)
//            {
//                e.printStackTrace();
//            }
//        });
//    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private void sleep(int milliseconds) {
        try {
            TimeUnit.MICROSECONDS.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RouterServerHandlerV2 extends SimpleChannelInboundHandler<ByteBuf> {
    private static ExecutorService executorService = Executors.newSingleThreadExecutor();
    private PooledByteBufAllocator allocator = new PooledByteBufAllocator(false);

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        byte[] body = new byte[msg.readableBytes()];
        executorService.execute(() -> {
            ByteBuf respMsg = allocator.heapBuffer(body.length);
            respMsg.writeBytes(body);
            ctx.writeAndFlush(respMsg);
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

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

public class RouterClientHandler extends ChannelInboundHandlerAdapter {
    private static final byte[] msg = "are you ok\n".getBytes(StandardCharsets.UTF_8);

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println(ctx.alloc().getClass().getSimpleName() + "-" + ctx.alloc().isDirectBufferPooled());
        ctx.writeAndFlush(ctx.alloc().buffer(16).writeBytes(RouterClientHandler.msg));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ReferenceCountUtil.release(msg);
        ctx.writeAndFlush(Unpooled.buffer(16).writeBytes(RouterClientHandler.msg));
//        ctx.writeAndFlush(ctx.alloc().buffer(16).writeBytes(RouterClientHandler.msg));
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

package com.wentry.netty.handle;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author WJX
 * @title: SimpleDuplexHanlder
 * @projectName architecture-parent
 * @description: TODO
 * @date 2020/6/10 0010
 */
@ChannelHandler.Sharable
public class SimpleDuplexHandler extends ChannelDuplexHandler {

    @Override
    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        // 作为客户端时调用
        super.bind(ctx, localAddress, promise);
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        super.connect(ctx, remoteAddress, localAddress, promise);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        super.close(ctx, promise);
    }

    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        System.out.println("outbound read.");
        super.read(ctx);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        System.out.println("outbound write:"+msg);
        super.write(ctx, msg, promise);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) {
        ctx.executor().schedule(() -> {
            ctx.channel().write("handler has been added to pipeline.");
            ctx.write("hello world");
        }, 3, TimeUnit.SECONDS);
    }
}

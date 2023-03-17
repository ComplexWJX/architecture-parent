package com.wentry.netty.client;

import com.wentry.netty.handle.SimpleDuplexHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * [一句话描述类功能]
 *
 * @author rukawa
 * Created on 2023/03/17 12:28 by rukawa
 */
public class NettyClient {

    public static void main(String[] args) {
        Channel channel = new NettyClient().connect("localhost", 1887);

        channel.writeAndFlush("i have connected to server.");
    }

    private Logger logger = LoggerFactory.getLogger(NettyClient.class);

    public Channel connect(final String host, final int port) {
        //创建事件处理
        SimpleDuplexHandler duplexHandler = new SimpleDuplexHandler();
        //创建EventLoopGroup
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        //创建ServerBootstrap
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(bossGroup)
                .channel(NioSocketChannel.class) //指定所使用的NIO传输Channel
                //.localAddress(new InetSocketAddress(port)) //指定端口设置套接字地址
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast("duplexHandler", duplexHandler);
                    }
                });
        ChannelFuture f = bootstrap.connect(host, port);
        try {
            //异步地绑定服务器
            f.sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // Bind a shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        Future<?> bossWaiter = bossGroup.shutdownGracefully();
                        logger.info("系统关闭");
                        logger.info("Waiting for worker and boss event loop groups to terminate...");
                        try {
                            bossWaiter.await(10, TimeUnit.SECONDS);
                        } catch (InterruptedException iex) {
                            logger.warn("An InterruptedException was caught while waiting for event loops to terminate...");
                        }
                    })
            );
        }

        return f.channel();
    }
}

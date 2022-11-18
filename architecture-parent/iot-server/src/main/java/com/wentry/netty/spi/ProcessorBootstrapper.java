package com.wentry.netty.spi;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import com.wentry.netty.codec.Decoder;
import com.wentry.netty.handle.EchoServerBaseHandler;

import com.wentry.netty.handle.SimpleDuplexHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author WJX
 * @date 2020-06-09
 * 1.Channel---->Socket
 * 2.EventLoop--->控制流、多线程处理、并发
 * 3.ChannelFuture--->异步通知
 */
public class ProcessorBootstrapper {

    private Logger logger = LoggerFactory.getLogger(ProcessorBootstrapper.class);

    public void start(final String host, final int port) {
        //创建事件处理
        EchoServerBaseHandler customHandler = new EchoServerBaseHandler();
        Decoder decoder = new Decoder();
        SimpleDuplexHandler duplexHandler = new SimpleDuplexHandler();
        //创建EventLoopGroup
        EventLoopGroup boss_group = new NioEventLoopGroup();
        EventLoopGroup work_group = new NioEventLoopGroup();
        //创建ServerBootstrap
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(boss_group, work_group)
                .channel(NioServerSocketChannel.class) //指定所使用的NIO传输Channel
                //.localAddress(new InetSocketAddress(port)) //指定端口设置套接字地址
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addFirst("httpRequestDecoder", new HttpRequestDecoder())
                                .addLast("decoder",decoder)
//                                .addLast(new HttpServerCodec())//http服务解码
                                .addLast(new HttpObjectAggregator(2048))
                                .addLast(new HttpResponseEncoder())
                                .addLast("echoServerHandler", customHandler)
                                .addLast("duplexHandler", duplexHandler);
                    }
                }).childOption(ChannelOption.SO_KEEPALIVE, true);
        try {
            ChannelFuture f = bootstrap.bind(host, port);
            //异步地绑定服务器
            f.sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // Bind a shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        Future<?> bossWaiter = boss_group.shutdownGracefully();
                        Future<?> workerWaiter = work_group.shutdownGracefully();
                        logger.info("系统关闭");
                        logger.info("Waiting for worker and boss event loop groups to terminate...");
                        try {
                            workerWaiter.await(10, TimeUnit.SECONDS);
                            bossWaiter.await(10, TimeUnit.SECONDS);
                        } catch (InterruptedException iex) {
                            logger.warn("An InterruptedException was caught while waiting for event loops to terminate...");
                        }
                    })
            );
        }
    }
}

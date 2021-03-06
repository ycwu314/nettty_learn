package com.example.netty.simpleserver;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by Administrator on 2017/7/11 0011.
 */
@Slf4j
public class TimeClientUsingPojo {

    public static void main(String[] args) {
        int port = 7000;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup).channel(NioSocketChannel.class).handler(
                new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                            .addLast(new TimeDecoderUsingPojo(), new TimeClientHandlerUsingPojo());
                    }
                }).option(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future = bootstrap.connect("localhost", port).sync();
            log.info("client starts");

            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        } finally {
            workerGroup.shutdownGracefully();
        }

    }


    static class TimeDecoderUsingPojo extends ByteToMessageDecoder {

        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
            throws Exception {
            if (in.readableBytes() >= 4) {
                out.add(new UnixTime(in.readUnsignedInt()));
            }
        }
    }

    @Slf4j
    static class TimeClientHandlerUsingPojo extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            UnixTime unixTime = (UnixTime) msg;
            log.info("{}", unixTime);
            ctx.close();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }
    }
}

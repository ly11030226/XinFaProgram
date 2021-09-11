package com.szty.h5xinfa.baoao;

import android.content.Intent;
import android.util.Log;

import com.szty.h5xinfa.MyApplication;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.GenericFutureListener;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static com.szty.h5xinfa.baoao.ToolsKt.ACTION_RESULT;
import static com.szty.h5xinfa.baoao.ToolsKt.HEX_02;
import static com.szty.h5xinfa.baoao.ToolsKt.HEX_21;
import static com.szty.h5xinfa.baoao.ToolsKt.HEX_55;
import static com.szty.h5xinfa.baoao.ToolsKt.KEY_RESULT;

/**
 * @author Ly
 */
public class NettyClient {
    private static final String TAG = "NettyClient";
    //是否连接
    private volatile boolean isConnect = false;
    private NioEventLoopGroup group;
    private Bootstrap bootstrap;
    private Channel channel;

    private NettyClient() {
    }

    private static NettyClient instance;
    private static int mWinNum = 1;

    public static NettyClient getInstance() {
        if (instance == null) {
            synchronized (NettyClient.class) {
                if (instance == null) {
                    instance = new NettyClient();
                }
            }
        }
        return instance;
    }

    public void setWinNumber(int winNum) {
        mWinNum = winNum;
    }

    /**
     * 连接
     *
     * @return
     */
    public @NonNull Observable connect(final String ip, final String port) {
        return Observable.create(new ObservableOnSubscribe() {

            @Override
            public void subscribe(@NonNull final ObservableEmitter emitter) throws Throwable {
                group = new NioEventLoopGroup();
                bootstrap = new Bootstrap().group(group).remoteAddress(ip, Integer.parseInt(port)).channel(NioSocketChannel.class).option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000).handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new LoggingHandler(LogLevel.INFO));
                        /*byte类型编解码*/
                        pipeline.addLast(new ByteArrayDecoder());
                        pipeline.addLast(new ByteArrayEncoder());
                        /*string类型编解码*/
                        //                        pipeline.addLast(new StringDecoder(Charset.forName("UTF-8")));
                        //                        pipeline.addLast(new StringEncoder(Charset.forName("UTF-8")));
                        pipeline.addLast(new ChannelHandle());
                    }
                });
                try {
                    channel = bootstrap.connect().sync().channel();
                    emitter.onNext(channel.isActive());
                    isConnect = channel.isActive();
                    Log.i(TAG, "connect: isConnect ... " + isConnect);
                } catch (Exception e) {
                    emitter.onNext(false);
                    isConnect = false;
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /*发送命令*/
    public @NonNull Observable sendOrder(final String order) {
        return Observable.create((ObservableOnSubscribe) emitter -> {
            Log.i(TAG, "sendOrder: isConnect ... " + isConnect);
            if (isConnect) {
                channel.writeAndFlush(order).addListener((GenericFutureListener) future -> {
                    emitter.onNext(future.isSuccess());
                });
            } else {
                emitter.onNext(false);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /*是否连接*/
    private Boolean isConnect() {
        return isConnect;
    }

    /*重连*/
    private Observable<Boolean> reConnect(String ip, String port) {
        disConnect();
        return connect(ip, port);
    }


    /*关闭连接*/
    public void disConnect() {
        isConnect = false;
        group.shutdownGracefully();
    }

    class ChannelHandle extends SimpleChannelInboundHandler<String> {
        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            super.channelInactive(ctx);
            Log.e(TAG, "channelInactive 连接失败");
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            super.userEventTriggered(ctx, evt);
            if (evt instanceof IdleStateEvent) {
                if (((IdleStateEvent) evt).state() == IdleState.WRITER_IDLE) {
                    Log.d(TAG, "userEventTriggered write idle");
                } else if (((IdleStateEvent) evt).state() == IdleState.READER_IDLE) {
                    Log.d(TAG, "userEventTriggered read idle");
                    ctx.channel().close();
                }
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
            Log.i(TAG, "exceptionCaught");
            cause.printStackTrace();
            ctx.close();
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            //处理接收到的数据data
            Log.i(TAG, "channelRead0: msg ... " + msg);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            super.channelRead(ctx, msg);
            try {
                byte[] bytes = (byte[]) msg;
                if (bytes.length > 10) {
                    //接收包头
                    byte buffer_temp1 = bytes[0];
                    if (buffer_temp1 == HEX_55) {
                        //接收包头
                        byte buffer_temp2 = bytes[1];
                        if (buffer_temp2 == HEX_02) {
                            //接收地址和长度
                            byte buffer_temp3 = bytes[3];
                            if (buffer_temp3 == mWinNum) {
                                //定义一个剩余数据的数组长度
                                int datarest_len = bytes[5] - 6;
                                //如果是更新显示的命令
                                if (bytes[9] == HEX_21) {
                                    //创建一个显示内容长度的数组
                                    byte[] data = new byte[datarest_len - 11];
                                    //将datarest数组中的内容数据拷贝到data数组中
                                    System.arraycopy(bytes, 15, data, 0, datarest_len - 11);
                                    //把data数组转换成string
                                    String result = new String(data, "GBK");
                                    Intent i = new Intent(ACTION_RESULT);
                                    i.putExtra(KEY_RESULT, result);
                                    LocalBroadcastManager.getInstance(MyApplication.instance).sendBroadcast(i);
                                    Log.i(TAG, "channelRead: result ... " + result);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}

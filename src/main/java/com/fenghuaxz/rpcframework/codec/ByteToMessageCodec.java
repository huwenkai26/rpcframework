package com.fenghuaxz.rpcframework.codec;

import com.fenghuaxz.rpcframework.pojo.XRequest;
import com.fenghuaxz.rpcframework.pojo.XResponse;
import com.fenghuaxz.rpcframework.protostuff.GraphIOUtil;
import com.fenghuaxz.rpcframework.protostuff.LinkedBuffer;
import com.fenghuaxz.rpcframework.protostuff.Schema;
import com.fenghuaxz.rpcframework.protostuff.runtime.RuntimeSchema;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.UnsupportedMessageTypeException;

import java.util.List;

public class ByteToMessageCodec extends io.netty.handler.codec.ByteToMessageCodec<Object> {

    public static final Object PING = new XPing();
    private static final Class<?>[] mPojos = new Class[]{XPing.class, XRequest.class, XResponse.class};

    private static class XPing {
    }

    @Override
    protected final void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {

        int magic = -1;

        for (Class<?> pojo : mPojos) {
            magic++;
            if (pojo == msg.getClass()) {
                if (pojo == XPing.class) {
                    out.writeByte(magic);
                    return;
                }
                byte[] data = writeBefore(serialize(msg));
                out.writeByte(magic);
                out.writeInt(data.length);
                out.writeBytes(data);
                return;
            }
        }
        throw new UnsupportedMessageTypeException(msg.getClass().getName());
    }

    @Override
    protected final void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        msg.markReaderIndex();

        final byte magic = msg.readByte();
        if (magic < 0 || magic > mPojos.length - 1) {
            ctx.close();
            return;
        }

        final Class<?> cls;
        if ((cls = mPojos[magic]) == XPing.class) {
            out.add(PING);
            return;
        }

        if (msg.readableBytes() < 4) {
            msg.resetReaderIndex();
            return;
        }

        final int len = msg.readInt();

        if (msg.readableBytes() < len) {
            msg.resetReaderIndex();
            return;
        }

        final byte[] data;
        msg.readBytes(data = new byte[len]);
        out.add(deserialize(parseBefore(data), cls));
    }

    protected byte[] writeBefore(byte[] data) throws Exception {
        return data;
    }

    protected byte[] parseBefore(byte[] data) throws Exception {
        return data;
    }

    @SuppressWarnings("unchecked")
    protected <T> byte[] serialize(T obj) throws Exception {
        Schema<T> schema = (Schema<T>) RuntimeSchema.getSchema(obj.getClass());
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            return GraphIOUtil.toByteArray(obj, schema, buffer);
        } finally {
            buffer.clear();
        }
    }

    protected <T> T deserialize(byte[] data, Class<T> cls) throws Exception {
        T message;
        Schema<T> schema = RuntimeSchema.getSchema(cls);
        GraphIOUtil.mergeFrom(data, message = schema.newMessage(), schema);
        return message;
    }
}

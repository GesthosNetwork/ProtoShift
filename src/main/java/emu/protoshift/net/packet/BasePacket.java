package emu.protoshift.net.packet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import com.google.protobuf.GeneratedMessageV3;
import emu.protoshift.net.newproto.PacketHeadOuterClass.PacketHead;

public class BasePacket {
    private static final int const1 = 17767;
    private static final int const2 = -30293;
    private final PacketOpcodes opcode;
    private byte[] header;
    private byte[] data;
    private boolean useDispatchKey;
    public boolean shouldEncrypt = true;

    public BasePacket(byte[] header,PacketOpcodes opcode) {
        this.header = header;this.opcode = opcode;
    }

    public PacketOpcodes getOpcode() {
        return this.opcode;
    }

    public boolean useDispatchKey() {
        return this.useDispatchKey;
    }

    public void setUseDispatchKey(boolean useDispatchKey) {
        this.useDispatchKey = useDispatchKey;
    }

    public byte[] getHeader() {
        return this.header;
    }

    public byte[] getData() {
        return this.data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setData(GeneratedMessageV3 proto) {
        this.data = proto.toByteArray();
    }

    @SuppressWarnings("rawtypes")
    public void setData(GeneratedMessageV3.Builder proto) {
        this.data = proto.build().toByteArray();
    }

    public byte[] build() {
        if (this.getHeader() == null) {
            this.header = new byte[0];
        }
        if (this.getData() == null) {
            this.data = new byte[0];
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream(2 + 2 + 2 + 4 + this.getHeader().length + this.getData().length + 2);
        this.writeUint16(baos, const1);
        this.writeUint16(baos, this.opcode.value);
        this.writeUint16(baos, this.header.length);
        this.writeUint32(baos, this.data.length);
        this.writeBytes(baos, this.header);
        this.writeBytes(baos, this.data);
        this.writeUint16(baos, const2);
        return baos.toByteArray();
    }

    public void writeUint16(ByteArrayOutputStream baos, int i) {
        baos.write((byte) ((i >>> 8) & 0xFF));
        baos.write((byte) (i & 0xFF));
    }

    public void writeUint32(ByteArrayOutputStream baos, int i) {
        baos.write((byte) ((i >>> 24) & 0xFF));
        baos.write((byte) ((i >>> 16) & 0xFF));
        baos.write((byte) ((i >>> 8) & 0xFF));
        baos.write((byte) (i & 0xFF));
    }

    public void writeBytes(ByteArrayOutputStream baos, byte[] bytes) {
        try {
            baos.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
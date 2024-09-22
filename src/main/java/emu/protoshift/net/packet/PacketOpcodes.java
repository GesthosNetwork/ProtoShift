package emu.protoshift.net.packet;

public class PacketOpcodes {
    public int value;
    public int type;

    public PacketOpcodes(int value, int type) {
        this.value = value;
        this.type = type;
    }

    public static final int NONE = 0;

    public static class NewOpcodes {
        // Auto template new opcodes
    }

    public static class OldOpcodes {
        // Auto template old opcodes
    }
}

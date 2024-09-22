package emu.protoshift.server.game;

import emu.protoshift.net.packet.PacketOpcodes;
import lombok.Getter;

@Getter
public class Handler {
    private final GameSession session;
    private final PacketOpcodes opcode;
    private final byte[] header;
    private final byte[] payload;
    private final long timeMills;

    public Handler(GameSession session , PacketOpcodes opcode, byte[] header, byte[] payload, long timeMills) {
        this.session = session;
        this.opcode = opcode;
        this.header = header;
        this.payload = payload;
        this.timeMills = timeMills;
    }
}

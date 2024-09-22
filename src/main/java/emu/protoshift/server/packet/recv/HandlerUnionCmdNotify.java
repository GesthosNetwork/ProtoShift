
package emu.protoshift.server.packet.recv;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import emu.protoshift.net.packet.BasePacket;
import emu.protoshift.net.packet.Opcodes;
import emu.protoshift.net.packet.PacketHandler;
import emu.protoshift.net.packet.PacketOpcodes;
import emu.protoshift.server.game.GameSession;

@Opcodes(value = PacketOpcodes.newOpcodes.UnionCmdNotify, type = 1)
public class HandlerUnionCmdNotify extends PacketHandler {
    public static class Packet extends BasePacket {

        public Packet(byte[] header, emu.protoshift.net.newproto.UnionCmdNotifyOuterClass.UnionCmdNotify req) {
            super(header, new PacketOpcodes(PacketOpcodes.oldOpcodes.UnionCmdNotify, 2));

            var q = emu.protoshift.net.oldproto.UnionCmdNotifyOuterClass.UnionCmdNotify.newBuilder();
            try{
                String json = JsonFormat.printer().print(req);
                JsonFormat.parser().ignoringUnknownFields().merge(json, q);
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
            }

            this.setData(q.build());
        }
    }
    @Override
    public BasePacket Packet(byte[] payload) throws Exception {
        return new Packet(new byte[0], emu.protoshift.net.newproto.UnionCmdNotifyOuterClass.UnionCmdNotify.parseFrom(payload));
    }
    @Override
    public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        emu.protoshift.net.newproto.UnionCmdNotifyOuterClass.UnionCmdNotify req = emu.protoshift.net.newproto.UnionCmdNotifyOuterClass.UnionCmdNotify.parseFrom(payload);
        // Auto template
        session.send(new Packet(header, req));
    }

}

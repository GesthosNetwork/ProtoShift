
package emu.protoshift.server.packet.send;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import emu.protoshift.ProtoShift;
import emu.protoshift.net.packet.BasePacket;
import emu.protoshift.net.packet.Opcodes;
import emu.protoshift.net.packet.PacketHandler;
import emu.protoshift.net.packet.PacketOpcodes;
import emu.protoshift.server.game.GameSession;

import emu.protoshift.utils.MT19937;
import emu.protoshift.utils.Crypto;
import emu.protoshift.utils.Utils;

import javax.crypto.Cipher;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.HashMap;

@Opcodes(value = PacketOpcodes.oldOpcodes.GetPlayerTokenRsp, type = 2)
public class HandlerGetPlayerTokenRsp extends PacketHandler {
    public static class Packet extends BasePacket {

        public Packet(byte[] header, emu.protoshift.net.oldproto.GetPlayerTokenRspOuterClass.GetPlayerTokenRsp rsp) {
            super(header, new PacketOpcodes(PacketOpcodes.newOpcodes.GetPlayerTokenRsp,1));

            var p = emu.protoshift.net.newproto.GetPlayerTokenRspOuterClass.GetPlayerTokenRsp.newBuilder();;
            try{
                String json = JsonFormat.printer().print(rsp);
                JsonFormat.parser().ignoringUnknownFields().merge(json, p);
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
            }

            this.setUseDispatchKey(true);
            this.setData(p.build());
        }
    }
    @Override
    public BasePacket Packet(byte[] payload) throws Exception {
        return new Packet(new byte[0], emu.protoshift.net.oldproto.GetPlayerTokenRspOuterClass.GetPlayerTokenRsp.parseFrom(payload));
    }
    @Override
    public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        var rsp = emu.protoshift.net.oldproto.GetPlayerTokenRspOuterClass.GetPlayerTokenRsp.parseFrom(payload);

        long encrypt_seed;
        if ((encrypt_seed = rsp.getSecretKeySeed()) == 0) {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, Crypto.getPriKey(rsp.getKeyId()));
            byte[] seed_bytes = cipher.doFinal(Utils.base64Decode(rsp.getServerRandKey()));
            encrypt_seed = ByteBuffer.wrap(seed_bytes).getLong() ^ session.getClientSeed();
        }

        var encrypt_key = MT19937.generateKey(encrypt_seed);

        session.setEncryptKey(encrypt_key);

        // Auto template
        session.send(new Packet(header, rsp));
    }

}

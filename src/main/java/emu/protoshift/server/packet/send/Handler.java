package emu.protoshift.server.packet.send;

import emu.protoshift.net.packet.BasePacket;
import emu.protoshift.net.packet.Opcodes;
import emu.protoshift.net.packet.PacketHandler;
import emu.protoshift.net.packet.PacketOpcodes;
import emu.protoshift.server.game.GameSession;


@Opcodes(value = PacketOpcodes.NONE,type = 2)
public class Handler extends PacketHandler {
	public static class Packet extends BasePacket {

		public Packet() {
			super(new byte[0], new PacketOpcodes(PacketOpcodes.NONE, 1));

		}
	}
	@Override
	public BasePacket Packet(byte[] payload) throws Exception {
		return new Packet();
	}
	@Override
	public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
		// Auto template
		session.send(new Packet());
	}

}

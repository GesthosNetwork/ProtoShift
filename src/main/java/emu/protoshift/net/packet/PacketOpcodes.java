package emu.protoshift.net.packet;

public class PacketOpcodes {
public int value;
public int type;

	public PacketOpcodes(int value, int type) {
		this.value = value;
		this.type = type;
	}

	public static final int NONE = 0;

	public static class newOpcodes{
	public static final int AbilityChangeNotify = 1127;
	}

	public static class oldOpcodes{
	public static final int AbilityChangeNotify = 1131;
	}
	}
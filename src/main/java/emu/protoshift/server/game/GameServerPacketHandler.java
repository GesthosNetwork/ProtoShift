package emu.protoshift.server.game;

import emu.protoshift.ProtoShift;
import emu.protoshift.net.packet.Opcodes;
import emu.protoshift.net.packet.PacketHandler;
import emu.protoshift.net.packet.PacketOpcodes;
import emu.protoshift.net.packet.PacketOpcodesUtil;
import emu.protoshift.server.game.GameSession.SessionState;
import org.reflections.Reflections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static emu.protoshift.utils.Utils.bytesToHex;

public class GameServerPacketHandler {
    private final Map<Integer, PacketHandler> newHandlers = new HashMap<>();
    private final Map<Integer, PacketHandler> oldHandlers = new HashMap<>();

    public GameServerPacketHandler() {
        this.registerHandlers();
    }

    public void registerPacketHandler(Class<? extends PacketHandler> handlerClass) {
        try {
            Opcodes opcode = handlerClass.getAnnotation(Opcodes.class);

            if (opcode == null || opcode.disabled() || opcode.value() <= 0) {
                return;
            }

            PacketHandler packetHandler = handlerClass.getDeclaredConstructor().newInstance();

            if (opcode.type() == 1) {
                this.newHandlers.put(opcode.value(), packetHandler);
            } else {
                this.oldHandlers.put(opcode.value(), packetHandler);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void registerHandlers() {
        Reflections reflections = new Reflections("emu.protoshift.server.packet");
        Set<Class<? extends PacketHandler>> handlerClasses = reflections.getSubTypesOf(PacketHandler.class);

        for (var obj : handlerClasses) {
            this.registerPacketHandler(obj);
        }

        ProtoShift.getLogger().info("Registered newHandlers " + this.newHandlers.size() + " " + PacketHandler.class.getSimpleName() + "s");
        ProtoShift.getLogger().info("Registered oldHandlers " + this.oldHandlers.size() + " " + PacketHandler.class.getSimpleName() + "s");
    }

    public PacketHandler getHandler(PacketOpcodes opcode) {
        return opcode.type == 1 ? this.newHandlers.get(opcode.value) : this.oldHandlers.get(opcode.value);
    }

    public void handle(GameSession session, PacketOpcodes opcode, byte[] header, byte[] payload, HandlerManager handlers) {
        PacketHandler handler = (opcode.type == 1 ? this.newHandlers.get(opcode.value) : this.oldHandlers.get(opcode.value));

        ProtoShift.getLogger().info("Recv packet (" + opcode.value + ", " + opcode.type + "): " + PacketOpcodesUtil.getOpcodeName(opcode) + "\n"
                + bytesToHex(payload));


        if (opcode.value == PacketOpcodes.NONE) return;

        if (handler != null) {
            try {
                SessionState state = session.getState();

                if ((opcode.type == 1 && opcode.value == PacketOpcodes.newOpcodes.PingReq) || (opcode.type == 2 && opcode.value == PacketOpcodes.oldOpcodes.PingRsp)) {
                } else if (opcode.type == 1 && opcode.value == PacketOpcodes.newOpcodes.GetPlayerTokenReq) {
                    if (state != SessionState.WAITING_FOR_TOKEN) return;
                } else if (opcode.type == 2 && opcode.value == PacketOpcodes.oldOpcodes.GetPlayerTokenRsp) {
                    if (state != SessionState.WAITING_FOR_TOKEN) return;
                    session.setUseSecretKey(true);
                    session.setState(GameSession.SessionState.ACTIVE);
                } else {
                    if (state != SessionState.ACTIVE) return;
                }

                long time = System.currentTimeMillis();

                if (handlers == null
                        || (opcode.type == 1 && opcode.value == PacketOpcodes.newOpcodes.GetPlayerTokenReq)
                        || (opcode.type == 2 && opcode.value == PacketOpcodes.oldOpcodes.GetPlayerTokenRsp)
                        || (opcode.type == 1 && opcode.value == PacketOpcodes.newOpcodes.SetPlayerBornDataReq)
                        || (opcode.type == 2 && opcode.value == PacketOpcodes.oldOpcodes.SetPlayerBornDataRsp)
                        || (opcode.type == 1 && opcode.value == PacketOpcodes.newOpcodes.PlayerLoginReq)
                        || (opcode.type == 2 && opcode.value == PacketOpcodes.newOpcodes.PlayerLoginRsp)) {
                    handler.handle(session, header, payload);
                } else {
                    if (handlers.getSession() == session) {
                        handlers.getHandler().add(new Handler(session, opcode, header, payload, time));
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return;
        }

        ProtoShift.getLogger().

                info("Unhandled packet (" + opcode.value + ", " + opcode.type + "): " + PacketOpcodesUtil.getOpcodeName(opcode));

    }

    public void handle(GameSession session, PacketOpcodes opcode, byte[] header, byte[] payload) {
        this.handle(session, opcode, header, payload, null);
    }
}

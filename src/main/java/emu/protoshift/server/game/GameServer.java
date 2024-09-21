package emu.protoshift.server.game;

import emu.protoshift.ProtoShift;
import emu.protoshift.net.packet.PacketHandler;
import kcp.highway.ChannelConfig;
import kcp.highway.KcpServer;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import static emu.protoshift.Configuration.GAME_INFO;
import static emu.protoshift.utils.Language.translate;

public final class GameServer extends KcpServer {
    private final InetSocketAddress address;
    private final GameServerPacketHandler packetHandler;

    private static InetSocketAddress getAdapterInetSocketAddress() {
        InetSocketAddress inetSocketAddress;
        if (GAME_INFO.bindAddress.equals("")) {
            inetSocketAddress = new InetSocketAddress(GAME_INFO.bindPort);
        } else {
            inetSocketAddress = new InetSocketAddress(
                    GAME_INFO.bindAddress,
                    GAME_INFO.bindPort
            );
        }
        return inetSocketAddress;
    }

    public GameServer() {
        this(getAdapterInetSocketAddress());
    }

    public GameServer(InetSocketAddress address) {
        ChannelConfig channelConfig = new ChannelConfig();
        var interval = ProtoShift.getConfig().server.game.kcpConfig.interval;
        var resend = ProtoShift.getConfig().server.game.kcpConfig.resend;
        var ackNoDelay = ProtoShift.getConfig().server.game.kcpConfig.ackNoDelay;
        var fastFlush = ProtoShift.getConfig().server.game.kcpConfig.fastFlush;
        var useConvChannel = ProtoShift.getConfig().server.game.kcpConfig.useConvChannel;
        var conv = ProtoShift.getConfig().server.game.kcpConfig.conv;
        var multiply = ProtoShift.getConfig().server.game.kcpConfig.multiply;

        channelConfig.nodelay(true, interval, resend, true);
        channelConfig.setMtu(1400);
        channelConfig.setSndwnd(256);
        channelConfig.setRcvwnd(256);
        channelConfig.setTimeoutMillis(30 * 1000);
        channelConfig.setUseConvChannel(true);
        channelConfig.setAckNoDelay(ackNoDelay);
        channelConfig.setFastFlush(fastFlush);
        channelConfig.setUseConvChannel(useConvChannel);
        channelConfig.setConv(conv);

        this.init(GameSessionManager.getListener(), channelConfig, address);
        this.address = address;
        this.packetHandler = new GameServerPacketHandler();

        Runtime.getRuntime().addShutdownHook(new Thread(this::onServerShutdown));
    }

    public GameServerPacketHandler getPacketHandler() {
        return packetHandler;
    }

    public void start() {
        ProtoShift.getLogger().info(translate("messages.game.port_bind", Integer.toString(address.getPort())));
    }

    public void onServerShutdown() {

        List<GameSession> list = new ArrayList<>(GameSessionManager.getSessions().size());
        list.addAll(GameSessionManager.getSessions().values());

        for (GameSession session : list) {
            session.getSession().close();
        }
    }
}

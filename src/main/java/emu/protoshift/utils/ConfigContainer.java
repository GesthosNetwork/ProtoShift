package emu.protoshift.utils;

import com.google.gson.JsonObject;
import emu.protoshift.ProtoShift;
import emu.protoshift.ProtoShift.ServerDebugMode;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Locale;

import static emu.protoshift.ProtoShift.config;

public class ConfigContainer {
    private static int version() {
        return 3;
    }
    public static void updateConfig() {
        try {
            JsonObject configObject = ProtoShift.getGsonFactory()
                    .fromJson(new FileReader(ProtoShift.configFile), JsonObject.class);
            if (!configObject.has("version")) {
                ProtoShift.getLogger().info("Updating legacy ..");
                ProtoShift.saveConfig(null);
            }
        } catch (Exception ignored) {
        }

        var existing = config.version;
        var latest = version();

        if (existing == latest)
            return;

        ConfigContainer updated = new ConfigContainer();
        Field[] fields = ConfigContainer.class.getDeclaredFields();
        Arrays.stream(fields).forEach(field -> {
            try {
                field.set(updated, field.get(config));
            } catch (Exception exception) {
                ProtoShift.getLogger().error("Failed to update a configuration field.", exception);
            }
        });
        updated.version = version();

        try {
            ProtoShift.saveConfig(updated);
            ProtoShift.loadConfig();
        } catch (Exception exception) {
            ProtoShift.getLogger().warn("Failed to inject the updated ", exception);
        }
    }

    public Language language = new Language();
    public Server server = new Server();
    public int version = version();

    public static class Server {
        public ServerDebugMode debugLevel = ServerDebugMode.NONE;
        public Game game = new Game();
    }

    public static class Language {
        public Locale language = Locale.getDefault();
        public Locale fallback = Locale.US;
    }

    public static class Game {
        public String bindAddress = "0.0.0.0";
        public int bindPort = 21081;
        public KCPConfig kcpConfig = new KCPConfig();
        public boolean tickWhenHandlePacket = true;
        public int tick = 0;
        public String remoteAddress = "172.10.3.2";
        public int remotePort = 21081;
        public boolean enableConsole = true;
    }

    public static class KCPConfig {
        public int interval = 20;
        public int resend = 2;
        public boolean ackNoDelay = true;
        public boolean fastFlush = true;
        public boolean isPoolAllocator = true;
        public boolean isHeapAllocator = false;
        public boolean useConvChannel = true;
        public long conv = 1L;
        public int multiply = 2;
    }
}

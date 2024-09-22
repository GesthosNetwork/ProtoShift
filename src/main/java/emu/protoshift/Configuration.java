package emu.protoshift;

import emu.protoshift.utils.ConfigContainer;
import java.util.Locale;
import static emu.protoshift.ProtoShift.config;

public final class Configuration extends ConfigContainer {
    public static final Locale FALLBACK_LANGUAGE = config.language.fallback;
    public static final Server SERVER = config.server;
    public static final Game GAME_INFO = config.server.game;
}

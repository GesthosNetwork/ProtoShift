package emu.protoshift;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import emu.protoshift.server.game.GameServer;
import emu.protoshift.utils.ConfigContainer;
import emu.protoshift.utils.Crypto;
import emu.protoshift.utils.Language;
import emu.protoshift.utils.Utils;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Nullable;
import java.io.*;

import static emu.protoshift.utils.Language.translate;

public final class ProtoShift {
	private static final Logger log = LoggerFactory.getLogger(ProtoShift.class);
	private static LineReader consoleLineReader = null;
	
	private static Language language;

	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	public static final File configFile = new File("./config.json");

	private static GameServer gameServer;

	public static ConfigContainer config;

	static {
		System.setProperty("logback.configurationFile", "src/main/resources/logback.xml");
		ProtoShift.loadConfig();
		ConfigContainer.updateConfig();
		ProtoShift.loadLanguage();
		Utils.startupCheck();
	}

  	public static void main(String[] args) throws Exception {
		Crypto.loadKeys();
		boolean exitEarly = false;
		for (String arg : args) {
			switch (arg.toLowerCase()) {
				case "-version" -> {
					System.out.println("ProtoShift version: " + BuildConfig.VERSION + "-" + BuildConfig.GIT_HASH); exitEarly = true;
				}
			}
		} 
		
		if(exitEarly) System.exit(0);
	
		ProtoShift.getLogger().info(translate("messages.status.starting"));
		ProtoShift.getLogger().info(translate("messages.status.game_version", GameConstants.VERSION));
		ProtoShift.getLogger().info(translate("messages.status.version", BuildConfig.VERSION, BuildConfig.GIT_HASH));
	
		gameServer = new GameServer();
		gameServer.start();
		Runtime.getRuntime().addShutdownHook(new Thread(ProtoShift::onShutdown));
		startConsole();
 	}

	private static void onShutdown() {
	}

	public static void loadLanguage() {
		var locale = config.language.language;
		language = Language.getLanguage(Utils.getLanguageCode(locale));
	}

	public static void loadConfig() {
		if (!configFile.exists()) {
			getLogger().info("config.json could not be found. Generating a default configuration ...");
			config = new ConfigContainer();
			ProtoShift.saveConfig(config);
			return;
		} 

		try (FileReader file = new FileReader(configFile)) {
			config = gson.fromJson(file, ConfigContainer.class);
		} catch (Exception exception) {
			getLogger().error("There was an error while trying to load the configuration from config.json. Please make sure that there are no syntax errors. If you want to start with a default configuration, delete your existing config.json.");
			System.exit(1);
		} 
	}

	public static void saveConfig(@Nullable ConfigContainer config) {
		if(config == null) config = new ConfigContainer();
		
		try (FileWriter file = new FileWriter(configFile)) {
			file.write(gson.toJson(config));
		} catch (IOException ignored) {
			ProtoShift.getLogger().error("Unable to write to config file.");
		} catch (Exception e) {
			ProtoShift.getLogger().error("Unable to save config file.", e);
		}
	}

	public static ConfigContainer getConfig() {
		return config;
	}

	public static Language getLanguage() {
		return language;
	}

	public static Language getLanguage(String langCode) {
        return Language.getLanguage(langCode);
	}

	public static Logger getLogger() {
		return log;
	}

	public static LineReader getConsole() {
		if (consoleLineReader == null) {
			Terminal terminal = null;
			try {
				terminal = TerminalBuilder.builder().jna(true).build();
			} catch (Exception e) {
				try {
					terminal = TerminalBuilder.builder().dumb(true).build();
				} catch (Exception ignored) {
					}
			}
			consoleLineReader = LineReaderBuilder.builder()
					.terminal(terminal)
					.build();
		}
		return consoleLineReader;
	}

	public static Gson getGsonFactory() {
		return gson;
	}

	public static GameServer getGameServer() {
		return gameServer;
	}

	public static void startConsole() {
		getConsole();
		getLogger().info(translate("messages.status.done"));
		boolean isLastInterrupted = false;
		while (config.server.game.enableConsole) {
			try {
				consoleLineReader.readLine("> ");
			} catch (UserInterruptException e) {
				if (!isLastInterrupted) {
					isLastInterrupted = true;
					ProtoShift.getLogger().info("Press Ctrl-C again to shutdown.");
					continue;
				} else {
					Runtime.getRuntime().exit(0);
				}
			} catch (EndOfFileException e) {
				ProtoShift.getLogger().info("EOF detected.");
				continue;
			} catch (IOError e) {
				ProtoShift.getLogger().error("An IO error occurred.", e);
				continue;
			}

			isLastInterrupted = false;
		}
	}


	public enum ServerDebugMode {
		ALL, MISSING, NONE
	}
}

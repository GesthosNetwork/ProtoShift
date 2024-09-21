package emu.protoshift.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import emu.protoshift.ProtoShift;
import io.netty.buffer.ByteBuf;
import javax.annotation.Nullable;

@SuppressWarnings({"UnusedReturnValue", "BooleanMethodIsAlwaysInverted"})
public final class Utils {
	public static final Random random = new Random();
	
	public static int randomRange(int min, int max) {
		return random.nextInt(max - min + 1) + min;
	}

	private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();
	public static String bytesToHex(byte[] bytes) {
		if (bytes == null) return "";
	    char[] hexChars = new char[bytes.length * 2];
	    for (int j = 0; j < bytes.length; j++) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = HEX_ARRAY[v >>> 4];
	        hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	public static String bytesToHex(ByteBuf buf) {
	    return bytesToHex(byteBufToArray(buf));
	}
	
	public static byte[] byteBufToArray(ByteBuf buf) {
		byte[] bytes = new byte[buf.capacity()];
		buf.getBytes(0, bytes);
		return bytes;
	}

	public static String toFilePath(String path) {
		return path.replace("/", File.separator);
	}

	public static void startupCheck() {
		ConfigContainer config = ProtoShift.getConfig();
	}

	public static String readFromInputStream(@Nullable InputStream stream) {
		if(stream == null) return "empty";
		
		StringBuilder stringBuilder = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
			String line; while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
			} stream.close();
		} catch (IOException e) {
			ProtoShift.getLogger().warn("Failed to read from input stream.");
		} catch (NullPointerException ignored) {
			return "empty";
		} return stringBuilder.toString();
	}

	public static String getLanguageCode(Locale locale) {
		return String.format("%s-%s", locale.getLanguage(), locale.getCountry());
	}

	public static byte[] base64Decode(String toDecode) {
		return Base64.getDecoder().decode(toDecode);
	}


}

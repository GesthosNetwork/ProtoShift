package emu.protoshift.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import emu.protoshift.ProtoShift;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import static emu.protoshift.Configuration.*;

public final class Language {
    private static final Map<String, Language> cachedLanguages = new ConcurrentHashMap<>();
    
    private final JsonObject languageData;
    private final String languageCode;
    private final Map<String, String> cachedTranslations = new ConcurrentHashMap<>();

    public static Language getLanguage(String langCode) {
        if (cachedLanguages.containsKey(langCode)) {
            return cachedLanguages.get(langCode);
        }

        var fallbackLanguageCode = Utils.getLanguageCode(FALLBACK_LANGUAGE);
        var description = getLanguageFileDescription(langCode, fallbackLanguageCode);
        var actualLanguageCode = description.getLanguageCode();

        Language languageInst;
        if (description.getLanguageFile() != null) {
            languageInst = new Language(description);
            cachedLanguages.put(actualLanguageCode, languageInst);
        } else {
            languageInst = cachedLanguages.get(actualLanguageCode);
            cachedLanguages.put(langCode, languageInst);
        }

        return languageInst;
    }

    public static String translate(String key, Object... args) {
        String translated = ProtoShift.getLanguage().get(key);
        
        try {
            return translated.formatted(args);
        } catch (Exception exception) {
            ProtoShift.getLogger().error("Failed to format string: " + key, exception);
            return translated;
        }
    }

    public String getLanguageCode() {
        return languageCode;
    }

    private Language(LanguageStreamDescription description) {
        @Nullable JsonObject languageData = null;
        languageCode = description.getLanguageCode();
        
        try {
            languageData = ProtoShift.getGsonFactory().fromJson(Utils.readFromInputStream(description.getLanguageFile()), JsonObject.class);
        } catch (Exception exception) {
            ProtoShift.getLogger().warn("Failed to load language file: " + description.getLanguageCode(), exception);
        }
        
        this.languageData = languageData;
    }

    private static LanguageStreamDescription getLanguageFileDescription(String languageCode, String fallbackLanguageCode) {
        var fileName = languageCode + ".json";
        var fallback = fallbackLanguageCode + ".json";
        
        String actualLanguageCode = languageCode;
        InputStream file = ProtoShift.class.getResourceAsStream("/languages/" + fileName);

        if (file == null) {
            ProtoShift.getLogger().warn("Failed to load language file: " + fileName + ", falling back to: " + fallback);
            actualLanguageCode = fallbackLanguageCode;
            if (cachedLanguages.containsKey(actualLanguageCode)) {
                return new LanguageStreamDescription(actualLanguageCode, null);
            }
            
            file = ProtoShift.class.getResourceAsStream("/languages/" + fallback);
        }

        if(file == null) {
            ProtoShift.getLogger().warn("Failed to load language file: " + fallback + ", falling back to: en-US.json");
            actualLanguageCode = "en-US";
            if (cachedLanguages.containsKey(actualLanguageCode)) {
                return new LanguageStreamDescription(actualLanguageCode, null);
            }
            
            file = ProtoShift.class.getResourceAsStream("/languages/en-US.json");
        }

        if(file == null)
            throw new RuntimeException("Unable to load the primary, fallback, and 'en-US' language files.");

        return new LanguageStreamDescription(actualLanguageCode, file);
    }

    public String get(String key) {
        if(this.cachedTranslations.containsKey(key)) {
            return this.cachedTranslations.get(key);
        }
        
        String[] keys = key.split("\\.");
        JsonObject object = this.languageData;

        int index = 0;
        String valueNotFoundPattern = "";
        String result = valueNotFoundPattern + key;
        boolean isValueFound = false;

        while (true) {
            if(index == keys.length) break;
            
            String currentKey = keys[index++];
            if(object.has(currentKey)) {
                JsonElement element = object.get(currentKey);
                if(element.isJsonObject())
                    object = element.getAsJsonObject();
                else {
                    isValueFound = true;
                    result = element.getAsString(); break;
                }
            } else break;
        }

        if (!isValueFound && !languageCode.equals("en-US")) {
            var englishValue = ProtoShift.getLanguage("en-US").get(key);
            if (!englishValue.contains(valueNotFoundPattern)) {
                result += "\nhere is english version:\n" + englishValue;
            }
        }
        
        this.cachedTranslations.put(key, result); return result;
    }

    private static class LanguageStreamDescription {
        private final String languageCode;
        private final InputStream languageFile;

        public LanguageStreamDescription(String languageCode, InputStream languageFile) {
            this.languageCode = languageCode;
            this.languageFile = languageFile;
        }

        public String getLanguageCode() {
            return languageCode;
        }

        public InputStream getLanguageFile() {
            return languageFile;
        }
    }
}

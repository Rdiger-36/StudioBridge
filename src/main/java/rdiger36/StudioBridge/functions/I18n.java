package rdiger36.StudioBridge.functions;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The texts of the GUI in more than one language.
 *
 * <p>Every visible text of the GUI is looked up by a key, {@code I18n.t("menu.settings")},
 * in the table of the chosen language. A language is one file {@code /i18n/<code>.properties}
 * (UTF-8, key = text) whose {@code _name} entry is the language's name in itself. English is
 * the fallback for a key another language does not have yet, so a half translated table shows
 * English where it is missing rather than the key.</p>
 *
 * <p>Placeholders are written as {@code {name}} and filled in from name/value pairs:
 * {@code I18n.t("profile.delete", "name", profileName)}.</p>
 *
 * <p>What stays English is decided elsewhere: the output of the command line, which scripts
 * and the README rely on, and the content of the UDP package.</p>
 */
public class I18n {

    /** The setting value that follows the language of the operating system. */
    public static final String AUTO = "auto";

    /** The fallback language. */
    public static final String DEFAULT = "en";

    /**
     * Every language that ships, in the order of the language menu. Adding a file to
     * src/main/resources/i18n also means adding its code here; I18nTest checks that both match.
     */
    public static final List<String> LANGUAGES = Collections.unmodifiableList(List.of("en", "de"));

    private static final Map<String, Properties> tables = new LinkedHashMap<>();
    private static final Set<String> missingLogged = new HashSet<>();
    private static String current = DEFAULT;

    static {
        for (String code : LANGUAGES) {
            tables.put(code, load(code));
        }
    }

    private static Properties load(String code) {
        Properties table = new Properties();
        try (InputStream in = I18n.class.getResourceAsStream("/i18n/" + code + ".properties")) {
            if (in != null) {
                table.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            } else {
                System.err.println("Language file i18n/" + code + ".properties is missing.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return table;
    }

    /**
     * Chooses the language of the GUI from the setting.
     *
     * @param setting a language code, or {@link #AUTO} (also null or unknown) for the language of
     *                the operating system, falling back to English when it does not ship.
     */
    public static void setLanguage(String setting) {
        current = resolve(setting, Locale.getDefault().getLanguage());
    }

    /**
     * The language a setting stands for.
     *
     * @param setting the stored setting
     * @param systemLanguage the language code of the operating system
     */
    static String resolve(String setting, String systemLanguage) {
        if (setting != null && LANGUAGES.contains(setting)) return setting;
        if (systemLanguage != null && LANGUAGES.contains(systemLanguage)) return systemLanguage;
        return DEFAULT;
    }

    /** The code of the language the GUI is shown in. */
    public static String language() {
        return current;
    }

    /** The name a language gives itself, e.g. "Deutsch". */
    public static String name(String code) {
        Properties table = tables.get(code);
        return table == null ? code : table.getProperty("_name", code);
    }

    /** The keys of a language table, for the tests. */
    static Set<String> keys(String code) {
        Set<String> keys = new HashSet<>();
        Properties table = tables.get(code);
        if (table != null) keys.addAll(table.stringPropertyNames());
        return keys;
    }

    /** The raw text of a key in a given language, without fallback, or null. */
    public static String raw(String code, String key) {
        Properties table = tables.get(code);
        return table == null ? null : table.getProperty(key);
    }

    /**
     * The text for a key in the current language, with placeholders filled in.
     *
     * @param key the key, e.g. "menu.settings"
     * @param params name/value pairs for the {name} placeholders. A placeholder without a value
     *               is left as it is.
     * @return the text, the English text when the current language lacks the key, or the key
     *         itself when English lacks it too.
     */
    public static String t(String key, Object... params) {
        String text = tables.get(current).getProperty(key);
        if (text == null) text = tables.get(DEFAULT).getProperty(key);
        if (text == null) {
            if (missingLogged.add(key)) System.err.println("Missing translation key: " + key);
            return key;
        }
        for (int i = 0; i + 1 < params.length; i += 2) {
            text = text.replace("{" + params[i] + "}", String.valueOf(params[i + 1]));
        }
        return text;
    }

    /** The placeholders a text uses, for comparing two languages in the tests. */
    static List<String> placeholders(String text) {
        List<String> found = new ArrayList<>();
        Matcher m = Pattern.compile("\\{(\\w+)\\}").matcher(text == null ? "" : text);
        while (m.find()) {
            if (!found.contains(m.group(1))) found.add(m.group(1));
        }
        Collections.sort(found);
        return found;
    }
}

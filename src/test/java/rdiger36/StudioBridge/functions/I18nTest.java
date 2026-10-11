package rdiger36.StudioBridge.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.After;
import org.junit.Test;

/**
 * Holds every language file to the English keys and placeholders, so a translation can not
 * silently lose a text or a value it has to show.
 */
public class I18nTest {

    @After
    public void resetLanguage() {
        I18n.setLanguage(I18n.DEFAULT);
    }

    @Test
    public void everyLanguageFileIsListedAndEveryListedLanguageHasAFile() {
        File dir = new File("src/main/resources/i18n");
        List<String> files = new ArrayList<>();
        for (String name : dir.list()) {
            if (name.endsWith(".properties")) files.add(name.substring(0, name.length() - ".properties".length()));
        }
        assertEquals(new TreeSet<>(I18n.LANGUAGES), new TreeSet<>(files));
    }

    @Test
    public void everyLanguageHasEveryEnglishKeyWithTheSamePlaceholders() {
        Set<String> english = I18n.keys(I18n.DEFAULT);
        assertFalse(english.isEmpty());
        for (String code : I18n.LANGUAGES) {
            Set<String> keys = I18n.keys(code);
            Set<String> missing = new TreeSet<>(english);
            missing.removeAll(keys);
            assertEquals(code + " lacks keys", new TreeSet<String>(), missing);
            Set<String> extra = new TreeSet<>(keys);
            extra.removeAll(english);
            assertEquals(code + " has keys English does not", new TreeSet<String>(), extra);
            for (String key : english) {
                assertEquals(code + ": " + key, I18n.placeholders(I18n.raw(I18n.DEFAULT, key)), I18n.placeholders(I18n.raw(code, key)));
            }
        }
    }

    @Test
    public void everyLanguageNamesItself() {
        for (String code : I18n.LANGUAGES) {
            assertNotNull(code, I18n.raw(code, "_name"));
        }
        assertEquals("Deutsch", I18n.name("de"));
    }

    @Test
    public void resolvesTheSetting() {
        assertEquals("de", I18n.resolve("de", "en"));
        assertEquals("de", I18n.resolve(I18n.AUTO, "de"));
        assertEquals("en", I18n.resolve(I18n.AUTO, "fr"));
        assertEquals("en", I18n.resolve("xx", "fr"));
        assertEquals("en", I18n.resolve(null, null));
    }

    @Test
    public void fillsPlaceholdersAndFallsBack() {
        I18n.setLanguage("de");
        assertEquals("Achtung! Soll das Profil \"X1C\" gelöscht werden?", I18n.t("profile.delete", "name", "X1C"));
        I18n.setLanguage("en");
        assertEquals("Profile", I18n.t("main.profile"));
        assertEquals("no.such.key", I18n.t("no.such.key"));
        assertTrue(I18n.t("check.forIp", "ip", "192.168.1.5").endsWith("192.168.1.5"));
    }
}

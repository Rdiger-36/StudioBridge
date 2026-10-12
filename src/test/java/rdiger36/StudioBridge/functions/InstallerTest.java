package rdiger36.StudioBridge.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import rdiger36.StudioBridge.functions.Installer.Kind;

/**
 * Tests for picking and checking the release file of a package.
 */
public class InstallerTest {

    private static final String SHA = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

    private static JSONArray assets(String... names) {
        JSONArray assets = new JSONArray();
        for (String name : names) assets.put(new JSONObject().put("name", name));
        return assets;
    }

    @Test
    public void suffixMatchesTheFileNamesOfTheReleaseWorkflow() {
        assertEquals("_win_x86.msi", Installer.assetSuffix(Kind.MSI, "x86_64"));
        assertEquals("_win_arm64.msi", Installer.assetSuffix(Kind.MSI, "arm64"));
        assertEquals("-x86_64.AppImage", Installer.assetSuffix(Kind.APPIMAGE, "x86_64"));
        assertEquals("-aarch64.AppImage", Installer.assetSuffix(Kind.APPIMAGE, "arm64"));
        assertEquals("_macOS_arm64.dmg", Installer.assetSuffix(Kind.DMG, "arm64"));
    }

    @Test
    public void noSuffixWithoutAReleaseFileForTheArchitecture() {
        assertNull(Installer.assetSuffix(Kind.DMG, "x86_64"));
        assertNull(Installer.assetSuffix(Kind.MSI, "x86"));
    }

    @Test
    public void findsTheFileOfThePackage() {
        JSONArray assets = assets(
                "StudioBridge-2.3.0.jar",
                "StudioBridge-2.3.0_win_x86_NO-INSTALL.zip",
                "StudioBridge-2.3.0_win_x86.msi",
                "StudioBridge-2.3.0-aarch64.AppImage");
        assertEquals("StudioBridge-2.3.0_win_x86.msi", Installer.findAsset(assets, "_win_x86.msi").getString("name"));
        assertEquals("StudioBridge-2.3.0-aarch64.AppImage", Installer.findAsset(assets, "-aarch64.AppImage").getString("name"));
        assertNull(Installer.findAsset(assets, "_macOS_arm64.dmg"));
        assertNull(Installer.findAsset(assets, null));
        assertNull(Installer.findAsset(null, "_win_x86.msi"));
    }

    @Test
    public void readsTheSha256DigestOfGitHub() {
        assertEquals(SHA, Installer.sha256Of(new JSONObject().put("digest", "sha256:" + SHA)));
        assertEquals(SHA, Installer.sha256Of(new JSONObject().put("digest", "sha256:" + SHA.toUpperCase())));
    }

    @Test
    public void rejectsMissingOrOtherDigests() {
        assertNull(Installer.sha256Of(new JSONObject()));
        assertNull(Installer.sha256Of(new JSONObject().put("digest", JSONObject.NULL)));
        assertNull(Installer.sha256Of(new JSONObject().put("digest", "sha1:0123456789abcdef0123456789abcdef01234567")));
        assertNull(Installer.sha256Of(new JSONObject().put("digest", "sha256:1234")));
    }

    @Test
    public void writesLowerCaseHex() {
        assertEquals("00ff7f80", Installer.toHex(new byte[] { 0, (byte) 0xff, 0x7f, (byte) 0x80 }));
    }
}

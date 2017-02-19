package com.github.opticyclic.mkparser;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class MkParserTest {
    private MkParser mkParser;

    @BeforeClass
    public void setup() {
        mkParser = new MkParser();
    }

    @DataProvider(name = "product-lines")
    public static Object[][] productLines() {
        return new Object[][]{
                {"PRODUCT_MAKEFILES += $(LOCAL_DIR)/sdk_addon/ti_omap_addon.mk", "$(LOCAL_DIR)/sdk_addon/ti_omap_addon.mk"},
                {"PRODUCT_MAKEFILES := \\", ""},
                {"        $(LOCAL_DIR)/aosp_tuna.mk \\", "$(LOCAL_DIR)/aosp_tuna.mk"},
                {"        $(LOCAL_DIR)/full_tuna.mk", "$(LOCAL_DIR)/full_tuna.mk"}
        };
    }

    @Test(dataProvider = "product-lines")
    public void testGetMkFromLine(String line, String expectedFile) throws Exception {
        String mkFile = mkParser.getMkFromLine(line);
        Assert.assertEquals(mkFile, expectedFile);
    }

    @DataProvider(name = "inherited-lines")
    public static Object[][] inheritedLines() {
        return new Object[][]{
                {"$(call inherit-product-if-exists, vendor/ti/omap4/omap4-vendor.mk)", "vendor/ti/omap4/omap4-vendor.mk"},
                {"# $(call inherit-product-if-exists, vendor/ti/omap4/omap4-vendor.mk)", ""},
                {"$(call inherit-product, $(LOCAL_PATH)/qemu_base.mk)", "$(LOCAL_PATH)/qemu_base.mk"}
        };
    }

    @Test(dataProvider = "inherited-lines")
    public void testGetMkFromInheritedLines(String line, String expectedFile) throws Exception {
        String mkFile = mkParser.getMkFromInheritLine(line);
        Assert.assertEquals(mkFile, expectedFile);
    }

    @Test
    public void testGetPathFromString() throws Exception {
        String deviceDir = "/tmp/android/device/samsung/tuna/";
        String path = mkParser.getPathFromString(deviceDir, "$(LOCAL_DIR)/full_tuna.mk");
        Assert.assertEquals(path, deviceDir + "full_tuna.mk");
    }

    @Test
    public void testParseProductLines() throws Exception {
        List<String> allLines = new ArrayList<>();
        allLines.add("# See the License for the specific language governing permissions and");
        allLines.add("# limitations under the License.");
        allLines.add("#");
        allLines.add("");
        allLines.add("        PRODUCT_MAKEFILES ;= \\");
        allLines.add("        $(LOCAL_DIR)/aosp_maguro.mk \\");
        allLines.add("        $(LOCAL_DIR)/full_maguro.mk");
        String rootDir = "/tmp/android/device/samsung/tuna/";
        List<String> productLines = mkParser.parseProductLines(allLines, rootDir);
        List<String> expectedLines = new ArrayList<>();
        expectedLines.add("/tmp/android/device/samsung/tuna/aosp_maguro.mk");
        expectedLines.add("/tmp/android/device/samsung/tuna/full_maguro.mk");

        Assert.assertEquals(productLines, expectedLines);
    }

    @Test
    public void testParseInheritedLines() throws Exception {
        List<String> allLines = new ArrayList<>();
        allLines.add("$(call inherit-product, device/samsung/maguro/device.mk)");
        allLines.add("$(call inherit-product-if-exists, vendor/ti/omap4/omap4-vendor.mk)");
        String rootDir = "/tmp/android";
        String deviceDir = "/tmp/android/device/samsung/maguro/";
        List<String> inheritedLines = mkParser.parseInheritedLines(allLines, rootDir, deviceDir);
        List<String> expectedLines = new ArrayList<>();
        expectedLines.add("/tmp/android/device/samsung/maguro/device.mk");
        expectedLines.add("/tmp/android/vendor/ti/omap4/omap4-vendor.mk");

        Assert.assertEquals(inheritedLines, expectedLines);
    }

    @Test
    public void testParseDir() throws Exception {
        URL resource = this.getClass().getResource("/device/samsung/maguro/AndroidProducts.mk");
        Path path = Paths.get(resource.toURI());
        Path rootDir = Paths.get(this.getClass().getResource("/").toURI());
        Path deviceDir = path.getParent();
        TreeNode treeNode = mkParser.parseDir(rootDir, deviceDir);
        String output = treeNode.toString();
        int lines = new StringTokenizer(output, "\n").countTokens();
        Assert.assertEquals(lines, 8, output);
    }

}
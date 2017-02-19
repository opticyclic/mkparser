package com.github.opticyclic.mkparser;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

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

}
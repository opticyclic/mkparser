package com.github.opticyclic.mkparser;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

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
        Path rootDir = Paths.get(deviceDir);
        String path = mkParser.getPathFromString(rootDir, "$(LOCAL_DIR)/full_tuna.mk");
        Assert.assertEquals(path, deviceDir + "full_tuna.mk");
    }

}
package com.github.opticyclic.mkparser;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

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

}
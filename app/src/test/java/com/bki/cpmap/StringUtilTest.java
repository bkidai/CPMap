package com.bki.cpmap;

import com.bki.cpmap.utils.StringUtil;

import org.junit.Assert;
import org.junit.Test;

public class StringUtilTest {
    @Test
    public void parseStringToDouble() throws Exception {
        Assert.assertEquals(0, StringUtil.parseToDouble(""), 0);
        Assert.assertEquals(0d, StringUtil.parseToDouble("txt"), 0);
        Assert.assertEquals(0d, StringUtil.parseToDouble("10,05"), 0);
        Assert.assertEquals(12.11, StringUtil.parseToDouble("12.11000"), 0);
    }
}

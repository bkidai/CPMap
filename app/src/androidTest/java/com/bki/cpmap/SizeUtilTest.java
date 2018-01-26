package com.bki.cpmap;


import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.bki.cpmap.utils.SizeUtil;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SizeUtilTest {

    private Context instrumentationContext;

    @Before
    public void setup() {
        instrumentationContext = InstrumentationRegistry.getContext();
    }

    @Test
    public void convertBetweenPxAndDp() throws Exception {

        Assert.assertEquals(20, SizeUtil.dp2px(instrumentationContext,
                SizeUtil.px2dp(instrumentationContext, 20)));

        Assert.assertEquals(20, SizeUtil.px2dp(instrumentationContext,
                SizeUtil.dp2px(instrumentationContext, 20)));

    }


}

package com.bki.cpmap;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.bki.cpmap.utils.SharedPreferencesUtil;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class SharedPreferencesTest {

    private Context instrumentationContext;

    @Before
    public void setup() {
        instrumentationContext = InstrumentationRegistry.getContext();
    }

    @Test
    public void storeAndGetListObjects() throws Exception {

        List<Object> list = new ArrayList<>();
        list.add("first");
        list.add("second");

        String key = "key";

        // case : invalid key (none existing)
        Assert.assertEquals(new ArrayList<>(),
                SharedPreferencesUtil.getListObject("any", String.class, instrumentationContext));

        // store data
        SharedPreferencesUtil.putListObject(key, list, instrumentationContext);

        // check stored data
        Assert.assertEquals(list,
                SharedPreferencesUtil.getListObject(key, Object.class, instrumentationContext));

    }
}

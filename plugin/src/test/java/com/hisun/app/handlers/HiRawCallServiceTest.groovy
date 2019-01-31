package com.hisun.app.handlers

import org.junit.Test

import java.nio.charset.Charset

/**
 * Created by 杨 on 2019/1/14.
 */
class HiRawCallServiceTest {
    @Test
    public void testDefaultCharset() {
        System.out.println(Charset.defaultCharset().name());
        System.out.println(System.getProperty("file.encoding"));
    }
}

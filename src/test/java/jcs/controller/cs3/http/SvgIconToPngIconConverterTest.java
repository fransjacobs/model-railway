/*
 * Copyright (C) 2022 fransjacobs.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package jcs.controller.cs3.http;

import java.io.IOException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.tinylog.Logger;

/**
 *
 * @author fransjacobs
 */
public class SvgIconToPngIconConverterTest {

    //fkticon_a_179
    private static final String SVG1 = "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 48 48\"><path class=\"st0\" d=\"M42 18H28c-1.7 0-3 1.3-3 3v7c0 1.7 1.3 3 3 3h14c1.7 0 3-1.3 3-3v-7c0-1.7-1.3-3-3-3zm-7 11v-3.9L29 29v-9l6 3.9V20l7 4.5-7 4.5zM20 18H6c-1.7 0-3 1.3-3 3v7c0 1.7 1.3 3 3 3h14c1.7 0 3-1.3 3-3v-7c0-1.7-1.3-3-3-3zm-1 11-6-3.9V29l-7-4.5 7-4.5v3.9l6-3.9v9zM42 3H28c-1.7 0-3 1.3-3 3v7c0 1.7 1.3 3 3 3h14c1.7 0 3-1.3 3-3V6c0-1.7-1.3-3-3-3zM32 14V5l7 4.5-7 4.5zM20 3H6C4.3 3 3 4.3 3 6v7c0 1.7 1.3 3 3 3h14c1.7 0 3-1.3 3-3V6c0-1.7-1.3-3-3-3zm-8 11H9V5h3v9zm5 0h-3V5h3v9zm25 19H6c-1.7 0-3 1.3-3 3v6c0 1.7 1.3 3 3 3h36c1.7 0 3-1.3 3-3v-6c0-1.7-1.3-3-3-3zm0 8H29v2h-3v-2H6l20-2.2V35h3v3.4L42 37v4z\" fill=\"#010101\"/></svg>";

    //fkticon_a_175
    private static final String SVG2 = "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 48 48\"><path d=\"m15.2 29.4 4.2-4.2-1.4-1.4-2.8 2.8c-1.7-2-2.6-4.5-2.6-7.2 0-6.2 5-11.2 11.2-11.2 6.2 0 11.2 5 11.2 11.2 0 2.6-.9 5.1-2.6 7.1l-2.8-2.8-1.4 1.4 4.2 4.2.7-.7c2.5-2.5 3.8-5.8 3.8-9.3C36.9 12 31 6.1 23.7 6.1S10.5 12 10.5 19.3c0 3.5 1.4 6.8 3.9 9.3l.8.8zm-4.2 5h1.6v.9c.6-.7 1.3-1 2.1-1 .4 0 .8.1 1.1.3.3.2.6.4.8.8.3-.4.6-.6.9-.8.3-.2.7-.3 1.1-.3.5 0 .9.1 1.2.3s.6.5.8.9c.1.3.2.7.2 1.4V41h-1.7v-3.8c0-.7-.1-1.1-.2-1.3-.2-.2-.4-.4-.7-.4-.2 0-.5.1-.7.2-.2.1-.4.4-.5.7-.1.3-.1.7-.1 1.4V41H15v-3.6c0-.6 0-1.1-.1-1.2-.1-.2-.2-.3-.3-.4-.1-.1-.3-.1-.5-.1-.3 0-.5.1-.7.2-.2.1-.4.4-.5.6-.1.3-.1.7-.1 1.4V41H11v-6.6zm11.4 0H24v1c.2-.3.5-.6.9-.8.4-.2.8-.3 1.2-.3.8 0 1.4.3 1.9.9.5.6.8 1.4.8 2.5s-.3 2-.8 2.6c-.5.6-1.2.9-2 .9-.4 0-.7-.1-1-.2-.3-.1-.6-.4-.9-.7v3.3h-1.7v-9.2zm1.7 3.2c0 .7.1 1.3.4 1.6.3.4.7.5 1.1.5.4 0 .7-.2 1-.5.3-.3.4-.9.4-1.6 0-.7-.1-1.2-.4-1.5-.3-.3-.6-.5-1-.5s-.8.2-1.1.5c-.3.4-.4.8-.4 1.5zm7.8-5.7v3.3c.6-.7 1.2-1 2-1 .4 0 .8.1 1.1.2.3.1.6.3.7.6.2.2.3.5.3.8.1.3.1.7.1 1.3V41h-1.7v-3.5c0-.7 0-1.1-.1-1.3-.1-.2-.2-.3-.4-.4-.2-.1-.4-.2-.6-.2-.3 0-.5.1-.8.2s-.4.4-.5.6-.2.7-.2 1.3V41h-1.7v-9.1h1.8z\"/><path d=\"M23.8 22c1.4 0 2.6-1.2 2.6-2.6s-1.2-2.6-2.6-2.6c-.4 0-.7.1-1 .2l-5-5-1.4 1.4 5 5c-.1.3-.2.6-.2 1 0 1.4 1.1 2.6 2.6 2.6z\"/></svg>";

    public SvgIconToPngIconConverterTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of convertAndCacheAllFunctionsSvgIcons method, of class
     * SvgIconToPngIconConverter.
     */
    @Test
    public void testConvertAndCacheAllFunctionsSvgIcons1() {
        System.out.println("convertAndCacheAllFunctionsSvgIcons1");
        String json = SVG1;
        SvgIconToPngIconConverter instance = new SvgIconToPngIconConverter();

        try {
            instance.convertAndCacheFunctionImage("AAAA_fkticon_a_179", SVG1, true);
        } catch (IOException e) {
            Logger.error(e);
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testConvertAndCacheAllFunctionsSvgIcons2() {
        System.out.println("convertAndCacheAllFunctionsSvgIcons2");
        String json = SVG1;
        SvgIconToPngIconConverter instance = new SvgIconToPngIconConverter();

        try {
            instance.convertAndCacheFunctionImage("BBBB_fkticon_a_17", SVG2, true);
        } catch (IOException e) {
            Logger.error(e);
            Assert.fail(e.getMessage());
        }
    }

}

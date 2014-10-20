package com.sun.jersey.multipart.impl;


import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Files;

import com.sun.jersey.multipart.MultiPartConfig;

import org.junit.Before;
import org.junit.Test;

public class MultiPartReaderClientSideTest {

    private File tempDirectory;

    @Before
    public void setUp() throws Exception {
        tempDirectory = Files.createTempDirectory("junitTest").toFile();
        System.setProperty("java.io.tmpdir", tempDirectory.getAbsolutePath());
    }

    @Test
    public void testName() throws Exception {
        MultiPartConfig config = new MultiPartConfig();
        new MultiPartReaderClientSide(null, config);

        assertEquals(0, tempDirectory.listFiles().length);
    }
}
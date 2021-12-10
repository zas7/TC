/*
 * Copyright 2018-2021 Statistisches Amt des Kantons Zürich
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.zh.transferclient.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import ch.zh.transferclient.util.FileLockerReadOnly;

@Disabled("Klasse FileLockerReadOnly wird nicht benutzt")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
class FileLockerReadOnlyTest
    {
    
    static Path file;
    
    @BeforeAll
    static void before() throws IOException
        {
        file = new File("c:\\temp\\testAppendBlockeMode1.txt").toPath();
        Files.deleteIfExists(file);
        assertTrue(file.toFile().createNewFile());
        }
        
    @Test
    void close_input_stream_while_locked() throws Exception
        {
        FileInputStream fis = new FileInputStream(file.toFile());
        try (FileLockerReadOnly lock = new FileLockerReadOnly(fis))
            {
            fis.close();
            assertFalse(lock.isValid());
            }
        }
        
    @Test
    void lock_same_file_twice_is_possible() throws Exception
        {
        try (FileInputStream fis = new FileInputStream(file.toFile());
                FileLockerReadOnly lock = new FileLockerReadOnly(fis))
            {
            try (FileInputStream fis2 = new FileInputStream(file.toFile()))
                {
                assertThrows(OverlappingFileLockException.class, () -> new FileLockerReadOnly(fis2));
                }
            }
        }
        
    @Test
    void lock_same_file_sequentially() throws Exception
        {
        int i = 0;
        while (i++ < 1000)
            {
            try (FileInputStream fis = new FileInputStream(file.toFile());
                    FileLockerReadOnly lock = new FileLockerReadOnly(fis))
                {
                assertNotNull(lock);
                }
            }
        }
        
    @Test
    void read_from_blocked_file_must_be_possible() throws Exception
        {
        final String text = "ÖÄäö$ç%";
        Files.write(file, text.getBytes(StandardCharsets.UTF_8));
        try (FileInputStream fis = new FileInputStream(file.toFile());
                FileLockerReadOnly lock = new FileLockerReadOnly(fis))
            {
            List<String> readText = Files.readAllLines(file);
            assertEquals(text, readText.get(0));
            }
        }
        
    @Test
    void write_to_blocked_file_must_not_be_possible() throws Exception
        {
        final byte[] text = "ÖÄäö$ç%".getBytes(StandardCharsets.UTF_8);
        try (FileInputStream fis = new FileInputStream(file.toFile());
                FileLockerReadOnly lock = new FileLockerReadOnly(fis))
            {
            assertThrows(IOException.class, () -> Files.write(file, text));
            }
        }
        
    @Test
    void write_to_deblocked_file_must_be_possible() throws Exception
        {
        final byte[] text = "ÖÄäö$ç%".getBytes(StandardCharsets.UTF_8);
        try (FileInputStream fis = new FileInputStream(file.toFile()))
            {
            FileLockerReadOnly lock = new FileLockerReadOnly(fis);
            assertThrows(IOException.class, () -> Files.write(file, text));
            lock.close();
            assertFalse(lock.isValid());
            Files.write(file, text);
            List<String> readText = Files.readAllLines(file);
            assertEquals(new String(text), readText.get(0));
            }
        }
        
    /*-
     * Aus einem unbekannten Grund wird das File geleert. Anstatt den Text zu behalten, 
     * ist das File nachdem Test leer. Der Inputstream des Process hat vielleicht etwas damit zu tun
     */
    @Test
    void write_from_dos_to_a_locked_file_must_not_be_possible() throws Exception
        {
        // Path file = Files.createTempFile("testAppendBlockeMode2", ".txt");
        Path         testFile = new File("c:\\temp\\y.txt").toPath();
        final byte[] text     = "ÖÄäö$ç%".getBytes(StandardCharsets.UTF_8);
        Files.write(testFile, text);
        try (FileInputStream fis = new FileInputStream(testFile.toFile());
                FileLockerReadOnly lock = new FileLockerReadOnly(fis))
            {
            Process p = callDosCmd("cmd /c ping -n 10 localhost > " + testFile.toString());
            // Process p = callDosCmd("cmd /c echo A > " + filex.toString());
            p.waitFor();
            List<String> readText = Files.readAllLines(testFile);
            assertEquals(1, readText.size());
            assertEquals(new String(text), readText.get(0));
            }
        }
        
    private Process callDosCmd(String cmd) throws IOException
        {
        return Runtime.getRuntime().exec(cmd, null, new File("C:\\temp"));
        }
    }

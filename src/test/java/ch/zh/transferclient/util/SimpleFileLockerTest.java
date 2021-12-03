package ch.zh.transferclient.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class SimpleFileLockerTest
    {
    
    private void assertFileContent(Path file, final String line1) throws IOException
        {
        List<String> readText = Files.readAllLines(file);
        assertEquals(1, readText.size());
        assertEquals(line1, readText.get(0));
        }
        
    private Process callDosCmd(String cmd) throws IOException
        {
        return Runtime.getRuntime().exec(cmd);
        }
        
    @Test
    void change_locked_file_from_dos_is_possible() throws IOException, InterruptedException
        {
        final Path file = Files.createTempFile("change_locked_file_from_dos_is_possible", ".txt");
        Files.write(file, "AAAAA".getBytes(StandardCharsets.UTF_8));
        try (FileInputStream lock = SimpleFileLocker.getSharedLock(file.toFile(), 1, 1))
            {
            final String changedText = "BBBB";
            callDosCmd("cmd /c echo " + changedText + "> " + file.toString()).waitFor();
            assertFileContent(file, changedText);
            }
        }
        
    @Test
    void lock_existing_file_successfully() throws IOException
        {
        final Path file = Files.createTempFile("lock_existing_file_successfully", ".txt");
        try (FileInputStream lock = SimpleFileLocker.getSharedLock(file.toFile(), 0, 0))
            {
            assertNotNull(lock);
            }
        }
        
    @Test
    void lock_file_after_writing_from_dos() throws IOException, InterruptedException
        {
        final Path    file = Files.createTempFile("lock_file_after_writing_from_dos", ".txt");
        final Process prc  = startWritingToFileFromDos(file);
        try (FileInputStream lock = SimpleFileLocker.getSharedLock(file.toFile(), 0, 0))
            {
            assertNull(lock);
            }
        try (FileInputStream lock = SimpleFileLocker.getSharedLock(file.toFile(), 60, 200))
            {
            assertNotNull(lock);
            }
        prc.waitFor();
        }
        
    @Test
    void lock_file_while_writing_from_dos_is_not_possible() throws IOException, InterruptedException
        {
        final Path    file = Files.createTempFile("lock_file_while_writing_from_dos_is_not_possible", ".txt");
        final Process prc  = startWritingToFileFromDos(file);
        try (FileInputStream lock = SimpleFileLocker.getSharedLock(file.toFile(), 1, 1))
            {
            assertNull(lock);
            }
        prc.waitFor();
        }
        
    @Test
    void lock_same_file_sequentially() throws IOException
        {
        final Path file = Files.createTempFile("lock_same_file_sequentially", ".txt");
        int        i    = 0;
        while (i < 1000)
            {
            try (FileInputStream lock = SimpleFileLocker.getSharedLock(file.toFile(), 0, 0))
                {
                i++;
                }
            }
        }
        
    @Test
    void lock_same_file_twice_is_not_possible() throws IOException
        {
        final Path file = Files.createTempFile("lock_same_file_twice_is_not_possible", ".txt");
        try (FileInputStream lock1 = SimpleFileLocker.getSharedLock(file.toFile(), 0, 0);
                FileInputStream lock2 = SimpleFileLocker.getSharedLock(file.toFile(), 0, 0))
            {
            assertNotNull(lock1);
            assertNull(lock2);
            }
        }
        
    @Test
    void remove_locked_file_from_dos_is_not_possible() throws IOException, InterruptedException
        {
        Path         file      = Files.createTempFile("remove_locked_file_from_dos_is_not_possible", ".txt");
        final String text      = "AAAAA";
        final String deleteCmd = "cmd /c del /q /f " + file.toString();
        
        // create file and delete it from dos command
        Files.write(file, text.getBytes(StandardCharsets.UTF_8));
        callDosCmd(deleteCmd).waitFor();
        assertFalse(file.toFile().exists());
        
        // create file, lock and deletion from dos not possible
        Files.write(file, text.getBytes(StandardCharsets.UTF_8));
        try (FileInputStream lock = SimpleFileLocker.getSharedLock(file.toFile(), 1, 1))
            {
            // try to delete file from dos
            callDosCmd(deleteCmd).waitFor();
            assertFileContent(file, text);
            }
        }
        
    @Test
    void remove_file_from_dos_after_releasing_is_possible() throws IOException, InterruptedException
        {
        Path         file      = Files.createTempFile("remove_file_from_dos_after_releasing_is_possible", ".txt");
        final String text      = "AAAAA";
        final String deleteCmd = "cmd /c del /q /f " + file.toString();
        
        // create file and delete it from dos command
        Files.write(file, text.getBytes(StandardCharsets.UTF_8));
        callDosCmd(deleteCmd).waitFor();
        assertFalse(file.toFile().exists());
        
        // create file, lock and deletion from dos not possible
        Files.write(file, text.getBytes(StandardCharsets.UTF_8));
        try (FileInputStream lock = SimpleFileLocker.getSharedLock(file.toFile(), 1, 1))
            {
            // try to delete file from dos
            callDosCmd(deleteCmd).waitFor();
            assertFileContent(file, text);
            }
        // lock is released since it's Autoclosable
        assertTrue(file.toFile().exists());
        callDosCmd(deleteCmd).waitFor();
        assertFalse(file.toFile().exists());
        }
        
    private Process startWritingToFileFromDos(Path file) throws IOException, InterruptedException
        {
        final Process prc = callDosCmd("cmd /c ping -n 4 localhost > " + file.toString());
        Thread.sleep(200);
        assertTrue(file.toFile().exists());
        assertTrue(file.toFile().length() > 0);
        return prc;
        }
        
    @Test
    void try_to_lock_inexisting_file_with_retries() throws IOException
        {
        final File file = new File("try_to_lock_inexisting_file_with_retries");
        assertFalse(file.exists());
        try (FileInputStream lock = SimpleFileLocker.getSharedLock(file, 5, 5))
            {
            assertNull(lock);
            }
        }
        
    @Test
    void try_to_lock_inexisting_file_without_retries() throws IOException
        {
        final File file = new File("try_to_lock_inexisting_file_without_retries");
        assertFalse(file.exists());
        try (FileInputStream lock = SimpleFileLocker.getSharedLock(file, 0, 0))
            {
            assertNull(lock);
            }
        }
    }

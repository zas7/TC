package ch.zh.transferclient.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalUnit;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class FileUtilTest
    {
    
    @Test
    void getOneModifiedBeforeFile_immediately() throws Exception
        {
        final var directory = Files.createTempDirectory("getOneModifiedBeforeFile");
        Files.createDirectory(directory.resolve("subDir"));
        Files.createFile(directory.resolve("A"));
        final var directoryName = directory.toString();
        
        Thread.sleep(5); // give time to file system
        assertTrue(FileUtil.getOneModifiedBeforeFile(directoryName, 0).isPresent());
        assertFalse(FileUtil.getOneModifiedBeforeFile(directoryName, 2).isPresent());
        }
        
    @Test
    void getOneModifiedBeforeFile_wait() throws Exception
        {
        final var directory = Files.createTempDirectory("X");
        Files.createDirectory(directory.resolve("subDir"));
        final var file          = Files.createFile(directory.resolve("A"));
        final var directoryName = directory.toString();
        
        Thread.sleep(5); // give time to file system
        assertTrue(FileUtil.getOneModifiedBeforeFile(directoryName, 0).isPresent());
        
        final var waitTimeInSec = 3;
        final var lastModified  = Instant.ofEpochMilli(file.toFile().lastModified());
        final var waitUntil     = lastModified.plusSeconds(waitTimeInSec).toEpochMilli();
        while (System.currentTimeMillis() < waitUntil)
            {
            Thread.sleep(200);
            }
        assertTrue(FileUtil.getOneModifiedBeforeFile(directoryName, waitTimeInSec).isPresent());
        assertFalse(FileUtil.getOneModifiedBeforeFile(directoryName, waitTimeInSec + 1).isPresent());
        }
        
    @Test
    void getOneFile_is_successful() throws Exception
        {
        final var directory     = Files.createTempDirectory("getOneFile");
        final var directoryName = directory.toString();
        assertFalse(FileUtil.getOneFile(directoryName).isPresent());
        
        Files.createDirectory(directory.resolve("subDir"));
        assertFalse(FileUtil.getOneFile(directory.toString()).isPresent());
        
        Files.createFile(directory.resolve("A"));
        assertTrue(FileUtil.getOneFile(directoryName).isPresent());
        
        Files.createFile(directory.resolve("B"));
        assertTrue(FileUtil.getOneFile(directoryName).isPresent());
        }
    }
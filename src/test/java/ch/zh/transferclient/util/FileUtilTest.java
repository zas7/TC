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
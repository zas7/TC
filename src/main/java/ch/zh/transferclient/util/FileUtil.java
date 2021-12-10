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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

public class FileUtil
    {
    private FileUtil()
        {
        }
        
    /**
     * Get just one file from a directory listing
     * 
     * @param  directory   directory to be listed
     * @return             Optional o a path if directory contains files or an empty Optional
     * @throws IOException
     */
    public static Optional<Path> getOneFile(String directory) throws IOException
        {
        try (Stream<Path> fileList = Files.list(Paths.get(directory)))
            {
            return fileList.filter(Files::isRegularFile).findFirst();
            }
        }
        
    /**
     * Get just one file from a directory listing
     * 
     * @param  directory               directory to be listed
     * @param  modifiedBeforeInSeconds modify time of file was so many sec ago
     * @return                         Optional o a path if directory contains files, which was modified more than
     *                                 'modifiedBeforeInSeconds' ago or an empty Optional
     * @throws IOException
     */
    public static Optional<Path> getOneModifiedBeforeFile(String directory, long modifiedBeforeInSeconds) throws IOException
        {
        final long modifiedTime = System.currentTimeMillis() - modifiedBeforeInSeconds * 1000L;
        
        try (Stream<Path> fileList = Files.list(Paths.get(directory)))
            {
            return fileList.filter(f -> Files.isRegularFile(f) && f.toFile().lastModified() < modifiedTime).findFirst();
            }
        }
    }

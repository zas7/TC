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
        
    public static Optional<Path> getOneFile(String directory) throws IOException
        {
        try (Stream<Path> fileList = Files.list(Paths.get(directory)))
            {
            return fileList.filter(Files::isRegularFile).findFirst();
            }
        }
        
    public static Optional<Path> getOneModifiedBeforeFile(String directory, long modifiedBeforeInSeconds) throws IOException
        {
        final long modifiedTime = System.currentTimeMillis() - modifiedBeforeInSeconds * 1000L;
        
        try (Stream<Path> fileList = Files.list(Paths.get(directory)))
            {
            return fileList.filter(f -> Files.isRegularFile(f) && f.toFile().lastModified() < modifiedTime).findFirst();
            }
        }
    }

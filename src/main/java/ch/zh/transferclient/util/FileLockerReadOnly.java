package ch.zh.transferclient.util;

import java.io.FileInputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * FileLockerReadOnly lockt einen FileInputStream. Locktyp ist shared
 * 
 * Da ein Unittest nicht erfolgreich ist, habe ich das Locking anders umgesetzt. Siehe SimpleFileLocker
 * 
 * @author  Daniel Bierer, Stephan Zahner (Statistisches Amt des Kantons Zürich)
 *
 */
public class FileLockerReadOnly implements AutoCloseable
    {
    private FileChannel fch  = null;
    private FileLock    lock = null;
    
    public FileLockerReadOnly(FileInputStream fis) throws Exception
        {
        try
            {
            fch  = fis.getChannel();
            lock = fch.tryLock(0L, Long.MAX_VALUE, true); // shared lock
            }
        catch (Exception e)
            {
            close();
            throw e;
            }
        }
        
    private void close(AutoCloseable x)
        {
        try
            {
            if (x != null)
                {
                x.close();
                }
            }
        catch (Exception ignore)
            {
            }
        }
        
    public boolean isValid()
        {
        return lock != null && lock.isValid();
        }
        
    public void close()
        {
        close(lock);
        close(fch);
        }
    }

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

import java.io.FileInputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * FileLockerReadOnly locks one FileInputStream. Locking type is shared
 * 
 * Unit testing was'nt successful and therefore class not yet used. Kept for future use. Instead of this class
 * SimpleFilelocker was used
 * 
 * @author Daniel Bierer, Stephan Zahner (Statistisches Amt des Kantons Zürich)
 *
 */
public class FileLockerReadOnly implements AutoCloseable
    {
    private FileChannel fch  = null;
    private FileLock    lock = null;
    
    /**
     * Constructor. Locks the FileInputStream with a shared lock
     * 
     * @param  fis       FileInputStream to be locked
     * @throws Exception Exception when lock can not be applied
     */
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
        
    /**
     * Returns true if lock exists and is valid
     * 
     * @return
     */
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

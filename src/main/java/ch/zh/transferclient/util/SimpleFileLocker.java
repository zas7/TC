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

import java.io.File;
import java.io.FileInputStream;

public class SimpleFileLocker
    {
    private SimpleFileLocker()
        {
        }
        
    /**
     * Versucht ein File mit einem FileInputStream zu sperren. Der Sperrungstyp ist ein SharedLock.
     * Falls das Sperren nicht funkioniert, wird es wiederversucht und zwischen den Versuchen 
     * wird gewartet 
     * @param  file                             zu lockendes File
     * @param  retryCount                       max. Anzahl Versuche
     * @param  waitTimeBetweenRetriesInMilli    Wartezeit zwischen den Versuchen in Millisekunden
     * @return FileInputStream des Files, falls Sperren erfolgreich; Null sonst
     */
    public static FileInputStream getSharedLock(File file, int retryCount, long waitTimeBetweenRetriesInMilli)
        {
        try
            {
            boolean success = false;
            try
                {
                success = file.renameTo(file);
                if (success)
                    {
                    return new FileInputStream(file);
                    }
                }
            catch (Exception e)
                {
                success = false;
                }
            int c = retryCount > 0 ? retryCount : 0;
            while (!success && c > 0)
                {
                Thread.sleep(waitTimeBetweenRetriesInMilli);
                c--;
                try
                    {
                    success = file.renameTo(file);
                    }
                catch (Exception e)
                    {
                    success = false;
                    }
                }
            return success ? new FileInputStream(file) : null;
            }
        catch (InterruptedException e)
            {
            Thread.currentThread().interrupt();
            return null;
            }
        catch (Exception e)
            {
            return null;
            }
        }
    }

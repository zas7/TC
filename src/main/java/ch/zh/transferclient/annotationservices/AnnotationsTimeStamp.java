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

package ch.zh.transferclient.annotationservices;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class is used to generate time stamps for the backups.
 *
 * @author  Daniel Bierer, Stephan Zahner (Statistisches Amt des Kantons Zürich)
 * @version 2.5
 */
public class AnnotationsTimeStamp
    
    {
    
    /**
     * Constructs an AnnotationsTimeStamp object.
     */
    private AnnotationsTimeStamp()
        {
        //see also https://stackoverflow.com/questions/31409982/java-best-practice-class-with-only-static-methods
        }
    
    /**
     * Date format to be used for the time stamp.
     */
    private final static SimpleDateFormat SDF = new SimpleDateFormat("yyyy_MM_dd_HHmm");
    
    /**
     * Returns the time stamp for the backup files.
     * 
     * @return Date format for the sedex message id.
     */
    protected static String get_timestamp()
        {
        return SDF.format(new Date());
        }
        
    }

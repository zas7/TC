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

package ch.zh.transferclient.processing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import ch.zh.transferclient.gui.*;
import ch.zh.transferclient.main.Labels;
import ch.zh.transferclient.main.Logger;
import ch.zh.transferclient.properties.Properties;
import ch.zh.transferclient.util.FileUtil;
import ch.zh.transferclient.util.SimpleFileLocker;
import ch.zh.transferclient.util.TimeStamp;

/**
 * This class is used to process the input and receipt files.
 *
 * @author  Daniel Bierer, Stephan Zahner (Statistisches Amt des Kantons Zürich)
 * @version 2.5
 */
public class Processing
    
    {
    
    /**
     * Constructs a Processing object.
     */
    private Processing()
        {
        // see also https://stackoverflow.com/questions/31409982/java-best-practice-class-with-only-static-methods
        }
        
    /**
     * Processes one input and one receipt files.
     * 
     * @param properties The properties to be used.
     * @param gui        The graphical user interface to be used.
     */
    public synchronized static void process(final Properties properties, final Gui gui)
        
        {
        try
            {
            final String   sedex_sender_id = properties.get_sedex_sender_id();
            final String   dir_results     = properties.get_folder_results();
            
            // -------------------------------------------------//
            // Schlaufe ueber alle Files des Resultate-Ordners //
            // -------------------------------------------------//
            
            // Damit der Executor-Thread bei der Deaktivierung
            // auch bei vielen gleichzeitigen Versandanfragen
            // schnell unterbrochen wird, wird neu
            // das erste File der Liste verarbeitet.
            // Die Verarbeitung der folgenden Files erfolgt
            // dann im folgenden Taskdurchlauf.
            
            Optional<Path> pathOpt         = FileUtil.getOneFile(dir_results);
            if (pathOpt.isPresent())
                {
                
                // Lock file
                final File file = pathOpt.get().toFile();
                try (FileInputStream fis = SimpleFileLocker.getSharedLock(file, 15, 100))
                    {
                    if (fis == null)
                        {
                        JOptionPane.showMessageDialog(gui, String.format(Labels.get("DIALOG_FILE_LOCKED_MSG"), file.getAbsolutePath()), Labels.get("DIALOG_FILE_LOCKED_TITLE"), JOptionPane.WARNING_MESSAGE);
                        return;
                        }
                        
                    final String datafile_path    = file.getAbsolutePath();
                    final Path   path_source      = Paths.get(datafile_path);
                    final String sedex_message_id = sedex_sender_id + "-" + TimeStamp.getstamp_for_sedex_message_id();
                    
                    if (properties.get_archive_datafiles())
                        {
                        Files.copy(file.toPath(), new File("archive/data/data_" + sedex_message_id + "_"
                                + file.getName()).toPath());
                        }
                        
                    // ------------------------------//
                    // Verarbeitung des Input-Files //
                    // ------------------------------//
                    Processing_SingleInput.process(properties, gui, file, sedex_message_id, fis);
                    
                    Logger.info("FILE PROCESSED: " + sedex_message_id + ": " + path_source.toString());
                    }
                }
            }
        catch (Exception e)
            {
            Logger.error(e);
            SwingUtilities.invokeLater(new Runnable()
                {
                @Override
                public void run()
                    {
                    gui.get_dialog_fileprocessingerror().setvisible(e);
                    }
                });
            }
            
        // @formatter:off                                                                    //
        // ----------------------------------------------------------------------------------//
        // Receipts-Processing                                                               //
        // ----------------------------------------------------------------------------------//
        // @formatter:on                                                                     //
        try
            {
            Processing_Receipts.process_receipts(properties, gui);
            }
        catch (IOException | InterruptedException | InvocationTargetException e)
            {
            Logger.error(e);
            SwingUtilities.invokeLater(new Runnable()
                {
                @Override
                public void run()
                    {
                    gui.get_dialog_fileprocessingerror().setvisible(e);
                    }
                });
            }
        }
        
    }

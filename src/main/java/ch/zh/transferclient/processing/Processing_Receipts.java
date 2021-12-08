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

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import ch.zh.transferclient.gui.*;
import ch.zh.transferclient.main.Labels;
import ch.zh.transferclient.main.Logger;
import ch.zh.transferclient.properties.Properties;
import ch.zh.transferclient.util.FileUtil;
import ch.zh.transferclient.util.SimpleFileLocker;

/**
 * This class is used to process the receipts: The receipt files are evaluated in order to inform the end user about the
 * transfer status.
 * 
 * @author  Daniel Bierer, Stephan Zahner (Statistisches Amt des Kantons Zürich)
 * @version 2.5
 */
public class Processing_Receipts
    
    {
    
    private static final long WAIT_TIME_IN_SEC = 30;

    /**
     * Constructs a Processing_Receipts object.
     */
    private Processing_Receipts()
        {
        // see also https://stackoverflow.com/questions/31409982/java-best-practice-class-with-only-static-methods
        }
        
    /**
     * Vector for saving the receipts (vector is thread-safe).
     * 
     * Although the GUI protocol tables are always in newest state (because of using SwingUtilities.invokeAndWait()
     * instead of SwingUtilities.invokeLater()), a local (instead of a static) variable could not be used in the present
     * context because the end user could deactivate the Transfer-Client just in the moment after archiving a receipt
     * file and before updating the GUI table. In such a case the information from the receipt would be lost.
     * 
     * Can the problem illustrated in figure 5.1 of Goetz (2006) occur in our context?
     * 
     * No, this problem cannot occur because the executor thread is only adding receipts (and not removing receipts),
     * i.e. it cannot happen that the EDT thread requests a receipt which does not exist anymore.
     * 
     */
    private static final Vector<Processing_Receipts_Record> RECEIPTS = new Vector<>();
    
    /**
     * Processes one receipts: One receipt file is evaluated in order to inform the end user about the transfer status.
     * 
     * @param  properties                The properties to be used.
     * @param  gui                       The graphical user interface to be used.
     * @throws InterruptedException      Exception which can be thrown by SwingUtilities.invokeAndWait.
     * @throws InvocationTargetException Exception which can be thrown by SwingUtilities.invokeAndWait.
     * @throws IOException               Exception during file functions
     */
    protected static synchronized void process_receipts(final Properties properties, final Gui gui) throws InterruptedException, InvocationTargetException, IOException
        {
        
        final String            dir_sedex_receipts  = properties.get_sedex_dir_receipts();
        final ArrayList<String> sedex_recipient_ids = properties.get_sedex_recipient_ids();
        
        // Damit der Executor-Thread bei der Deaktivierung
        // auch bei vielen gleichzeitigen Versandanfragen
        // schnell unterbrochen wird, wird nur
        // das erste File der Liste verarbeitet.
        // Die Verarbeitung der folgenden Files erfolgt
        // dann im folgenden Taskdurchlauf.
        
        Optional<Path>          pathOpt             = FileUtil.getOneModifiedBeforeFile(dir_sedex_receipts, WAIT_TIME_IN_SEC);
        if (!pathOpt.isPresent())
            {
            return;
            }
            
        // Lock file
        final File file_receipt = pathOpt.get().toFile();
        try (FileInputStream fis = SimpleFileLocker.getSharedLock(file_receipt, 20, 100))
            {
            if (fis == null)
                {
                JOptionPane.showMessageDialog(gui, String.format(Labels.get("DIALOG_FILE_INVALID_RECEIPT"), file_receipt.getAbsolutePath()), Labels.get("DIALOG_FILE_INVALID_RECEIPT_TITLE"), JOptionPane.WARNING_MESSAGE);
                return;
                }
            }
        File file_receipt_copy = new File("archive/receipts/" + file_receipt.getName());
        
        try
            {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder        builder = factory.newDocumentBuilder();
            
            // Falls die folgende Zeile die Exception "premature end of file"
            // werfen sollte, wird das File nicht verschoben (da dann die
            // nachfolgenden Zeilen nicht ausgefuehrt werden). Dies bedeutet,
            // dass der Executor-Thread das File beim naechsten Durchgang
            // erneut im Sedex-Receipts-Ordner antreffen wird und erneut
            // versuchen wird, das File auszuwerten.
            Document               doc     = builder.parse(file_receipt);
            
            // Falls der Executor die nachfolgende Zeile erreicht, heisst das,
            // dass das Parsen erfolgreich war und jetzt versucht werden kann,
            // das Receipt-File in den Archiv-Ordner zu verschieben.
            // Hinweis:
            // Die Quittungsfiles werden ab Version 2.4
            // des Transfer-Clients fortwährend ins
            // Archiv verschoben. Dies hat den Vorteil,
            // dass bereits verarbeitete Quittungen nicht mehr
            // behandelt werden muessen.
            
            Files.copy(file_receipt.toPath(), file_receipt_copy.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            Files.deleteIfExists(file_receipt.toPath());
            
            // Falls es bis hierhin ohne Fehler duchgefuehrt werden konnte,
            // wird die Information des Receipts extrahiert und im
            // Arbeitsspeicher abgelegt.
            Processing_Receipts_Record receipt = Processing_Receipts_ExtractElements.extract(doc, gui);
            RECEIPTS.add(receipt);
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
            Logger.info("FILE FAILED: " + file_receipt.toPath());
            // File wird nicht als Receipt-File verarbeitet und ins Verzeichnis der fehlerhaften Files veschoben
            // Damit bei einem fehlerhaften File nicht ständig ein Fehlerfenster erscheint, wird es aus dem Verzeichnis
            // verschoben
            try
                {
                Files.move(file_receipt.toPath(), Paths.get("failed", file_receipt.getName()), StandardCopyOption.REPLACE_EXISTING);
                }
            catch (Exception e2)
                {
                Logger.error(e2);
                // Sicherstellen, dass das File wirklich aus dem Verzeichnis weg ist
                Files.deleteIfExists(file_receipt.toPath());
                Logger.info("FAILED FILE DELETED: " + file_receipt.toPath());
                }
            }
            
        Processing_Receipts_GUIUpdate.process_GUIUpdate(gui, sedex_recipient_ids, RECEIPTS);
        
        }
        
    }

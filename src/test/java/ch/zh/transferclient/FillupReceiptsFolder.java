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

package ch.zh.transferclient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;

/**
 * The class simulates receipts. For every envelop file in the outbox, a receipt file in the receipt directory is
 * created.
 * 
 * @author  Daniel Bierer, Stephan Zahner (Statistisches Amt des Kantons Zürich)
 * @version 2.5
 *
 */
public class FillupReceiptsFolder
    
    {
    static boolean toggle1 = false;
    static boolean toggle2 = false;
    
    private FillupReceiptsFolder()
        {
        }
        
   // @formatter:off
   /**
     * Writes a receipt files to the receipts folder.
     * 
     * @param  args        Command-line arguments. 
     *                     1: sedexOutputDirectory 
     *                     2: state with the values 
     *                        s: successful: All receipt messages have state successful 
     *                        u: unsuccessful: All receipt messages have state unsuccessful 
     *                        a: alternate: Half of receipt messages have state successful. The other half is
     *                           unsuccessful
     * @throws IOException
     */
    // @formatter:on
    
    private static void setToggle(String state)
        {
        switch (state) {
        case "s":
            toggle1 = true;
            toggle2 = true;
            break;
        case "u":
            toggle1 = false;
            toggle2 = false;
            break;
        case "a":
            toggle1 = true;
            toggle2 = false;
            break;
        default:
            throw new IllegalArgumentException("Parameters: sedexOutputDirectory, state");
        }
        
        }
    
    private static boolean getToggle() {
    boolean result = toggle1;
    toggle1 = toggle2;
    toggle2 = result;
    return result;
    }
        
    public static void main(String[] args) throws IOException
        
        {
        
        if (args.length != 2)
            {
            System.out.println("Parameters: sedexOutputDirectory, msgState [s(uccessful), u(nsuccessful), a(lternate)]");
            throw new IllegalArgumentException("Parameters: sedexOutputDirectory, state");
            }
            
        Path sedexOutputDir  = Paths.get(args[0]);
        Path sedexReceiptDir = sedexOutputDir.getParent().resolve("receipts");
        setToggle(args[1]);
        
        // 1. list all envelop files in the output directory
        // 2. extract the sedex id from the file name
        // 3. create a simulation receipt
        // @formatter:off
        Files.list(sedexOutputDir)
        .filter(f -> Files.isRegularFile(f) && f.getFileName().toString().startsWith("envl_"))
        .map(f -> f.getFileName().toString().replace("envl_", "").replace(".xml", ""))
        .forEach(n -> writeFile(sedexReceiptDir, n, getToggle()));
        // @formatter:on
        }
        
    /**
     * Writes a receipt file.
     * 
     * @param  sedexMessageId Sedex Message ID to be used.
     * @throws IOException
     */
    private static void writeFile(Path outputDir, String sedexMessageId, boolean successful)
        {
        
        /*----------------------------------------------*/
        /* Schreiben des Receipts */
        /*----------------------------------------------*/
        
        String ts                = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm-ss-nnn"));
        Path   simulationReceipt = outputDir.resolve("simulation_receipt_" + ts + ".xml");
        String statusInfo        = successful ? "Message successfully transmitted" : "Error during receiving";
        String statusCode        = successful ? "100" : "501";
        
        String text              = """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <receipt xmlns="http://www.ech.ch/xmlns/eCH-0090/2" version="2.0">
                <eventDate>2018-04-25T15:54:50.892+02:00</eventDate>
                <statusCode>%s</statusCode>
                <statusInfo>%s</statusInfo>
                <messageId>%s</messageId>
                <messageType>1055</messageType>
                <messageClass>0</messageClass>
                <senderId>4-143849-0</senderId>
                <recipientId>4-143849-0</recipientId>
                </receipt>""".formatted(statusCode, statusInfo, sedexMessageId);
        try
            {
            Files.write(simulationReceipt, text.getBytes(StandardCharsets.UTF_8));
            System.out.println("File created: " + simulationReceipt);
            }
        catch (IOException e)
            {
            e.printStackTrace();
            }
        }
        
    }

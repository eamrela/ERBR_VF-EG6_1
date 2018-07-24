package com.vfeg.erbr.util;


import com.vfeg.erbr.logger.LoggerProcess;
import com.vfeg.erbr.logger.SEVERITY;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author eelaamr
 */
public class CutoverPlanReader {
    private final static LoggerProcess logger = new LoggerProcess(CutoverPlanReader.class);
    
    public static TreeMap<String,CutoverRecord> readCutoverFile(String filePath){
        
        TreeMap<String,CutoverRecord> migrationPlan = new TreeMap<String,CutoverRecord>();
        try {
            RandomAccessFile raf = new RandomAccessFile(new File(filePath), "r");
            String line = null;
            CutoverRecord record = null;
            // skipping header 
            raf.readLine();
            String[] migrationRecords;
            while((line=raf.readLine())!=null){
                migrationRecords = line.split(",");
                if(migrationRecords.length==4){
                    record = new CutoverRecord();
                    // source site
                    record.setSiteName(migrationRecords[0].trim());
                    //source rnc
                    record.setSourceRnc(migrationRecords[1].trim());
                    //target rnc
                    record.setTargetRnc(migrationRecords[2].trim());
                    //target lac
                    record.setTargetLac(migrationRecords[3].trim());
                    
                    migrationPlan.put(record.getSiteName(),record);
                }else if(migrationRecords.length>2){
                    //System.out.println("The input record is invalid: "+line);
                    logger.logThis(SEVERITY.ERROR,"The input record is invalid: "+line,true);
                }
            }
        } catch (FileNotFoundException ex) {
            //System.out.println("File not found: "+filePath);
            logger.logThis(SEVERITY.ERROR,"File not found: "+filePath,true);
            Logger.getLogger(CutoverPlanReader.class.getName()).log(Level.SEVERE, null, ex);
            logger.logThis(SEVERITY.ERROR, ex.getMessage(), true);
        } catch (IOException ex) {
            //System.out.println("Error while reading file");
            logger.logThis(SEVERITY.ERROR,"Error while reading file",true);
            Logger.getLogger(CutoverPlanReader.class.getName()).log(Level.SEVERE, null, ex);
            logger.logThis(SEVERITY.ERROR, ex.getMessage(), true);
        }
        return migrationPlan;
    }
}

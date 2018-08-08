/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vfeg.erbr.util;

import com.vfeg.erbr.logger.LoggerProcess;
import com.vfeg.erbr.logger.SEVERITY;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eelaamr
 */

public class NewSiteReader {
    private final static LoggerProcess logger = new LoggerProcess(NewSiteReader.class);
    
    public static TreeMap<String,NewSiteRecord> readNewSiteFile(String filePath){
        
        TreeMap<String,NewSiteRecord> newSitePlan = new TreeMap<String, NewSiteRecord>();
        try {
            RandomAccessFile raf = new RandomAccessFile(new File(filePath), "r");
            String line = null;
            NewSiteRecord record = null;
            // skipping header 
            raf.readLine();
            String[] newSitesRecords;
            while((line=raf.readLine())!=null){
                newSitesRecords = line.split(",");
                if(newSitesRecords.length==9){
                    record = new NewSiteRecord();
                    // source site
                    record.setSourceCell(newSitesRecords[0].trim());
                    //source rnc
                    record.setSourceRnc(newSitesRecords[1].trim());
                    //target rnc
                    record.setTargetCell(newSitesRecords[2].trim());
                    //target lac
                    record.setTargetRNC(newSitesRecords[3].trim());
                    //target hcsPrio
                    record.setHcsPrio(newSitesRecords[4].trim());
                    //target qHcs
                    record.setqHcs(newSitesRecords[5].trim());
                    //target mobilityRelationType
                    record.setMobilityRelationType(newSitesRecords[6].trim());
                    //target qOffset1sn
                    record.setqOffset1sn(newSitesRecords[7].trim());
                    //target qOffset2sn
                    record.setqOffset2sn(newSitesRecords[8].trim());
                    
                    record.setRelationId(record.getSourceCell()+"_"+record.getTargetCell());
                    
                    newSitePlan.put(record.getRelationId(),record);
                }else if(newSitesRecords.length>2){
                    //System.out.println("The input record is invalid: "+line);
                    logger.logThis(SEVERITY.ERROR,"The input record is invalid: "+line,true);
                }
            }
        } catch (FileNotFoundException ex) {
            //System.out.println("File not found: "+filePath);
            logger.logThis(SEVERITY.ERROR,"File not found: "+filePath,true);
            Logger.getLogger(NewSiteReader.class.getName()).log(Level.SEVERE, null, ex);
            logger.logThis(SEVERITY.ERROR, ex.getMessage(), true);
        } catch (IOException ex) {
            //System.out.println("Error while reading file");
            logger.logThis(SEVERITY.ERROR,"Error while reading file",true);
            Logger.getLogger(NewSiteReader.class.getName()).log(Level.SEVERE, null, ex);
            logger.logThis(SEVERITY.ERROR, ex.getMessage(), true);
        }
        return newSitePlan;
    }
}
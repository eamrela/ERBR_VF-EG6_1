/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vfeg.erbr.main;

import com.vfeg.erbr.configuration.AppConf;
import com.vfeg.erbr.generator.XMLGenerator;
import com.vfeg.erbr.logger.LoggerProcess;
import com.vfeg.erbr.logger.SEVERITY;
import com.vfeg.erbr.mongo.MongoClient;
import com.vfeg.erbr.util.CutoverPlanReader;
import com.vfeg.erbr.util.CutoverRecord;
import com.vfeg.erbr.util.NewSiteReader;
import com.vfeg.erbr.util.NewSiteRecord;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author eelaamr
 */
public class Processor {
    
    private static LoggerProcess logger;
     
    private String cutoverPlanPath;
    private String newSiteRelationPlanPath;
    private String workingDir;
    private String cutoverSummary="";

    private TreeMap<String,CutoverRecord> cutoverRecords;
    private TreeMap<String,NewSiteRecord> newSiteRecords;
    
    
    public boolean initializeApp(){
        try{
        AppConf.initializeApp();
        logger = new LoggerProcess(Processor.class);
        System.out.println("Initializing MongoDB");
        MongoClient.initializeDB();
        return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        
    }
    
    public void createRunDirs(){
        System.out.println("Creating working directory");
        workingDir = workingDir+"/ERBR_v"+AppConf.getAppVersion()+"_"+AppConf.getMydateFile();
        new File(workingDir).mkdirs();
        AppConf.setWorkingDir(workingDir);
        System.out.println("Working Directory Created: "+workingDir);
        System.out.println("Copying plan to : "+workingDir);
        if(cutoverPlanPath!=null){
        copyFile(new File(cutoverPlanPath), new File(workingDir+"\\"+new File(cutoverPlanPath).getName()));
        }
        
    }
    
    private void copyFile(File source, File dest){
    InputStream is = null;
    OutputStream os = null;
    try {
        is = new FileInputStream(source);
        os = new FileOutputStream(dest);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) > 0) {
            os.write(buffer, 0, length);
        }
    }   catch (FileNotFoundException ex) {
            Logger.getLogger(Processor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Processor.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
        try {
            is.close();
            os.close();
        } catch (IOException ex) {
            Logger.getLogger(Processor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
    
    public void readCutoverFile(){
        System.out.println("Reading migration records");
        cutoverRecords = CutoverPlanReader.readCutoverFile(cutoverPlanPath);
        System.out.println("Cutover Plan Includes: "+cutoverRecords.size());
        for (Map.Entry<String, CutoverRecord> entry : cutoverRecords.entrySet()) {
            System.out.println(">"+entry.getKey());
        }
        
    }
    
    public String dumpLogFile() {
        String source = AppConf.getApplicationPath()+"/log";
        File srcDir = new File(source);

        String destination = AppConf.getApplicationPath()+"/DUMP_"+AppConf.getMydateFile();
        File destDir = new File(destination);

        try {
            FileUtils.copyDirectory(srcDir, destDir);
            return "DUMP file: "+destination;
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to create DUMP file";
        }
    }
    
    public void generateRNCFile() {
        System.out.println("Generating RNCs file");
        String fileContent = "RNCs that are involved in Creation/Deletion on each OSS\n";
               fileContent += "=======================================================\n\n";
        for (Map.Entry<String, TreeMap<String, String>> entry : XMLGenerator.getInvolvedRncs().entrySet()) {
            String key = entry.getKey();
            fileContent += "OSS: "+key+"\n";
            fileContent += "---------------\n";
            TreeMap<String, String> value = entry.getValue();
            for (Map.Entry<String, String> entry1 : value.entrySet()) {
                String key1 = entry1.getKey();
                fileContent += "> "+key1+"\n";
            }
        }
        PrintWriter writer;
        try {
            writer = new PrintWriter(new File(AppConf.getWorkingDir()+"/Involved_RNCs.txt"));
            writer.println(fileContent);
            writer.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Processor.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    public void generateSummary() {
        System.out.println("Generating Summary file");
        String fileContent = "Summary for the cutover activity\n";
               fileContent += "=======================================================\n\n";
               fileContent += getCutoverSummary();
        PrintWriter writer;
        try {
            writer = new PrintWriter(new File(AppConf.getWorkingDir()+"/Cutover_summary.txt"));
            writer.println(fileContent);
            writer.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Processor.class.getName()).log(Level.SEVERE, null, ex);
        } 
        resetCutoverSummary();
    }
    
    public void resetCutoverSummary(){
        cutoverSummary ="";
    }
    
    public void appendSummary(String str){
        cutoverSummary += str +"\n";
    }

    public String getCutoverSummary() {
        return cutoverSummary;
    }

    public void setCutoverPlanPath(String cutoverPlanPath) {
        this.cutoverPlanPath = cutoverPlanPath;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public void setNewSiteRelationPlanPath(String newSiteRelationPlanPath) {
        this.newSiteRelationPlanPath = newSiteRelationPlanPath;
    }
    
    
    
    // ------------------- NEW Site ---------------------------------//
    public void readNewSiteFile(){
        System.out.println("Reading New Site records");
        newSiteRecords = NewSiteReader.readNewSiteFile(newSiteRelationPlanPath);
        System.out.println("New Site Plan Includes: "+newSiteRecords.size());
        
    }
    
    
    //--------------------- APPLICATION LOGIC ------------------------//
    public void doCutover(){
        System.out.println("Going to generate cutover files...it might take a while");
        System.out.println("Please wait");
        String out="";
        for (Map.Entry<String, CutoverRecord> record : cutoverRecords.entrySet()) {
            System.out.println("Validating Record :"+record.getKey());
            logger.logThis(SEVERITY.INFO,"Validating Record :"+record.getKey(),true);
            if(record.getValue().validateRecord()){
                out = "Record: "+record.getKey()+" validation [OK]";
                appendSummary(out);
                logger.logThis(SEVERITY.INFO,out,true);
                
                out = record.getValue().fillOSSandCells();
                appendSummary(out);
                logger.logThis(SEVERITY.INFO,out,true);
                
                out = record.getValue().fillRelations();
                appendSummary(out);
                logger.logThis(SEVERITY.INFO,out,true);
                
                out = "Adjusting relations [Incoming/Outgoing] for IuB: "+record.getKey();
                appendSummary(out);
                logger.logThis(SEVERITY.INFO,out,true);
                record.getValue().performRelationEntities();
                out = "Finished adjusting relations [Incoming/Outgoing] for IuB: "+record.getKey();
                appendSummary(out);
                logger.logThis(SEVERITY.INFO,out,true);
                
                out = "Applying Cutover logic on all relations related to: "+record.getKey();
                appendSummary(out);
                logger.logThis(SEVERITY.INFO,out,true);
                out = "This might take a while, please wait...";
                appendSummary(out);
                logger.logThis(SEVERITY.INFO,out,true);
                
                out = "Fetching and Mapping ExternalUtranCells";
                appendSummary(out);
                logger.logThis(SEVERITY.INFO,out,true);
                
                out = record.getValue().performIncomingOutgoingFilling();
                appendSummary(out);
                logger.logThis(SEVERITY.INFO,out,true);
                
                out = "\nPreparing [UtranRelation] Deletion Files\n";
                out += "Preparing [ExternalUtranCell] Deletion Files\n";
                out += "Preparing [UtranRelation] Creation Files\n";
                out += "Preparing [ExternalUtranCell] Creation Files\n";
                appendSummary(out);
                logger.logThis(SEVERITY.INFO,out,true);
                record.getValue().generateXML();
                
                out = "\nWriting [UtranRelation] Deletion Files\n";
                out += "Writing [ExternalUtranCell] Deletion Files\n";
                out += "Writing [UtranRelation] Creation Files\n";
                out += "Writing [ExternalUtranCell] Creation Files\n";
                appendSummary(out);
                logger.logThis(SEVERITY.INFO,out,true);
            }
        }
        XMLGenerator.generateFiles();
        generateRNCFile();
        generateSummary();
        System.out.println("Finished generating rehoming files");
        System.out.println(". . . . . . . . . . . . . . . . . .");
        XMLGenerator.resetCollections();
    }
    
    public void doNewSiteRelation(){
       System.out.println("Going to generate cutover files...it might take a while");
        System.out.println("Please wait");
        String out="";
        for (Map.Entry<String, NewSiteRecord> record : newSiteRecords.entrySet()) {
          System.out.println("Validating Record :"+record.getKey());
            logger.logThis(SEVERITY.INFO,"Validating Record :"+record.getKey(),true);
            if(record.getValue().validateRecord()){
                
                out = "Record: "+record.getKey()+" validation [OK]";
                appendSummary(out);
                logger.logThis(SEVERITY.INFO,out,true);
                
                
                out = record.getValue().fillOSS();
                appendSummary(out);
                logger.logThis(SEVERITY.INFO,out,true);
                
                out = "Fetching External Cell";
                appendSummary(out);
                logger.logThis(SEVERITY.INFO,out,true);
                record.getValue().fetchExternalCell();
                
                out = "\nPreparing [UtranRelation] Creation Files\n";
                out += "Preparing [ExternalUtranCell] Creation Files\n";
                appendSummary(out);
                logger.logThis(SEVERITY.INFO,out,true);
                
                out = record.getValue().doLogic();
                appendSummary(out);
                logger.logThis(SEVERITY.INFO,out,true);
                
                out = "\nWriting [UtranRelation] Creation Files\n";
                out += "Writing [ExternalUtranCell] Creation Files\n";
                appendSummary(out);
                logger.logThis(SEVERITY.INFO,out,true);
            }
        }
        XMLGenerator.generateFiles();
        System.out.println("Finished generating new site relation files");
        System.out.println(". . . . . . . . . . . . . . . . . .");
        XMLGenerator.resetCollections();
    }
    
}

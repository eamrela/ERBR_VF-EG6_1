/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vfeg.erbr.generator;

import com.vfeg.erbr.configuration.AppConf;
import com.vfeg.erbr.logger.LoggerProcess;
import com.vfeg.erbr.logger.SEVERITY;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eelaamr
 */
public class XMLGenerator {
    
    private final static LoggerProcess logger = new LoggerProcess(XMLGenerator.class);
    private static TreeMap<String,Integer> counts = new TreeMap<String,Integer>();
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static TreeMap<String,TreeMap<String,String>> externalUtranDeletion = new TreeMap<String, TreeMap<String, String>>();;
    private static TreeMap<String,TreeMap<String,String>> utranRelationDeletion = new TreeMap<String, TreeMap<String, String>>();
    private static TreeMap<String,TreeMap<String,String>> externalUtranCreation = new TreeMap<String, TreeMap<String, String>>();
    private static TreeMap<String,TreeMap<String,String>> utranRelationCreation = new TreeMap<String, TreeMap<String, String>>();
    private static TreeMap<String,TreeMap<String,String>> paramterAdjustmentCreation = new TreeMap<String, TreeMap<String, String>>();
    private static TreeMap<String,TreeMap<String,String>> involvedRncs = new TreeMap<String, TreeMap<String, String>>();
    private static TreeMap<String,TreeMap<Integer,String>> fileFooters = new TreeMap<String,TreeMap<Integer,String>>();
    
    public static void resetCollections(){
        externalUtranCreation = new TreeMap<String, TreeMap<String, String>>();
        utranRelationDeletion = new TreeMap<String, TreeMap<String, String>>();
        externalUtranDeletion = new TreeMap<String, TreeMap<String, String>>();
        utranRelationCreation = new TreeMap<String, TreeMap<String, String>>();
        paramterAdjustmentCreation = new TreeMap<String, TreeMap<String, String>>();
        involvedRncs = new TreeMap<String, TreeMap<String, String>>();
        counts = new TreeMap<String,Integer>();
        fileFooters = new TreeMap<String,TreeMap<Integer,String>>();
    }
    
    public static void addScript(String oss,String type,String id,String script){
        if(type.equals("EXTERNAL_DELETION")){
            if(externalUtranDeletion.containsKey(oss)){
                externalUtranDeletion.get(oss).put(id, script);
            }else{
                externalUtranDeletion.put(oss, new TreeMap<String, String>());
                externalUtranDeletion.get(oss).put(id, script);
            }
        }else if(type.equals("EXTERNAL_CREATION")){
            if(externalUtranCreation.containsKey(oss)){
                externalUtranCreation.get(oss).put(id, script);
            }else{
                externalUtranCreation.put(oss, new TreeMap<String, String>());
                externalUtranCreation.get(oss).put(id, script);
            }
        }else if(type.equals("RELATION_DELETION")){
            if(utranRelationDeletion.containsKey(oss)){
                utranRelationDeletion.get(oss).put(id, script);
            }else{
                utranRelationDeletion.put(oss, new TreeMap<String, String>());
                utranRelationDeletion.get(oss).put(id, script);
            }
        }else if(type.equals("RELATION_CREATION")){
            if(utranRelationCreation.containsKey(oss)){
                utranRelationCreation.get(oss).put(id, script);
            }else{
                utranRelationCreation.put(oss, new TreeMap<String, String>());
                utranRelationCreation.get(oss).put(id, script);
            }
        }else if(type.equals("PARAMTER_ADJUSTMENT")){
            if(paramterAdjustmentCreation.containsKey(oss)){
                paramterAdjustmentCreation.get(oss).put(id, script);
            }else{
                paramterAdjustmentCreation.put(oss, new TreeMap<String, String>());
                paramterAdjustmentCreation.get(oss).put(id, script);
            }
        }
    }
    
    public static void addRNC(String OSS,String RNC){
        if(involvedRncs.containsKey(OSS)){
            involvedRncs.get(OSS).put(RNC, "");
        }else{
            involvedRncs.put(OSS, new TreeMap<String, String>());
            involvedRncs.get(OSS).put(RNC, "");
        }
    }

    public static TreeMap<String, TreeMap<String, String>> getInvolvedRncs() {
        return involvedRncs;
    }
    
    public static void appendToFile(String fileName,String script,String targetOSS){
        File mtxDir = new File(AppConf.getWorkingDir()+"/OSS_"+targetOSS);
        if(!mtxDir.exists()){
                mtxDir.mkdirs();
        }
        String filePath = null;
        if(fileName.contains("DEFINE_UtranRelation") || fileName.contains("ADJUSTMENT_ExternalUtranCell")){
            if(getCounter("relationCountForFile"+targetOSS)<AppConf.getRelationPerFile()){
            filePath = AppConf.getWorkingDir()+"/OSS_"+targetOSS+"/"+fileName+"_"+getCounter("filesCounters"+targetOSS)+".xml";
            addCounter("relationCountForFile"+targetOSS, false);
            }else{
            filePath = AppConf.getWorkingDir()+"/OSS_"+targetOSS+"/"+fileName+"_"+getCounter("filesCounters"+targetOSS)+".xml";    
            addCounter("relationCountForFile"+targetOSS, true);
            }
        
        
            if(getCounter("breakPoint"+filePath)<AppConf.getBreakLinePerRelation()){
            addCounter("breakPoint"+filePath, false);
            }else{
            script += "<!-- @Annotation:breakpoint:COMMIT_NO_41993;Commit\n" +
                       "Break Point Added by ERBR Phase -->";
            addCounter("breakPoint"+filePath, true);
            }
        
        }else{
            filePath = AppConf.getWorkingDir()+"/OSS_"+targetOSS+"/"+fileName+".xml";   
        }

        
        File file = new File(filePath);
        PrintWriter out = null;
        if ( file.exists() && !file.isDirectory() ) {
            try {
                out = new PrintWriter(new FileOutputStream(file, true));
                out.append(script+"\n");
                out.flush();
                out.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(XMLGenerator.class.getName()).log(Level.SEVERE, null, ex);
                logger.logThis(SEVERITY.ERROR,ex.getMessage(),true);
            }
        }
        else {
            try {
                out = new PrintWriter(file);
                out.append(XMLGenerator.getFileHeader());
                fileFooters.put(filePath, new TreeMap<Integer, String>());
                fileFooters.get(filePath).put(2, XMLGenerator.getFileFooter());
//                if(fileName.contains("UtranCell") && !fileName.contains("External") && !fileName.contains("Delete")){
//                out.append(XMLGenerator.getUtranBundleHeader(targetRNC));
//                fileFooters.get(filePath).put(1, XMLGenerator.getUtranBundleFooter());
//                }
                
                out.append(script+"\n");
                out.flush();
                out.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(XMLGenerator.class.getName()).log(Level.SEVERE, null, ex);
                logger.logThis(SEVERITY.ERROR,ex.getMessage(),true);
            }
        }
    }
    
    public static void addFooters(){
        for (Map.Entry<String, TreeMap<Integer, String>> footer : fileFooters.entrySet()) {
            for (Map.Entry<Integer, String> innerFooter : footer.getValue().entrySet()) {
                appendFooter(footer.getKey(), innerFooter.getValue());
            }
        }
    }
    
    public static void appendFooter(String filePath,String footer){
        
        
        File file = new File(filePath);
        PrintWriter out = null;
        if ( file.exists() && !file.isDirectory() ) {
            try {
                out = new PrintWriter(new FileOutputStream(file, true));
                out.append(footer+"\n");
                out.flush();
                out.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(XMLGenerator.class.getName()).log(Level.SEVERE, null, ex);
                logger.logThis(SEVERITY.ERROR,ex.getMessage(),true);
            }
        }
    }
    
    public static void addCounter(String countName,boolean reset){
        if(reset){
            counts.put(countName, 0);
        }
        else if(counts.containsKey(countName)){
            counts.put(countName, (counts.get(countName)+1));
        }else{
            counts.put(countName, 1);
        }
    }
    
    public static Integer getCounter(String countName){
        if(counts.containsKey(countName)){
            return counts.get(countName);
        }else{
            return 1;
        }
    }
    
    public static String getFileHeader(){
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<!-- Authour: Amr El Ansary (EELAAMR - amr.elansary@ericson.com) MMEA MS ITOSS&Tools -->\n"+
                AppConf.getBulkCMHeader() +"\n"+
                "<fileHeader fileFormatVersion=\""+AppConf.getFileFormatVersion()+"\" senderName=\""+AppConf.getSenderName()+"\" vendorName=\""+AppConf.getVendorName()+"\"/>\n" +
                "<configData dnPrefix=\""+AppConf.getDnPrefix()+"\">\n" +
                "  <xn:SubNetwork id=\""+AppConf.getSubNetwork()+"\">\n";
    }
     
    public static String getFileFooter(){
        return "\n</xn:SubNetwork>\n" +
                "</configData>\n" +
                "<fileFooter dateTime=\""+sdf.format(new Date()).replaceAll(" ", "T")+"\"/>\n" +
                "</bulkCmConfigDataFile>";
    }
    
    public static String getUtranBundleHeader(String targetRNC){
        return "<xn:SubNetwork id=\""+targetRNC+"\">\n" +
                "<xn:MeContext id=\""+targetRNC+"\">\n" +
                "<xn:ManagedElement id=\"1\">\n" +
                "<un:RncFunction id=\"1\">\n";
    }
    
    public static String getUtranBundleFooter(){
        return "\n</un:RncFunction>\n" +
                "</xn:ManagedElement>\n" +
                "</xn:MeContext>\n" +
                "</xn:SubNetwork>\n";
    }
    
    public static String getUtranRelationBundleHeader(String targetRNC,String utranCell){
        return "<xn:SubNetwork id=\""+targetRNC+"\">\n" +
                "<xn:MeContext id=\""+targetRNC+"\">\n" +
                "<xn:ManagedElement id=\"1\">\n" +
                "<un:RncFunction id=\"1\">\n"
                + "<un:UtranCell id=\""+utranCell+"\">\n";
    }
    
    public static String getUtranRelationBundleFooter(){
        return "\n</un:UtranCell>\n"
                + "</un:RncFunction>\n" +
                "</xn:ManagedElement>\n" +
                "</xn:MeContext>\n" +
                "</xn:SubNetwork>\n";
    }
    
    
    public static void generateFiles(){
        for (Map.Entry<String, TreeMap<String, String>> entry : externalUtranDeletion.entrySet()) {
            for (Map.Entry<String, String> entry1 : entry.getValue().entrySet()) {
                appendToFile("DELETE_ExternalUtranCell", entry1.getValue(), entry.getKey());
            }
        }
        for (Map.Entry<String, TreeMap<String, String>> entry : utranRelationDeletion.entrySet()) {
            for (Map.Entry<String, String> entry1 : entry.getValue().entrySet()) {
                appendToFile("DELETE_UtranRelation", entry1.getValue(), entry.getKey());
            }
        }
        for (Map.Entry<String, TreeMap<String, String>> entry : externalUtranCreation.entrySet()) {
            for (Map.Entry<String, String> entry1 : entry.getValue().entrySet()) {
                appendToFile("DEFINE_ExternalUtranCell", entry1.getValue(), entry.getKey());
            }
        }
        for (Map.Entry<String, TreeMap<String, String>> entry : utranRelationCreation.entrySet()) {
            for (Map.Entry<String, String> entry1 : entry.getValue().entrySet()) {
                appendToFile("DEFINE_UtranRelation", entry1.getValue(), entry.getKey());
            }
        }
        
        for (Map.Entry<String, TreeMap<String, String>> entry : paramterAdjustmentCreation.entrySet()) {
            for (Map.Entry<String, String> entry1 : entry.getValue().entrySet()) {
                appendToFile("ADJUSTMENT_ExternalUtranCell", entry1.getValue(), entry.getKey());
            }
        }
        addFooters();
    }
    
    
}

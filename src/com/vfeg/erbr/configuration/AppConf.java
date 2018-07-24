/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vfeg.erbr.configuration;

import com.vfeg.erbr.logger.LoggerProcess;
import com.vfeg.erbr.logger.SEVERITY;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eelaamr
 */
public class AppConf {
    private static String appVersion = "6_1";
    //<editor-fold defaultstate="collapsed" desc="vars">
    private static String confFilePath;
    private static String mongoIp;
    private static Integer mongoPort;
    private static Integer relationPerFile;
    private static Integer breakLinePerRelation;
    private static LoggerProcess logger; 
    private static String applicationPath;
    private static String mydateFile = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    private static String MCC;
    private static String MNC;
    private static final String MNCLength="2";
    private static String vsDataFormatVersion;
    private static String fileFormatVersion;
    private static final String senderName="eelaamr";
    private static final String vendorName="Ericsson";
    private static final String dnPrefix="DC=www.ericsson.com";
    private static String SubNetwork;
    private static String BulkCMHeader;
    private static String workingDir;
    private static String loggerConf;
      private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static Date lastOSSOneUpdateUtranCell;
    private static Date lastOSSTwoUpdateUtranCell;
    private static Date lastOSSOneUpdateUtranRelations;
    private static Date lastOSSTwoUpdateUtranRelations;
    private static Date lastOSSOneUpdateExternalUtranCell;
    private static Date lastOSSTwoUpdateExternalUtranCell;

    
    //</editor-fold>

    public static void initLogger(){
        setLoggerConf(applicationPath+"/conf/log4j.properties");
        logger = new LoggerProcess(AppConf.class);
    }
    
    public static void initializeApp(){
        setConfFilePath(applicationPath+"/conf/ERBR.conf");
        if(confFilePath!=null){
            initLogger();
            try {
                RandomAccessFile raf = new RandomAccessFile(new File(confFilePath), "r");
                String line = null;
                while((line=raf.readLine())!=null){
                    if(line.contains("mongoIp")){
                    mongoIp = line.split("\\t")[1];
                    continue;
                    }
                    if(line.contains("mongoPort")){
                    mongoPort = Integer.parseInt(line.split("\\t")[1]);
                    continue;
                    }
                    if(line.contains("relationPerFile")){
                    relationPerFile = Integer.parseInt(line.split("\\t")[1]);
                    continue;
                    }
                 if(line.contains("breakLinePerRelation")){
                    breakLinePerRelation = Integer.parseInt(line.split("\\t")[1]);
                    continue;
                }
                 // XML CONF
                 if(line.contains("mcc")){
                    MCC = line.split("\\t")[1];
                    continue;
                }
                 if(line.contains("mnc")){
                    MNC = line.split("\\t")[1];
                    continue;
                }
                if(line.contains("vsDataFormatVersion")){
                    vsDataFormatVersion = line.split("\\t")[1];
                    continue;
                }
                 if(line.contains("fileFormatVersion")){
                    fileFormatVersion = line.split("\\t")[1];
                    continue;
                }
                 if(line.contains("subNetwork")){
                    SubNetwork = line.split("\\t")[1];
                    continue;
                }
                 if(line.contains("bulkCMHeader")){
                    BulkCMHeader = line.split("\\t")[1];
                }
                 
                 
                    
                }
            } catch (FileNotFoundException ex) {
                //System.out.println("Configuration file not found!");
                logger.logThis(SEVERITY.ERROR, "Configuration file not found!",true);
                Logger.getLogger(AppConf.class.getName()).log(Level.SEVERE, null, ex);
                logger.logThis(SEVERITY.ERROR, ex.getMessage(),true);
            } catch (IOException ex) {
                //System.out.println("Error while reading Configuration file");
                logger.logThis(SEVERITY.ERROR,"Error while reading Configuration file",true);
                Logger.getLogger(AppConf.class.getName()).log(Level.SEVERE, null, ex);
                logger.logThis(SEVERITY.ERROR, ex.getMessage(),true);
            }
        }else{
            //System.out.println("Configuration file path is missing");
            logger.logThis(SEVERITY.ERROR, "Configuration file path is missing",true);
        }
    }
 
    
    //<editor-fold defaultstate="collapsed" desc="Setters/Getters">
    
    public static void setWorkingDir(String workingDir) {
        AppConf.workingDir = workingDir;
    }

    public static String getAppVersion() {
        return appVersion;
    }

    public static String getMNCLength() {
        return MNCLength;
    }

    public static String getSenderName() {
        return senderName;
    }

    public static String getVendorName() {
        return vendorName;
    }

    public static String getDnPrefix() {
        return dnPrefix;
    }

    
    
    public static void setLoggerConf(String loggerConf) {
        AppConf.loggerConf = loggerConf;
    }

    public static String getWorkingDir() {
        return workingDir;
    }

    public static String getLoggerConf() {
        return loggerConf;
    }

    public static String getConfFilePath() {
        return confFilePath;
    }

    public static void setConfFilePath(String confFilePath) {
        AppConf.confFilePath = confFilePath;
    }

    public static String getMongoIp() {
        return mongoIp;
    }

    public static void setMongoIp(String mongoIp) {
        AppConf.mongoIp = mongoIp;
    }

    public static Integer getMongoPort() {
        return mongoPort;
    }

    public static void setMongoPort(Integer mongoPort) {
        AppConf.mongoPort = mongoPort;
    }

    public static Integer getRelationPerFile() {
        return relationPerFile;
    }

    public static void setRelationPerFile(Integer relationPerFile) {
        AppConf.relationPerFile = relationPerFile;
    }

    public static Integer getBreakLinePerRelation() {
        return breakLinePerRelation;
    }

    public static void setBreakLinePerRelation(Integer breakLinePerRelation) {
        AppConf.breakLinePerRelation = breakLinePerRelation;
    }

    public static LoggerProcess getLogger() {
        return logger;
    }

    public static void setLogger(LoggerProcess logger) {
        AppConf.logger = logger;
    }

    public static String getApplicationPath() {
        return applicationPath;
    }

    public static void setApplicationPath(String applicationPath) {
        AppConf.applicationPath = applicationPath;
    }

    public static String getMydateFile() {
        return mydateFile;
    }

    public static void setMydateFile(String mydateFile) {
        AppConf.mydateFile = mydateFile;
    }

    public static String getMCC() {
        return MCC;
    }

    public static void setMCC(String MCC) {
        AppConf.MCC = MCC;
    }

    public static String getMNC() {
        return MNC;
    }

    public static void setMNC(String MNC) {
        AppConf.MNC = MNC;
    }

    public static String getVsDataFormatVersion() {
        return vsDataFormatVersion;
    }

    public static void setVsDataFormatVersion(String vsDataFormatVersion) {
        AppConf.vsDataFormatVersion = vsDataFormatVersion;
    }

    public static String getFileFormatVersion() {
        return fileFormatVersion;
    }

    public static void setFileFormatVersion(String fileFormatVersion) {
        AppConf.fileFormatVersion = fileFormatVersion;
    }

    public static String getSubNetwork() {
        return SubNetwork;
    }

    public static void setSubNetwork(String SubNetwork) {
        AppConf.SubNetwork = SubNetwork;
    }

    public static String getBulkCMHeader() {
        return BulkCMHeader;
    }

    public static void setBulkCMHeader(String BulkCMHeader) {
        AppConf.BulkCMHeader = BulkCMHeader;
    }
    

    public static SimpleDateFormat getSdf() {
        return sdf;
    }

    public static void setSdf(SimpleDateFormat sdf) {
        AppConf.sdf = sdf;
    }

    public static Date getLastOSSOneUpdateUtranCell() {
        return lastOSSOneUpdateUtranCell;
    }

    public static void setLastOSSOneUpdateUtranCell(Date lastOSSOneUpdateUtranCell) {
        AppConf.lastOSSOneUpdateUtranCell = lastOSSOneUpdateUtranCell;
    }

    public static Date getLastOSSTwoUpdateUtranCell() {
        return lastOSSTwoUpdateUtranCell;
    }

    public static void setLastOSSTwoUpdateUtranCell(Date lastOSSTwoUpdateUtranCell) {
        AppConf.lastOSSTwoUpdateUtranCell = lastOSSTwoUpdateUtranCell;
    }

    public static Date getLastOSSOneUpdateUtranRelations() {
        return lastOSSOneUpdateUtranRelations;
    }

    public static void setLastOSSOneUpdateUtranRelations(Date lastOSSOneUpdateUtranRelations) {
        AppConf.lastOSSOneUpdateUtranRelations = lastOSSOneUpdateUtranRelations;
    }

    public static Date getLastOSSTwoUpdateUtranRelations() {
        return lastOSSTwoUpdateUtranRelations;
    }

    public static void setLastOSSTwoUpdateUtranRelations(Date lastOSSTwoUpdateUtranRelations) {
        AppConf.lastOSSTwoUpdateUtranRelations = lastOSSTwoUpdateUtranRelations;
    }

    public static Date getLastOSSOneUpdateExternalUtranCell() {
        return lastOSSOneUpdateExternalUtranCell;
    }

    public static void setLastOSSOneUpdateExternalUtranCell(Date lastOSSOneUpdateExternalUtranCell) {
        AppConf.lastOSSOneUpdateExternalUtranCell = lastOSSOneUpdateExternalUtranCell;
    }

    public static Date getLastOSSTwoUpdateExternalUtranCell() {
        return lastOSSTwoUpdateExternalUtranCell;
    }

    public static void setLastOSSTwoUpdateExternalUtranCell(Date lastOSSTwoUpdateExternalUtranCell) {
        AppConf.lastOSSTwoUpdateExternalUtranCell = lastOSSTwoUpdateExternalUtranCell;
    }
    //</editor-fold>
    
}

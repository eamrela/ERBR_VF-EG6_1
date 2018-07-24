/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vfeg.erbr.logger;


import com.vfeg.erbr.configuration.AppConf;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author mostafa.fathy
 */
public class LoggerProcess {
        private static Logger logger ;
        public static String loggingMessage="";
//        public static String loggingMessageAcc="";
        public static String configurationFile;
        private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        /**
         * 
         * @param clazz  Set your class name.
         */
    public LoggerProcess(Class clazz){
           logger = Logger.getLogger(clazz);
           configurationFile = AppConf.getLoggerConf();
            PropertyConfigurator.configure(configurationFile);
    }
    
    /**
     * 
     * @param severity
     * @param message
     * @param check if true print in GUI.
     */
    
    //<editor-fold defaultstate="collapsed" desc="LogThis Message">
    public void logThis(SEVERITY severity, String message, boolean check){
        if( null != severity)
            switch (severity) {
                case INFO:
                    logger.info(message);
                    break;
                case DEBUG:
                    logger.debug(message);
                    break;
                case ERROR:
                    logger.error(message);
                    break;
                default:
                    break;
            }
        
        if(check){
//           loggingMessageAcc += severity+" "+sdf.format(new Date())+" - "+message+"\n";
           loggingMessage = severity+" "+sdf.format(new Date())+" - "+message+"\n";
            System.out.println(loggingMessage);
        }   
    }
//</editor-fold>
   
    
    
    /**
     * 
     * @return  Return Logging Message To System.
     */
    public static String getLoggingMessage(){
    return loggingMessage;
    }
    
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vfeg.erbr.util;

import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import static com.mongodb.client.model.Filters.and;
import com.vfeg.erbr.configuration.AppConf;
import com.vfeg.erbr.generator.XMLGenerator;
import com.vfeg.erbr.mongo.MongoClient;
import java.util.Date;
import java.util.TreeMap;
import org.bson.Document;

/**
 *
 * @author eelaamr
 */
public class NewSiteRecord {
 
     private Document tmp;
    // Inputs from User 
    private String relationId;
    private String sourceCell;
    private String sourceRnc;
    private String targetCell;
    private String targetRNC;
    
    private String hcsPrio;
    private String qHcs;
    private String mobilityRelationType;
    private String qOffset1sn;
    private String qOffset2sn;
    
    // auto filled 
    private String sourceOss;
    private String targetOss;
    
    private boolean externalNotDefined=false;
    private Document destinationCellExternal; // incoming, outgoing
    private String externalUtranCellName;
    

    
    public boolean validateRecord(){
        if(sourceCell.length()>3 && 
                sourceRnc.length()>2 && 
                targetCell.length()>2 && 
                 targetRNC.length()>2){
            return true;
        }
        return false;
    }
    
    public String fillOSS(){
        String result = "Source Cell/RNC: "+sourceCell+" / "+sourceRnc+"\n";
        result += "Target Cell/RNC: "+targetCell+" / "+targetRNC+"\n";
        result += "Identifying OSSs related to Source/Target RNCs \n";
        Document tmp;
        
        tmp = MongoClient.getUtranCellCollection().find(
                Filters.eq("rnc", sourceRnc)).first();
        if(tmp!=null){
            sourceOss = tmp.get("ossNr")+"";
        }
        
        tmp = MongoClient.getUtranCellCollection().find(
                Filters.eq("rnc", targetRNC)).first();
        if(tmp!=null){
            targetOss = tmp.get("ossNr")+"";
        }
        result += "Source OSS ["+sourceRnc+"]: "+sourceOss+" \n";
        result += "Target OSS ["+targetRNC+"]: "+targetOss+" \n";
        return result;
    }
    
      public void fetchExternalCell(){
        String cellName = targetCell;
        String oss = sourceOss;
        String cId=null;
        if(cellName!=null){
            if(cellName.length()>2){
                
                tmp = MongoClient.getExternalUtranCellCollection().find(Filters.eq("_id", cellName)).first();
                if(tmp!=null){ // found normal external cell
                    destinationCellExternal = tmp;
                 }else{
                    tmp = MongoClient.getUtranCellCollection().find(Filters.eq("_id", cellName)).first();
                    if(tmp!=null){
                        cId = tmp.getString("cId");
                    }
                 }
                
                    if(cellName.contains("6022-")){
                    cId = cellName.split("-")[2];
                    }
                
                if(cId!=null){
                tmp = MongoClient.getExternalUtranCellCollection().find(
                and(Filters.regex("_id", "6022-.*-"+cId),Filters.eq("ossNr", Integer.parseInt(oss)))).first();
                if(tmp!=null){
                     destinationCellExternal = tmp;
                     tmp = MongoClient.getUtranCellCollection().find(
                    and(Filters.eq("cId", destinationCellExternal.get("cId")),Filters.eq("rnc", "RNC"+destinationCellExternal.get("rncId")))).first();
                     if(tmp!=null){
                         externalUtranCellName = tmp.getString("_id");
                     }
                }else{
                    tmp = MongoClient.getExternalUtranCellCollection().find(Filters.regex("_id", "6022-.*-"+cId)).first();
                    if(tmp!=null){
                       destinationCellExternal = tmp;
                     tmp = MongoClient.getUtranCellCollection().find(
                    and(Filters.eq("cId", destinationCellExternal.get("cId")),Filters.eq("rnc", "RNC"+destinationCellExternal.get("rncId")))).first();
                     if(tmp!=null){
                         externalUtranCellName = tmp.getString("_id");
                     } 
                    }else{
                        externalNotDefined = true;
                        tmp = MongoClient.getUtranCellCollection().find(Filters.eq("_id", cellName)).first();
                        if(tmp!=null){ // found normal external cell
                            destinationCellExternal = tmp; 
                         }
                    }
                }
                }
            }
        }
        
    }
      
    public String doLogic(){
         String res = "";
         if(sourceOss.equals(targetOss)){
             //-> create relation directly 
             res += "Relation: "+relationId+" has source and destination on the same OSS";
             XMLGenerator.addScript(sourceOss,"RELATION_CREATION",
                                            relationId,
                                            generateUtranRelations(sourceOss, sourceRnc,sourceCell,
                                                    getDestinationNameInternal(targetCell,targetRNC),targetCell));
         }else{
             res += "Relation: "+relationId+" has source and destination on different OSS";
             res += "Going to check if external cell is defined on source OSS";
             if(externalNotDefined){
                 res += "External Cell not defined";
                 res += "Defining External Cell";
                XMLGenerator.addScript(sourceOss,
                                    "EXTERNAL_CREATION", 
                                     destinationCellExternal.getString("_id"), 
                                     generateExternalUtranCell(sourceOss, targetRNC, null));  
             }
            //-> Define Outgoing Relations on TaregtOSS
            XMLGenerator.addScript(sourceOss,"RELATION_CREATION",
                                    relationId,
                                    generateUtranRelations(sourceOss, sourceRnc,sourceCell,
                                            getDestinationNameExternal(destinationCellExternal),targetCell));
             
         }
         return res;
    }
    
    //<editor-fold defaultstate="collapsed" desc="Setter/Getter">
    
    public String getSourceCell() {
        return sourceCell;
    }

   
    public String getRelationId() {
        return relationId;
    }

    public void setRelationId(String relationId) {
        this.relationId = relationId;
    }

    
    public void setSourceCell(String sourceCell) {
        this.sourceCell = sourceCell;
    }

    public String getSourceRnc() {
        return sourceRnc;
    }

    public void setSourceRnc(String sourceRnc) {
        this.sourceRnc = sourceRnc;
    }

    public String getTargetCell() {
        return targetCell;
    }

    public void setTargetCell(String targetCell) {
        this.targetCell = targetCell;
    }

    public String getTargetRNC() {
        return targetRNC;
    }

    public void setTargetRNC(String targetRNC) {
        this.targetRNC = targetRNC;
    }

    public String getSourceOss() {
        return sourceOss;
    }

    public void setSourceOss(String sourceOss) {
        this.sourceOss = sourceOss;
    }

    public String getTargetOss() {
        return targetOss;
    }

    public void setTargetOss(String targetOss) {
        this.targetOss = targetOss;
    }


    public String getHcsPrio() {
        return hcsPrio;
    }

    public void setHcsPrio(String hcsPrio) {
        this.hcsPrio = hcsPrio;
    }

    public String getqHcs() {
        return qHcs;
    }

    public void setqHcs(String qHcs) {
        this.qHcs = qHcs;
    }

    public String getMobilityRelationType() {
        return mobilityRelationType;
    }

    public void setMobilityRelationType(String mobilityRelationType) {
        this.mobilityRelationType = mobilityRelationType;
    }

    public String getqOffset1sn() {
        return qOffset1sn;
    }

    public void setqOffset1sn(String qOffset1sn) {
        this.qOffset1sn = qOffset1sn;
    }

    public String getqOffset2sn() {
        return qOffset2sn;
    }

    public void setqOffset2sn(String qOffset2sn) {
        this.qOffset2sn = qOffset2sn;
    }
    //</editor-fold>
    
    
    public String getDestinationNameInternal(String destination,String destinationRnc){
        return "SubNetwork="+AppConf.getSubNetwork()+",SubNetwork="+destinationRnc+",MeContext="+destinationRnc+",ManagedElement=1,RncFunction=1,UtranCell="+destination;

    }
    
    public String getDestinationNameExternal(Document destination){
        if(destination!=null){
        return "SubNetwork="+AppConf.getSubNetwork()+",ExternalUtranCell="+destinationCellExternal.getString("_id");        
        }
        return null;
    }
    
    
    
    public String generateUtranRelations(
            String oss,String targetRnc,String source,String destination,String destName){
        try{
            //<editor-fold defaultstate="collapsed" desc="Param">
        String utranRelationStr="";
        String id  = "G"+source+"-G"+destName;
 
        utranRelationStr = 
"<un:UtranRelation id=\""+id+"\" modifier=\"create\">\n" +
"    <un:attributes>\n" +
"     <un:adjacentCell>"+destination+"</un:adjacentCell>\n" +
"    </un:attributes>\n" +
" <xn:VsDataContainer id=\""+id+"\" modifier=\"create\">\n" +
"  <xn:attributes>\n" +
" <xn:vsDataType>vsDataUtranRelation</xn:vsDataType>\n" +
" <xn:vsDataFormatVersion>"+AppConf.getVsDataFormatVersion()+"</xn:vsDataFormatVersion>\n" +
"    <es:vsDataUtranRelation>\n" +
"     <es:qOffset1sn>"+qOffset1sn+"</es:qOffset1sn>\n" +
"     <es:qOffset2sn>"+qOffset2sn+"</es:qOffset2sn>\n" +
"     <es:loadSharingCandidate>0</es:loadSharingCandidate>\n" +
"     <es:selectionPriority>0</es:selectionPriority>\n" +
"     <es:frequencyRelationType>0</es:frequencyRelationType>\n" +
"     <es:nodeRelationType>0</es:nodeRelationType>\n" +
"    <es:hcsSib11Config>\n" +
"     <es:penaltyTime>0</es:penaltyTime>\n" +
"     <es:hcsPrio>"+hcsPrio+"</es:hcsPrio>\n" +
"     <es:qHcs>"+qHcs+"</es:qHcs>\n" +
"     <es:temporaryOffset2>0</es:temporaryOffset2>\n" +
"     <es:temporaryOffset1>0</es:temporaryOffset1>\n" +
"    </es:hcsSib11Config>\n" +
"     <es:mobilityRelationType>"+mobilityRelationType+"</es:mobilityRelationType>\n" +
"     <es:createdBy>Operator</es:createdBy>\n" +
"     <es:creationTime>"+AppConf.getSdf().format(new Date())+"</es:creationTime>\n" +
"    </es:vsDataUtranRelation>\n" +
" </xn:attributes>\n" +
" </xn:VsDataContainer>\n" +
"</un:UtranRelation>";
        
        utranRelationStr = 
                "<xn:SubNetwork id=\""+targetRnc+"\">\n" +
                "<xn:MeContext id=\""+targetRnc+"\">\n" +
                "<xn:ManagedElement id=\"1\">\n" +
                "<un:RncFunction id=\"1\">\n" +
                "<un:UtranCell id=\""+source+"\" modifier=\"update\">\n"+
                utranRelationStr+
                "</un:UtranCell>\n" +
                "</un:RncFunction>\n" +
                "</xn:ManagedElement>\n" +
                "</xn:MeContext>\n" +
                "</xn:SubNetwork>";
        
         return utranRelationStr;
        //</editor-fold>
        }catch(Exception e){
            e.printStackTrace();
            return "";
        }
    }
    
     public String generateExternalUtranCell(String oss,String rncId,String targetLac){
        try{
        //<editor-fold defaultstate="collapsed" desc="Param">
        String externalUtranCellStr=
        "<un:ExternalUtranCell id=\""+(externalUtranCellName!=null?externalUtranCellName:destinationCellExternal.getString("_id"))+"\" modifier=\"create\">\n" +
        "    <un:attributes>\n" +
        "     <un:cId>"+destinationCellExternal.getString("cId")+"</un:cId>\n";
        
        externalUtranCellStr += "     <un:lac>"+(targetLac!=null?targetLac:destinationCellExternal.get("lac"))+"</un:lac>\n";
        
        externalUtranCellStr +="     <un:mcc>"+AppConf.getMCC()+"</un:mcc>\n" +
        "     <un:mnc>"+AppConf.getMNC()+"</un:mnc>\n" +
        "     <un:primaryCpichPower>"+destinationCellExternal.get("primaryCpichPower")+"</un:primaryCpichPower>\n" +
        "     <un:primaryScramblingCode>"+destinationCellExternal.get("primaryScramblingCode")+"</un:primaryScramblingCode>\n" +
        "     <un:rac>"+destinationCellExternal.get("rac")+"</un:rac>\n" +
        "     <un:rncId>"+(rncId!=null?rncId.replaceAll("\\D+", ""):destinationCellExternal.get("rnc"))+"</un:rncId>\n" +
        "     <un:uarfcnDl>"+destinationCellExternal.get("uarfcnDl")+"</un:uarfcnDl>\n" +
        "     <un:uarfcnUl>"+destinationCellExternal.get("uarfcnUl")+"</un:uarfcnUl>\n" +
        "     <un:userLabel>"+destinationCellExternal.get("userLabel")+"</un:userLabel>\n" +
        "    </un:attributes>\n" +
        " <xn:VsDataContainer id=\""+(externalUtranCellName!=null?externalUtranCellName:destinationCellExternal.getString("_id"))+"\" modifier=\"create\">\n" +
        "  <xn:attributes>\n" +
        " <xn:vsDataType>vsDataExternalUtranCell</xn:vsDataType>\n" +
        " <xn:vsDataFormatVersion>"+AppConf.getVsDataFormatVersion()+"</xn:vsDataFormatVersion>\n" +
        "    <es:vsDataExternalUtranCell>\n" +
        "     <es:individualOffset>"+destinationCellExternal.get("individualOffset")+"</es:individualOffset>\n" +
        "     <es:maxTxPowerUl>"+destinationCellExternal.get("maxTxPowerUl")+"</es:maxTxPowerUl>\n" +
        "     <es:qQualMin>"+destinationCellExternal.get("qQualMin")+"</es:qQualMin>\n" +
        "     <es:qRxLevMin>"+destinationCellExternal.get("qRxLevMin")+"</es:qRxLevMin>\n" +
        "     <es:agpsEnabled>"+destinationCellExternal.get("agpsEnabled")+"</es:agpsEnabled>\n" +
        "    <es:cellCapability>\n" +
        "     <es:fdpchSupport>"+(destinationCellExternal.get("cellCapability")!=null?
            ((Document)destinationCellExternal.get("cellCapability")).get("fdpchSupport"):"1")+"</es:fdpchSupport>\n" +
        "    </es:cellCapability>\n" +
        "     <es:transmissionScheme>"+destinationCellExternal.get("transmissionScheme")+"</es:transmissionScheme>\n" +
        "     <es:parentSystem/>\n" +
        "     <es:mncLength>"+AppConf.getMNCLength()+"</es:mncLength>\n" +
        "     <es:hsAqmCongCtrlSpiSupport/>\n" +
        "     <es:hsAqmCongCtrlSupport>0</es:hsAqmCongCtrlSupport>\n" +
        "     <es:srvccCapability>"+destinationCellExternal.get("srvccCapability")+"</es:srvccCapability>\n" +
        "     <es:reportingRange1a>"+destinationCellExternal.get("reportingRange1a")+"</es:reportingRange1a>\n" +
        "     <es:reportingRange1b>"+destinationCellExternal.get("reportingRange1b")+"</es:reportingRange1b>\n" +
        "     <es:timeToTrigger1a>"+destinationCellExternal.get("timeToTrigger1a")+"</es:timeToTrigger1a>\n" +
        "     <es:timeToTrigger1b>"+destinationCellExternal.get("timeToTrigger1b")+"</es:timeToTrigger1b>\n" +
        "     <es:rimCapable>"+destinationCellExternal.get("rimCapable")+"</es:rimCapable>\n" +
        "     <es:lbUtranCellOffloadCapacity>"+destinationCellExternal.get("lbUtranCellOffloadCapacity")+"</es:lbUtranCellOffloadCapacity>\n" +
        "    </es:vsDataExternalUtranCell>\n" +
        " </xn:attributes>\n" +
        " </xn:VsDataContainer>\n" +
        "</un:ExternalUtranCell>\n";

        //</editor-fold>
         return externalUtranCellStr;
        }catch(Exception e){
            e.printStackTrace();
            return "";
        }
    }
    
    
    

    
}

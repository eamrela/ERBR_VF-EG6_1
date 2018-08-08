/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vfeg.erbr.util;

import com.mongodb.client.model.Filters;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.or;
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
public class CutoverRelation {
    
    //----------- Logical flags -------//
    private boolean emptyDestination=false;
    private Document tmp;
    //---------- Manually Filled------//
    private String relationId; // filled
    private String relationName; // filled
    private String relationType; // filled
    private String relationSourceCell; // filled
    private String relationDestinationCell; // filled
    private Document relationObject; // filled
    
    //---------- Auto filled ------------//
    private String relationSourceRnc; // incoming
    private String relationDestinationRnc; //  outgoing
    private String relationSourceRncOSS; // incoming
    private String relationDestinationRncOSS; // outgoing
    
    private boolean externalNotDefined=false;
    private Document destinationCellExternal; // incoming, outgoing
    private String externalUtranCellName;
    
    //--------- XML Scripts -------------//
    private String externalUtranCellDeletion; // relationDestinationRncOSS
    private String externalUtranCellCreation; // relationDestinationRncOSS
    private String utranRelationDeletion; // relationSourceRncOSS
    private String utranCellCreation; // relationSourceRncOSS

    
    //--------- Logical Operations ---------- //
    public void autoFillLogicalFields(){
        if(relationType.equals("OUTGOING")){
            //- relationDestinationRnc //- relationDestinationRncOSS //- destinationCellExternal
            fillRelationDestinationRncAndOSS();
            if(!emptyDestination){
                fetchExternalCell(relationDestinationCell,relationDestinationRnc,relationDestinationRncOSS);
            }
        }else if(relationType.equals("INCOMING")){
            //- relationSourceRnc //- relationSourceRncOSS //- destinationCellExternal
            fillRelationSourceRncAndOSS();
            fetchExternalCell(relationDestinationCell,relationSourceRnc,relationDestinationRncOSS);
        }
    }
    
    public void fillRelationDestinationRncAndOSS(){
        
       if(relationDestinationCell!=null){
        if(relationDestinationCell.length()>2){
           if(relationDestinationCell.contains("MeContext")){ // Tested and Validated
               relationDestinationRnc = relationDestinationCell.substring(relationDestinationCell.indexOf("MeContext=")+10,relationDestinationCell.indexOf(",Managed"));
               relationDestinationRncOSS = getOSSByRnc(relationDestinationRnc);
           }else if(relationDestinationCell.contains("ExternalUtranCell") &&
                        !relationDestinationCell.contains("6022-")){
               tmp = MongoClient.getUtranCellCollection().find(
                       Filters.eq("_id",relationDestinationCell.substring(relationDestinationCell.indexOf("ExternalUtranCell=")+18))).first();
               if(tmp!=null){
                  relationDestinationRnc = tmp.getString("rnc");
                  relationDestinationRncOSS = tmp.get("ossNr")+"";
               }
           }else if(relationDestinationCell.contains("6022-")){ // Tested and Validated
                relationDestinationRnc = "RNC"+relationDestinationCell.split("-")[1];
                relationDestinationRncOSS = getOSSByRnc(relationDestinationRnc);
           }else{
               relationDestinationCell = null;
           }
        }else{
           emptyDestination = true; 
        }
       }else{
           emptyDestination = true;
       }
    }
    
    public void fillRelationSourceRncAndOSS(){
        tmp = MongoClient.getUtranCellCollection().find(
                Filters.eq("_id", relationSourceCell)).first();
        if(tmp!=null){
            relationSourceRnc = tmp.getString("rnc");
            relationSourceRncOSS = tmp.get("ossNr")+"";
        }else{
            relationSourceRnc = null;
            relationSourceRncOSS = null;
        }
    }
    
    public void fetchExternalCell(String cellName,String rnc,String oss){
        String cId=null;
        if(cellName!=null && rnc!=null){
            if(cellName.length()>2){
                if(cellName.contains("MeContext")){
                    cellName = cellName.substring(cellName.indexOf("UtranCell=")+10); // 04941
                    tmp = MongoClient.getExternalUtranCellCollection().find(Filters.eq("_id", cellName)).first();
                    if(tmp!=null){ // found normal external cell
                        destinationCellExternal = tmp;
                     }else{
                        tmp = MongoClient.getUtranCellCollection().find(Filters.eq("_id", cellName)).first();
                        if(tmp!=null){
                            cId = tmp.getString("cId");
                        }
                     }
                }else if(cellName.contains("ExternalUtranCell") && !cellName.contains("6022-")){
                    cellName = cellName.substring(cellName.indexOf("ExternalUtranCell=")+18);
                    tmp = MongoClient.getExternalUtranCellCollection().find(Filters.eq("_id", cellName)).first();
                    if(tmp!=null){ // found normal external cell
                        destinationCellExternal = tmp;
                     }else{
                        tmp = MongoClient.getUtranCellCollection().find(Filters.eq("_id", cellName)).first();
                        if(tmp!=null){
                            cId = tmp.getString("cId");
                            externalUtranCellName = tmp.getString("_id");
                        }
                     }
                 }

                if(cellName.contains("6022-")){
                    cId = cellName.split("-")[2];
                    rnc = cellName.split("-")[1];
                 }
                
                if(cId!=null){ // "51232"
                tmp = MongoClient.getExternalUtranCellCollection().find(Filters.regex("_id", "6022-"+rnc.replaceAll("\\D+", "")+"-"+cId)).first();
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
                            externalUtranCellName = tmp.getString("_id");
                         }else{
                           tmp = MongoClient.getUtranCellCollection().find(Filters.eq("cId", cId)).first();  
                           if(tmp!=null){
                               destinationCellExternal = tmp;
                                externalUtranCellName = tmp.getString("_id");
                           }
                        }
                    
                }
                }
            }
        }
        
    }

    
    //---------- XML Generation -----------------// 
    public void doLogic(String sourceRncOSS, String targetRncOSS,String targetLac){
        try{
        if(sourceRncOSS.equals(targetRncOSS)){
            //<editor-fold defaultstate="collapsed" desc="S=T">
            if(relationType.equals("INCOMING")){ //- S=T (Check SourceCell)
                if(targetRncOSS.equals(relationSourceRncOSS)){ //- S=T=N
                    //<editor-fold defaultstate="collapsed" desc="T=N (TESTED)">
                    //-->  create relation with new RNC on OSS Target
                    XMLGenerator.addScript(targetRncOSS,"RELATION_CREATION",
                                            relationId,
                                            generateUtranRelations(targetRncOSS, relationSourceRnc,relationSourceCell,
                                                    getDestinationNameInternal(relationDestinationCell, relationDestinationRnc)));
                    XMLGenerator.addRNC(relationSourceRncOSS, relationSourceRnc);
                    //</editor-fold>
                }else if(!targetRncOSS.equals(relationSourceRncOSS)){ //- S=T & T!=N
                    //<editor-fold defaultstate="collapsed" desc="T!=N (TESTED)">
                    //--> Delete relation from relationSourceRncOSS
                    XMLGenerator.addScript(relationSourceRncOSS, "RELATION_DELETION", 
                                            relationId, 
                                            deleteUtranRelations());
                    if(destinationCellExternal!=null){
                    //--> Delete externalCell form relationSourceRncOSS
                    XMLGenerator.addScript(relationSourceRncOSS, "EXTERNAL_DELETION", 
                                            destinationCellExternal.getString("_id"), 
                                            deleteExternalUtranCell());
                    }
                    //--> Create externalCell on relationSourceRncOSS with new RncId
                    XMLGenerator.addScript(relationSourceRncOSS, "EXTERNAL_CREATION", 
                                            destinationCellExternal.getString("_id"), 
                                            generateExternalUtranCell(relationSourceRncOSS, relationDestinationRnc,targetLac,true));
                    //--> Define Incoming Relation as External on relationSourceRncOSS 
                    XMLGenerator.addScript(relationSourceRncOSS,"RELATION_CREATION",
                                            relationId,
                                            generateUtranRelations(relationSourceRncOSS, relationSourceRnc,relationSourceCell,
                                                    getDestinationNameExternal(destinationCellExternal,true)));
                    XMLGenerator.addRNC(relationSourceRncOSS, relationSourceRnc);
                    //</editor-fold>
                }
            }else if(relationType.equals("OUTGOING")){ //- S=T (Check DestinationCell)
                 if(targetRncOSS.equals(relationDestinationRncOSS)){ //- S=T=N
                     //<editor-fold defaultstate="collapsed" desc="T=N (TESTED)">
                    //-->  create relation with new RNC on OSS Target
                    XMLGenerator.addScript(targetRncOSS,"RELATION_CREATION",
                                            relationId,
                                            generateUtranRelations(targetRncOSS, relationSourceRnc,relationSourceCell,
                                                    getDestinationNameInternal(relationDestinationCell, relationDestinationRnc)));
                    XMLGenerator.addRNC(relationSourceRncOSS, relationSourceRnc);
                    //</editor-fold>
                }else if(!targetRncOSS.equals(relationDestinationRncOSS)){ //- S=T & T!=N
                    //<editor-fold defaultstate="collapsed" desc="T!=N (TESTED)">
                    //-> Define External Neighbor Cell on TargetOSS if not exist
                    if(externalNotDefined){
                       XMLGenerator.addScript(targetRncOSS,"EXTERNAL_CREATION", 
                                            destinationCellExternal.getString("_id"), 
                                            generateExternalUtranCell(targetRncOSS, relationDestinationRnc, null,false)); 
                    }
                    //-> Define relation on Target OSS as External
                    XMLGenerator.addScript(targetRncOSS,"RELATION_CREATION",
                                            relationId,
                                            generateUtranRelations(targetRncOSS, relationSourceRnc,relationSourceCell,
                                                    getDestinationNameExternal(destinationCellExternal,false)));
                    XMLGenerator.addRNC(relationSourceRncOSS, relationSourceRnc);
                    //</editor-fold>
                }
            }
            //</editor-fold>
        }else if(!sourceRncOSS.equals(targetRncOSS)){
            //<editor-fold defaultstate="collapsed" desc="S!=T">
            if(relationType.equals("INCOMING")){ //- S!=T (check SourceCell)
                if(targetRncOSS.equals(relationSourceRncOSS)){ //- S!=T T=N
                    //<editor-fold defaultstate="collapsed" desc="T=N">
                    //-> Delete incoming relation from relationSourceRncOSS
                    XMLGenerator.addScript(relationSourceRncOSS, "RELATION_DELETION", 
                                            relationId, 
                                            deleteUtranRelations());
                    if(destinationCellExternal!=null){
                    //-> Delete delete external cell
                    XMLGenerator.addScript(relationSourceRncOSS, "EXTERNAL_DELETION", 
                                            destinationCellExternal.getString("_id"), 
                                            deleteExternalUtranCell());
                    }
                    //-> create relation on relationSourceRncOSS
                   XMLGenerator.addScript(relationSourceRncOSS,"RELATION_CREATION",
                                            relationId,
                                            generateUtranRelations(relationSourceRncOSS, relationSourceRnc,relationSourceCell,
                                                    getDestinationNameInternal(relationDestinationCell, relationDestinationRnc)));
                   XMLGenerator.addRNC(relationSourceRncOSS, relationSourceRnc);
                    //</editor-fold>
                }else if(!targetRncOSS.equals(relationSourceRncOSS)){ //- S!=T & T!=N
                    //<editor-fold defaultstate="collapsed" desc="T!=N">
                    //-> Define External Cell on relationSourceRncOSS with new RncId
                    XMLGenerator.addScript(relationSourceRncOSS, "EXTERNAL_CREATION", 
                                            destinationCellExternal.getString("_id"), 
                                            generateExternalUtranCell(relationSourceRncOSS, relationDestinationRnc, targetLac,true));
                    //-> Define Incoming Relations on relationSourceRncOSS as External
                    XMLGenerator.addScript(relationSourceRncOSS,"RELATION_CREATION",
                                            relationId,
                                            generateUtranRelations(relationSourceRncOSS, relationSourceRnc,relationSourceCell,
                                                    getDestinationNameExternal(destinationCellExternal,true)));
                    XMLGenerator.addRNC(relationSourceRncOSS, relationSourceRnc);
                //</editor-fold>
                }
            }else if(relationType.equals("OUTGOING")){ //- S!=T (check Destination)
                if(targetRncOSS.equals(relationDestinationRncOSS)){ //- S!=T T=N
                    //<editor-fold defaultstate="collapsed" desc="T=N">
                    //-> create relation on relationDestinationRncOSS
                    XMLGenerator.addScript(targetRncOSS,"RELATION_CREATION",
                                            relationId,
                                            generateUtranRelations(targetRncOSS, relationSourceRnc,relationSourceCell,
                                                    getDestinationNameInternal(relationDestinationCell, relationDestinationRnc)));
                    XMLGenerator.addRNC(relationSourceRncOSS, relationSourceRnc);
                    //</editor-fold>
                }else if(!targetRncOSS.equals(relationDestinationRncOSS)){ //- S!=T & T!=N
                    //<editor-fold defaultstate="collapsed" desc="T!=N">
                    //-> Define External Cell on TargetOSS if not exist
                    if(externalNotDefined){
                       XMLGenerator.addScript(targetRncOSS,"EXTERNAL_CREATION", 
                                            destinationCellExternal.getString("_id"), 
                                            generateExternalUtranCell(targetRncOSS, relationDestinationRnc, null,false));  
                    }
                    //-> Define Outgoing Relations on TaregtOSS
                    XMLGenerator.addScript(targetRncOSS,"RELATION_CREATION",
                                            relationId,
                                            generateUtranRelations(targetRncOSS, relationSourceRnc,relationSourceCell,
                                                    getDestinationNameExternal(destinationCellExternal,false)));
                    XMLGenerator.addRNC(relationSourceRncOSS, relationSourceRnc);
                    //</editor-fold>
                }
            }
            //</editor-fold>
        }
        //-> Create Inter relation
        if(relationType.equals("INTER")){
           XMLGenerator.addScript(targetRncOSS,"RELATION_CREATION",
                                            relationId,
                                            generateUtranRelations(targetRncOSS, relationDestinationRnc,relationSourceCell,
                                                    getDestinationNameInternal(relationDestinationCell,relationDestinationRnc)));
           XMLGenerator.addRNC(relationSourceRncOSS, relationSourceRnc); 
        }
        }catch(Exception e){
            System.out.println("Relation : "+relationId+" went into an error!!!!!!: "+e.getMessage());
        }
    }
    

    //<editor-fold defaultstate="collapsed" desc="Setter/Getter">
    
    public String getRelationId() {
        return relationId;
    }

    public boolean isEmptyDestination() {
        return emptyDestination;
    }
    

    public void setRelationId(String relationId) {
        this.relationId = relationId;
    }

    public String getRelationName() {
        return relationName;
    }

    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    public String getRelationSourceCell() {
        return relationSourceCell;
    }

    public void setRelationSourceCell(String relationSourceCell) {
        this.relationSourceCell = relationSourceCell;
    }

    public String getRelationSourceRnc() {
        return relationSourceRnc;
    }

    public void setRelationSourceRnc(String relationSourceRnc) {
        this.relationSourceRnc = relationSourceRnc;
    }

    public String getRelationDestinationCell() {
        return relationDestinationCell;
    }

    public void setRelationDestinationCell(String relationDestinationCell) {
        this.relationDestinationCell = relationDestinationCell;
    }

    public Document getRelationObject() {
        return relationObject;
    }

    public void setRelationObject(Document relationObject) {
        this.relationObject = relationObject;
    }

    public String getRelationSourceRncOSS() {
        return relationSourceRncOSS;
    }

    public void setRelationSourceRncOSS(String relationSourceRncOSS) {
        this.relationSourceRncOSS = relationSourceRncOSS;
    }

    public String getRelationDestinationRnc() {
        return relationDestinationRnc;
    }

    public void setRelationDestinationRnc(String relationDestinationRnc) {
        this.relationDestinationRnc = relationDestinationRnc;
    }

    public String getRelationDestinationRncOSS() {
        return relationDestinationRncOSS;
    }

    public void setRelationDestinationRncOSS(String relationDestinationRncOSS) {
        this.relationDestinationRncOSS = relationDestinationRncOSS;
    }

    public Document getDestinationCellExternal() {
        return destinationCellExternal;
    }

    public void setDestinationCellExternal(Document destinationCellExternal) {
        this.destinationCellExternal = destinationCellExternal;
    }

    public String getExternalUtranCellDeletion() {
        return externalUtranCellDeletion;
    }

    public void setExternalUtranCellDeletion(String externalUtranCellDeletion) {
        this.externalUtranCellDeletion = externalUtranCellDeletion;
    }

    public String getExternalUtranCellCreation() {
        return externalUtranCellCreation;
    }

    public void setExternalUtranCellCreation(String externalUtranCellCreation) {
        this.externalUtranCellCreation = externalUtranCellCreation;
    }

    public String getUtranRelationDeletion() {
        return utranRelationDeletion;
    }

    public void setUtranRelationDeletion(String utranRelationDeletion) {
        this.utranRelationDeletion = utranRelationDeletion;
    }

    public String getUtranCellCreation() {
        return utranCellCreation;
    }

    public void setUtranCellCreation(String utranCellCreation) {
        this.utranCellCreation = utranCellCreation;
    }
    //</editor-fold>
    
    public String getOSSByRnc(String rnc){
        tmp = MongoClient.getUtranCellCollection().find(
                       Filters.eq("rnc",rnc)).first();
        if(tmp!=null){
            return tmp.get("ossNr")+"";
        }
        return null;
    }

    public String getDestinationNameInternal(String destination,String destinationRnc){
        if(destination.contains("MeContext")){
            destination = destination.substring(destination.indexOf("UtranCell=")+10);
        }else if(destination.contains("ExternalUtranCell")){
            destination = destination.substring(destination.indexOf("ExternalUtranCell=")+18);
        }
        if(destination.contains("6022-") && externalUtranCellName!=null){
            destination = externalUtranCellName;
        }

        return "SubNetwork="+AppConf.getSubNetwork()+",SubNetwork="+destinationRnc+",MeContext="+destinationRnc+",ManagedElement=1,RncFunction=1,UtranCell="+destination;

    }
    
    public String getDestinationNameExternal(Document destination, boolean useCellName){
        if(destination!=null){
//            if(externalNotDefined){
//        return "SubNetwork="+AppConf.getSubNetwork()+",ExternalUtranCell="+destinationCellExternal.getString("_id");        
//            }else{
        return "SubNetwork="+AppConf.getSubNetwork()+",ExternalUtranCell="+(useCellName?
                (externalUtranCellName!=null?externalUtranCellName:destinationCellExternal.getString("_id")):destinationCellExternal.getString("_id"));
//            }
        }else{
            return null;
        }
    }
    
    //------------ Scripts Generator ----------------------------------//
    
     public String deleteUtranRelations(){
        String utranRelationStr = 
                "<xn:SubNetwork id=\""+relationObject.getString("rnc")+"\">\n" +
                "<xn:MeContext id=\""+relationObject.getString("rnc")+"\">\n" +
                "<xn:ManagedElement id=\"1\">\n" +
                "<un:RncFunction id=\"1\">\n" +
                "<un:UtranCell id=\""+relationObject.getString("utranCell")+"\" modifier=\"update\">\n"+
                "<un:UtranRelation id=\""+relationObject.getString("utranRelation")+"\" modifier=\"delete\">\n" +
                "<un:attributes>\n" +
                "<un:adjacentCell>"+relationObject.getString("adjacentCell")+"</un:adjacentCell>\n" +
                "</un:attributes>\n"+
                "</un:UtranRelation>\n"+
                "</un:UtranCell>\n" +
                "</un:RncFunction>\n" +
                "</xn:ManagedElement>\n" +
                "</xn:MeContext>\n" +
                "</xn:SubNetwork>";
        return utranRelationStr;
    }
    
     public String deleteExternalUtranCell(){
       String utranCellsStr = "<un:ExternalUtranCell id=\""+destinationCellExternal.getString("_id")+"\" modifier=\"delete\"/>";
       return utranCellsStr;
    }
    
     public String generateUtranRelations(
            String oss,String targetRnc,String source,String destination){
        try{
            //<editor-fold defaultstate="collapsed" desc="Param">
        TreeMap<String,String> param = null;
        String utranRelationStr="";
        param = new TreeMap<String,String>();
        
        param.put("id", relationObject.getString("utranRelation"));
        param.put("adjacentCell", destination);
        
        param.put("source", relationObject.getString("utranCell"));
        param.put("createdBy",(relationObject.getString("createdBy")!=null?relationObject.getString("createdBy"):"Operator"));
        param.put("creationTime",(relationObject.getString("creationTime")!=null?relationObject.getString("creationTime"):AppConf.getSdf().format(new Date())));
        param.put("frequencyRelationType",(relationObject.getString("frequencyRelationType")!=null?relationObject.getString("frequencyRelationType"):"0"));
        param.put("hcsSib11Config-hcsPrio",(((Document)relationObject.get("hcsSib11Config")).getString("hcsPrio")!=null?
                ((Document)relationObject.get("hcsSib11Config")).getString("hcsPrio"):"0"));
        param.put("hcsSib11Config-penaltyTime",(((Document)relationObject.get("hcsSib11Config")).getString("penaltyTime")!=null?
                ((Document)relationObject.get("hcsSib11Config")).getString("penaltyTime"):"0"));
        param.put("hcsSib11Config-qHcs",(((Document)relationObject.get("hcsSib11Config")).getString("qHcs")!=null?
                ((Document)relationObject.get("hcsSib11Config")).getString("qHcs"):"0"));
        param.put("hcsSib11Config-temporaryOffset1",(((Document)relationObject.get("hcsSib11Config")).getString("temporaryOffset1")!=null?
                ((Document)relationObject.get("hcsSib11Config")).getString("temporaryOffset1"):"0"));
        param.put("hcsSib11Config-temporaryOffset2",(((Document)relationObject.get("hcsSib11Config")).getString("temporaryOffset2")!=null?
                ((Document)relationObject.get("hcsSib11Config")).getString("temporaryOffset2"):"0"));
        param.put("loadSharingCandidate",(relationObject.getString("loadSharingCandidate")!=null?relationObject.getString("loadSharingCandidate"):"0"));
        param.put("mobilityRelationType",(relationObject.getString("mobilityRelationType")!=null?relationObject.getString("mobilityRelationType"):"0"));
        param.put("nodeRelationType",(relationObject.getString("nodeRelationType")!=null?relationObject.getString("nodeRelationType"):"0"));
        param.put("qOffset1sn",(relationObject.getString("qOffset1sn")!=null?relationObject.getString("qOffset1sn"):"0"));
        param.put("qOffset2sn",(relationObject.getString("qOffset2sn")!=null?relationObject.getString("qOffset2sn"):"0"));
        param.put("selectionPriority",(relationObject.getString("selectionPriority")!=null?relationObject.getString("selectionPriority"):""));
        
        
        
        
                utranRelationStr = 
        "<un:UtranRelation id=\""+param.get("id")+"\" modifier=\"create\">\n" +
        "    <un:attributes>\n";
        
         if(param.get("adjacentCell")!=null){
             if(param.get("adjacentCell").length()>2){
                 utranRelationStr += "  <un:adjacentCell>"+param.get("adjacentCell")+"</un:adjacentCell>\n";
             }
         }else{
             utranRelationStr += "  <un:adjacentCell>"+relationDestinationCell+"</un:adjacentCell>\n";
         }
         
        
        utranRelationStr += 
        "    </un:attributes>\n" +
        " <xn:VsDataContainer id=\""+param.get("id")+"\" modifier=\"create\">\n" +
        "  <xn:attributes>\n" +
        " <xn:vsDataType>vsDataUtranRelation</xn:vsDataType>\n" +
        " <xn:vsDataFormatVersion>"+AppConf.getVsDataFormatVersion()+"</xn:vsDataFormatVersion>\n" +
        "    <es:vsDataUtranRelation>\n" +
        "     <es:qOffset1sn>"+param.get("qOffset1sn")+"</es:qOffset1sn>\n" +
        "     <es:qOffset2sn>"+param.get("qOffset2sn")+"</es:qOffset2sn>\n" +
        "     <es:loadSharingCandidate>"+param.get("loadSharingCandidate")+"</es:loadSharingCandidate>\n" +
        "     <es:selectionPriority>"+param.get("selectionPriority")+"</es:selectionPriority>\n" +
        "     <es:frequencyRelationType>"+param.get("frequencyRelationType")+"</es:frequencyRelationType>\n" +
        "     <es:nodeRelationType>"+param.get("nodeRelationType")+"</es:nodeRelationType>\n" +
        "    <es:hcsSib11Config>\n" +
        "     <es:penaltyTime>"+param.get("hcsSib11Config-penaltyTime")+"</es:penaltyTime>\n" +
        "     <es:hcsPrio>"+param.get("hcsSib11Config-hcsPrio")+"</es:hcsPrio>\n" +
        "     <es:qHcs>"+param.get("hcsSib11Config-qHcs")+"</es:qHcs>\n" +
        "     <es:temporaryOffset2>"+param.get("hcsSib11Config-temporaryOffset2")+"</es:temporaryOffset2>\n" +
        "     <es:temporaryOffset1>"+param.get("hcsSib11Config-temporaryOffset1")+"</es:temporaryOffset1>\n" +
        "    </es:hcsSib11Config>\n" +
        "     <es:mobilityRelationType>"+param.get("mobilityRelationType")+"</es:mobilityRelationType>\n" +
        "     <es:createdBy>"+param.get("createdBy")+"</es:createdBy>\n" +
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
                "<un:UtranCell id=\""+(param.containsKey("source")?param.get("source"):source)+"\" modifier=\"update\">\n"+
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
    
     public String generateExternalUtranCell(String oss,String rncId,String targetLac,boolean useCellName){
        try{
        //<editor-fold defaultstate="collapsed" desc="Param">
        String externalUtranCellStr=
        "<un:ExternalUtranCell id=\""+(useCellName?
                (externalUtranCellName!=null?externalUtranCellName:destinationCellExternal.getString("_id")):destinationCellExternal.getString("_id"))+"\" modifier=\"create\">\n" +
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
        " <xn:VsDataContainer id=\""+(useCellName?
                (externalUtranCellName!=null?externalUtranCellName:destinationCellExternal.getString("_id")):destinationCellExternal.getString("_id"))+"\" modifier=\"create\">\n" +
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

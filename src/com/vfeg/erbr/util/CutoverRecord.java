/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vfeg.erbr.util;

import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import static com.mongodb.client.model.Filters.or;
import com.vfeg.erbr.mongo.MongoClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.bson.Document;

/**
 *
 * @author eelaamr
 */
public class CutoverRecord {
    
    //---------- Manually Filled------//
    private String siteName; // Filled
    private String sourceRnc; // Filled
    private String targetRnc; // Filled
    private String targetLac; // Filled
    
    //-------- Auto Filled -----------//
    private String sourceRncOSS; // Filled
    private String targetRncOSS; // Filled
    private List<Document> siteCells; // Filled
    
    //--------- Cutover content -----//
    private List<Document> siteIncomingRelations; // adjacentcell is the site
    private List<Document> siteOutgoingRelations; // utrancell is the site
    private TreeMap<String,CutoverRelation> siteRelatedRelations;

    //------------ Auto filling methods -------------//
    public boolean validateRecord(){
        if(siteName.length()>3 && 
                sourceRnc.length()>2 && 
                 targetRnc.length()>2){
            siteCells = new ArrayList<Document>();
            siteIncomingRelations = new ArrayList<Document>();
            siteOutgoingRelations = new ArrayList<Document>();
            siteRelatedRelations = new TreeMap<String, CutoverRelation>();
            return true;
        }
        return false;
    }
    
    public String fillOSSandCells(){
        String result = "IuB: "+siteName+"\n";
        result += "RNC: "+sourceRnc+"\n";
        result += "Looking up in DB for cells related to this IuB\n";
        FindIterable<Document> cells = MongoClient.getUtranCellCollection().find(
                Filters.regex("utranCellIubLink", ".*"+sourceRnc+".*IubLink=Iub_"+siteName));
        result += "Found cells\n";
        for(Document cell:cells){
            result += cell.getString("_id")+"\n";
            if(sourceRncOSS==null){
            sourceRncOSS = cell.get("ossNr")+"";
            }
            siteCells.add(cell);
        }
        result += "Identifying OSSs related to Source/Target RNCs \n";
        FindIterable<Document> tmp = MongoClient.getUtranCellCollection().find(
                Filters.eq("rnc", targetRnc));
        for(Document c : tmp){
            setTargetRncOSS(c.get("ossNr")+"");
            break;
        }
        result += "Source OSS ["+sourceRnc+"]: "+sourceRncOSS+" \n";
        result += "Target OSS ["+targetRnc+"]: "+targetRncOSS+" \n";
        return result;
    }
    
    public String fillRelations(){
         siteIncomingRelations = new ArrayList<Document>();
         siteOutgoingRelations = new ArrayList<Document>();
         FindIterable<Document> tmp;
         int incomingRelationCounter=0;
         int outgoingRelationCounter=0;
         String result = "Looking up relations (Incoming/Outgoing) for: "+siteName+"\n";
        for(Document cell:siteCells){
                incomingRelationCounter=0;
                outgoingRelationCounter=0;
                result += "Cell: "+cell.getString("_id")+" / 6022-"+sourceRnc.replaceAll("\\D+", "")+"-"+cell.get("cId")+" \n";
              // ------------ Incoming
              tmp = MongoClient.getUtranRelationCollection().find(
                    or(Filters.regex("adjacentCell", ".*tranCell="+cell.get("_id")+".*"),
                    Filters.regex("adjacentCell", ".*UtranCell=6022-"+sourceRnc.replaceAll("\\D+", "")+"-"+cell.get("cId")+".*")));
              for(Document inRelation:tmp){
                  incomingRelationCounter++;
                  siteIncomingRelations.add(inRelation);
              }
             // ------------ Outgoing
              tmp = MongoClient.getUtranRelationCollection().find(Filters.eq("utranCell", cell.getString("_id")));
              for(Document inRelation:tmp){
                  outgoingRelationCounter++;
                  siteOutgoingRelations.add(inRelation);
              }
              
              result+="Found "+incomingRelationCounter+" Incoming relations\n";
              result+="Found "+outgoingRelationCounter+" Outgoing relations\n\n";
        }
         
         return result;
    }
    
    //------------ Logical Operations ------------ //
    public void performRelationEntities(){
        CutoverRelation relation;
        //<editor-fold defaultstate="collapsed" desc="Incoming">
        for (Document siteIncomingRelation : siteIncomingRelations) {
            relation = new CutoverRelation();
            relation.setRelationId(siteIncomingRelation.getString("_id"));
            relation.setRelationName(siteIncomingRelation.getString("utranRelation"));
            relation.setRelationType("INCOMING");
            relation.setRelationSourceCell(siteIncomingRelation.getString("utranCell"));
            relation.setRelationDestinationCell(siteIncomingRelation.getString("adjacentCell"));
            relation.setRelationObject(siteIncomingRelation);
            
            relation.setRelationDestinationRnc(targetRnc);
            relation.setRelationDestinationRncOSS(targetRncOSS);
            //------ Inter relations
            if(relation.getRelationSourceCell().contains(siteName.replaceAll("\\D+", ""))){
                relation.setRelationSourceRnc(targetRnc);
                relation.setRelationSourceRncOSS(targetRncOSS);
                relation.setRelationDestinationRnc(targetRnc);
                relation.setRelationDestinationRncOSS(targetRncOSS);
                relation.setRelationType("INTER");
            }
            
            siteRelatedRelations.put(relation.getRelationId(), relation);
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Outgoing">
         for (Document siteOutgoingRelation : siteOutgoingRelations) {
            relation = new CutoverRelation();
            relation.setRelationId(siteOutgoingRelation.getString("_id"));
            relation.setRelationName(siteOutgoingRelation.getString("utranRelation"));
            relation.setRelationType("OUTGOING");
            relation.setRelationSourceCell(siteOutgoingRelation.getString("utranCell"));
            relation.setRelationDestinationCell(siteOutgoingRelation.getString("adjacentCell"));
            relation.setRelationObject(siteOutgoingRelation);
            
            relation.setRelationSourceRnc(targetRnc);
            relation.setRelationSourceRncOSS(targetRncOSS);
            
            //------ Inter relations
            if(relation.getRelationDestinationCell().contains(siteName.replaceAll("\\D+", ""))){
                relation.setRelationSourceRnc(targetRnc);
                relation.setRelationSourceRncOSS(targetRncOSS);
                relation.setRelationDestinationRnc(targetRnc);
                relation.setRelationDestinationRncOSS(targetRncOSS);
                relation.setRelationType("INTER");
            }
            
            siteRelatedRelations.put(relation.getRelationId(), relation);
         }
        //</editor-fold>
    }
    
    public String performIncomingOutgoingFilling(){
        String result = "\n\n";
        siteIncomingRelations.clear();
        siteOutgoingRelations.clear();
        TreeMap<String,CutoverRelation> tmpMap = new TreeMap<String, CutoverRelation>();
        for (Map.Entry<String, CutoverRelation> relation : siteRelatedRelations.entrySet()) {
            relation.getValue().autoFillLogicalFields();
            
            tmpMap.put(relation.getValue().getRelationId(), relation.getValue());
            result += relation.getValue().getRelationId()+"\n";
        }
        siteRelatedRelations = tmpMap;
        return result;
    }
    
    public void generateXML(){
        for (Map.Entry<String, CutoverRelation> relation : siteRelatedRelations.entrySet()) {
          relation.getValue().doLogic(sourceRncOSS,targetRncOSS,targetLac);
        }
    }
    
    //<editor-fold defaultstate="collapsed" desc="Setter/Getter">
    
    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        if(siteName!=null){
        this.siteName = siteName.trim();
        }
    }

    public String getSourceRnc() {
        return sourceRnc;
    }

    public void setSourceRnc(String sourceRnc) {
        if(sourceRnc!=null){
        this.sourceRnc = sourceRnc.trim();
        }
    }

    public String getTargetRnc() {
        return targetRnc;
    }

    public void setTargetRnc(String targetRnc) {
        if(targetRnc!=null){
        this.targetRnc = targetRnc.trim();
        }
    }

    public String getTargetLac() {
        return targetLac;
    }

    public void setTargetLac(String targetLac) {
        if(targetLac!=null){
        this.targetLac = targetLac.trim();
        }
    }

    public String getSourceRncOSS() {
        return sourceRncOSS;
    }

    public void setSourceRncOSS(String sourceRncOSS) {
        this.sourceRncOSS = sourceRncOSS;
    }

    public String getTargetRncOSS() {
        return targetRncOSS;
    }

    public void setTargetRncOSS(String targetRncOSS) {
        this.targetRncOSS = targetRncOSS;
    }

    public TreeMap<String, CutoverRelation> getSiteRelatedRelations() {
        return siteRelatedRelations;
    }

    public void setSiteRelatedRelations(TreeMap<String, CutoverRelation> siteRelatedRelations) {
        this.siteRelatedRelations = siteRelatedRelations;
    }
    //</editor-fold>

   
    
    
    
}

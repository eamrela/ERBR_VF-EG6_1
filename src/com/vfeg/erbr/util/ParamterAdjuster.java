/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vfeg.erbr.util;

import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.vfeg.erbr.generator.XMLGenerator;
import com.vfeg.erbr.mongo.MongoClient;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;

/**
 *
 * @author eelaamr
 */
public class ParamterAdjuster {
    
    private List<Document> cellsToAdjustOSS1; 
    private List<Document> cellsToAdjustOSS2; 
    
    public String collectCellsToAdjust(){
        cellsToAdjustOSS1 = new ArrayList<Document>();
        cellsToAdjustOSS2 = new ArrayList<Document>();
        String result = "Checking DB for cells with missing [uarfcnUl]\n";
        FindIterable<Document> cells = MongoClient.getExternalUtranCellCollection().find(Filters.eq("uarfcnUl", ""));
        for(Document cell:cells){
         if((cell.get("ossNr")+"").equals("1")){
             cellsToAdjustOSS1.add(cell);
         }else{
             cellsToAdjustOSS2.add(cell);
         }
        }
        result+= "Found ["+cellsToAdjustOSS1.size()+"] cell on OSS [1]\n";
        result+= "Found ["+cellsToAdjustOSS2.size()+"] cell on OSS [2]\n";
        return result;
    }
    
    public String generateCellsAdjustment(Document cell){
        if(cell!=null){
        return  "<!-- uarfcnDl: "+cell.get("uarfcnDl")+" -->\n"+ 
                "<un:ExternalUtranCell id=\""+cell.getString("_id")+"\" modifier =\"update\">\n" +
                "<un:attributes>\n" +
                "<un:uarfcnUl>"+cell.get("uarfcnUl")+"</un:uarfcnUl>\n" +
                "</un:attributes>\n" +
                " </un:ExternalUtranCell>";
        }
        return "";
    }

    public Integer calculateUL(Integer DL){
        Integer UL = 0;
        if((DL > 10562)){
            UL=5*((DL/5)-190);
        }else if(2937<DL && DL<3088){
            UL=5*((DL/5)-45);
        }
        return UL;
    }
    
    public void applyFormula() {
        for (Document cellsToAdjustOSS11 : cellsToAdjustOSS1) {
            if(!cellsToAdjustOSS11.get("uarfcnDl").equals("") 
                    && cellsToAdjustOSS11.get("uarfcnUl").equals("")){
            
            cellsToAdjustOSS11.append("uarfcnUl", calculateUL(Integer.parseInt(cellsToAdjustOSS11.get("uarfcnDl")+"")));
            
            XMLGenerator.addScript("1",
                                  "PARAMTER_ADJUSTMENT",
                                   cellsToAdjustOSS11.getString("_id"),
                                   generateCellsAdjustment(cellsToAdjustOSS11));
            }
        }
        for (Document cellsToAdjustOSS22 : cellsToAdjustOSS2) {
            if(cellsToAdjustOSS22.get("uarfcnDl")!=null 
                    && cellsToAdjustOSS22.get("uarfcnUl").equals("")){
            
            cellsToAdjustOSS22.append("uarfcnUl", calculateUL(Integer.parseInt(cellsToAdjustOSS22.get("uarfcnDl")+"")));
            
            XMLGenerator.addScript("2",
                                  "PARAMTER_ADJUSTMENT",
                                   cellsToAdjustOSS22.getString("_id"),
                                   generateCellsAdjustment(cellsToAdjustOSS22));
            }
        }
    }
    
    public static void main(String[] args) {
        ParamterAdjuster x = new ParamterAdjuster();
        System.out.println(x.calculateUL(10662));
    }
}

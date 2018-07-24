/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vfeg.erbr.mongo;


import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.WriteModel;
import com.vfeg.erbr.configuration.AppConf;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;

/**
 *
 * @author eelaamr
 */
public class MongoClient {
    
    private static com.mongodb.MongoClient mongoClient;
    private static MongoDatabase database;
    private static MongoCollection<Document> xmlConfigurationCollection;
    private static MongoCollection<Document> appConfigurationCollection;
    private static MongoCollection<Document> utranCellCollection;
    private static MongoCollection<Document> utranRelationCollection;
    private static MongoCollection<Document> externalUtranCellCollection;
    private static String connection = "Disconnected";
    
    public static void initializeDB(){
        mongoClient = new com.mongodb.MongoClient( AppConf.getMongoIp() , AppConf.getMongoPort() );
        database = mongoClient.getDatabase("erbr");
        xmlConfigurationCollection = database.getCollection("xmlConfiguration"); 
        appConfigurationCollection = database.getCollection("appConfiguration");
        utranCellCollection = database.getCollection("utranCell");
        utranRelationCollection = database.getCollection("utranRelation");
        externalUtranCellCollection = database.getCollection("externalUtranCell");
        connection = "Connected";
        readAppConf();
    }

    public static String getConnection() {
        return connection;
    }
    


    public static com.mongodb.MongoClient getMongoClient() {
        return mongoClient;
    }

    public static void setMongoClient(com.mongodb.MongoClient mongoClient) {
        MongoClient.mongoClient = mongoClient;
    }

    public static MongoDatabase getDatabase() {
        return database;
    }

    public static void setDatabase(MongoDatabase database) {
        MongoClient.database = database;
    }

    public static MongoCollection<Document> getXmlConfigurationCollection() {
        return xmlConfigurationCollection;
    }

    public static void setXmlConfigurationCollection(MongoCollection<Document> xmlConfigurationCollection) {
        MongoClient.xmlConfigurationCollection = xmlConfigurationCollection;
    }

    public static MongoCollection<Document> getAppConfigurationCollection() {
        return appConfigurationCollection;
    }

    public static void setAppConfigurationCollection(MongoCollection<Document> appConfigurationCollection) {
        MongoClient.appConfigurationCollection = appConfigurationCollection;
    }

    public static MongoCollection<Document> getUtranCellCollection() {
        return utranCellCollection;
    }

    public static void setUtranCellCollection(MongoCollection<Document> utranCellCollection) {
        MongoClient.utranCellCollection = utranCellCollection;
    }

    public static MongoCollection<Document> getUtranRelationCollection() {
        return utranRelationCollection;
    }

    public static void setUtranRelationCollection(MongoCollection<Document> utranRelationCollection) {
        MongoClient.utranRelationCollection = utranRelationCollection;
    }

    private static void readAppConf() {
         FindIterable<Document> res = appConfigurationCollection.find().limit(1);
         for(Document conf : res){
             AppConf.setLastOSSOneUpdateUtranCell(conf.getDate("lastOSSOneUpdateUtranCell"));
             AppConf.setLastOSSOneUpdateUtranRelations(conf.getDate("lastOSSOneUpdateUtranRelations"));
             AppConf.setLastOSSTwoUpdateUtranCell(conf.getDate("lastOSSTwoUpdateUtranCell"));
             AppConf.setLastOSSTwoUpdateUtranRelations(conf.getDate("lastOSSTwoUpdateUtranRelations"));
             AppConf.setLastOSSOneUpdateExternalUtranCell(conf.getDate("lastOSSOneUpdateExternalUtranCell"));
             AppConf.setLastOSSTwoUpdateExternalUtranCell(conf.getDate("lastTwoOneUpdateExternalUtranCell"));
         }
    }

    public static MongoCollection<Document> getExternalUtranCellCollection() {
        return externalUtranCellCollection;
    }
    
    
    
    
    
}

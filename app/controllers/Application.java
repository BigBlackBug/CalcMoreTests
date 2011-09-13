package controllers;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import java.util.logging.Level;
import play.*;
import play.mvc.*;

import java.util.*;
import models.DbError;
import models.WordCountPair;
import play.data.validation.Required;
import play.modules.gae.GAE;


public class Application extends Controller {

    static DatastoreService datastore = GAE.getDatastore();
    
    public static void index() {
       List<WordCountPair> countPairs = getWordCountPairs();
        render(countPairs);
        //render();
    }

    
    public static void saveWord(@Required String word, @Required String count){
        
        if(word.trim().isEmpty() || count.trim().isEmpty()){
            index();
            return;
        }
        
        try {
            Long longCount=Long.parseLong(count.trim());
            saveOrUpdateWord(word.trim(), longCount);
        }
        catch(NumberFormatException  nfe){
            System.err.print("cant parse string to long");
        }
        
        index();
    }
    
    public static void saveOrUpdateWord(String word, Long count){
        
        Key entityKey=KeyFactory.createKey("WordCountPair", word);

        Transaction tx = datastore.beginTransaction();
        
        for(;;){
            
            try{
            
                try {
                    
                    Entity existed = datastore.get(tx,entityKey);
                    Long currCount = (Long)existed.getProperty("count");
                    existed.setProperty("count", currCount+count);
                    datastore.put(tx,existed);

                    System.out.println("UPDATED!");
                }
                catch(EntityNotFoundException enfe){
                    
                    Entity newEntity = new Entity(entityKey);
                    newEntity.setProperty("count",count);
                    datastore.put(tx, newEntity);

                    System.out.println("SAVED!");
                }
                
            }
            catch(ConcurrentModificationException  e){
                new DbError("concurrent exception", new Date()).save();
                continue;
            }
            break;
        }
        
        try{
            tx.commit();
        }
        catch(Exception e){
            new DbError("commiting exception!!", new Date()).save();
        }
        finally{
            if(tx.isActive())
                tx.rollback();
        }
        
    }
    
    
    static void printWordPair(String word) {
        try {
            System.out.println(GAE.getDatastore().get(KeyFactory.createKey("WordCountPair", word)));
        } catch (EntityNotFoundException ex) {
            java.util.logging.Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    static void printDB(){
        List<Entity> asList = 
                GAE.getDatastore().prepare(new Query("WordCountPair"))
                    .asList(FetchOptions.Builder.withDefaults());
        System.out.println(asList);
    }
    
    static public List<WordCountPair> getWordCountPairs(){
        return WordCountPair.fromEntities(
                GAE.getDatastore().prepare(new Query("WordCountPair"))
                    .asList(FetchOptions.Builder.withDefaults()));
    }
    
    

    
    
}
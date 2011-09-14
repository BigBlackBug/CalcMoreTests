package controllers;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.gson.Gson;
import java.util.List;
import models.DbError;
import models.WordCountPair;
import play.data.validation.Required;
import play.modules.gae.GAE;
import play.modules.siena.SienaPlugin;
import play.mvc.Controller;
import play.mvc.Http;
import siena.Json;
import siena.Model;

/**
 *
 * @author iliax
 */
public class CalcMoreRestAPI extends  Controller{
    
    public static void saveWord(@Required String word, @Required String count){
        
        if(word==null || count==null || word.trim().isEmpty() || count.trim().isEmpty()){
            response.status=Http.StatusCode.BAD_REQUEST;
            renderText("bad request!");
            return;
        }
        
        try {
            Long longCount=Long.parseLong(count.trim());
            Application.saveOrUpdateWord(word.trim(), longCount);
        }
        catch(NumberFormatException  nfe){
            System.err.print("cant parse string to long");
            response.status=Http.StatusCode.BAD_REQUEST;
            renderJSON("bad request!");
        }
               
    }
    
    public static void clearWordsDB(){
        //Application.len=new Long(0);
        WordCountPair.all().delete();
        System.out.println("db is clear now!");
        renderText("deleted!");
    }
    
    public static void wordsCount(){
        renderJSON(GAE.getDatastore().prepare(new Query("WordCountPair")).countEntities());
    }
    
    public static void words(){
        renderJSON(Application.getWordCountPairs());
    }
    
    static public void getErrors(){
        renderJSON(DbError.all(DbError.class).fetch());
    }
    
    static public void deleteErrors(){
        int delete = DbError.all(DbError.class).delete();
        if(delete!=0)
            renderJSON("deleted!");
        else
            renderJSON("there is no errors!");
    }
    
   
    
}

package controllers;

import com.google.gson.Gson;
import java.util.List;
import models.WordCountPair;
import play.data.validation.Required;
import play.mvc.Controller;
import play.mvc.Http;
import siena.Json;

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
            WordCountPair  existed;
            
            if((existed = WordCountPair.all().filter("word", word).get()) ==null){
                new WordCountPair(word.trim(), longCount).save();
                System.out.println(" saved!");
            } else {
                existed.count+=longCount;
                existed.update();
                System.out.println(" up!");
            } 
            
            response.status=Http.StatusCode.OK;
        }
        catch(NumberFormatException  nfe){
            System.err.print("cant parse string to long");
            response.status=Http.StatusCode.BAD_REQUEST;
            renderText("bad request!");
        }
        
    }
    
    public static void clearWordsDB(){
        WordCountPair.all().delete();
        System.out.println("db is clear now!");
        renderText("deleted!");
    }
    
    public static void wordsCount(){
        renderJSON(WordCountPair.all().count());
    }
    
    public static void words(){
        renderJSON(WordCountPair.all().fetch());
    }
    
}

package controllers;

import play.*;
import play.mvc.*;

import java.util.*;
import models.WordCountPair;
import play.data.validation.Required;


public class Application extends Controller {

    
    public static void index() {
        
        List<WordCountPair> countPairs=WordCountPair.all().fetch();
        render(countPairs);
    }

    
    public static void saveWord(@Required String word, @Required String count){
        
        if(word.trim().isEmpty() || count.trim().isEmpty()){
            index();
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
            
        }
        catch(NumberFormatException  nfe){
            System.err.print("cant parse string to long");
        }
        
        index();
    }
    

    
}
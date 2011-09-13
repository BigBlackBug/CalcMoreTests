package models;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import siena.*;


/**
 *
 * @author iliax
 */
public class WordCountPair extends Model {
    
//    @Id
//    public Long id;
    
    public String word;
    
    public Long  count;

    public WordCountPair(String word, Long count) {
        this.word = word;
        this.count = count;
    }

    public WordCountPair(com.google.appengine.api.datastore.Entity dsEntity) {
        word=dsEntity.getKey().getName();
        count=(Long) dsEntity.getProperty("count");
    }
    
    
    public static Query<WordCountPair> all(){
        return WordCountPair.all(WordCountPair.class);
    }

    @Override
    public String toString() {
        return word+" : "+count;
    }

    @Override
    public boolean equals(Object that) {
        if(that==null)
            return false;
        
        if(that instanceof WordCountPair){
            WordCountPair pair=(WordCountPair)that;
            return ((pair.word.equals(word))&&(pair.count.equals(count)));
        }
        
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.word != null ? this.word.hashCode() : 0);
        hash = 79 * hash + (this.count != null ? this.count.hashCode() : 0);
        return hash;
    }
    
    public static List<WordCountPair> fromEntities(List<com.google.appengine.api.datastore.Entity> entities){
        List<WordCountPair> pairs=new LinkedList<WordCountPair>();
        for(com.google.appengine.api.datastore.Entity e : entities){
            pairs.add(new WordCountPair(e));
        }
        return pairs;
    }
    
}

package models;

import java.math.BigDecimal;
import siena.*;


/**
 *
 * @author iliax
 */
public class WordCountPair extends Model {
    
    @Id
    public Long id;
    
    public String word;
    
    public Long  count;

    public WordCountPair(String word, Long count) {
        this.word = word;
        this.count = count;
    }

    public WordCountPair() {
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
    
    
}

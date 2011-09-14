package models;

import java.util.Date;
import siena.Id;
import siena.Model;

/**
 *
 * @author iliax
 */
public class DbError extends Model{
    
    @Id
    Long id;
    
    public String message;
    
    public Date date;
    
    public Exception exception;
    
    public long timeInMillis;

    public DbError(String message, Date date) {
        this.message = message;
        this.date = date;
        timeInMillis=date.getTime();
    }

    public DbError(String message, Date date, Exception exception) {
        this.message = message;
        this.date = date;
        timeInMillis=date.getTime();
        this.exception = exception;
    }


    @Override
    public String toString() {
        return message+" "+exception;
    }
    
    
}

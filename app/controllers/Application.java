package controllers;

import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.sun.xml.internal.ws.api.streaming.XMLStreamReaderFactory.Woodstox;
import java.util.logging.Level;
import play.*;
import play.mvc.*;

import java.util.*;
import models.DbError;
import models.WordCountPair;
import play.data.validation.Required;
import play.modules.gae.GAE;
import play.modules.gae.GAEInit;
import play.modules.siena.SienaEnhancer;
import play.modules.siena.SienaPlugin;

public class Application extends Controller {

    static DatastoreService datastore = GAE.getDatastore();

    public static void index() {
        List<WordCountPair> countPairs = getWordCountPairs();
        render(countPairs);
        //render();
    }

    public static void saveWord(@Required String word, @Required String count) {

        if (word.trim().isEmpty() || count.trim().isEmpty()) {
            index();
            return;
        }

        try {
            Long longCount = Long.parseLong(count.trim());
            saveOrUpdateWord(word.trim(), longCount);
        } catch (NumberFormatException nfe) {
            System.err.print("cant parse string to long");
        }

        index();
    }

    //public static Long len=new Long(0);
    public static void saveOrUpdateWord(String word, Long count) {


////        Версия Siena 
//	 	
//         WordCountPair  existed;
//	 for(;;){	
//             SienaPlugin.pm().beginTransaction();
//            try{ 
//               if((existed = WordCountPair.all().filter("word", word).get()) ==null){
//
//                    new WordCountPair(word.trim(), count).save();
//
//                   //System.out.println(" saved!");
//
//               } else {
//
//                   existed.count+=count;
//
//                   existed.update();
//
//                   //System.out.println(" up!");
//
//                } 
//               
//               SienaPlugin.pm().commitTransaction();
//            }
//            catch(ConcurrentModificationException exception){
//                new DbError("concurrent err siena", new Date()).save();
//                //SienaPlugin.pm().rollbackTransaction();
//                continue;
//            }
//            break;
//         }
////        Версия Siena end	 	

////     
        Key entityKey = KeyFactory.createKey("WordCountPair", word);

        Transaction tx;

        for (;;) {

            try {

                tx = datastore.beginTransaction();

                try {

                    Entity existed = datastore.get(tx, entityKey);
                    Long currCount = (Long) existed.getProperty("count");
                    existed.setProperty("count", currCount + count);
                    datastore.put(tx, existed);

                    //System.out.println("UPDATED!");
                } catch (EntityNotFoundException enfe) {

                    Entity newEntity = new Entity(entityKey);
                    newEntity.setProperty("count", count);
                    datastore.put(tx, newEntity);

                    //System.out.println("SAVED!");
                }

            } /*
             * в доке блин сказано что put может кидать ConcurrentModificationException,
             * на практике ни разу не кинул! кидает tx при коммите
             */ catch (java.util.ConcurrentModificationException e) {
                new DbError("concurrent modification exception by DS.put method", new Date()).save();
                continue;
            }


            try {
                tx.commit();
            } /*
             * если транзакция уже выполнена, откачена, 
             * либо попытка завершить или откатить ее уже завершилась неудачей.
             */ catch (java.lang.IllegalStateException e) {
                new DbError("commiting exception", new Date(), e).save();

                continue;
            } /*
             * при возникновении ошибки в хранилище данных.
             */ catch (DatastoreFailureException e) {
                new DbError("commiting exception", new Date(), e).save();

                continue;
            } /*
             * вот здесь то все и происходит при конкуррентной записи
             */ catch (java.util.ConcurrentModificationException e) {
                //new DbError("concurrent modification exception log", new Date()).save();
                continue;
            } //это можно не писать скорее всего- tx уже закоммичен или отвалился
            finally {
                try {
                    if (tx.isActive()) {
                        tx.rollback();
                        new DbError("rolling back tx", new Date()).save();
                    }
                } catch (IllegalStateException e) {
                    //new DbError("IllegalStateException", new Date()).save();
                } catch (Exception e) {
                    new DbError("UNKNOWN EXCEPTION with rollback", new Date()).save();
                }
            }

            /*
             * если tx нормально  закоммитился
             */
            break;
        }

    }

    static void printWordPair(String word) {
        try {
            System.out.println(GAE.getDatastore().get(KeyFactory.createKey("WordCountPair", word)));
        } catch (EntityNotFoundException ex) {
            java.util.logging.Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static void printDB() {
        List<Entity> asList =
                GAE.getDatastore().prepare(new Query("WordCountPair")).asList(FetchOptions.Builder.withDefaults());
        System.out.println(asList);
    }

    static public List<WordCountPair> getWordCountPairs() {
        return WordCountPair.fromEntities(
                GAE.getDatastore().prepare(new Query("WordCountPair")).asList(FetchOptions.Builder.withDefaults()));
        //return WordCountPair.all(WordCountPair.class).fetch();
    }

    public static void testUser(String username) {
        //пришёл запрос новый

        Key key = KeyFactory.createKey("USER", username);
        try {
            Entity user = datastore.get(key);
            String prop;
            if ((prop = (String) user.getProperty("status")).equals("done") || prop.equals("in_progress")) {
                renderText("new");
            } else {//free
                renderText("ok");
            }
        } catch (EntityNotFoundException ex) {
            renderText("ok");
        }

    }

    public static void firstRequest() {
        Query q = new Query("USER");
        q.addFilter("status", Query.FilterOperator.EQUAL, "free");
        List<Entity> freeUsers = datastore.prepare(q).asList(null);
        if (freeUsers == null || freeUsers.isEmpty()) {
            renderText("new");
        } else {
            renderJSON(freeUsers.get(0));
        }
    }

    public static void updateStatus(String username, String status) {

        Key key = KeyFactory.createKey("USER", username);
        try {
            Entity user = datastore.get(key);
            user.setProperty("status", status);
          
        } catch (EntityNotFoundException ex) {
            return;
        }
    }
}

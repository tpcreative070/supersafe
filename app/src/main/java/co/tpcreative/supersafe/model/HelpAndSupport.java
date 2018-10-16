package co.tpcreative.supersafe.model;
import java.io.Serializable;

public class HelpAndSupport implements Serializable{

    public Categories categories;
    public String title;
    public String content;
    public String nummberName;

    public HelpAndSupport(Categories categories,String title,String content,String nummberName){
        this.categories = categories;
        this.title = title;
        this.content = content;
        this.nummberName = nummberName;
    }
    public HelpAndSupport(){
        this.categories = new Categories();
        this.title = "";
        this.content = "";
        this.nummberName = null;
    }

    public int getCategoryId() {
        return categories.id;
    }

    public String getCategoryName() {
        return categories.name;
    }

}

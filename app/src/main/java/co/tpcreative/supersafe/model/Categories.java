package co.tpcreative.supersafe.model;

import java.io.Serializable;

public class Categories implements Serializable {
    public int id;
    public String name;


    public Categories(){
        this.id = 0;
        this.name = "";
    }

    public Categories(int id,String name){
        this.id = id;
        this.name = name;
    }

}

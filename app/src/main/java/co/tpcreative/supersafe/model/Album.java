package co.tpcreative.supersafe.model;

import java.io.Serializable;

public class Album implements Serializable{

    private final String name;
    private final int imageResource;

    public Album(String name,int imageResource ) {
        this.name = name;
        this.imageResource = imageResource;
    }

    public String getName() {
        return name;
    }

    public int getImageResource() {
        return imageResource;
    }

}

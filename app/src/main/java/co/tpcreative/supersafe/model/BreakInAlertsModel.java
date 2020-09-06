package co.tpcreative.supersafe.model;

import java.io.Serializable;

public class BreakInAlertsModel implements Serializable {
    public int id;
    public String fileName;
    public long time;
    public String pin;
    public BreakInAlertsModel(BreakInAlertsEntityModel index){
        this.id = index.id;
        this.fileName = index.fileName;
        this.time = index.time;
        this.pin = index.pin;
    }
    public BreakInAlertsModel(){
    }
}

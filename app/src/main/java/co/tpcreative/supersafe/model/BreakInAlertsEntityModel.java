package co.tpcreative.supersafe.model;

import java.io.Serializable;

import co.tpcreative.supersafe.common.entities.BreakInAlertsEntity;

public class BreakInAlertsEntityModel implements Serializable {
    public int id;
    public String fileName;
    public long time;
    public String pin;
    public BreakInAlertsEntityModel(BreakInAlertsEntity index){
        this.id = index.id;
        this.fileName = index.fileName;
        this.time = index.time;
        this.pin = index.pin;
    }

    public BreakInAlertsEntityModel(BreakInAlertsModel index){
        this.id = index.id;
        this.fileName = index.fileName;
        this.time = index.time;
        this.pin = index.pin;
    }
}

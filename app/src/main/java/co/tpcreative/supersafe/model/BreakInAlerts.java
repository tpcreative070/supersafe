package co.tpcreative.supersafe.model;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.room.InstanceGenerator;

@Entity(tableName = "breakinalerts")
public class BreakInAlerts implements Serializable{
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String fileName;
    public long time;
    public String pin;

    private static BreakInAlerts instance;

    @Ignore
    public static BreakInAlerts getInstance(){
        if (instance==null){
            instance = new BreakInAlerts();
        }
        return instance;
    }

    @Ignore
    public List<BreakInAlerts> getList(){
        List<BreakInAlerts> mList = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getList();
        if (mList==null){
            mList = new ArrayList<>();
        }
        Collections.sort(mList, new Comparator<BreakInAlerts>() {
            @Override
            public int compare(BreakInAlerts lhs, BreakInAlerts rhs) {
                int count_1 = (int) lhs.id;
                int count_2 = (int) rhs.id;
                return count_1 - count_2;
            }
        });
        return mList;
    }

}

package co.tpcreative.supersafe.ui.albumdetail;
import android.app.Activity;
import android.os.Bundle;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.room.InstanceGenerator;

public class AlbumDetailPresenter extends Presenter<BaseView> {

    protected List<Items> mList;
    protected MainCategories mainCategories;
    protected int videos = 0;
    protected int photos = 0;
    protected int audios = 0;
    protected List<Integer> mListExportShare = new ArrayList<>();
    protected List<File> mListShare = new ArrayList<>();
    protected EnumStatus status = EnumStatus.OTHER;



    public AlbumDetailPresenter(){
        mList = new ArrayList<>();
        mListExportShare = new ArrayList<>();
        mListShare = new ArrayList<>();
    }

    public void  getData(Activity activity){
        BaseView view = view();
        mList.clear();
        try {
            Bundle bundle = activity.getIntent().getExtras();
            mainCategories = (MainCategories) bundle.get(SuperSafeApplication.getInstance().getString(R.string.key_main_categories));
            if (mainCategories!=null){
                final List<Items> data = InstanceGenerator.getInstance(view.getContext()).getListItems(mainCategories.categories_local_id,false,mainCategories.isFakePin);
                if (data!=null){
                    mList = data;
                    onCalculate();
                }
                view.onSuccessful("Successful",EnumStatus.RELOAD);
            }
            else{
                Utils.onWriteLog("Main categories is null", EnumStatus.WRITE_FILE);
            }
        }
        catch (Exception e){
            Utils.onWriteLog(""+e.getMessage(), EnumStatus.WRITE_FILE);
        }
    }

    public void onCalculate(){
        photos = 0;
        videos = 0;
        audios = 0;
        for (Items index : mList){
            final EnumFormatType enumTypeFile = EnumFormatType.values()[index.formatType];
            switch (enumTypeFile){
                case IMAGE:{
                    photos+=1;
                    break;
                }
                case VIDEO:{
                    videos+=1;
                    break;
                }
                case AUDIO:{
                    audios+=1;
                    break;
                }
            }
        }
    }

    public void  getData(){
        BaseView view = view();
        mList.clear();
        try {
            if (mainCategories!=null){
                final List<Items> data = InstanceGenerator.getInstance(view.getContext()).getListItems(mainCategories.categories_local_id,false,mainCategories.isFakePin);
                if (data!=null){
                    mList = data;
                    onCalculate();
                }
                view.onSuccessful("Successful",EnumStatus.RELOAD);
            }
            else{
                Utils.onWriteLog("Main categories is null", EnumStatus.WRITE_FILE);
            }
        }
        catch (Exception e){
            Utils.onWriteLog(""+e.getMessage(), EnumStatus.WRITE_FILE);
        }
    }

    public void onDelete(){
        BaseView view = view();
        for (Items index : mList){
            if (index.isChecked()){
                index.isDeleteLocal = true;
                InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(index);
            }
        }
        getData();
    }

}

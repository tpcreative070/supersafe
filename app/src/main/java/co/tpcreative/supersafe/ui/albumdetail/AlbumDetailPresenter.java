package co.tpcreative.supersafe.ui.albumdetail;
import android.app.Activity;
import android.os.Bundle;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.entities.ItemEntity;
import co.tpcreative.supersafe.common.entities.MainCategoryEntity;
import co.tpcreative.supersafe.common.helper.SQLHelper;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.common.entities.InstanceGenerator;
import co.tpcreative.supersafe.model.ItemModel;
import co.tpcreative.supersafe.model.MainCategoryModel;

public class AlbumDetailPresenter extends Presenter<BaseView<Integer>> {
    private final static String TAG = AlbumDetailPresenter.class.getSimpleName();
    protected List<ItemModel> mList;
    protected MainCategoryModel mainCategories;
    protected int videos = 0;
    protected int photos = 0;
    protected int audios = 0;
    protected int others = 0;
    protected List<File> mListShare = new ArrayList<>();
    protected EnumStatus status = EnumStatus.OTHER;
    protected List<HashMap<Integer, ItemModel>> mListHashExporting;

    public AlbumDetailPresenter(){
        mList = new ArrayList<>();
        mListShare = new ArrayList<>();
        mListHashExporting = new ArrayList<>();
    }

    public void  getData(Activity activity){
        BaseView view = view();
        mList.clear();
        try {
            Bundle bundle = activity.getIntent().getExtras();
            mainCategories = (MainCategoryModel) bundle.get(SuperSafeApplication.getInstance().getString(R.string.key_main_categories));
            if (mainCategories!=null){
                final List<ItemModel> data = SQLHelper.getListItems(mainCategories.categories_local_id,false,false,mainCategories.isFakePin);
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
        others = 0;
        for (ItemModel index : mList){
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
                case FILES:{
                    others+=1;
                    break;
                }
            }
        }
    }

    public void  getData(EnumStatus enumStatus){
        BaseView view = view();
        mList.clear();
        try {
            if (mainCategories!=null){
                final List<ItemModel> data = SQLHelper.getListItems(mainCategories.categories_local_id,false,false,mainCategories.isFakePin);
                if (data!=null){
                    mList = data;
                    onCalculate();
                }
                view.onSuccessful("Successful",enumStatus);
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
        BaseView<Integer> view = view();
        for (int i =0 ; i<mList.size();i++){
            if (mList.get(i).isChecked()){
                Utils.Log(TAG,"Delete position at "+ i);
                mList.get(i).isDeleteLocal = true;
               SQLHelper.updatedItem(mList.get(i));
                view.onSuccessful("Successful",EnumStatus.DELETE,i);
            }
        }
        view.onSuccessful("Successful",EnumStatus.DELETE);
        getData(EnumStatus.REFRESH);
    }
}

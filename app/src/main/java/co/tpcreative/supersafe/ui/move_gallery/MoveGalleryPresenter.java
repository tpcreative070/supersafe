package co.tpcreative.supersafe.ui.move_gallery;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.GalleryAlbum;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.room.InstanceGenerator;

public class MoveGalleryPresenter extends Presenter<MoveGalleryView>{

    private static final String TAG = MoveGalleryPresenter.class.getSimpleName();
    protected List<GalleryAlbum> mList ;
    protected int videos = 0;
    protected int photos = 0;
    protected int audios = 0;
    protected int others = 0;

    public MoveGalleryPresenter(){
        mList = new ArrayList<>();
    }

    public void  getData(String categories_local_id,boolean isFakePIN){
        mList.clear();
        MoveGalleryView view = view();
        final List<MainCategories> list = MainCategories.getInstance().getListMoveGallery(categories_local_id,isFakePIN);
        if (isFakePIN) {
            final MainCategories main = MainCategories.getInstance().getMainItemFakePin();
            if (!main.categories_local_id.equals(categories_local_id)){
                list.add(main);
                Collections.sort(list, new Comparator<MainCategories>() {
                    @Override
                    public int compare(MainCategories lhs, MainCategories rhs) {
                        int count_1 = (int) lhs.categories_max;
                        int count_2 = (int) rhs.categories_max;
                        return count_1 - count_2;
                    }
                });
            }
        }
        if (list!=null){
            for (MainCategories index : list){
                final List<Items> mListItem = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListItems(index.categories_local_id,false,isFakePIN);
                photos = 0;
                videos = 0;
                audios = 0;
                others = 0;
                for (Items i : mListItem){
                    final EnumFormatType enumTypeFile = EnumFormatType.values()[i.formatType];
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
                            others +=1;
                            break;
                        }
                    }
                }
                mList.add(new GalleryAlbum(index,photos,videos,audios,others));
            }
        }
        view.onSuccessful("Successful", EnumStatus.RELOAD);
    }

    public void onMoveItemsToAlbum(int position){
        MoveGalleryView view = view();
        final GalleryAlbum gallery = mList.get(position);
        final List<Items>mList = view.getListItems();
        if (mList!=null){
            for(int i = 0;i<mList.size();i++){
                final Items item = mList.get(i);
                if (item.isChecked){
                    item.categories_local_id = gallery.main.categories_local_id;
                    item.categories_id = gallery.main.categories_id;
                    if (item.isSyncCloud && item.isSyncOwnServer){
                        item.isUpdate = true;
                    }
                    Utils.Log(TAG,"Warning " +item.isUpdate + "; isSyncCloud "+ item.isSyncCloud + "; isSyncOwnServer " + item.isSyncOwnServer);
                    InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(item);
                }
            }
            view.onSuccessful("Successful",EnumStatus.MOVE);
        }
        else{
            Utils.Log(TAG,"Nulll");
        }
    }
}

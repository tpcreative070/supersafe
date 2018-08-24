package co.tpcreative.suppersafe.model;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.services.SupperSafeApplication;

public class CategoryFolder {

    public final String main_album;
    public final String cards_ids;
    public final String videos;
    public final String significant_other;

    public CategoryFolder(String main_album,String cards_ids,String videos,String significant_other){
        this.main_album = main_album;
        this.cards_ids = cards_ids;
        this.videos = videos;
        this.significant_other = significant_other;
    }

    public CategoryFolder(){
        this.main_album = SupperSafeApplication.getInstance().getString(R.string.key_main_album);
        this.cards_ids = SupperSafeApplication.getInstance().getString(R.string.key_card_ids);
        this.videos = SupperSafeApplication.getInstance().getString(R.string.key_videos);;
        this.significant_other = SupperSafeApplication.getInstance().getString(R.string.key_significant_other);;
    }

}

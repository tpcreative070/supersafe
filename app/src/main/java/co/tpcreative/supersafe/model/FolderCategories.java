package co.tpcreative.supersafe.model;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;

public class FolderCategories {

    public final String main_album;
    public final String cards_ids;
    public final String videos;
    public final String significant_other;

    public FolderCategories(String main_album, String cards_ids, String videos, String significant_other){
        this.main_album = main_album;
        this.cards_ids = cards_ids;
        this.videos = videos;
        this.significant_other = significant_other;
    }

    public FolderCategories(){
        this.main_album = SuperSafeApplication.getInstance().getString(R.string.key_main_album);
        this.cards_ids = SuperSafeApplication.getInstance().getString(R.string.key_card_ids);
        this.videos = SuperSafeApplication.getInstance().getString(R.string.key_videos);;
        this.significant_other = SuperSafeApplication.getInstance().getString(R.string.key_significant_other);;
    }

}

package co.tpcreative.supersafe.model;

import co.tpcreative.supersafe.common.entities.MainCategoryEntity;

public class GalleryAlbum {
    public final int videos;
    public final int photos;
    public final int audios;
    public final int others;
    public final MainCategoryModel main;


    public GalleryAlbum(MainCategoryModel main, int photos, int videos, int audios, int others){
        this.main = main;
        this.photos = photos;
        this.videos = videos;
        this.audios = audios;
        this.others = others;
    }
}

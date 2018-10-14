package co.tpcreative.supersafe.model;

public class GalleryAlbum {
    public final int videos;
    public final int photos;
    public final int audios;
    public final MainCategories main;


    public GalleryAlbum(MainCategories main,int photos,int videos, int audios){
        this.main = main;
        this.photos = photos;
        this.videos = videos;
        this.audios = audios;
    }
}

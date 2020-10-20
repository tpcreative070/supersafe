package co.tpcreative.supersafe.model

class GalleryAlbum(main: MainCategoryModel?, photos: Int, videos: Int, audios: Int, others: Int) {
    val videos: Int
    val photos: Int
    val audios: Int
    val others: Int
    val main: MainCategoryModel?

    init {
        this.main = main
        this.photos = photos
        this.videos = videos
        this.audios = audios
        this.others = others
    }
}
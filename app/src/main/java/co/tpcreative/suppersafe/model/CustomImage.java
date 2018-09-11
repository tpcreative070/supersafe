package co.tpcreative.suppersafe.model;
import java.util.ArrayList;
import java.util.List;


public class CustomImage {

    private String url;
    private String description;
    private static CustomImage instance;


    public static  CustomImage getInstance(){
        if (instance==null){
            instance = new CustomImage();
        }
        return instance;
    }

    public List<CustomImage> getList(){
        List<CustomImage> mList = new ArrayList<>();
        String root = "http://192.168.0.102/files/";
        mList.add(new CustomImage(root+"20180910_135632.jpg","Image 1"));
        mList.add(new CustomImage(root+"20180910_135639.jpg","Image 2"));
        mList.add(new CustomImage(root+"20180910_135644.jpg","Image 3"));
        mList.add(new CustomImage(root+"20180910_135647.jpg","Image 4"));
        mList.add(new CustomImage(root+"20180910_135540.jpg","Image 4"));
        mList.add(new CustomImage(root+"20180910_135542.jpg","Image 4"));
        return mList;
    }

    public CustomImage(){

    }

    public CustomImage(String url, String description) {
        this.url = url;
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public String getDescription() {
        return description;
    }

}
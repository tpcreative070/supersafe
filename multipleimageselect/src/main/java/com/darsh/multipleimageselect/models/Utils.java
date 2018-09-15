package com.darsh.multipleimageselect.models;

import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.util.HashMap;

public class Utils {
    public static HashMap<String,MimeTypeFile> mediaTypeSupport(){
        HashMap<String,MimeTypeFile> hashMap = new HashMap<>();
        hashMap.put("mp4",new MimeTypeFile(".mp4", EnumFormatType.VIDEO,"video/mp4"));
        hashMap.put("3gp",new MimeTypeFile(".3gp", EnumFormatType.VIDEO,"video/3gp"));
        hashMap.put("wmv",new MimeTypeFile(".wmv", EnumFormatType.VIDEO,"video/wmv"));
        hashMap.put("mkv",new MimeTypeFile(".mkv", EnumFormatType.VIDEO,"video/mkv"));
        hashMap.put("m4a",new MimeTypeFile(".m4a", EnumFormatType.AUDIO,"audio/m4a"));
        hashMap.put("aac",new MimeTypeFile(".aac", EnumFormatType.AUDIO,"audio/aac"));
        hashMap.put("mp3",new MimeTypeFile(".mp3", EnumFormatType.AUDIO,"audio/mp3"));
        hashMap.put("wav",new MimeTypeFile(".wav", EnumFormatType.AUDIO,"audio/wav"));
        hashMap.put("m4a",new MimeTypeFile(".m4a", EnumFormatType.AUDIO,"audio/m4a"));
        hashMap.put("jpg",new MimeTypeFile(".jpg", EnumFormatType.IMAGE,"image/jpeg"));
        hashMap.put("png",new MimeTypeFile(".png", EnumFormatType.IMAGE,"image/png"));
        return hashMap;
    }

    public static String getFileExtension(String url){
        String fileExt = MimeTypeMap.getFileExtensionFromUrl(url.toLowerCase());
        return fileExt;
    }


}

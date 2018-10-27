package com.darsh.multipleimageselect.models;

public class MimeTypeFile {

    public String extension;
    public EnumFormatType formatType;
    public String mimeType ;

    public MimeTypeFile(String extension, EnumFormatType formatType, String mimeType){
        this.extension = extension;
        this.formatType = formatType;
        this.mimeType = mimeType;
    }
}

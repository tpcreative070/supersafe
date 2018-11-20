package co.tpcreative.supersafe.model;

import java.io.Serializable;

public class ImportFiles implements Serializable{
    public MainCategories mainCategories;
    public String path ;
    public MimeTypeFile mimeTypeFile;
    public int position;
    public boolean isImport;

    public ImportFiles(MainCategories mainCategories,MimeTypeFile mimeTypeFile,String path,int position,boolean isImport){
        this.mainCategories = mainCategories;
        this.mimeTypeFile = mimeTypeFile;
        this.path = path;
        this.position = position;
        this.isImport = isImport;
    }


}

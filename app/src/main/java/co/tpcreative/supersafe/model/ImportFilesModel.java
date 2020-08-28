package co.tpcreative.supersafe.model;
import java.io.Serializable;
import co.tpcreative.supersafe.common.entities.MainCategoryEntity;
import co.tpcreative.supersafe.common.util.Utils;

public class ImportFilesModel implements Serializable{
    public MainCategoryEntity mainCategories;
    public String path ;
    public MimeTypeFile mimeTypeFile;
    public int position;
    public boolean isImport;
    public String unique_id;

    public ImportFilesModel(MainCategoryEntity mainCategories, MimeTypeFile mimeTypeFile, String path, int position, boolean isImport){
        this.mainCategories = mainCategories;
        this.mimeTypeFile = mimeTypeFile;
        this.path = path;
        this.position = position;
        this.isImport = isImport;
        this.unique_id = Utils.getUUId();
    }
}

package co.tpcreative.supersafe.model;
import java.io.File;

public class ExportFiles {
    public File input;
    public File output;
    public int position;
    public boolean isExport;

    public ExportFiles(File input,File output,int position,boolean isExport){
        this.input = input;
        this.output = output;
        this.position = position;
        this.isExport = isExport;
    }

}

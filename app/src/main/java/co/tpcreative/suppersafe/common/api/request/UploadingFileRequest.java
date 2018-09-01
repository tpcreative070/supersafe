/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.tpcreative.suppersafe.common.api.request;
import java.io.File;
import java.util.List;
import java.util.Map;

public class UploadingFileRequest {
    public List<File> list;
    public Map<String,String> mapHeader;
    public Map<String,String> mapBody;
    public Map<String,Object> mapObject;
    public int code ;
}

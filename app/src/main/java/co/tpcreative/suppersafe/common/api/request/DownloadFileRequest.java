/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.tpcreative.suppersafe.common.api.request;
import java.util.Map;


public class DownloadFileRequest {
    public String Authorization;
    public String file ;
    public String api_name ;
    public String file_name ;
    public String config;
    public String device_param;
    public String path_folder_output;
    public Map<String,String> mapHeader;
    public Map<String,Object> mapObject;
    public int code ;
}

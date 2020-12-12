/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.tpcreative.supersafe.common.api.request
import java.io.File

class UploadingFileRequest {
    var list: MutableList<File?>? = null
    var mapHeader: MutableMap<String?, String?>? = null
    var mapBody: MutableMap<String?, String?>? = null
    var mapObject: MutableMap<String?, Any?>? = null
    var code = 0
}
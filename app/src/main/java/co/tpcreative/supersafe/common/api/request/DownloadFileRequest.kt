/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.tpcreative.supersafe.common.api.request
import co.tpcreative.supersafe.model.ItemModel

class DownloadFileRequest {
    var Authorization: String? = null
    var file: String? = null
    var file_name: String? = null
    var items: ItemModel? = null
    var path_folder_output: String? = null
    var id: String? = null
    var code = 0
}
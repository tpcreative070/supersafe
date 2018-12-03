package co.tpcreative.supersafe.model;

import co.tpcreative.supersafe.ui.albumcover.AlbumCoverActivity;

public class MessageEvent {
    public EnumStatus enumStatus;
    public String object;

    public MessageEvent(){
        enumStatus = EnumStatus.OTHER;
        object = "";
    }
}

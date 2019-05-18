package co.tpcreative.supersafe.model;
public class MessageEvent {
    public EnumStatus enumStatus;
    public String object;
    public MessageEvent(){
        enumStatus = EnumStatus.OTHER;
        object = "";
    }
}

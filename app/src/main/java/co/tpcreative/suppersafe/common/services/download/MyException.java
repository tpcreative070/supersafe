package co.tpcreative.suppersafe.common.services.download;

/**
 * Created by PC on 11/1/2017.
 */

public class MyException extends Exception {
    public String message;

    public MyException(String message){
        this.message = message;
    }
    // Overrides Exception's getMessage()
    @Override
    public String getMessage(){
        return message;
    }

}

package co.tpcreative.supersafe.common.controller;
import android.util.Log;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import java.util.HashMap;
import java.util.Map;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;

public class ManagerNetwork {

    // Tag used to cancel the request
    private static final String tag_json_obj = "json_obj_req";
    private static final String TAG = ManagerNetwork.class.getSimpleName();
    private static final String url = SuperSafeApplication.getInstance().getUrl();
    private ManagerNetworkListener listener;
    private  static ManagerNetwork instance;

    public static ManagerNetwork getInstance(){
        if (instance==null){
            instance = new ManagerNetwork();
        }
        return instance;
    }

    public void setListener(ManagerNetworkListener ls){
        this.listener = ls;
    }


    public void onCheckout(){
        Log.d(TAG,"url :" + url );
        StringRequest jsonObjReq = new StringRequest(Request.Method.POST,
                url+"/api/signin",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response.toString());
                        if (listener!=null){
                           listener.showSuccessful(response.toString());
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                if (listener!=null){
                    listener.showError(error.getMessage());
                }
            }
        }) {

            /**
             * Passing some request headers
             * */

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                headers.put("Authorization", SuperSafeApplication.getInstance().getApplicationContext().getString(R.string.authorization));
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", "tpcreative.co@gmail.com");
                params.put("password", "tpcreative.co");
                return params;
            }
        };
        // Adding request to request queue
        SuperSafeApplication.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
    }

    public void onSignUp(final String email,final String name,ManagerNetworkListener ls){
        Log.d(TAG,"url :" + url );
        StringRequest jsonObjReq = new StringRequest(Request.Method.POST,
                url+"/api/signup",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response.toString());
                        if (ls!=null){
                            ls.showSuccessful(response.toString());
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                if (ls!=null){
                    ls.showError(error.getMessage());
                }
            }

        }) {

            /**
             * Passing some request headers
             * */

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                headers.put("Authorization", SuperSafeApplication.getInstance().getApplicationContext().getString(R.string.authorization));
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("password", "tpcreative.co");
                params.put("name",name);
                return params;
            }
        };
        // Adding request to request queue
        SuperSafeApplication.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);

    }


    public void onSignIn(final String email,ManagerNetworkListener ls){
        Log.d(TAG,"url :" + url );
        StringRequest jsonObjReq = new StringRequest(Request.Method.POST,
                url+"/api/signin",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response.toString());
                        if (ls!=null){
                            ls.showSuccessful(response.toString());
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                if (ls!=null){
                    ls.showError(error.getMessage());
                }
            }

        }) {

            /**
             * Passing some request headers
             * */

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                headers.put("Authorization", SuperSafeApplication.getInstance().getApplicationContext().getString(R.string.authorization));
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("password","tpcreative.co");
                return params;
            }
        };
        // Adding request to request queue
        SuperSafeApplication.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
    }

    public interface ManagerNetworkListener{
        void showError(final String message);
        void showSuccessful(final String z);
    }

}

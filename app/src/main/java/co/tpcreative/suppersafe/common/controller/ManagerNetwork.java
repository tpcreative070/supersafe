package co.tpcreative.suppersafe.common.controller;
import android.util.Log;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import java.util.HashMap;
import java.util.Map;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.services.SupperSafeApplication;

public class ManagerNetwork {

    // Tag used to cancel the request
    private static final String tag_json_obj = "json_obj_req";
    private static final String TAG = ManagerNetwork.class.getSimpleName();
    private static final String url = SupperSafeApplication.getInstance().getUrl();

    public static void onCheckout(){
        Log.d(TAG,"url :" + url );
        StringRequest jsonObjReq = new StringRequest(Request.Method.POST,
                url+"/api/signin",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response.toString());
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
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
                headers.put("Authorization",SupperSafeApplication.getInstance().getApplicationContext().getString(R.string.authorization));
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
        SupperSafeApplication.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);

    }

}

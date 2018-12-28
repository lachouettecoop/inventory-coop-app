package org.coop.inventory.api;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.entity.StringEntity;

public class ApiClient {
    public void setHostname(String hostName) {
        this._hostName = hostName;
    }
    private String _hostName;

    public void ping(JsonHttpResponseHandler responseHandler) {
        get("ping", null, responseHandler);
    }

    public void login(String email, String password, JsonHttpResponseHandler responseHandler) {
        try {
            JSONObject jsonParams = new JSONObject();
            jsonParams.put("email", email);
            jsonParams.put("password", password);
            StringEntity entity = new StringEntity(jsonParams.toString());
            post("login", null, entity, responseHandler);
        } catch (JSONException e) {
            //TODO
        } catch (UnsupportedEncodingException e) {
            //TODO
        }
    }

    public void setAuthorization(String token) {
        client.addHeader("Authorization", "Bearer " + token);
    }

    public void getInventories(JsonHttpResponseHandler responseHandler) {
        get("inventories", null, responseHandler);
    }

    public void getProducts(String inventoryId, JsonHttpResponseHandler responseHandler) {
        RequestParams where = new RequestParams("where", "{\"inventory\":\""+ inventoryId + "\"}");
        get("products", where, responseHandler);
    }

    public void getCounts(String inventoryId, JsonHttpResponseHandler responseHandler) {
        RequestParams where = new RequestParams("where", "{\"inventory\":\""+ inventoryId + "\"}");
        get("counts", where, responseHandler);
    }

    public void postQty(String inventory, String product, String zone, String counter, double qty, JsonHttpResponseHandler responseHandler) {
        try {
            JSONObject jsonParams = new JSONObject();
            jsonParams.put("inventory", inventory);
            jsonParams.put("product", product);
            jsonParams.put("zone", zone);
            jsonParams.put("counter", counter);
            jsonParams.put("qty", qty);
            StringEntity entity = new StringEntity(jsonParams.toString());
            post("counts", null, entity, responseHandler);
        } catch (JSONException e) {
            //TODO
        } catch (UnsupportedEncodingException e) {
            //TODO
        }
    }

    private void get(String url, RequestParams params, JsonHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    private void post(String url, RequestParams params, StringEntity payload, JsonHttpResponseHandler responseHandler) {
        client.post(null, getAbsoluteUrl(url), payload, "application/json", responseHandler);
    }

    private String getAbsoluteUrl(String relativeUrl) {
        return _hostName + "/api/v1/" + relativeUrl;
    }

    public static ApiClient inst()
    {
        if (instance == null)
            instance = new ApiClient();
        return instance;
    }
    private ApiClient() {
        client = new AsyncHttpClient();
        client.setMaxRetriesAndTimeout(2, 2000);
        client.addHeader("Accept", "application/json");
        client.addHeader("Content-type", "application/json;charset=utf-8");
    }
    private static ApiClient instance = null;
    private AsyncHttpClient client = null;
}

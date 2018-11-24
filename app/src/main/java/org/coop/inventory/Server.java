package org.coop.inventory;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.coop.inventory.api.ApiClient;
import org.coop.inventory.model.InventoryModel;
import org.coop.inventory.model.ModelStorage;
import org.coop.inventory.model.ProductModel;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cz.msebera.android.httpclient.Header;

public class Server extends AppCompatActivity {
    private EditText serverUrl;
    private Button serverCheck;
    private Button next;
    private RadioGroup rgp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server);

        serverUrl = findViewById(R.id.serverUrl);
        serverCheck = findViewById(R.id.serverCheck);

        serverUrl.setText("192.168.1.26:8000");
    }

    private String getServerUrl() {
        return serverUrl.getText().toString();
    }

    public void checkServer(View view) {
        ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this
                .findViewById(android.R.id.content)).getChildAt(0);

        if (next != null) {
            viewGroup.removeView(next);
            next = null;
        }
        if (rgp != null) {
            viewGroup.removeView(rgp);
            rgp = null;
        }

        String url = getServerUrl();
        if (!url.startsWith("http")) {
            url = "http://" + url;
        }
        ApiClient.getInstance().setHostname(url);

        ApiClient.getInstance().ping(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.get("name").equals("inventory-coop") &&
                            response.get("status").equals("ok")) showInventorySelection();
                } catch (JSONException e)
                {
                    showToast("Bad server");
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                showToast("Bad server");
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                showToast("Bad server");
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                showToast("Bad server");
            }
        });
    }

    public void showToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public void showInventorySelection() {
        ModelStorage.inst().getInventoriesList().clear();
        ApiClient.getInstance().getInventories(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONArray inventories = response.getJSONArray("items");
                    int rbnId = 1000;
                    for (int i = 0 ; i < inventories.length(); i++) {
                        JSONObject inventory = inventories.getJSONObject(i);
                        ModelStorage.inst().addInventories(
                                inventory.getString("_id"),
                                inventory.getString("date"),
                                inventory.getInt("state"),
                                rbnId++
                        );
                    }
                    createInventoryList();
                } catch (JSONException e)
                {
                    showToast("Bad server");
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                showToast("Bad server");
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                showToast("Bad server");
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                showToast("Bad server");
            }
        });
    }

    public void createInventoryList() {
        ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this
                .findViewById(android.R.id.content)).getChildAt(0);

        rgp = new RadioGroup(getApplicationContext());

        List<InventoryModel> inventories = ModelStorage.inst().getInventoriesList();
        boolean isFirst = true;
        for (InventoryModel inventory : inventories) {
            if (inventory.getState() == InventoryModel.ACTIVE) {
                RadioButton rbn = new RadioButton(this);
                rbn.setId(inventory.getRbnId());
                rbn.setText(inventory.getDate());
                rgp.addView(rbn);
                if (isFirst) {
                    rgp.check(rbn.getId());
                    isFirst = false;
                }
            }
        }
        viewGroup.addView(rgp);

        next = new Button(getApplicationContext());
        next.setText(R.string.next);
        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ModelStorage.inst().setSelectedInventory(rgp.getCheckedRadioButtonId());
                getProducts();
            }
        });
        viewGroup.addView(next);
    }

    private void getProducts() {
        ApiClient.getInstance().getProducts(
                ModelStorage.inst().getSelectedInventory().getId(),
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            JSONArray products = response.getJSONArray("items");
                            for (int i = 0 ; i < products.length(); i++) {
                                JSONObject product = products.getJSONObject(i);
                                ModelStorage.inst().getSelectedInventory().addProduct(
                                        product.getString("_id"),
                                        product.getString("name"),
                                        product.getString("barcode")
                                );
                            }
                            getCounts();
                        } catch (JSONException e)
                        {
                            showToast("Bad server");
                        }
                    }
                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        showToast("Bad server");
                    }
                });
    }

    private void getCounts() {
        ApiClient.getInstance().getCounts(
                ModelStorage.inst().getSelectedInventory().getId(),
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            JSONArray counts = response.getJSONArray("items");
                            for (int i = 0 ; i < counts.length(); i++) {
                                JSONObject count = counts.getJSONObject(i);
                                String productId = count.getString("product");
                                ProductModel product = ModelStorage.inst().getSelectedInventory().getProductsById().get(productId);
                                product.addCount(
                                        count.getString("_id"),
                                        count.getString("zone"),
                                        count.getString("counter"),
                                        count.getInt("qty"),
                                        count.getString("updated")
                                );
                                ModelStorage.inst().addZone(count.getString("zone"));
                                ModelStorage.inst().addCounter(count.getString("counter"));
                            }
                            Intent intentApp = new Intent(Server.this, Identification.class);
                            Server.this.startActivity(intentApp);
                        } catch (JSONException e) {
                            showToast("Bad server");
                        }
                    }
                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        showToast("Bad server");
                    }
                });
    }
}

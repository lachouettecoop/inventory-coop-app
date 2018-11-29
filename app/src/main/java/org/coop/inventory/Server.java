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
    private RadioGroup rgp;
    private Button serverValidate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server);

        serverUrl = findViewById(R.id.txtServerUrl);
        serverCheck = findViewById(R.id.btnServerCheck);
        rgp = findViewById(R.id.rgInventoryList);
        serverValidate = findViewById(R.id.btnServerValidate);

        serverUrl.setText("192.168.1.26:8000");

        serverValidate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                serverUrl.setEnabled(false);
                serverCheck.setEnabled(false);
                serverValidate.setEnabled(false);
                ModelStorage.inst().setSelectedInventory(rgp.getCheckedRadioButtonId());
                getProducts();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        rgp.clearCheck();
        rgp.removeAllViews();
        serverUrl.setEnabled(true);
        serverCheck.setEnabled(true);
        serverValidate.setEnabled(false);
    }

    private String getServerUrl() {
        return serverUrl.getText().toString();
    }

    public void checkServer(View view) {
        rgp.clearCheck();
        rgp.removeAllViews();
        serverCheck.setEnabled(false);
        serverValidate.setEnabled(false);

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
                } finally {
                    serverCheck.setEnabled(true);
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                showToast("Bad server");
                serverCheck.setEnabled(true);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                showToast("Bad server");
                serverCheck.setEnabled(true);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                showToast("Bad server");
                serverCheck.setEnabled(true);
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
        if(isFirst) {
            // No inventory -> No button
            serverValidate.setEnabled(false);
        } else {
            serverValidate.setEnabled(true);
        }
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

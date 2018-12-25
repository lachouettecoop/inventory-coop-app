package org.coop.inventory;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.coop.inventory.api.ApiClient;
import org.coop.inventory.mixin.JWTUtils;
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
    private EditText txtServerUrl;
    private EditText txtEmail;
    private EditText txtPassword;
    private Button btnServerCheck;
    private RadioGroup rgpInventoryList;
    private Button btnServerNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server);

        txtServerUrl = findViewById(R.id.txtServerUrl);
        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        btnServerCheck = findViewById(R.id.btnServerCheck);
        rgpInventoryList = findViewById(R.id.rgInventoryList);
        btnServerNext = findViewById(R.id.btnServerNext);

        txtServerUrl.setText("192.168.123.1:8000");
        txtServerUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                setBtnServerCheckEnabled();
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        txtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                setBtnServerCheckEnabled();
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        txtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                setBtnServerCheckEnabled();
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        btnServerCheck.setEnabled(false);

        btnServerNext.setEnabled(false);
        btnServerNext.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setEnabled(false);
                btnServerNext.setEnabled(false);
                ModelStorage.inst().setSelectedInventory(rgpInventoryList.getCheckedRadioButtonId());
                getProducts();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        rgpInventoryList.clearCheck();
        rgpInventoryList.removeAllViews();
        setEnabled(true);
        btnServerNext.setEnabled(false);
    }

    private void setBtnServerCheckEnabled() {
        String serverUrl = txtServerUrl.getText().toString();
        String email = txtEmail.getText().toString();
        String password = txtPassword.getText().toString();

        btnServerCheck.setEnabled(!serverUrl.isEmpty() && !email.isEmpty() && !password.isEmpty());
    }

    private void setEnabled(boolean enabled) {
        txtServerUrl.setEnabled(enabled);
        txtEmail.setEnabled(enabled);
        txtPassword.setEnabled(enabled);
        btnServerCheck.setEnabled(enabled);
    }

    private String getTxtServerUrl() {
        return txtServerUrl.getText().toString();
    }

    public void checkServer(View view) {
        rgpInventoryList.clearCheck();
        rgpInventoryList.removeAllViews();
        setEnabled(false);
        btnServerNext.setEnabled(false);

        String url = getTxtServerUrl();
        if (!url.startsWith("http")) {
            url = "http://" + url;
        }
        ApiClient.inst().setHostname(url);

        ApiClient.inst().login(txtEmail.getText().toString(), txtPassword.getText().toString(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    String token = response.getString("token");
                    try {
                        JSONObject userInfo = JWTUtils.decoded(token);
                        ModelStorage.inst().setCounterName(userInfo.getString("lastname"));
                        ApiClient.inst().setAuthorization(token);
                        showInventorySelection();
                    } catch (Exception e) {
                        showToast("Bad token" + e);
                    }
                } catch (JSONException e) {
                    showToast("Bad server");
                } finally {
                    setEnabled(true);
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                showToast("Bad server");
                setEnabled(true);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                showToast("Bad server");
                setEnabled(true);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                showToast("Bad server");
                setEnabled(true);
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
        ApiClient.inst().getInventories(new JsonHttpResponseHandler() {
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
                rgpInventoryList.addView(rbn);
                if (isFirst) {
                    rgpInventoryList.check(rbn.getId());
                    isFirst = false;
                }
            }
        }
        if(isFirst) {
            // No inventory -> No button
            btnServerNext.setEnabled(false);
        } else {
            btnServerNext.setEnabled(true);
        }
    }

    private void getProducts() {
        ApiClient.inst().getProducts(
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
        ApiClient.inst().getCounts(
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

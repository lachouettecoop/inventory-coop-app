package org.coop.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.coop.inventory.api.ApiClient;
import org.coop.inventory.model.CountView;
import org.coop.inventory.model.CountModel;
import org.coop.inventory.model.ModelStorage;
import org.coop.inventory.model.ProductModel;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import cz.msebera.android.httpclient.Header;

public class Inventory extends AppCompatActivity {

    public final int CUSTOMIZED_REQUEST_CODE = 0x0000ffff;

    private AutoCompleteTextView txtBarcode;
    private AutoCompleteTextView txtName;
    private EditText txtQty;
    private Button btnValidate;

    private CountsArrayAdapter countsAdapter = null;
    private ProductModel selectedProduct = null;
    private Double selectedQty = 0.0;
    private boolean reactOnTextChange = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inventory);

        txtBarcode = findViewById(R.id.txtBarcode);
        txtName = findViewById(R.id.txtName);
        txtQty = findViewById(R.id.txtQty);
        btnValidate = findViewById(R.id.btnValidate);

        btnValidate.setEnabled(false);

        txtBarcode.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line,
                ModelStorage.inst().getSelectedInventory().getBarcodes()));
        txtBarcode.setThreshold(4);
        txtBarcode.setHint(R.string.barcode);
        txtBarcode.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (reactOnTextChange) {
                    reactOnTextChange = false;
                    String barcode = s.toString();
                    ProductModel product = ModelStorage.inst().getSelectedInventory().getProductsByBarcode().get(barcode);
                    if (product != null) {
                        txtName.setText(product.getName());
                        selectedProduct = product;
                        txtQty.requestFocus();
                    } else {
                        txtName.setText("");
                        selectedProduct = null;
                    }
                    txtQty.setText("");
                    reactOnTextChange = true;
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        txtName.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line,
                ModelStorage.inst().getSelectedInventory().getProductNames()));
        txtName.setThreshold(1);
        txtName.setHint(R.string.productName);
        txtName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (reactOnTextChange) {
                    reactOnTextChange = false;
                    String name = s.toString();
                    ProductModel product = ModelStorage.inst().getSelectedInventory().getProductsByName().get(name);
                    if (product != null) {
                        txtBarcode.setText(product.getBarcode());
                        selectedProduct = product;
                        txtQty.requestFocus();
                    } else {
                        txtBarcode.setText("");
                        selectedProduct = null;
                    }
                    txtQty.setText("");
                    reactOnTextChange = true;
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        txtQty.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    selectedQty = Double.valueOf(s.toString());
                } catch (NumberFormatException e) {
                    selectedQty = 0.0;
                }
                if (selectedProduct != null && selectedQty != 0) {
                    btnValidate.setEnabled(true);
                } else {
                    btnValidate.setEnabled(false);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        List<CountView> counts = new ArrayList<>();
        for(ProductModel product : ModelStorage.inst().getSelectedInventory().getProductsByName().values()) {
            for(CountModel count: product.getCounts().values()) {
                if(count.getCounterName().equalsIgnoreCase(ModelStorage.inst().getCounterName())) {
                    counts.add(new CountView(product.getName(), count.getQty(), count.getUpdated()));
                }
            }
        }
        Collections.sort(counts, new Comparator<CountView>() {
            @Override
            public int compare(CountView o1, CountView o2) {
                return o2.updateAt.compareTo(o1.updateAt);
            }
        });

        countsAdapter = new CountsArrayAdapter(this, counts);
        ListView listView = findViewById(R.id.countsView);
        listView.setAdapter(countsAdapter);
    }

    public void scanBarcodeCustomLayout(View view) {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(AnyOrientationCaptureActivity.class);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
        integrator.setPrompt("Scanner le code barre");
        integrator.setOrientationLocked(false);
        integrator.setBeepEnabled(false);
        integrator.initiateScan();
    }

    public void recordCount(View view) {
        btnValidate.setEnabled(false);
        if (selectedProduct != null && selectedQty != 0) {
            ApiClient.getInstance().postQty(
                    ModelStorage.inst().getSelectedInventory().getId(),
                    selectedProduct.getId(),
                    ModelStorage.inst().getZoneName(),
                    ModelStorage.inst().getCounterName(),
                    selectedQty,
                    new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Date date = new Date();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                            String updated = dateFormat.format(date);
                            double qty = selectedQty;
                            try {
                                qty = response.getDouble("qty");
                                updated = response.getString("updated");
                            } catch (JSONException e) {
                            }
                            countsAdapter.insert(new CountView(selectedProduct.getName(), qty, updated), 0);
                            txtBarcode.setText("");
                            //TODO
                        }
                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            showToast("Bad server");
                            btnValidate.setEnabled(true);
                        }
                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            showToast("Bad server");
                            btnValidate.setEnabled(true);
                        }
                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                            showToast("Bad server");
                            btnValidate.setEnabled(true);
                        }
                    }
            );
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != CUSTOMIZED_REQUEST_CODE && requestCode != IntentIntegrator.REQUEST_CODE) {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        switch (requestCode) {
            case CUSTOMIZED_REQUEST_CODE: {
                Toast.makeText(this, "REQUEST_CODE = " + requestCode, Toast.LENGTH_LONG).show();
                break;
            }
            default:
                break;
        }

        IntentResult result = IntentIntegrator.parseActivityResult(resultCode, data);

        if(result.getContents() == null) {
            Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
        } else {
            txtBarcode.setText(result.getContents());
        }
    }

    public void showToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
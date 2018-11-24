package org.coop.inventory;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import org.coop.inventory.model.ModelStorage;

public class Identification extends AppCompatActivity {

    private Button indentificationNext;
    private AutoCompleteTextView identificationName;
    private EditText identificationZone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.identification);

        indentificationNext = findViewById(R.id.identificationNext);
        identificationZone = findViewById(R.id.identificationZone);
        identificationName = findViewById(R.id.autoCompleteTextView1);

        indentificationNext.setEnabled(false);
        identificationZone.addTextChangedListener(new IdentificationTextWatcher());

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, ModelStorage.inst().getCounters());
        identificationName.setAdapter(adapter);
        identificationName.setThreshold(1);
        identificationName.setHint(R.string.name);
    }

    private class IdentificationTextWatcher implements TextWatcher {
        @Override
        public void afterTextChanged(Editable s) { manageNextButton(); }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }

    private void manageNextButton() {
        if(identificationName.getText().length() > 0 && identificationZone.getText().length() > 0) {
            indentificationNext.setEnabled(true);
        } else {
            indentificationNext.setEnabled(false);
        }
    }

    public void switchToServerActivity(View view) {
        ModelStorage.inst().setCounterName(identificationName.getText().toString());
        ModelStorage.inst().setZoneName(identificationZone.getText().toString());
        Intent intentApp = new Intent(Identification.this, Inventory.class);
        Identification.this.startActivity(intentApp);
    }
}
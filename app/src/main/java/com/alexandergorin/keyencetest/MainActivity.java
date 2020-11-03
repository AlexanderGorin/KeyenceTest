package com.alexandergorin.keyencetest;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import com.google.android.material.textfield.TextInputEditText;
import com.keyence.autoid.sdk.scan.DecodeResult;
import com.keyence.autoid.sdk.scan.ScanManager;
import com.keyence.autoid.sdk.scan.scanparams.DataOutput;

public class MainActivity extends AppCompatActivity implements ScanManager.DataListener {
    private ScanManager _scanManager;
    private final DataOutput dataOutput = new DataOutput();
    private boolean _defaultKeyStrokeEnabled = true;
    private AppCompatTextView textView;
    private Button button;
    boolean isLocked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textViewScanner);
        button = findViewById(R.id.buttonScanner);
        textView.setText("------------");

        button.setOnClickListener(v -> {
            if (isLocked) {
                unlockScanner();
            } else {
                lockScanner();
            }
        });

        _scanManager = ScanManager.createScanManager(getApplicationContext());
        unlockScanner();
    }

    private void lockScanner() {
        boolean success = _scanManager.lockScanner();
        if (success) {
            textView.setText("Scanner is LOCKED");
            isLocked = true;
            button.setText("Unlock scanner");
        }
    }

    private void unlockScanner() {
        boolean success = _scanManager.unlockScanner();
        if (success) {
            textView.setText("Scanner is UNLOCKED");
            isLocked = false;
            button.setText("Lock scanner");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        _scanManager.getConfig(dataOutput);
        _defaultKeyStrokeEnabled = dataOutput.keyStrokeOutput.enabled;
        dataOutput.keyStrokeOutput.enabled = false;
        _scanManager.setConfig(dataOutput);
        _scanManager.addDataListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        dataOutput.keyStrokeOutput.enabled = _defaultKeyStrokeEnabled;
        _scanManager.setConfig(dataOutput);
        _scanManager.removeDataListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _scanManager.releaseScanManager();
    }

    @Override
    public void onDataReceived(DecodeResult decodeResult) {
        View view = this.getCurrentFocus();
        if (view instanceof TextInputEditText) {
            ((TextInputEditText) view).setText(decodeResult.getData());
            View next = view.focusSearch(View.FOCUS_DOWN);
            if (next != null) {
                next.requestFocus();
            }
        }
    }
}
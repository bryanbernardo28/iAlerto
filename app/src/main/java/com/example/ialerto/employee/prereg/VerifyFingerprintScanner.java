package com.example.ialerto.employee.prereg;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.usb.UsbManager;
import android.os.Bundle;

import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.ReaderCollection;
import com.digitalpersona.uareu.UareUException;
import com.example.ialerto.Globals;

public class VerifyFingerprintScanner extends AppCompatActivity {

    private ReaderCollection readers;
    private Bundle savedInstanceState = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Context applContext = getApplicationContext();
            readers = Globals.getInstance().getReaders(applContext);
        } catch (UareUException e) {
            e.printStackTrace();
        }

        int nSize = readers.size();
        if (nSize > 1){
            Intent i = new Intent();
            i.putExtra("device_name",readers.get(0).GetDescription().name);
            i.putExtra("has_device", true);
            setResult(Activity.RESULT_OK, i);
            finish();
        }
        else{
            Intent i = new Intent();
            i.putExtra("device_name", (nSize == 0 ? "" : readers.get(0).GetDescription().name));
            i.putExtra("has_device", (nSize == 0 ? false : true));
            if (getIntent() != null && getIntent().getAction() != null && getIntent().getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED))
            {
                InitDevice(0);
            }
            setResult(Activity.RESULT_OK, i);
            finish();
        }
    }

    private void InitDevice(int position) {
        try {
            readers.get(position).Open(Reader.Priority.COOPERATIVE);
            readers.get(position).Close();
        } catch (UareUException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        onCreate(savedInstanceState);
        super.onConfigurationChanged(newConfig);
    }
}
package com.whpe.nfcdemo;

import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;

import com.whpe.nfcdemo.activity.CPUActivity;
import com.whpe.nfcdemo.activity.CardActivity;
import com.whpe.nfcdemo.activity.M1Activity;

public class MainActivity extends CardActivity implements View.OnClickListener{

    private Button rwM1Button;
    private Button rwCPUButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rwM1Button = (Button) findViewById(R.id.rwM1Button);
        rwCPUButton = (Button) findViewById(R.id.rwCPUButton);

        if(cardManager.getNfcAdapter() == null){
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("你的设备不支持NFC")
                    .setPositiveButton("确定", null)
                    .show();
            rwM1Button.setClickable(false);
            rwCPUButton.setClickable(false);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(cardManager.getNfcAdapter() != null && !cardManager.getNfcAdapter().isEnabled()){
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("你没有开启NFC")
                    .setPositiveButton("忽略", null)
                    .setNegativeButton("去开启", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
                            startActivity(intent);
                        }
                    }).show();
        }

        rwM1Button.setOnClickListener(this);
        rwCPUButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Button button = (Button) v;
        switch (button.getId()){
            case R.id.rwM1Button :
                startActivity(new Intent(MainActivity.this, M1Activity.class));
                break;
            case R.id.rwCPUButton :
                startActivity(new Intent(MainActivity.this, CPUActivity.class));
                break;
        }
    }
}

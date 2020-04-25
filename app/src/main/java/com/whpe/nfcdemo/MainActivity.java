package com.whpe.nfcdemo;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.king.zxing.CaptureActivity;
import com.king.zxing.Intents;
import com.whpe.nfcdemo.activity.CPUActivity;
import com.whpe.nfcdemo.activity.CardActivity;
import com.whpe.nfcdemo.activity.M1Activity;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends CardActivity implements View.OnClickListener{

    private Button rwM1Button;
    private Button rwCPUButton;
    private Button scanQRButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rwM1Button = findViewById(R.id.rwM1Button);
        rwCPUButton = findViewById(R.id.rwCPUButton);
        scanQRButton = findViewById(R.id.scanQRButton);

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
        scanQRButton.setOnClickListener(this);
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
            case R.id.scanQRButton :
                startScan(CaptureActivity.class, button.getText().toString());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && data!=null){
            switch (requestCode){
                case 0X01:
                    String result = data.getStringExtra(Intents.Scan.RESULT);
                    Toast.makeText(getContext(), result, Toast.LENGTH_SHORT).show();
                    break;
            }

        }
    }

    private Context getContext(){
        return this;
    }

    /**
     * 扫码
     * @param cls
     * @param title
     */
    private void startScan(Class<?> cls, String title) {
        String[] perms = {Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(this, perms)) {//有权限
            Intent intent = new Intent(this, cls);
            intent.putExtra("key_title", title);
            intent.putExtra("key_continuous_scan", false);
            startActivityForResult(intent, 0X01);
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, "App扫码需要用到拍摄权限",
                    0x01, perms);
        }
    }
}

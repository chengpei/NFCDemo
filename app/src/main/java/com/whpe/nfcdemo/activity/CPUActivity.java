package com.whpe.nfcdemo.activity;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.whpe.nfcdemo.R;
import com.whpe.nfcdemo.common.CommonUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

public class CPUActivity extends CardActivity implements View.OnClickListener{

    private EditText apduEditText;
    private Button clearButton;
    private Button sendButton;
    private TextView resultTextView;

    private Button select1001Button;
    private Button queryButton;

    private NfcAdapter nfcAdapter;

    private IsoDep cpu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cpu);

        apduEditText = (EditText) findViewById(R.id.apduEditText);
        clearButton = (Button) findViewById(R.id.clearButton);
        sendButton = (Button) findViewById(R.id.sendButton);
        resultTextView = (TextView) findViewById(R.id.resultTextView);

        select1001Button = (Button) findViewById(R.id.select1001Button);
        queryButton = (Button) findViewById(R.id.queryButton);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        cpu = IsoDep.get(cardManager.getTag());
        for(String tech : cardManager.getTechList()){
            Log.i("CPUActivity", tech);
        }
        if(cardManager.getTechList().contains("android.nfc.tech.IsoDep")){
            Toast.makeText(this, "感应到卡！", Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "不是CPU卡！", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        clearButton.setOnClickListener(this);
        sendButton.setOnClickListener(this);
        select1001Button.setOnClickListener(this);
        queryButton.setOnClickListener(this);
    }

    private String sendMessage(byte[] messageByte){
        if(cpu == null){
            Toast.makeText(this, "未感应到卡！", Toast.LENGTH_LONG).show();
            return null;
        }
        try {
            cpu.connect();
            byte[] transceive = cpu.transceive(messageByte);
            return CommonUtils.Bytes2HexString(transceive, true);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                cpu.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        Button button = (Button) v;
        String response = "";
        switch (button.getId()){
            case R.id.clearButton :
                apduEditText.setText("");
                break;
            case R.id.sendButton :
                String message = apduEditText.getText().toString();
                if(CommonUtils.isEmpty(message)){
                    Toast.makeText(this, "指令为空！", Toast.LENGTH_LONG).show();
                    return;
                }
                response = sendMessage(CommonUtils.HexString2Bytes(message));
                resultTextView.setText(response);
                break;
            case R.id.select1001Button :
                response = sendMessage(CommonUtils.HexString2Bytes("00a40000021001"));
                resultTextView.setText(response);
                break;
            case R.id.queryButton :
                response = sendMessage(CommonUtils.HexString2Bytes("805c000204"));
                response = response.replaceAll(" ", "");
                String resultCode = response.substring(response.length()-4, response.length());
                if(!"9000".equals(resultCode)){
                    Toast.makeText(this, "请先选择武汉通1001应用！", Toast.LENGTH_LONG).show();
                    return;
                }
                String balanceHex = response.substring(0, response.length()-4);
                int balanceInt = Integer.parseInt(balanceHex, 16);
                resultTextView.setText("武汉通余额：" + balanceInt/100.0);
                break;
        }
    }
}

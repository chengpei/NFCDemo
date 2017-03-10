package com.whpe.nfcdemo.activity;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.whpe.nfcdemo.R;
import com.whpe.nfcdemo.bean.Result;
import com.whpe.nfcdemo.common.CommonUtils;
import com.whpe.nfcdemo.common.HttpUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CPUActivity extends CardActivity implements View.OnClickListener{

    private EditText apduEditText;
    private Button clearButton;
    private Button sendButton;
    private TextView resultTextView;

    private Button select1001Button;
    private Button queryButton;
    private Button rechargeButton;

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
        rechargeButton = (Button) findViewById(R.id.rechargeButton);

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

            String ye = readYe();
            if(ye != null){
                resultTextView.setText("余额为：" + ye);
            }
        }else {
            Toast.makeText(this, "不是CPU卡！", Toast.LENGTH_LONG).show();
        }
    }

    private String readYe() {
        try{
            cpu.connect();
            String step1 = "00A40000023F00";
            String step2 = "00A40000021002";
            String step3 = "0020000003123456";
            String step4 = "805c000204";
            Log.i("executeRecharge", "发 == " + step1);
            byte[] transceive = cpu.transceive(CommonUtils.HexString2Bytes(step1));
            String returnMsg = CommonUtils.Bytes2HexString(transceive, false);
            Log.i("executeRecharge", "收 == " + returnMsg);
            if(!returnMsg.endsWith("9000")){
                throw new RuntimeException("选3F00失败，" + returnMsg);
            }

            Log.i("executeRecharge", "发 == " + step2);
            transceive = cpu.transceive(CommonUtils.HexString2Bytes(step2));
            returnMsg = CommonUtils.Bytes2HexString(transceive, false);
            Log.i("executeRecharge", "收 == " + returnMsg);
            if(!returnMsg.endsWith("9000")){
                throw new RuntimeException("选1002失败，" + returnMsg);
            }

            Log.i("executeRecharge", "发 == " + step3);
            transceive = cpu.transceive(CommonUtils.HexString2Bytes(step3));
            returnMsg = CommonUtils.Bytes2HexString(transceive, false);
            Log.i("executeRecharge", "收 == " + returnMsg);
            if(!returnMsg.endsWith("9000")){
                throw new RuntimeException("校验PIN失败，" + returnMsg);
            }

            Log.i("executeRecharge", "发 == " + step4);
            transceive = cpu.transceive(CommonUtils.HexString2Bytes(step4));
            returnMsg = CommonUtils.Bytes2HexString(transceive, false);
            Log.i("executeRecharge", "收 == " + returnMsg);
            if(!returnMsg.endsWith("9000")){
                throw new RuntimeException("读余额失败，" + returnMsg);
            }
            String yeHexStr = returnMsg.substring(0, returnMsg.length()-4);
            int balanceInt = Integer.parseInt(yeHexStr, 16);
            return balanceInt/100.00d + "";
        }catch (Exception e){
            Log.e("readYe", e.getMessage());
            return null;
        }finally {
            try {
                cpu.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        clearButton.setOnClickListener(this);
        sendButton.setOnClickListener(this);
        select1001Button.setOnClickListener(this);
        queryButton.setOnClickListener(this);
        rechargeButton.setOnClickListener(this);
    }

    private String sendMessage(byte[] messageByte){
        if(cpu == null){
            Toast.makeText(this, "未感应到卡！", Toast.LENGTH_LONG).show();
            return null;
        }
        try {
            cpu.connect();
            byte[] transceive = cpu.transceive(messageByte);
            return CommonUtils.Bytes2HexString(transceive, false);
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
            case R.id.rechargeButton :
                executeRecharge();
                break;
        }
    }

    private void executeRecharge() {
        Result result = new Result(true);
        if(cpu == null){
            Toast.makeText(this, "未感应到卡！", Toast.LENGTH_LONG).show();
            return;
        }
        RechargeHandler rechargeHandler = new RechargeHandler();
        RechargeThread rechargeThread = new RechargeThread(cpu, rechargeHandler);
        rechargeThread.start();
    }

    public class RechargeThread extends Thread{

        private IsoDep cpu;

        private RechargeHandler rechargeHandler;

        public RechargeThread(IsoDep cpu, RechargeHandler rechargeHandler){
            this.cpu = cpu;
            this.rechargeHandler = rechargeHandler;
        }

        @Override
        public void run() {
            Map<String, String> params = new HashMap<String, String>();
            String resultCode = "31";
            boolean needConfirm = false;
            try {
                cpu.connect();
                String step1 = "00A40000023F00";
                String step2 = "00A40000021002";
                String step3 = "0020000003123456";
                String step4 = "805000020B0100000001100000000001";
                String step5 = "805200000B20170310101409D47C8E2204";
                Log.i("executeRecharge", "发 == " + step1);
                byte[] transceive = cpu.transceive(CommonUtils.HexString2Bytes(step1));
                String returnMsg = CommonUtils.Bytes2HexString(transceive, false);
                Log.i("executeRecharge", "收 == " + returnMsg);
                if(!returnMsg.endsWith("9000")){
                    throw new RuntimeException("选3F00失败，" + returnMsg);
                }

                Log.i("executeRecharge", "发 == " + step2);
                transceive = cpu.transceive(CommonUtils.HexString2Bytes(step2));
                returnMsg = CommonUtils.Bytes2HexString(transceive, false);
                Log.i("executeRecharge", "收 == " + returnMsg);
                if(!returnMsg.endsWith("9000")){
                    throw new RuntimeException("选1002失败，" + returnMsg);
                }

                Log.i("executeRecharge", "发 == " + step3);
                transceive = cpu.transceive(CommonUtils.HexString2Bytes(step3));
                returnMsg = CommonUtils.Bytes2HexString(transceive, false);
                Log.i("executeRecharge", "收 == " + returnMsg);
                if(!returnMsg.endsWith("9000")){
                    throw new RuntimeException("校验PIN失败，" + returnMsg);
                }

                Log.i("executeRecharge", "发 == " + step4);
                transceive = cpu.transceive(CommonUtils.HexString2Bytes(step4));
                returnMsg = CommonUtils.Bytes2HexString(transceive, false);
                Log.i("executeRecharge", "收 == " + returnMsg);
                if(!returnMsg.endsWith("9000")){
                    throw new RuntimeException("圈存初始化失败，" + returnMsg);
                }

                // 发送HTTP请求获取8052指令
                params.clear();
                params.put("orderNo", "20170310092840000080");
                params.put("initializeForLoad", returnMsg);
                String response = HttpUtils.sendHttpRequest("http://58.19.246.6:7000/api/rechargeApply.do", params);
                Result result = JSON.parseObject(response, Result.class);
                if(!result.isSuccess()){
                    throw new RuntimeException(result.getMessage());
                }else {
                    needConfirm = true; // 充值申请成功后，需要确认
                }

                String creditForLoad = (String) result.getData().get("creditForLoad");
                Log.i("executeRecharge", "发 == " + creditForLoad);
                transceive = cpu.transceive(CommonUtils.HexString2Bytes(creditForLoad));
                returnMsg = CommonUtils.Bytes2HexString(transceive, false);
                Log.i("executeRecharge", "收 == " + returnMsg);
                if(!returnMsg.endsWith("9000")){
                    throw new RuntimeException("圈存失败，" + returnMsg);
                }else {
                    resultCode = "3"; // 充值成功
                }

            } catch (Exception e) {
                Log.e("executeRecharge", e.getMessage());
                Message msg = new Message();
                msg.what = 1;
                msg.obj = e.getMessage();
                rechargeHandler.sendMessage(msg);
            }finally {
                if(needConfirm){
                    // 发送HTTP请求 通知充值结果
                    params.clear();
                    params.put("orderNo", "20170310092840000080");
                    params.put("resultCode", resultCode);
                    String response = HttpUtils.sendHttpRequest("http://58.19.246.6:7000/api/rechargeConfirm.do", params);
                    Result result = JSON.parseObject(response, Result.class);
                    Message msg = new Message();
                    msg.what = 1;
                    msg.obj = result.getMessage();
                    rechargeHandler.sendMessage(msg);
                }
                try {
                    cpu.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class RechargeHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    Toast.makeText(CPUActivity.this, (String)msg.obj, Toast.LENGTH_LONG).show();
                    new AlertDialog.Builder(CPUActivity.this)
                            .setTitle("提示")
                            .setMessage((String)msg.obj)
                            .setPositiveButton("确定", null)
                            .show();
                    break;
            }
        }
    }
}

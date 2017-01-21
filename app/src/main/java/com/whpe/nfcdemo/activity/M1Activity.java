package com.whpe.nfcdemo.activity;

import android.content.Intent;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.whpe.nfcdemo.R;
import com.whpe.nfcdemo.common.CommonUtils;

import java.io.IOException;

public class M1Activity extends CardActivity implements View.OnClickListener{

    private EditText sectorsEditText;
    private EditText blockEditText;
    private EditText contentEditText;
    private Button readButton;
    private Button writeButton;

    private MifareClassic m1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_m1);

        sectorsEditText = (EditText) findViewById(R.id.sectorsEditText);
        blockEditText = (EditText) findViewById(R.id.blockEditText);
        contentEditText = (EditText) findViewById(R.id.contentEditText);
        readButton = (Button) findViewById(R.id.readButton);
        writeButton = (Button) findViewById(R.id.writeButton);

    }

    @Override
    protected void onResume() {
        super.onResume();
        readButton.setOnClickListener(this);
        writeButton.setOnClickListener(this);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        m1 = MifareClassic.get(cardManager.getTag());
        int sectorCount = m1.getSectorCount();
        int blockCount = m1.getBlockCount();
        Log.i("M1Activity", "扇区数：" + sectorCount);
        Log.i("M1Activity", "块 数：" + blockCount);
        if(cardManager.getTechList().contains("android.nfc.tech.MifareClassic")){
            Toast.makeText(this, "感应到卡！", Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "不是M1卡！", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 读取指定扇区 和 块的数据
     * @param sectors
     * @param block
     * @return
     */
    private String readM1Card(int sectors, int block) {
        if(cardManager.getTag() == null){
            Toast.makeText(this, "未感应到卡！", Toast.LENGTH_LONG).show();
            return null;
        }
        try {
            m1.connect();
            m1.authenticateSectorWithKeyA(sectors, MifareClassic.KEY_DEFAULT);
            int blockIndex = m1.sectorToBlock(sectors) + block;
            byte[] blockByte = m1.readBlock(blockIndex);
            return CommonUtils.Bytes2HexString(blockByte, true);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                m1.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     *
     * @param sectors
     * @param block
     * @param data
     */
    private void write(int sectors, int block, byte[] data){
        if(m1 == null){
            throw new RuntimeException("未感应到卡");
        }
        try {
            m1.connect();
            m1.authenticateSectorWithKeyA(sectors, MifareClassic.KEY_DEFAULT);
            int blockIndex = m1.sectorToBlock(sectors) + block;
            m1.writeBlock(blockIndex, data);
            Toast.makeText(this, "写入成功！", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                m1.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if(CommonUtils.isEmpty(sectorsEditText.getText().toString()) || CommonUtils.isEmpty(blockEditText.getText().toString())){
            Toast.makeText(this, "请填写扇区和块的索引号", Toast.LENGTH_LONG).show();
            return;
        }
        int sectors = Integer.parseInt(sectorsEditText.getText().toString());
        int block = Integer.parseInt(blockEditText.getText().toString());
        Button button = (Button) v;
        switch (button.getId()){
            case R.id.readButton :
                String result = readM1Card(sectors, block);
                contentEditText.setText(result);
                break;
            case R.id.writeButton :
                if(CommonUtils.isEmpty(contentEditText.getText().toString())){
                    Toast.makeText(this, "请填写写入的内容", Toast.LENGTH_LONG).show();
                    return;
                }
                String data = contentEditText.getText().toString();
                write(sectors, block, CommonUtils.HexString2Bytes(data));
                break;
        }
    }
}

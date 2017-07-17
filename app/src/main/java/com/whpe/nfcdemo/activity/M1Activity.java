package com.whpe.nfcdemo.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.whpe.nfcdemo.R;
import com.whpe.nfcdemo.common.CommonUtils;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class M1Activity extends CardActivity implements View.OnClickListener{

    private EditText sectorsEditText;
    private EditText blockEditText;
    private EditText contentEditText;
    private Button readButton;
    private Button writeButton;

    private EditText keyAEditText;
    private EditText keyBEditText;
    private Button updateParkCardButton;
    private TextView markTextView;

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

        keyAEditText = (EditText) findViewById(R.id.keyAEditText);
        keyAEditText = (EditText) findViewById(R.id.keyBEditText);
        updateParkCardButton = (Button) findViewById(R.id.updateParkCardButton);
        markTextView = (TextView) findViewById(R.id.markTextView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        readButton.setOnClickListener(this);
        writeButton.setOnClickListener(this);
        updateParkCardButton.setOnClickListener(this);
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
     * 获取KeyA
     * @return
     */
    private byte[] getKeyA(){
        String keyA = keyAEditText.getText().toString();
        return (keyA == null || keyA.length() != 12)?MifareClassic.KEY_DEFAULT:CommonUtils.HexString2Bytes(keyA);
    }

    /**
     * 获取KeyB
     * @return
     */
    private byte[] getKeyB(){
        String keyB = keyBEditText.getText().toString();
        return (keyB == null || keyB.length() != 12)?MifareClassic.KEY_DEFAULT:CommonUtils.HexString2Bytes(keyB);
    }

    /**
     * 读取指定扇区 和 块的数据
     * @param sectors
     * @param block
     * @return
     */
    private String readM1Card(int sectors, int block) {
        if(cardManager.getTag() == null){
            throw new RuntimeException("未感应到卡！");
        }
        try {
            m1.connect();
            if(!m1.authenticateSectorWithKeyA(sectors, getKeyA())){
                throw new RuntimeException("授权失败密钥A不正确");
            }
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
            if(!m1.authenticateSectorWithKeyA(sectors, getKeyA())){
                throw new RuntimeException("授权失败密钥A不正确");
            }
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


    /**
     * 更新停车卡日期
     */
    private void updateParkCard() {
        SimpleDateFormat sdf_yyMMddHHmmss = new SimpleDateFormat("yyMMddHHmmss");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 读出1扇区2块数据
        String s1k2 = readM1Card(1, 2);
        String realInDateStr = s1k2.substring(2, 14); // 实际进场时间 yyMMddHHmmss
        Date realInDate = null;
        try {
            realInDate = sdf_yyMMddHHmmss.parse(realInDateStr);
        } catch (ParseException e) {
            throw new RuntimeException("进场时间不正确【"+realInDateStr+"】，不是停车卡");
        }
        if(System.currentTimeMillis() - realInDate.getTime() < 1500000){
            throw new RuntimeException("进场不足25分钟，无需修改");
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -5); // 当前时间减5分钟作为进场时间
        String updateInDate = new SimpleDateFormat("yyMMddHHmmss").format(calendar.getTime());
        String newS1k2 = s1k2.substring(0, 2) + updateInDate + s1k2.substring(14);
        write(1, 2, CommonUtils.HexString2Bytes(newS1k2));

        StringBuilder message = new StringBuilder();
        message.append("实际进场时间：" + sdf.format(realInDate));
        message.append("\r\n");
        message.append("修改后进场时间：" + sdf.format(calendar.getTime()));
        message.append("\r\n");
        message.append("修改完成！");
        markTextView.setText(message);
    }

    @Override
    public void onClick(View v) {
        if(CommonUtils.isEmpty(sectorsEditText.getText().toString()) || CommonUtils.isEmpty(blockEditText.getText().toString())){
            Toast.makeText(this, "请填写扇区和块的索引号", Toast.LENGTH_LONG).show();
            return;
        }
        final int sectors = Integer.parseInt(sectorsEditText.getText().toString());
        final int block = Integer.parseInt(blockEditText.getText().toString());
        Button button = (Button) v;
        try{
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
                    if(contentEditText.getText().toString().length() != 32){
                        Toast.makeText(this, "写入数据长度有误", Toast.LENGTH_LONG).show();
                        return;
                    }
                    final String data = contentEditText.getText().toString();
                    new AlertDialog.Builder(M1Activity.this).setTitle("系统提示") //设置对话框标题
                            .setMessage("你确认将数据写入" + sectors + "扇区" + block + "块？") //设置显示的内容
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() { //添加确定按钮
                                @Override
                                public void onClick(DialogInterface dialog, int which) { //确定按钮的响应事件
                                    write(sectors, block, CommonUtils.HexString2Bytes(data));
                                }
                            }).setNegativeButton("取消", null).show();//在按键响应事件中显示此对话框
                    break;
                case R.id.updateParkCardButton :
                    updateParkCard();
                    break;
            }
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}

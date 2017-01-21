package com.whpe.nfcdemo.activity;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.whpe.nfcdemo.common.CardManager;

import java.util.Arrays;

public class CardActivity  extends AppCompatActivity {

    protected CardManager cardManager = CardManager.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cardManager.setNfcAdapter(NfcAdapter.getDefaultAdapter(this));
    }

    @Override
    protected void onPause() {
        super.onPause();
        cardManager.disableNfcForegroundDispatch(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cardManager.enableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(cardManager.getNfcAdapter().EXTRA_TAG);
        cardManager.setUid(tag.getId());
        cardManager.setTag(tag);
        String[] techList = tag.getTechList();
        for(String tech : techList){
            Log.i("CardActivity", tech);
        }
        cardManager.setTechList(Arrays.asList(techList));
    }
}

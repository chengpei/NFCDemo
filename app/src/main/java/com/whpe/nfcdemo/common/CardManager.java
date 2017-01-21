package com.whpe.nfcdemo.common;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;

import java.util.List;

public class CardManager {

    private static CardManager cardManager;

    private CardManager(){}

    public static CardManager getInstance(){
        if(cardManager == null){
            cardManager = new CardManager();
        }
        return cardManager;
    }

    private NfcAdapter nfcAdapter;

    private Tag tag;

    private byte[] uid;

    private List<String> techList;

    public void enableForegroundDispatch(Activity targetActivity){
        if (nfcAdapter != null && nfcAdapter.isEnabled()) {
            Intent intent = new Intent(targetActivity,
                    targetActivity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(targetActivity, 0, intent, 0);

            nfcAdapter.enableForegroundDispatch(
                    targetActivity, pendingIntent, null, new String[][]{
                            new String[]{NfcA.class.getName()}
                    });
        }
    }

    public void disableNfcForegroundDispatch(Activity targetActivity) {
        if (nfcAdapter != null && nfcAdapter.isEnabled()) {
            nfcAdapter.disableForegroundDispatch(targetActivity);
        }
    }

    public NfcAdapter getNfcAdapter() {
        return nfcAdapter;
    }

    public void setNfcAdapter(NfcAdapter nfcAdapter) {
        this.nfcAdapter = nfcAdapter;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public byte[] getUid() {
        return uid;
    }

    public void setUid(byte[] uid) {
        this.uid = uid;
    }

    public List<String> getTechList() {
        return techList;
    }

    public void setTechList(List<String> techList) {
        this.techList = techList;
    }
}

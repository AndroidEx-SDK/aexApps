/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.phone.utils;

import android.annotation.TargetApi;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;

/**
 * Callback class, invoked when an NFC card is scanned while the device is running in reader mode.
 *
 * Reader mode can be invoked by calling NfcAdapter
 */
@TargetApi(19)
public class NfcReader implements NfcAdapter.ReaderCallback {
    public static final String ACTION_NFC_CARDINFO="com.example.cts.textnfc.cardinfo";
    public static int READER_FLAGS = NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;
    private AccountCallback accountCallback;

    public NfcReader(AccountCallback accountCallback) {
        this.accountCallback = accountCallback;
    }

    /**
     * NFC读卡
     * @param tag Discovered tag
     */
    @Override
    public void onTagDiscovered(Tag tag) {
        IsoDep isoDep = IsoDep.get(tag);
        byte[] newBuffer=tag.getId();
        String cardId=RfidUtil.convertToCardNo(newBuffer,4);
        if(accountCallback!=null){
            accountCallback.onAccountReceived(cardId);
        }
    }

    @TargetApi(19)
    public interface AccountCallback {
        public void onAccountReceived(String account);
    }
}


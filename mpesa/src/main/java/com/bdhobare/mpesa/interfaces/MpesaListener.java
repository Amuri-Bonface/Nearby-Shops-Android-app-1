package com.bdhobare.mpesa.interfaces;

import com.bdhobare.mpesa.utils.Pair;



public interface MpesaListener {
    public void onMpesaError(Pair<Integer, String> result);
    public void onMpesaSuccess(String MerchantRequestID, String CheckoutRequestID, String CustomerMessage);
}

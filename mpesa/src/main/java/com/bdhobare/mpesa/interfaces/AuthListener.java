package com.bdhobare.mpesa.interfaces;

import com.bdhobare.mpesa.Mpesa;
import com.bdhobare.mpesa.utils.Pair;


public interface AuthListener {
    public void onAuthError(Pair<Integer, String> result);
    public void onAuthSuccess();
}

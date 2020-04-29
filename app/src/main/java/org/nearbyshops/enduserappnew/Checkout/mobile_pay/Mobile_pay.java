package org.nearbyshops.enduserappnew.Checkout.mobile_pay;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bdhobare.mpesa.Mode;
import com.bdhobare.mpesa.Mpesa;
import com.bdhobare.mpesa.interfaces.AuthListener;
import com.bdhobare.mpesa.interfaces.MpesaListener;
import com.bdhobare.mpesa.models.STKPush;
import com.bdhobare.mpesa.utils.Pair;

import org.json.JSONObject;
import org.nearbyshops.enduserappnew.R;

public class Mobile_pay extends AppCompatActivity implements AuthListener, MpesaListener {


    //TODO: Replace these values from
    // Values below are for sandbox implementation
   /* public static final String BUSINESS_SHORT_CODE = "174379";
    public static final String PASSKEY = "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919";
    public static final String CONSUMER_KEY = "pnR5QUANQSsDnaP8xp2NhLeJb68NYlHi";
    public static final String CONSUMER_SECRET = "so7e1iOpnQWrS0D0";
    public static final String CALLBACK_URL = "YOUR_CALLBACK_URL";*/

    public String BUSINESS_SHORT_CODE;
    public String PASSKEY;
    public String CONSUMER_KEY;
    public String CONSUMER_SECRET ;
    public String CALLBACK_URL;

    public static final String NOTIFICATION = "PushNotification";
    public static final String SHARED_PREFERENCES = "com.bdhobare.mpesa_android_sdk";

    Button pay,xit;
    ProgressDialog dialog;
    TextView phone;
    EditText amount;

    private BroadcastReceiver mRegistrationBroadcastReceiver;

    @Override
    protected void onStart() {
        super.onStart();
        //retrieve configuration
        retieve_mpesa_credentials();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_via_mpesa);
        // toolbar
       /* Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
*/
        //retrieve configuration
        retieve_mpesa_credentials();

       //Initialize views
        xit= findViewById(R.id.exit);
        pay = findViewById(R.id.pay);
        phone = findViewById(R.id.phone);
        amount = findViewById(R.id.amount);

        //get details
        Intent intent = getIntent();
        String retrievePhone = intent.getStringExtra("Phone");
        String retrieveAmount = intent.getStringExtra("Amount");

        //post to textview
        phone.setText(retrievePhone.toString());
        amount.setText(retrieveAmount.toString());

        dialog = new ProgressDialog(this);
        dialog.setMessage("Processing");
        dialog.setIndeterminate(true);

       //OnClick on pay button
        pay.setOnClickListener(view -> {

            String p = phone.getText().toString();
            int a = Integer.valueOf(amount.getText().toString());
            pay(p, a);

        });

        xit.setOnClickListener(v -> finish());
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(NOTIFICATION)) {
                    String title = intent.getStringExtra("title");
                    String message = intent.getStringExtra("message");
                    int code = intent.getIntExtra("code", 0);


                }
            }
        };
    }

    @Override
    public void onAuthError(Pair<Integer, String> result) {
        Log.e("Error", result.message);
    }

    @Override
    public void onAuthSuccess() {
        //TODO make payment
        pay.setEnabled(true);
    }

    private void pay(String phone, int amount){
        dialog.show();
        STKPush.Builder builder = new STKPush.Builder(BUSINESS_SHORT_CODE, PASSKEY, amount,BUSINESS_SHORT_CODE, phone);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        String token = sharedPreferences.getString("InstanceID", "");

        builder.setFirebaseRegID(token);
        builder.setCallBackURL(Constants.CALLBACKURL);
        STKPush push = builder.build();
        Mpesa.getInstance().pay(this, push);
    }


    @Override
    public void onMpesaError(Pair<Integer, String> result) {
        dialog.hide();
        Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMpesaSuccess(String MerchantRequestID, String CheckoutRequestID, String CustomerMessage) {
        dialog.hide();
        Toast.makeText(this, CustomerMessage, Toast.LENGTH_SHORT).show();

    }
    @Override
    protected void onResume() {
        super.onResume();
        //retrieve configuration
        retieve_mpesa_credentials();

        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(NOTIFICATION));

    }
    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


    //retrieve mpesa credentials
    private void retieve_mpesa_credentials()
    {
        StringRequest stringRequest1 =new StringRequest(Request.Method.POST, Constants.PAYCONFIGURL, response -> {
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(response);

                //TODO
                //Mpesa keys stored on a server
                BUSINESS_SHORT_CODE = jsonObject.getString("business_short_code");
                PASSKEY=jsonObject.getString("pass_key");
                CONSUMER_KEY = jsonObject.getString("consumer_key");
                CONSUMER_SECRET = jsonObject.getString("consumer_secret");
                CALLBACK_URL=jsonObject.getString("call_back_url");

                Mpesa.with(this, CONSUMER_KEY, CONSUMER_SECRET, Mode.PRODUCTION);

            }catch (Exception e)
            {
                Toast.makeText(getApplicationContext(),e.getMessage().toString(),Toast.LENGTH_LONG).show();
            }

        }, error -> {

        }){
           /* @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("phone_number", phoneNo);
                hashMap.put("user_login",user_id);
                return hashMap;
            }*/
        };

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(stringRequest1);
    }

}
package com.astapley.thememe.better;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ForgotPassword extends Activity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);

        EditText emailEditText = (EditText)findViewById(R.id.emailEditText);
        emailEditText.setTypeface(User.RalewayRegular);

        Button submitButton = (Button)findViewById(R.id.submitButton);
        submitButton.setTypeface(User.RalewaySemiBold);
    }

    @Override
    public void onBackPressed() {
        this.finish();
        overridePendingTransition(R.anim.no_anim, R.anim.slide_out_from_top);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.submitButton:
                Log.d(User.LOGTAG, "Forgot password here");
                break;
        }
    }
}

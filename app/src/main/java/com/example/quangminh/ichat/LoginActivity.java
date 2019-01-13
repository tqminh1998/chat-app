package com.example.quangminh.ichat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

public class LoginActivity extends AppCompatActivity {

    private Button loginButton;
    private TextView loginEmailTxt, loginPassTxt, loginWithPhoneTxt, signUpTxt, forgetPasswordTxt;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        InitializeVariable();

        signUpTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToRegisterActivity();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserLogin();
            }
        });
    }

    private void UserLogin() {
        String email = loginEmailTxt.getText().toString();
        String password = loginPassTxt.getText().toString();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "Email can not be empty...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Password can not be empty...", Toast.LENGTH_SHORT).show();
        }
        else {
            loadingBar.setTitle("Login");
            loadingBar.setMessage("Please wait...");
            loadingBar.show();

            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            loadingBar.dismiss();
                            if (task.isSuccessful())
                            {
                                SendUserToMainActivity();
                                Toast.makeText(LoginActivity.this, "Logged in successfully",
                                        Toast.LENGTH_SHORT).show();
                            }
                            else{
                                String message = task.getException().toString();
                                Toast.makeText(LoginActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                                
                            }
                        }
                    });
        }

    }


    private void InitializeVariable() {
        loginButton = (Button) findViewById(R.id.loginButton);
        loginEmailTxt = (TextView) findViewById(R.id.logiEemailEditText);
        loginPassTxt = (TextView) findViewById(R.id.loginPasswordEditText);
        loginWithPhoneTxt = (TextView) findViewById(R.id.loginWithPhone);
        signUpTxt = (TextView) findViewById(R.id.signUpTextView);
        forgetPasswordTxt = (TextView) findViewById(R.id.forgetPassword);

        loadingBar = new ProgressDialog(this);
    }


    private void SendUserToMainActivity() {
        //User can not back to login activity without click logout
        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void SendUserToRegisterActivity() {
        Intent registerIntent = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(registerIntent);
    }

}

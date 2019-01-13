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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private TextView registerEmailTxt, registerPasswordTxt, alreadyAccountTxt, signUpWithGoogleButton;
    private Button signUpButton;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog loadingBar;

    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();

        rootRef = FirebaseDatabase.getInstance().getReference();

        InitializeVariable();

        alreadyAccountTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToLoginActivity();
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });
    }

    private void CreateNewAccount() {
        String email = registerEmailTxt.getText().toString();
        String password = registerPasswordTxt.getText().toString();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "Email can not be empty...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Password can not be empty...", Toast.LENGTH_SHORT).show();
        }
        else {
            loadingBar.setTitle("Creating new account...");
            loadingBar.setMessage("Please wait...");
            //loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            loadingBar.dismiss();
                            if (task.isSuccessful()) {
                                String currentUserID = firebaseAuth.getCurrentUser().getUid();
                                rootRef.child("Users").child(currentUserID).setValue("");

                                SendUserToLoginActivity();
                                Toast.makeText(RegisterActivity.this, "Account created successfully",
                                        Toast.LENGTH_SHORT).show();

                            } else {
                                String message = task.getException().toString();
                                Toast.makeText(RegisterActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        }
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(loginIntent);
    }

    private void InitializeVariable() {
        registerEmailTxt = (TextView) findViewById(R.id.registerEmailEditText);
        registerPasswordTxt = (TextView) findViewById(R.id.registerPasswordEditText);
        alreadyAccountTxt = (TextView) findViewById(R.id.alreadyHaveAccount);
        signUpButton = (Button)findViewById(R.id.signUpButton);
        signUpWithGoogleButton = (TextView)findViewById(R.id.registerWithGoogle);

        loadingBar = new ProgressDialog(this);
    }
}

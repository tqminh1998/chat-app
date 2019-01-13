package com.example.quangminh.ichat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button updateButton;
    private EditText setStatus, setUsername;
    private CircleImageView setUserImage;

    private String currentUserID;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        InitializeVariable();

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsUpdate();
            }
        });

        RetrieveUserInfo();
    }

    private void RetrieveUserInfo() {
        rootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if ((dataSnapshot.exists())  && (dataSnapshot.hasChild("name"))
                                && (dataSnapshot.hasChild("image"))){
                            String retUsername = dataSnapshot.child("name").getValue().toString();
                            String retStatus = dataSnapshot.child("status").getValue().toString();
                            String retImage = dataSnapshot.child("image").getValue().toString();

                            setUsername.setText(retUsername);
                            setStatus.setText(retStatus);
                        }
                        else if ((dataSnapshot.exists())  && (dataSnapshot.hasChild("name"))){
                            String retUsername = dataSnapshot.child("name").getValue().toString();
                            String retStatus = dataSnapshot.child("status").getValue().toString();

                            setUsername.setText(retUsername);
                            setStatus.setText(retStatus);
                        }
                        else{
                            Toast.makeText(SettingsActivity.this, "Please update your profile", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void SettingsUpdate() {
        String userName = setUsername.getText().toString();
        String status = setStatus.getText().toString();

        if (TextUtils.isEmpty(userName)){
            Toast.makeText(this, "User name can not be empty", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(status)){
            Toast.makeText(this, "Status can not be empty", Toast.LENGTH_SHORT).show();
        }
        else{
            HashMap<String, String> profileMap  = new HashMap<>();
            profileMap.put("uid",currentUserID);
            profileMap.put("name", userName);
            profileMap.put("status", status);

            rootRef.child("Users").child(currentUserID).setValue(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                SendUserToMainActivity();
                                Toast.makeText(SettingsActivity.this, "Profile updated",
                                        Toast.LENGTH_SHORT).show();
                            }
                            else{
                                String message = task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "Error: "+message,
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void InitializeVariable() {
        updateButton = (Button) findViewById(R.id.update_settings_button);
        setUsername = (EditText) findViewById(R.id.set_user_name);
        setStatus = (EditText)findViewById(R.id.set_status);
        setUserImage = (CircleImageView) findViewById(R.id.set_profile_image);
    }

    private void SendUserToMainActivity() {
        //User can not back to login activity without click logout
        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}

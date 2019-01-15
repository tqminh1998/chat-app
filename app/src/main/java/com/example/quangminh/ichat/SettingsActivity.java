package com.example.quangminh.ichat;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.w3c.dom.Text;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button updateButton;
    private EditText setStatus, setUsername;
    private CircleImageView setUserImage;

    private Toolbar toolbar;

    private String currentUserID;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference rootRef;

    private StorageReference userImgRef;

    private static final int Gallerypick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        userImgRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        InitializeVariable();

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsUpdate();
            }
        });

        RetrieveUserInfo();

        setUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");

                startActivityForResult(galleryIntent, Gallerypick);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Gallerypick && resultCode == RESULT_OK && data != null){
            Uri imageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK){
                Uri resUri = result.getUri();

                StorageReference path = userImgRef.child(currentUserID+".jpg");
                path.putFile(resUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SettingsActivity.this, "Image uploaded", Toast.LENGTH_SHORT).show();
                            final String downloadUrl = "task.getResult().getMetadata().getReference().getDownloadUrl().toString()";

                            rootRef.child("Users").child(currentUserID).child("image")
                                    .setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (!task.isSuccessful()){
                                                String message = task.getException().toString();
                                                Toast.makeText(SettingsActivity.this, "Error: "+message,
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                        }
                        else{
                            String message = task.getException().toString();
                            Toast.makeText(SettingsActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
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

                            //Picasso.get().load(retImage).into(setUserImage);

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
            HashMap<String, Object> profileMap  = new HashMap<>();
            profileMap.put("uid",currentUserID);
            profileMap.put("name", userName);
            profileMap.put("status", status);

            rootRef.child("Users").child(currentUserID).updateChildren(profileMap)
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
        toolbar = (Toolbar) findViewById(R.id.setting_toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Settings");
    }

    private void SendUserToMainActivity() {
        //User can not back to login activity without click logout
        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}

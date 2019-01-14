package com.example.quangminh.ichat;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiveUID, currentState, senderID;
    private CircleImageView userProfileImage;
    private TextView userName, userStatus;
    private Button addFriendButton, declineButton;

    private DatabaseReference userRef, friendReqRef, contactRef;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        firebaseAuth = FirebaseAuth.getInstance();
        friendReqRef = FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        receiveUID  = getIntent().getExtras().get("visit_user_id").toString();
        senderID = firebaseAuth.getCurrentUser().getUid();

        userProfileImage = (CircleImageView) findViewById(R.id.visit_profile_img);
        userName = (TextView) findViewById(R.id.visit_user_name);
        userStatus = (TextView) findViewById(R.id.visit_user_status);
        addFriendButton = (Button) findViewById(R.id.add_friend_button);
        declineButton = (Button) findViewById(R.id.decline_request_button);

        currentState = "new";

        RetrieveUserInfo();

    }

    private void RetrieveUserInfo() {
        userRef.child(receiveUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("image"))){
                    String strUserImage = dataSnapshot.child("image").getValue().toString();
                    String strUserName = dataSnapshot.child("name").getValue().toString();
                    String strUserStatus = dataSnapshot.child("status").getValue().toString();

                    //Picasso.get().load(strUserImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userName.setText(strUserName);
                    userStatus.setText(strUserStatus);

                    ManageFriendRequests();

                }
                else {
                    String strUserName = dataSnapshot.child("name").getValue().toString();
                    String strUserStatus = dataSnapshot.child("status").getValue().toString();

                    userName.setText(strUserName);
                    userStatus.setText(strUserStatus);

                    ManageFriendRequests();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void ManageFriendRequests() {
        friendReqRef.child(senderID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(receiveUID)){
                            String request_type = dataSnapshot.child(receiveUID).child("request_type").getValue().toString();

                            if (request_type.equals("sent")){
                                currentState = "request_sent";
                                addFriendButton.setText("Cancel Request");
                            }
                            else if (request_type.equals("received")){
                                currentState = "request_received";
                                addFriendButton.setText("Accept this user");

                                declineButton.setVisibility(View.VISIBLE);
                                declineButton.setEnabled(true);

                                declineButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CancelFriendRequest();
                                    }
                                });
                            }
                        }
                        else{
                            contactRef.child(senderID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(receiveUID)){
                                                currentState = "friends";
                                                addFriendButton.setText("Unfriend");

                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


        if (!senderID.equals(receiveUID)){
            addFriendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addFriendButton.setEnabled(false);

                    if (currentState.equals("new")){
                        SendFriendRequest();
                    }
                    if (currentState.equals("request_sent")){
                        CancelFriendRequest();
                    }
                    if (currentState.equals("request_received")){
                        AcceptFriendRequest();
                    }
                    if (currentState.equals("friends")){
                        Unfriend();
                    }

                }
            });
        }
        else {
            addFriendButton.setVisibility(View.INVISIBLE);
        }

    }

    private void Unfriend() {
        contactRef.child(senderID).child(receiveUID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            contactRef.child(receiveUID).child(senderID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                addFriendButton.setEnabled(true);
                                                currentState = "new";
                                                addFriendButton.setText("Add friend +");


                                                declineButton.setVisibility(View.INVISIBLE);
                                                declineButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    private void AcceptFriendRequest() {

        contactRef.child(senderID).child(receiveUID)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            contactRef.child(receiveUID).child(senderID)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                friendReqRef.child(senderID).child(receiveUID)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    friendReqRef.child(receiveUID).child(senderID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    addFriendButton.setEnabled(true);
                                                                                    currentState = "friends";
                                                                                    addFriendButton.setText("Unfriend");

                                                                                    declineButton.setVisibility(View.INVISIBLE);
                                                                                    declineButton.setEnabled(false);
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    private void CancelFriendRequest() {
        friendReqRef.child(senderID).child(receiveUID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            friendReqRef.child(receiveUID).child(senderID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                addFriendButton.setEnabled(true);
                                                currentState = "new";
                                                addFriendButton.setText("Add friend +");


                                                declineButton.setVisibility(View.INVISIBLE);
                                                declineButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void SendFriendRequest() {
        friendReqRef.child(senderID).child(receiveUID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            friendReqRef.child(receiveUID).child(senderID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                addFriendButton.setEnabled(true);
                                                currentState = "request_sent";
                                                addFriendButton.setText("Cancle Request");
                                            }

                                        }
                                    });
                        }
                    }
                });
    }
}

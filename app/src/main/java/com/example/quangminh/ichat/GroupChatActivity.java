package com.example.quangminh.ichat;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageButton sendButton;
    private EditText userInputMessage;
    private ScrollView scrollView;
    private TextView displayTextMessage;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference userRef, groupNameRef, groupMessKeyRef;

    private String currentGroupName, currentUserID, currentUserName, currentDate, currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getExtras().get("groupName").toString();

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        groupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);

        InitializeVariable();

        GetUserInfo();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessageToDB();

                //clear after send
                userInputMessage.setText("");

                scrollView.fullScroll(scrollView.FOCUS_DOWN);
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        groupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()){
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void DisplayMessages(DataSnapshot dataSnapshot) {
        Iterator iterator = dataSnapshot.getChildren().iterator();

        while (iterator.hasNext()){
            String date = (String) ((DataSnapshot)iterator.next()).getValue();
            String message = (String) ((DataSnapshot)iterator.next()).getValue();
            String username = (String) ((DataSnapshot)iterator.next()).getValue();
            String time = (String) ((DataSnapshot)iterator.next()).getValue();

            displayTextMessage.append(username+": " +message+"\n"+date+"   "+time+"\n\n\n");

            scrollView.fullScroll(scrollView.FOCUS_DOWN);
        }

    }

    private void SendMessageToDB() {
        String message = userInputMessage.getText().toString();
        String messageKey = groupNameRef.push().getKey();

        if (!TextUtils.isEmpty(message)){
            Calendar calDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyy");
            currentDate = currentDateFormat.format(calDate.getTime());

            Calendar calTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calTime.getTime());

            HashMap<String, Object> groupMessageKey  = new HashMap<>();
            groupNameRef.updateChildren(groupMessageKey);

            groupMessKeyRef = groupNameRef.child(messageKey);

            HashMap<String, Object> messageInfoMap = new HashMap<>();

            messageInfoMap.put("name", currentUserName);
            messageInfoMap.put("message", message);
            messageInfoMap.put("date", currentDate);
            messageInfoMap.put("time", currentTime);

            groupMessKeyRef.updateChildren(messageInfoMap);

        }
    }

    private void GetUserInfo() {

        userRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void InitializeVariable() {

        toolbar = (Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(currentGroupName);


        sendButton = (ImageButton) findViewById(R.id.send_message_button);
        userInputMessage = (EditText) findViewById(R.id.input_group_message);
        scrollView = (ScrollView)findViewById(R.id.group_chat_scroll_view);
        displayTextMessage = (TextView) findViewById(R.id.group_chat_text_view);

    }
}

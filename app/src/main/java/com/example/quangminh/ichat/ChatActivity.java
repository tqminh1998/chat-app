package com.example.quangminh.ichat;

import android.content.Context;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String messReceiverID, messReceiverName, senderID;

    FirebaseAuth firebaseAuth;
    DatabaseReference  rootRef;

    private TextView tvUserName, tvUserLastSeen;
    private CircleImageView ciUserImage;

    private Toolbar chatToolbar;
    private ImageButton sendPMButton;
    private EditText etInputMess;

    private final List<Message> messageList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessageList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        rootRef = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        senderID = firebaseAuth.getCurrentUser().getUid();

        messReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        messReceiverName = getIntent().getExtras().get("visit_user_name").toString();


        InitializeVariable();

        tvUserName.setText(messReceiverName);

        sendPMButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendPrivateMessage();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        rootRef.child("Messages").child(senderID).child(messReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Message message = dataSnapshot.getValue(Message.class);
                        messageList.add(message);

                        messageAdapter.notifyDataSetChanged();

                        userMessageList.smoothScrollToPosition(userMessageList.getAdapter().getItemCount());
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

    private void SendPrivateMessage() {
        String strMessage = etInputMess.getText().toString();

        if (TextUtils.isEmpty(strMessage)){
            Toast.makeText(this, "Write your message first", Toast.LENGTH_SHORT).show();
        }
        else{
            String messageSenderRef = "Messages/"+senderID+"/"+messReceiverID;
            String messageReceiverRef = "Messages/"+messReceiverID+"/"+senderID;

            DatabaseReference userMessageKey = rootRef.child("Messages")
                    .child(senderID).child(messReceiverID).push();

            String messagePushID = userMessageKey.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", strMessage);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", senderID);


            Map messageBodyDetail = new HashMap();

            messageBodyDetail.put(messageSenderRef + "/"+messagePushID, messageTextBody);
            messageBodyDetail.put(messageReceiverRef + "/"+messagePushID,messageTextBody);

            rootRef.updateChildren(messageBodyDetail).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (!task.isSuccessful()){
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            etInputMess.setText("");
        }
    }

    private void InitializeVariable() {


        chatToolbar = (Toolbar)findViewById(R.id.chat_activity_toolbar);
        setSupportActionBar(chatToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar_layout, null);
        actionBar.setCustomView(actionBarView);


        tvUserName = (TextView) findViewById(R.id.chat_bar_user_name);
        tvUserLastSeen = (TextView) findViewById(R.id.chat_bar_last_seen);
        ciUserImage = (CircleImageView) findViewById(R.id.chat_bar_image);


        sendPMButton = (ImageButton) findViewById(R.id.send_private_message_button);
        etInputMess = (EditText) findViewById(R.id.input_private_message);

        messageAdapter = new MessageAdapter(messageList);
        userMessageList = (RecyclerView) findViewById(R.id.private_message_list);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessageList.setLayoutManager(linearLayoutManager);
        userMessageList.setAdapter(messageAdapter);
    }
}

package com.example.quangminh.ichat;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> userMessageList;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference userRef;




    public MessageAdapter(List<Message> userMessageList){
        this.userMessageList = userMessageList;
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView tvSendMess, tvReceiveMess;
        public CircleImageView ciRecImg;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            tvReceiveMess = (TextView) itemView.findViewById(R.id.receive_message);
            tvSendMess = (TextView) itemView.findViewById(R.id.sender_message);
            ciRecImg = (CircleImageView) itemView.findViewById(R.id.chat_mess_image);
        }
    }



    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_message_layout, viewGroup,false);


        firebaseAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder messageViewHolder, int i) {

        String messSenderID = firebaseAuth.getCurrentUser().getUid();

        Message message = userMessageList.get(i);

        String fromUserID = message.getFrom();
        String fromMessageType = message.getType();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (fromMessageType.equals("text")){
            messageViewHolder.tvReceiveMess.setVisibility(View.INVISIBLE);
            messageViewHolder.ciRecImg.setVisibility(View.INVISIBLE);

            if (fromUserID.equals(messSenderID)){
                messageViewHolder.tvSendMess.setVisibility(View.VISIBLE);
                messageViewHolder.tvSendMess.setTextColor(Color.WHITE);
                messageViewHolder.tvSendMess.setText(message.getMessage());
            }
            else{
                messageViewHolder.tvSendMess.setVisibility(View.INVISIBLE);

                messageViewHolder.tvReceiveMess.setVisibility(View.VISIBLE);
                messageViewHolder.ciRecImg.setVisibility(View.VISIBLE);

                messageViewHolder.tvReceiveMess.setTextColor(Color.BLACK);
                messageViewHolder.tvReceiveMess.setText(message.getMessage());



            }
        }

    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }



}

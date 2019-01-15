package com.example.quangminh.ichat;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {
    private View privateChatView;
    private RecyclerView chatList;

    private FirebaseAuth firebaseAuth;
    private String currentUserID;
    private DatabaseReference chatRef, userRef;

    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privateChatView = inflater.inflate(R.layout.fragment_chat, container, false);


        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        chatRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        chatList = (RecyclerView) privateChatView.findViewById(R.id.chat_list);
        chatList.setLayoutManager(new LinearLayoutManager(getContext()));

        return privateChatView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contact> options =
                new FirebaseRecyclerOptions.Builder<Contact>()
                .setQuery(chatRef, Contact.class)
                .build();

        FirebaseRecyclerAdapter<Contact, ChatsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contact, ChatsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contact model) {
                        final String userID = getRef(position).getKey();

                        userRef.child(userID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    if (dataSnapshot.hasChild("image")){
                                        final String strImage = dataSnapshot.child("image").getValue().toString();
                                    }
                                    final String strUserName = dataSnapshot.child("name").getValue().toString();
                                    final String strStatus = dataSnapshot.child("status").getValue().toString();

                                    holder.userName.setText(strUserName);
                                    holder.userStatus.setText("Last Seen: ");

                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("visit_user_id", userID);
                                            chatIntent.putExtra("visit_user_name",strUserName);
                                            startActivity(chatIntent);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view  = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_display_layout, viewGroup, false);

                        return new ChatsViewHolder(view);
                    }
                };

        chatList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder{
        TextView userName, userStatus;
        CircleImageView profileImage;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name_in_list);
            userStatus = itemView.findViewById(R.id.user_profile_status_in_list);
            profileImage = itemView.findViewById(R.id.user_profile_image_in_list);


        }
    }
}

package com.example.quangminh.ichat;


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
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private View requestsFragmentView;
    private RecyclerView myRequestList;

    private DatabaseReference friendRequetsRef, userRef, contactRef;

    private FirebaseAuth firebaseAuth;
    private String currentUserID;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        requestsFragmentView = inflater.inflate(R.layout.fragment_requests, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        friendRequetsRef = FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        myRequestList = (RecyclerView) requestsFragmentView.findViewById(R.id.requests_list);
        myRequestList.setLayoutManager(new LinearLayoutManager(getContext()));

        return requestsFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contact> options =
                new FirebaseRecyclerOptions.Builder<Contact>()
                .setQuery(friendRequetsRef.child(currentUserID), Contact.class)
                .build();

        FirebaseRecyclerAdapter<Contact,RequestsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contact, RequestsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestsViewHolder holder, int position, @NonNull Contact model) {
                        holder.itemView.findViewById(R.id.accept_button_user_layout).setVisibility(View.VISIBLE);
                        holder.itemView.findViewById(R.id.decline_button_user_layout).setVisibility(View.VISIBLE);

                        final String list_user_id = getRef(position).getKey();

                        DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();

                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    String type = dataSnapshot.getValue().toString();

                                    if (type.equals("received")){
                                        userRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.hasChild("image")){
                                                    final String requestUserName = dataSnapshot.child("name").getValue().toString();
                                                    final String requestUserStatus = dataSnapshot.child("status").getValue().toString();
                                                    final String requestUserImage = dataSnapshot.child("image").getValue().toString();

                                                    holder.userName.setText(requestUserName);
                                                    holder.userStatus.setText(requestUserStatus);
                                                    //Picasso.get().load(requestUserImage).into(holder.profileImage);
                                                }
                                                else{
                                                    final String requestUserName = dataSnapshot.child("name").getValue().toString();
                                                    final String requestUserStatus = dataSnapshot.child("status").getValue().toString();

                                                    holder.userName.setText(requestUserName);
                                                    holder.userStatus.setText(requestUserStatus);
                                                }

                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {

                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view  = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_display_layout,viewGroup,false);
                        RequestsViewHolder holder = new RequestsViewHolder(view);

                        return holder;
                    }
                };

        myRequestList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder{
        TextView userName, userStatus;
        CircleImageView profileImage;
        Button acceptButton, declineButton;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name_in_list);
            userStatus = itemView.findViewById(R.id.user_profile_status_in_list);
            profileImage = itemView.findViewById(R.id.user_profile_image_in_list);

            acceptButton = itemView.findViewById(R.id.accept_button_user_layout);
            declineButton = itemView.findViewById(R.id.decline_request_button);


        }
    }
}

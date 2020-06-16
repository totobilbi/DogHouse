package com.roeico7.dogadopt.Chat;


import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.roeico7.dogadopt.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.roeico7.dogadopt.Notifications.APIService;
import com.roeico7.dogadopt.Notifications.Client;
import com.roeico7.dogadopt.Notifications.Data;
import com.roeico7.dogadopt.Notifications.Response;
import com.roeico7.dogadopt.Notifications.Sender;
import com.roeico7.dogadopt.Notifications.Token;
import com.roeico7.dogadopt.Translate.Translate;
import com.roeico7.dogadopt.objects.User;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

import static com.roeico7.dogadopt.logic.GeneralStuff.resources;

/**
 * A simple {@link Fragment} subclass.
 */
public class Chat extends Fragment {


    private Toolbar toolbar;
    private Fragment mFragment;
    private RecyclerView rv_chat;
    private ImageView iv_profile;
    private TextView tv_name, tv_userStatus;
    private EditText et_message;
    private ImageButton btn_send;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference usersDbRef;
    private String hisUid, myUid, hisImage, myImage;

    private ValueEventListener seenListener;
    private DatabaseReference userRefForSeen;
    private List<ModelChat> chatList;
    private ChatAdapter chatAdapter;


    private APIService apiService;
    private boolean notify = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFragment = this;
        toolbar = view.findViewById(R.id.toolbar);
        rv_chat = view.findViewById(R.id.rv_chat);
        iv_profile = view.findViewById(R.id.iv_profile);
        tv_name = view.findViewById(R.id.tv_name);
        tv_userStatus = view.findViewById(R.id.tv_userStatus);
        et_message = view.findViewById(R.id.et_message);
        btn_send = view.findViewById(R.id.btn_send);

        myImage = "null";

        apiService = Client.getRetrofit(resources.getString(R.string.key_fcm_url)).create(APIService.class);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        rv_chat.setHasFixedSize(true);
        rv_chat.setLayoutManager(linearLayoutManager);


        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        usersDbRef = firebaseDatabase.getReference(resources.getString(R.string.key_users));

        myUid = firebaseAuth.getCurrentUser().getUid();
        hisUid = ChatArgs.fromBundle(getArguments()).getHisUid();


        loadChatInfo();

        iv_profile.setOnClickListener(v-> {
            ChatDirections.ActionChatFragToNavigationProfile action = ChatDirections.actionChatFragToNavigationProfile().setUid(hisUid);
            NavHostFragment.findNavController(mFragment).navigate(action);
        });


        //hebrew adjustment
        if(Translate.shared.checkDisplayLanguage())
            btn_send.setRotation(180);


        btn_send.setOnClickListener(v -> {
            notify = true;
            String message = et_message.getText().toString().trim();

            if (TextUtils.isEmpty(message))
                Toast.makeText(getContext(), resources.getString(R.string.empty_message), Toast.LENGTH_SHORT).show();
            else {
                sendMessage(message);
            }

            et_message.setText("");
        });

    }


    @Override
    public void onPause() {
        super.onPause();
        userRefForSeen.removeEventListener(seenListener);

        String timeStamp = String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(timeStamp);
        checkTypingStatus(resources.getString(R.string.key_noOne));
    }

    @Override
    public void onStart() {
        super.onStart();
        checkOnlineStatus(resources.getString(R.string.key_online));
    }


    private void seenMessage() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference(resources.getString(R.string.key_chats));

        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid)) {
                        HashMap<String, Object> hasSeenHashMap = new HashMap<>();
                        hasSeenHashMap.put(resources.getString(R.string.key_isSeen), true);
                        ds.getRef().updateChildren(hasSeenHashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void readMessages() {
        chatList = new ArrayList<>();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(resources.getString(R.string.key_chats));

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid) ||
                            chat.getReceiver().equals(hisUid) && chat.getSender().equals(myUid)) {
                        chatList.add(chat);
                    }
                    chatAdapter = new ChatAdapter(getContext(), chatList, hisImage, myImage);
                    chatAdapter.notifyDataSetChanged();
                    rv_chat.setAdapter(chatAdapter);
                }

                if(!chatList.isEmpty()) {
                    rv_chat.postDelayed(() -> {
                        if (rv_chat != null && chatList.size()-1 >=0) {

                            rv_chat.smoothScrollToPosition(chatList.size() - 1);
                        }
                    }, 1000);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void checkOnlineStatus(String status) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(resources.getString(R.string.key_users)).child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(resources.getString(R.string.key_onlineStatus), status);

        dbRef.updateChildren(hashMap);
    }


    private void checkTypingStatus(String typing) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(resources.getString(R.string.key_users)).child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(resources.getString(R.string.key_typingTo), typing);

        dbRef.updateChildren(hashMap);
    }


    private void sendMessage(String message) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        String timeStamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(resources.getString(R.string.key_sender), myUid);
        hashMap.put(resources.getString(R.string.key_receiver), hisUid);
        hashMap.put(resources.getString(R.string.key_message), message);
        hashMap.put(resources.getString(R.string.key_timeStamp), timeStamp);
        hashMap.put(resources.getString(R.string.key_isSeen), false);
        databaseReference.child(resources.getString(R.string.key_chats)).push().setValue(hashMap);


        String msg = message;
        DatabaseReference database = FirebaseDatabase.getInstance().getReference(resources.getString(R.string.key_users)).child(myUid);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                if (notify) {
                    sendNotification(hisUid, user.getUsername(), message);
                }

                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }






    private void sendNotification(String hisUid, String username, String message) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference(resources.getString(R.string.key_tokens));
        Query query = allTokens.orderByKey().equalTo(hisUid);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(myUid, username+": "+message, resources.getString(R.string.new_message), hisUid, R.drawable.dog_house_logo);

                    Sender sender = new Sender(data, token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<Response>() {
                                @Override
                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {

                                }

                                @Override
                                public void onFailure(Call<Response> call, Throwable t) {

                                }
                            });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }






    private void loadChatInfo() {
        usersDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.getKey().equals(myUid)) {
                        myImage = "" + ds.child(resources.getString(R.string.key_avatar)).getValue();
                    }

                    if (ds.getKey().equals(hisUid)) {
                        String name = "" + ds.child(resources.getString(R.string.key_username)).getValue();
                        tv_name.setText(name);

                        hisImage = "" + ds.child(resources.getString(R.string.key_avatar)).getValue();
                        Picasso.get().load(hisImage).placeholder(R.drawable.ic_default2).into(iv_profile);

                        String typingStatus = "" + ds.child(resources.getString(R.string.key_typingTo)).getValue();

                        if (typingStatus.equals(myUid))
                            tv_userStatus.setText(resources.getString(R.string.typing));
                        else {
                            String onlineStatus = "" + ds.child(resources.getString(R.string.key_onlineStatus)).getValue();
                            if (onlineStatus.equals(resources.getString(R.string.key_online))) {
                                tv_userStatus.setText(onlineStatus);
                            } else {
                                if(onlineStatus.equals(resources.getString(R.string.key_offline))) {
                                    tv_userStatus.setText(resources.getString(R.string.offline));
                                } else {
                                    long time = Long.valueOf(onlineStatus);
                                    SimpleDateFormat sdf = new SimpleDateFormat(resources.getString(R.string.key_datePattern));
                                    Date dateResult = new Date(time);
                                    String dateTime = sdf.format(dateResult);
                                    tv_userStatus.setText(resources.getString(R.string.last_seen) + dateTime);
                                }
                            }
                        }


                    }
                }
                onTypingChanged();

                readMessages();

                seenMessage();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void onTypingChanged() {
        et_message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() == 0)
                    checkTypingStatus(resources.getString(R.string.key_noOne));
                else {
                    checkTypingStatus(hisUid);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }


}

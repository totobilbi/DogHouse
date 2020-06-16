package com.roeico7.dogadopt.Chat;



import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.google.firebase.database.ValueEventListener;
import com.roeico7.dogadopt.Translate.Translate;
import com.roeico7.dogadopt.logic.RecyclerItemClickListener;

import java.util.ArrayList;
import java.util.List;

import static com.roeico7.dogadopt.logic.GeneralStuff.resources;


public class ChatList extends Fragment {

    private RecyclerView rv_chat;
    private List<String> chatList;
    private List<String> timeStamp;
    private ChatListAdapter chatListAdapter;
    private  Fragment mFragment;
    private String myUid;
    private ImageView iv_profile;


    public ChatList() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat_list, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rv_chat = view.findViewById(R.id.rv_chat);
        iv_profile = view.findViewById(R.id.iv_profile);

        //hebrew translation adjustment
        if(Translate.shared.checkDisplayLanguage()) {
            Toolbar toolbar = view.findViewById(R.id.toolbar);
            View childAt = toolbar.getChildAt(0);
            toolbar.removeView(childAt);
            toolbar.addView(childAt);
        }

        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mFragment = this;

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(false);
        rv_chat.setHasFixedSize(true);
        rv_chat.setLayoutManager(linearLayoutManager);

        readChats();

        rv_chat.addOnItemTouchListener(
                new RecyclerItemClickListener(getContext(), rv_chat ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        String hisUid = ((TextView) rv_chat.getLayoutManager().findViewByPosition(position).findViewById(R.id.tv_uid)).getText().toString();
                        ChatListDirections.ActionNavigationNotificationsToChatFrag3 action = ChatListDirections.actionNavigationNotificationsToChatFrag3(hisUid);
                        NavHostFragment.findNavController(mFragment).navigate(action);
                    }

                    @Override public void onLongItemClick(View view, int position) {

                    }
                })
        );




    }



    private void readChats() {
        chatList = new ArrayList<>();
        timeStamp = new ArrayList<>();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(resources.getString(R.string.key_chats));

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();
                timeStamp.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    addChats(chat);
                }

                sortChatsByTime();

                chatListAdapter = new ChatListAdapter(getContext(), chatList, timeStamp);
                chatListAdapter.notifyDataSetChanged();
                rv_chat.setAdapter(chatListAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }





    private void sortChatsByTime() {
        int n = chatList.size();
        for (int i = 0; i < n-1; i++)
            for (int j = 0; j < n-i-1; j++)
                if (Long.valueOf(timeStamp.get(j)) < Long.valueOf(timeStamp.get(j+1)))
                {

                    String timeTemp =  timeStamp.get(j);
                    timeStamp.set(j, timeStamp.get(j+1));
                    timeStamp.set(j+1, timeTemp);

                    String chatTemp =  chatList.get(j);
                    chatList.set(j, chatList.get(j+1));
                    chatList.set(j+1, chatTemp);
                }
    }





    private void addChats(ModelChat chat) {
        if(chat.getReceiver().equals(myUid)) {
            if(!chatList.contains(chat.getSender())) {
                chatList.add(chat.getSender());
                timeStamp.add(chat.getTimeStamp());
            } else {
                int index = chatList.indexOf(chat.getSender());
                if(Long.valueOf(timeStamp.get(index))<Long.valueOf(chat.getTimeStamp())) {
                    timeStamp.set(index, chat.getTimeStamp());
                }
            }
        }

        if(chat.getSender().equals(myUid)) {
            if(!chatList.contains(chat.getReceiver())) {
                chatList.add(chat.getReceiver());
                timeStamp.add(chat.getTimeStamp());
            } else {
                int index = chatList.indexOf(chat.getReceiver());
                if(Long.valueOf(timeStamp.get(index))<Long.valueOf(chat.getTimeStamp())) {
                    timeStamp.set(index, chat.getTimeStamp());
                }
            }
        }
    }






}

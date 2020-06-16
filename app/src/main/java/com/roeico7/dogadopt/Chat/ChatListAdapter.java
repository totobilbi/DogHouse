package com.roeico7.dogadopt.Chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.roeico7.dogadopt.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.roeico7.dogadopt.logic.GeneralStuff.resources;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.MyHolder> {

    private Context context;
    private List<String> chatList;
    private List<String> timeStamp;
    private DatabaseReference usersDbRef;


    public ChatListAdapter(Context context, List<String> chatList,  List<String> timeStamp) {
        this.context = context;
        this.chatList = chatList;
        this.timeStamp = timeStamp;

    }


    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_item, parent, false);
        usersDbRef = FirebaseDatabase.getInstance().getReference(resources.getString(R.string.key_users));
        return new ChatListAdapter.MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        Query userQuery = usersDbRef.child(chatList.get(position));
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds: dataSnapshot.getChildren()) {

                    if(ds.getKey().equals(resources.getString(R.string.key_username))) {
                        String name = "" + ds.getValue();
                        holder.tv_name.setText(name);
                    }

                    if(ds.getKey().equals(resources.getString(R.string.key_userID))) {
                        String hisUid = "" + ds.getValue();
                        holder.tv_uid.setText(hisUid);
                    }

                    if(ds.getKey().equals(resources.getString(R.string.key_avatar))) {
                        String avatar = "" + ds.getValue();
                        Picasso.get().load(avatar).into(holder.iv_avatar);
                    }

                }
                long time =  Long.valueOf(timeStamp.get(position));
                SimpleDateFormat sdf = new SimpleDateFormat(resources.getString(R.string.key_datePattern));
                Date dateResult = new Date(time);
                String dateTime = sdf.format(dateResult);
                holder.tv_time.setText(dateTime);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }


    class MyHolder extends RecyclerView.ViewHolder {

        private TextView tv_name, tv_uid, tv_time;
        private ImageView iv_avatar;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_uid = itemView.findViewById(R.id.tv_uid);
            tv_time = itemView.findViewById(R.id.tv_time);
            iv_avatar = itemView.findViewById(R.id.iv_avatar);

        }
    }
}

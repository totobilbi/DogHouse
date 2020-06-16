package com.roeico7.dogadopt.Chat;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.roeico7.dogadopt.R;
import com.roeico7.dogadopt.logic.GeneralStuff;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.roeico7.dogadopt.logic.GeneralStuff.resources;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyHolder> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    private Context context;
    private List<ModelChat> chatList;
    private String hisImage, myImage;
    private FirebaseUser fUser;


    public ChatAdapter(Context context, List<ModelChat> chatList, String hisImage, String myImage) {
        this.context = context;
        this.chatList = chatList;
        this.hisImage = hisImage;
        this.myImage = myImage;
    }



    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, parent, false);
            return new MyHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, parent, false);
            return new MyHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String message = chatList.get(position).getMessage();
        long timeStamp =  Long.valueOf(chatList.get(position).getTimeStamp());

        SimpleDateFormat sdf = new SimpleDateFormat(resources.getString(R.string.key_datePattern));
        Date dateResult = new Date(timeStamp);
        String dateTime = sdf.format(dateResult);

        holder.messageLayout.setOnClickListener(v-> {
            if(chatList.get(position).getSender().equals(myUid) && !chatList.get(position).getMessage().equals(resources.getString(R.string.message_deleted))) {


                ProgressDialog progressDialog = new ProgressDialog(context);
                GeneralStuff.shared.executeAlert(progressDialog,
                        resources.getString(R.string.delete_message),
                        resources.getString(R.string.delete_message_text),
                        false,
                        () -> deleteMessage(position));
            }

        });

        if(chatList.get(position).getReceiver().equals(myUid))
            if(!hisImage.equals("null"))
                Picasso.get().load(hisImage).into(holder.iv_profile);


        if(chatList.get(position).getSender().equals(myUid))
            if(!myImage.equals("null"))
                Picasso.get().load(myImage).into(holder.iv_profile);


        holder.tv_message.setText(message);
        holder.tv_time.setText(dateTime);

        if(position==chatList.size()-1) {
            if(chatList.get(position).isSeen()) {
                holder.tv_isSeen.setText(resources.getString(R.string.seen));
            } else {
                holder.tv_isSeen.setText(resources.getString(R.string.delivered));
            }
        } else {
            holder.tv_isSeen.setVisibility(View.GONE);
        }
    }


    private void deleteMessage(int position) {

        String msgTimeStamp = chatList.get(position).getTimeStamp();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(resources.getString(R.string.key_chats));

        Query query = dbRef.orderByChild(resources.getString(R.string.key_timeStamp)).equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()) {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put(resources.getString(R.string.key_message), resources.getString(R.string.message_deleted));
                    ds.getRef().updateChildren(hashMap);

                }
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

    @Override
    public int getItemViewType(int position) {
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if(chatList.get(position).getSender().equals(fUser.getUid()))
            return MSG_TYPE_LEFT;
        else
            return MSG_TYPE_RIGHT;
    }

    class MyHolder extends RecyclerView.ViewHolder {
        ImageView iv_profile;
        TextView tv_message, tv_time, tv_isSeen;
        LinearLayout messageLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            iv_profile = itemView.findViewById(R.id.iv_profile);
            tv_message = itemView.findViewById(R.id.tv_message);
            tv_time = itemView.findViewById(R.id.tv_time);
            tv_isSeen = itemView.findViewById(R.id.tv_isSeen);
            messageLayout = itemView.findViewById(R.id.messageLayout);

        }
    }
}

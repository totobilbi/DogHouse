package com.roeico7.dogadopt.Home;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.roeico7.dogadopt.R;
import com.roeico7.dogadopt.Translate.Translate;
import com.roeico7.dogadopt.logic.GeneralStuff;
import com.roeico7.dogadopt.objects.Dog;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.List;

import static com.roeico7.dogadopt.logic.GeneralStuff.resources;

public class DogAdapter extends RecyclerView.Adapter<DogAdapter.DogViewHolder> {


    public DogAdapter(List<Dog> dogs, LayoutInflater inflater) {
        this.dogs = dogs;
        this.inflater = inflater;
    }

    //properties:
    List<Dog> dogs;
    LayoutInflater inflater; // takes xml files -> view

    @NonNull
    @Override
    public DogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate the xml file -> View:
        View v = inflater.inflate(R.layout.dog_item, parent, false);
        return new DogViewHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull DogViewHolder holder, int position) {
        Dog d = dogs.get(position);

        String gender = Translate.shared.translateGender(d.getGender());
        String type = Translate.shared.translateRaceType(d.getType());

        holder.tv_dog_name.setText(d.getName());
        holder.tv_dog_gender.setText( gender);
        holder.tv_dog_type.setText(type.replaceAll("[\\s']", "\n"));
        holder.tv_dog_age.setText (d.getAge());
        holder.tv_distance.setText(d.getDogOwner().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) ? resources.getString(R.string.my_dog) : GeneralStuff.shared.getCurrentUserDistanceFromDog(d)+" " + resources.getString(R.string.km));
        holder.tv_owner.setText("Owner: "+ d.getDogOwner());
        Picasso.get().load(d.getPic()).into(holder.iv_icon);
//
//        holder.itemView.setOnClickListener(v-> {
//                Intent intent = new Intent (Intent.ACTION_VIEW, Uri.parse(d.getArtistUrl()));
//                holder.itemView.getContext().startActivity(intent);
//        });

        // TODO: how to show image from the internet (URL)
    }

    @Override
    public int getItemCount() {
        return dogs.size();
    }



    // the view holder class -> hold refrences to views in the
    // we need a class for Song View
    // here the properties are the TextView and ImageView from the layout.xml item file
    public static class DogViewHolder extends RecyclerView.ViewHolder {
        TextView tv_dog_name, tv_dog_type, tv_dog_age, tv_dog_gender, tv_owner, tv_distance;
        ImageView iv_icon;


        public DogViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_dog_age = itemView.findViewById(R.id.tv_dog_age);
            tv_dog_name = itemView.findViewById(R.id.tv_dog_name);
            tv_dog_gender = itemView.findViewById(R.id.tv_dog_gender);
            tv_dog_type = itemView.findViewById(R.id.tv_dog_type);
            iv_icon = itemView.findViewById(R.id.iv_icon);
            tv_owner = itemView.findViewById(R.id.tv_owner);
            tv_distance = itemView.findViewById(R.id.tv_distance);


        }
    }







}

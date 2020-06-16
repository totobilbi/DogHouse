package com.roeico7.dogadopt.Home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.ToggleButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.roeico7.dogadopt.R;
import com.roeico7.dogadopt.Translate.Translate;
import com.roeico7.dogadopt.objects.Dog;
import com.squareup.picasso.Picasso;

public class DisplayBasicInfo extends Fragment {

    private ImageView dogImage, dog_type_icon;
    private TextView dog_name;
    private TextView dog_type, gender_type, dog_age;
    private Button add_dog;
    private NumberPicker np;
    private ToggleButton gender_button;
    private Dog dogInfo;
    private ImageView iv_title;


    public DisplayBasicInfo(Dog dog) {
        this.dogInfo = dog;
    }






    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.display_basic_info, container, false);
        return  view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        dogImage = view.findViewById(R.id.iv_icon);
        dog_name = view.findViewById(R.id.tv_dog_name2);
        gender_type = view.findViewById(R.id.tv_dog_gender2);
        dog_age = view.findViewById(R.id.tv_dog_age2);
        dog_type = view.findViewById(R.id.tv_dog_type2);

        applyCurrentInfo();
    }




    private void applyCurrentInfo() {
        dog_name.setText(dogInfo.getName());
        String race = Translate.shared.translateRaceType(dogInfo.getType());
        dog_type.setText(race.replaceAll("[\\s']", "\n"));
        dogImage.setImageResource(R.drawable.dog_image_placeholder);
        Picasso.get().load(dogInfo.getPic()).into(dogImage);
        dogImage.setBackgroundResource(R.drawable.add_dog_pic_border);
        gender_type.setText(Translate.shared.translateGender(dogInfo.getGender()));
        dog_age.setText(dogInfo.getAge());
    }
}

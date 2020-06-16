package com.roeico7.dogadopt.Home;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.roeico7.dogadopt.R;
import com.roeico7.dogadopt.Translate.Translate;
import com.roeico7.dogadopt.objects.Dog;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.languageid.FirebaseLanguageIdentification;

import java.util.ArrayList;


import static com.roeico7.dogadopt.logic.GeneralStuff.resources;


public class DisplayAdditionalInfo extends Fragment {

    private TextView tv_description;
    private ArrayList<String> options;
    private Dog dogInfo;
    private LinearLayout options_layout;


    public DisplayAdditionalInfo(Dog dog) {
        this.dogInfo = dog;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.display_additional_info, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        options_layout = view.findViewById(R.id.options_layout);
        tv_description = view.findViewById(R.id.tv_description);

        //get current marked options from dog
        options = dogInfo.getOptions();

        if(options!=null) {
            initOptions();
        }

        FirebaseLanguageIdentification languageIdentifier =
                FirebaseNaturalLanguage.getInstance().getLanguageIdentification();

        languageIdentifier.identifyLanguage(dogInfo.getDescription())
                .addOnSuccessListener(languageCode -> {
                    if (languageCode != "und") {
                        if (Translate.shared.checkDisplayLanguage()) {
                            if(languageCode.equals("iw"))
                                tv_description.setText(dogInfo.getDescription());
                            else
                                tv_description.setText(resources.getString(R.string.ui_dog_description_untranslateable));
                        } else {
                            if(languageCode.equals("en"))
                                tv_description.setText(dogInfo.getDescription());
                            else
                                tv_description.setText(resources.getString(R.string.ui_dog_description_untranslateable));
                        }
                    }
                }).
                addOnFailureListener(e -> tv_description.setText(dogInfo.getDescription()));





    }



    private void initOptions() {
        options_layout.setWeightSum(options.size());
        for(String option: options) {
            TextView textView = new TextView(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.weight = 1;
            textView.setTextAppearance(getContext(), R.style.DogDisplayText);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

            switch (option) {
                case "Option1":
                    textView.setText("• " + resources.getString(R.string.ui_service_dog));
                    break;

                case "Option2":
                    textView.setText("• " +  resources.getString(R.string.ui_vaccinated));
                    break;

                case "Option3":
                    textView.setText("• " + resources.getString(R.string.ui_mouth_guard));
                    break;

                case "Option4":
                    textView.setText("• " + resources.getString(R.string.ui_neutered_sterile));
                    break;

                case "Option5":
                    textView.setText("• " + resources.getString(R.string.ui_friendly));
                    break;

                case "Option6":
                    textView.setText("• " + resources.getString(R.string.ui_healthy));
                    break;

                case "Option7":
                    textView.setText("• " + resources.getString(R.string.ui_aggressive));
                    break;

                case "Option8":
                    textView.setText("• " + resources.getString(R.string.ui_trained));
                    break;
            }
            textView.setLayoutParams(params);
            options_layout.addView(textView);
        }
    }






}

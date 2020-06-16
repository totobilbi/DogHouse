package com.roeico7.dogadopt.Translate;

import com.roeico7.dogadopt.R;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.roeico7.dogadopt.logic.GeneralStuff.resources;


public class Translate {

    private Translate() {
    } // hide the constructor

    public static Translate shared = new Translate();

    public Boolean checkDisplayLanguage() {
        String displayLanguage = Locale.getDefault().getDisplayLanguage();
        if (displayLanguage.equals("עברית")) {
            return true;
        } else if (displayLanguage.equals("English")) {
            return false;
        }
        return null;
    }


    public String translateGender(String gender) {
        if (Translate.shared.checkDisplayLanguage()) {
            if (gender.equals("Female") || gender.equals("Male"))
                gender = gender.equals("Female") ? "נקבה" : "זכר";

        } else {
            if (gender.equals("נקבה") || gender.equals("זכר"))
                gender = gender.equals("נקבה") ? "Female" : "Male";
        }

        return gender;
    }


    public String translateRaceType(String type) {
        List<String> enLanguage = Arrays.asList(resources.getStringArray(R.array.dogs_type_en));
        List<String> heLanguage = Arrays.asList(resources.getStringArray(R.array.dogs_type_he));

        if(Translate.shared.checkDisplayLanguage()) {
            if (!heLanguage.contains(type)) {
                int indexOf = enLanguage.indexOf(type);
                type = heLanguage.get(indexOf);
            }
        } else {
            if (!enLanguage.contains(type)) {
                int indexOf = heLanguage.indexOf(type);
                type = enLanguage.get(indexOf);
            }
        }

        return type;
    }


}

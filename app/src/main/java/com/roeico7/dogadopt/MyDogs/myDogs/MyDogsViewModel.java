package com.roeico7.dogadopt.MyDogs.myDogs;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.roeico7.dogadopt.logic.FirebaseDAO;
import com.roeico7.dogadopt.objects.Dog;

import java.util.List;

public class MyDogsViewModel extends ViewModel {

    private MutableLiveData<List<Dog>> mDog;

    public MyDogsViewModel() {
        mDog = new MutableLiveData<>();
        FirebaseDAO.shared.readUserDogs(mDog);
    }



    public LiveData<List<Dog>> getDogs() {
        return mDog;
    }
}

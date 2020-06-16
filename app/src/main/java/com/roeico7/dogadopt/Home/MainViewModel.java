package com.roeico7.dogadopt.Home;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.roeico7.dogadopt.logic.FirebaseDAO;
import com.roeico7.dogadopt.objects.User;

import java.util.List;

public class MainViewModel extends ViewModel {



    private MutableLiveData<List<User>> mUsers;

    public MainViewModel() {
        mUsers = new MutableLiveData<>();
        FirebaseDAO.shared.readAllUsers(mUsers);
    }


    public LiveData<List<User>> getUsers() {
        return mUsers;
    }
}
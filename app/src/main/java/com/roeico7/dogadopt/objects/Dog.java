package com.roeico7.dogadopt.objects;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Dog implements Parcelable {
    private String dogOwner;
    private String dogID;
    private String name;
    private String pic;
    private String type;
    private String gender;
    private String age;
    private String description;
    private ArrayList<String> options;

    public Dog() {

    }

    public Dog(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public Dog(String dogID, String name, String type, String gender, String age) {
        this.dogID = dogID;
        this.name = name;
        this.type = type;
        this.gender = gender;
        this.age = age;
    }

    public String getDogOwner() {
        return dogOwner;
    }

    public void setDogOwner(String dogOwner) {
        this.dogOwner = dogOwner;
    }

    public Dog(String dogOwner, String dogID, String name, String pic, String type, String gender, String age) {
        this.dogOwner = dogOwner;
        this.dogID = dogID;
        this.name = name;
        this.pic = pic;
        this.type = type;
        this.gender = gender;
        this.age = age;
    }

    public Dog(String dogOwner, String dogID, String name, String pic, String type, String gender, String age, String description, ArrayList<String> options) {
        this.dogOwner = dogOwner;
        this.dogID = dogID;
        this.name = name;
        this.pic = pic;
        this.type = type;
        this.gender = gender;
        this.age = age;
        this.description = description;
        this.options = options;
    }

    protected Dog(Parcel in) {
        dogID = in.readString();
        name = in.readString();
        pic = in.readString();
        type = in.readString();
        gender = in.readString();
        age = in.readString();
    }

    public static final Creator<Dog> CREATOR = new Creator<Dog>() {
        @Override
        public Dog createFromParcel(Parcel in) {
            return new Dog(in);
        }

        @Override
        public Dog[] newArray(int size) {
            return new Dog[size];
        }
    };

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDogID() {
        return dogID;
    }

    public void setDogID(String dogID) {
        this.dogID = dogID;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public ArrayList<String> getOptions() {
        return options;
    }

    public void setOptions(ArrayList<String> options) {
        this.options = options;
    }


    @Override
    public String toString() {
        return "Dog{" +
                "dogID=" + dogID +
                ", name='" + name + '\'' +
                ", pic='" + pic + '\'' +
                ", type='" + type + '\'' +
                ", gender='" + gender + '\'' +
                ", age=" + age +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(dogID);
        dest.writeString(name);
        dest.writeString(pic);
        dest.writeString(type);
        dest.writeString(gender);
        dest.writeString(age);
    }
}

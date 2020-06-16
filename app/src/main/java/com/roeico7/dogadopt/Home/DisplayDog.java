package com.roeico7.dogadopt.Home;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.roeico7.dogadopt.R;
import com.roeico7.dogadopt.logic.GeneralStuff;
import com.roeico7.dogadopt.objects.Dog;
import com.roeico7.dogadopt.objects.User;

import java.util.HashMap;

import static com.roeico7.dogadopt.logic.GeneralStuff.resources;

/**
 * A simple {@link Fragment} subclass.
 */
public class DisplayDog extends Fragment {



    private FloatingActionButton btn_message, btn_phone, btn_location;
    private Dog dog;
    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final int NUM_PAGES = 2;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter pagerAdapter;


    public DisplayDog() {
        // Required empty public constructor
    }


    public DisplayDog(Dog dog) {
        this.dog = dog;
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_display_dog, container, false);


        return v;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        btn_message = view.findViewById(R.id.btn_message);
        btn_phone = view.findViewById(R.id.btn_phone);
        btn_location = view.findViewById(R.id.btn_location);
        dog = DisplayDogArgs.fromBundle(getArguments()).getDog();

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        // Instantiate a ViewPager and a PagerAdapter.
        mPager = view.findViewById(R.id.pager);
        tabLayout.setupWithViewPager(mPager);
        pagerAdapter = new ScreenSlidePagerAdapter(getChildFragmentManager());
        mPager.setPageTransformer(true, new DepthPageTransformer());
        mPager.setAdapter(pagerAdapter);


        // message owner of dog
        btn_message.setOnClickListener(b-> {
            String ownerID = dog.getDogOwner();
            DisplayDogDirections.ActionDisplayDogToChatFrag action = DisplayDogDirections.actionDisplayDogToChatFrag(ownerID);
            NavOptions options = new
                    NavOptions.Builder().setPopUpTo(R.id.displayDog, true).build();
            NavHostFragment.findNavController(this).navigate(action,options);
        });


        //call owner of dog
        btn_phone.setOnClickListener(b-> {
            User user = GeneralStuff.shared.getDogOwner(dog);
            String phone = user.getPhone();
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
            startActivity(intent);
        });


        //navigate to owner location
        btn_location.setOnClickListener(b-> {
            User user = GeneralStuff.shared.getDogOwner(dog);
            HashMap<String, String> profileInfo = user.getProfileInfo();
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("geo:0,0" + "?q=" + profileInfo.get("streetAddress") + "+" + profileInfo.get("city")));
            startActivity(intent);
        });
    }




    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position==0)
                return new DisplayBasicInfo(dog);
            else
                return new DisplayAdditionalInfo(dog);
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            if(position==0)
                return resources.getString(R.string.ui_basic_info);
            else
                return resources.getString(R.string.ui_additional_info);
        }
    }


    private  class DepthPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0f);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1f);
                view.setTranslationX(0f);
                view.setScaleX(1f);
                view.setScaleY(1f);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE
                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0f);
            }
        }
    }
}

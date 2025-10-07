package com.example.habittrackerrpg.ui.friends;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class FriendsPagerAdapter extends FragmentStateAdapter {

    public FriendsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new MyFriendsFragment();
            case 1:
                return new SearchUsersFragment();
            default:
                return new MyFriendsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
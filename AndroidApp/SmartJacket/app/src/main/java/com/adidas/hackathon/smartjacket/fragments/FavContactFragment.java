package com.adidas.hackathon.smartjacket.fragments;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.adidas.hackathon.smartjacket.R;
import com.adidas.hackathon.smartjacket.settings.AppSharedPreferences;
import com.adidas.hackathon.smartjacket.ui.controls.AdidasButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FavContactFragment extends Fragment {

    public static final String TAG = FavContactFragment.class.getName();

    @BindView(R.id.contact_screen_fav_name)
    TextView favoriteContactName;
    @BindView(R.id.change_fav_contact_button)
    AdidasButton changeFavContactButton;

    public static FavContactFragment newInstance() {
        Bundle args = new Bundle();

        FavContactFragment fragment = new FavContactFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_fav_contact, container, false);
        ButterKnife.bind(this, rootView);

        favoriteContactName.setText(AppSharedPreferences.getPrefContactName(getContext()));

        return rootView;
    }

    @OnClick({R.id.change_fav_contact_button})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.change_fav_contact_button:
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_main, ContactsFragment.newInstance(), ContactsFragment.TAG)
                        .addToBackStack(null)
                        .commit();
                break;
        }
    }

}

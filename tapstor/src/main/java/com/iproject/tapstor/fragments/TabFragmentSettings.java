package com.iproject.tapstor.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.iproject.tapstor.R;
import com.iproject.tapstor.SendEmailActivity;
import com.iproject.tapstor.UserProfileActivity;

public class TabFragmentSettings extends Fragment {

    RelativeLayout rl1, rl5, rl6;
    private View rootView;

    public TabFragmentSettings() {

    }


    public static TabFragmentSettings newInstance() {
        return new TabFragmentSettings();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_tab_3, container, false);
        setTheRowClickListeners();

        return rootView;

    }

    private void setTheRowClickListeners() {
        rl1 = (RelativeLayout) rootView.findViewById(R.id.my_profile);
        rl5 = (RelativeLayout) rootView.findViewById(R.id.suggest_enterprise);
        rl6 = (RelativeLayout) rootView.findViewById(R.id.add_enterprise);

        // my_profile
        rl1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), UserProfileActivity.class));
            }
        });

        // suggest_enterprise
        rl5.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent i = new Intent(getActivity(), SendEmailActivity.class);
                i.putExtra("SUGGESTION", true);
                startActivity(i);

            }
        });

        // add_enterprise
        rl6.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), SendEmailActivity.class);
                i.putExtra("SUGGESTION", false);
                startActivity(i);

            }
        });

    }
}
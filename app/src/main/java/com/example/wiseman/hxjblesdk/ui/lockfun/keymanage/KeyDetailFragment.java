package com.example.wiseman.hxjblesdk.ui.lockfun.keymanage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.hxjblesdk.R;
import com.example.hxjblinklibrary.blinkble.entity.reslut.LockKeyResult;

public class KeyDetailFragment extends Fragment {

    private KeyDetailViewModel mViewModel;
    private LockKeyResult keyDetails;
    private TextView keyDetailTv;

    public static KeyDetailFragment newInstance() {
        return new KeyDetailFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.key_detail_fragment, container, false);
        keyDetailTv = inflate.findViewById(R.id.key_detail_text);
        return inflate;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(KeyDetailViewModel.class);
        keyDetails = KeyDetailFragmentArgs.fromBundle(getArguments()).getKeyDetails();
        keyDetailTv.setText(keyDetails.toString());
        //TODO: Use the ViewModel
    }
}
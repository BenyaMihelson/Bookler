package com.mitlosh.bookplayer.ui.fragment;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mitlosh.bookplayer.R;
import com.mitlosh.bookplayer.databinding.FragmentSupportBinding;
import com.mitlosh.bookplayer.utils.Utils;

public class SupportFragment extends BaseFragment {

    public static final String TAG = "SupportFragment";

    private FragmentSupportBinding binding;
    private String email;
    private String message;
    private SupportFI callback;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_support, container, false);

        binding.sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSend();
            }
        });

        return binding.getRoot();
    }

    private void attemptSend() {
        email = binding.emailInput.getEditText().getText().toString();
        message = binding.messageInput.getEditText().getText().toString();

        boolean isValid = true;

        if(email.isEmpty()){
            binding.emailInput.setError(getString(R.string.error_empty_field));
            isValid = false;
        }else if(!Utils.isValidEmail(email)){
            binding.emailInput.setError(getString(R.string.error_email_invalid));
            isValid = false;
        }else{
            binding.emailInput.setErrorEnabled(false);
        }

        if(message.isEmpty()){
            binding.messageInput.setError(getString(R.string.error_empty_field));
            isValid = false;
        }else{
            binding.messageInput.setErrorEnabled(false);
        }

        if(isValid) sendMessage(email, message);
    }

    private void sendMessage(String email, String message) {
        Utils.hideKeyboard(getActivity());
        binding.textForm.setVisibility(View.GONE);
        binding.progress.setVisibility(View.VISIBLE);
        if(callback != null) callback.sendFeedback(email, message);
    }

    public void onSendSuccess() {
        binding.result.setText(R.string.message_send);
        binding.result.setVisibility(View.VISIBLE);
        binding.progress.setVisibility(View.GONE);
    }

    public void onSendFail(String error) {
        binding.result.setText(error);
        binding.result.setVisibility(View.VISIBLE);
        binding.progress.setVisibility(View.GONE);
    }

    @Override
    protected int getTitleRes() {
        return R.string.support;
    }

    @Override
    protected boolean showFABOnResume() {
        return false;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callback = (SupportFI) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }

    public interface SupportFI{
        void sendFeedback(String email, String message);
    }
}

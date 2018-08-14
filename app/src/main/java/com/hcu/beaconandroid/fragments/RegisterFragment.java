package com.hcu.beaconandroid.fragments;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hcu.beaconandroid.R;
import com.hcu.beaconandroid.model.Response;
import com.hcu.beaconandroid.model.User;
import com.hcu.beaconandroid.network.NetworkUtil;

import java.io.IOException;

import retrofit2.adapter.rxjava.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.hcu.beaconandroid.utils.Validation.validateEmail;
import static com.hcu.beaconandroid.utils.Validation.validateUsername;
import static com.hcu.beaconandroid.utils.Validation.validateFields;

public class RegisterFragment extends Fragment {

    public static final String TAG = RegisterFragment.class.getSimpleName();

    private EditText mEtName, mEtSurname, mEtUsername, mEtEmail, mEtDetail, mEtPassword;
    private Button mBtRegister;
    private TextView mTvLogin;
    private TextInputLayout mTiName, mTiSurname, mTiUsername, mTiEmail, mTiDetail, mTiPassword;
    private ProgressBar mProgressbar;

    private CompositeSubscription mSubscriptions;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_register,container,false);
        mSubscriptions = new CompositeSubscription();
        initViews(view);
        return view;
    }

    private void initViews(View v) {

        mEtName = v.findViewById(R.id.et_name);
        mEtSurname =  v.findViewById(R.id.et_surname);
        mEtUsername=  v.findViewById(R.id.et_username);
        mEtEmail =  v.findViewById(R.id.et_email);
        mEtDetail =  v.findViewById(R.id.et_detail);
        mEtPassword =  v.findViewById(R.id.et_password);
        mBtRegister = v.findViewById(R.id.btn_register);
        mTvLogin =  v.findViewById(R.id.tv_login);
        mTiName =  v.findViewById(R.id.ti_name);
        mTiSurname =  v.findViewById(R.id.ti_username);
        mTiEmail =  v.findViewById(R.id.ti_email);
        mTiPassword =  v.findViewById(R.id.ti_password);
        mProgressbar = v.findViewById(R.id.progress);

        mBtRegister.setOnClickListener(view -> register());
        mTvLogin.setOnClickListener(view -> goToLogin());
    }

    private void register() {

        setError();

        String name = mEtName.getText().toString();
        String surname = mEtSurname.getText().toString();
        String username = mEtUsername.getText().toString();
        String email = mEtEmail.getText().toString();
        String detail = mEtDetail.getText().toString();
        String password = mEtPassword.getText().toString();

        int err = 0;

        //todo detail  ve username için validation yapılacak
        if (!validateFields(name)) {

            err++;
            mTiName.setError("Name should not be empty !");
        }

        if (!validateFields(surname)) {

            err++;
            mTiSurname.setError("Surname should not be empty !");
        }

        if (!validateUsername(username)) {

            err++;
            mEtUsername.setError("Username should be valid !");
        }

        if (!validateEmail(email)) {

            err++;
            mTiEmail.setError("Email should be valid !");
        }

        if (!validateFields(password)) {

            err++;
            mTiPassword.setError("Password should not be empty !");
        }

        if (err == 0) {

            User user = new User();
            user.setName(name);
            user.setSurname(surname);
            user.setUsername(username);
            user.setEmail(email);
            user.setDetail(detail);
            user.setPassword(password);


            mProgressbar.setVisibility(View.VISIBLE);
            registerProcess(user);

        } else {

            showSnackBarMessage("Enter Valid Details !");
        }
    }

    private void setError() {

        mTiName.setError(null);
        mTiSurname.setError(null);
        mTiEmail.setError(null);
        mTiPassword.setError(null);
    }

    private void registerProcess(User user) {

        mSubscriptions.add(NetworkUtil.getRetrofit().register(user)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void handleResponse(Response response) {

        mProgressbar.setVisibility(View.GONE);
        showSnackBarMessage(response.getMessage());
    }

    private void handleError(Throwable error) {

        mProgressbar.setVisibility(View.GONE);

        if (error instanceof HttpException) {

            Gson gson = new GsonBuilder().create();

            try {

                String errorBody = ((HttpException) error).response().errorBody().string();
                Response response = gson.fromJson(errorBody,Response.class);
                showSnackBarMessage(response.getMessage());

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {

            showSnackBarMessage("Network Error !");
        }
    }

    private void showSnackBarMessage(String message) {

        if (getView() != null) {

            Snackbar.make(getView(),message,Snackbar.LENGTH_SHORT).show();
        }
    }

    private void goToLogin(){

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        LoginFragment fragment = new LoginFragment();
        ft.replace(R.id.mainfragmentFrame, fragment, LoginFragment.TAG);
        ft.commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();
    }

}

package com.hcu.beaconandroid.fragments;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hcu.beaconandroid.ProfileActivity;
import com.hcu.beaconandroid.R;
import com.hcu.beaconandroid.model.Response;
import com.hcu.beaconandroid.network.NetworkUtil;
import com.hcu.beaconandroid.utils.Constants;

import java.io.IOException;

import retrofit2.adapter.rxjava.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.hcu.beaconandroid.utils.Validation.validateFields;
import static com.hcu.beaconandroid.utils.Validation.validateUsername;

public class LoginFragment extends Fragment {

    public static final String TAG = LoginFragment.class.getSimpleName();

    private EditText mEtUsername, mEtPassword;
    private Button mBtLogin;
    private TextView mTvRegister, mTvForgotPassword;
    private TextInputLayout mTiUsername, mTiPassword;
    private ProgressBar mProgressBar;

    private CompositeSubscription mSubscriptions;
    private SharedPreferences mSharedPreferences;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login,container,false);
        mSubscriptions = new CompositeSubscription();
        initViews(view);
        initSharedPreferences();
        return view;
    }

    private void initViews(View v) {

        mEtUsername = v.findViewById(R.id.et_username);
        mEtPassword = v.findViewById(R.id.et_password);
        mBtLogin = v.findViewById(R.id.btn_login);
        mTiUsername = v.findViewById(R.id.ti_username);
        mTiPassword = v.findViewById(R.id.ti_password);
        mProgressBar = v.findViewById(R.id.progress);
        mTvRegister = v.findViewById(R.id.tv_register);
        mTvForgotPassword = v.findViewById(R.id.tv_forgot_password);

        mBtLogin.setOnClickListener(view -> login());
        mTvRegister.setOnClickListener(view -> goToRegister());
        //mTvForgotPassword.setOnClickListener(view -> showDialog());
    }

    private void initSharedPreferences() {

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    private void login() {

        setError();

        String username = mEtUsername.getText().toString();
        String password = mEtPassword.getText().toString();

        int err = 0;
        //todo username için validate işlemi yapılacak
        if (!validateUsername(username)) {

            err++;
            mTiUsername.setError("Username should be valid !");
        }

        if (!validateFields(password)) {

            err++;
            mTiPassword.setError("Password should not be empty !");
        }

        if (err == 0) {

            loginProcess(username, password);
            mProgressBar.setVisibility(View.VISIBLE);

        } else {

            showSnackBarMessage("Enter Valid Details !");
        }
    }

    private void setError() {

        mTiUsername.setError(null);
        mTiPassword.setError(null);
    }

    private void loginProcess(String username, String password) {

        mSubscriptions.add(NetworkUtil.getRetrofit(username, password).login()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void handleResponse(Response response) {

        mProgressBar.setVisibility(View.GONE);

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(Constants.FULLNAME,response.getFullname());
        editor.putString(Constants.TOKEN,response.getToken());
        editor.putString(Constants.EMAIL,response.getEmail());
        editor.putString(Constants.DETAIL,response.getDetail());
        editor.apply();

        mEtUsername.setText(null);
        mEtPassword.setText(null);

        //Toast.makeText(getContext(),"giriş yapılacak",Toast.LENGTH_LONG).show();
        // TODO: 25.07.2018 GİRİŞ YAPTIKTAN SONRA GELECEK EKRAN BURADAN ÇAĞRILACAK
        Intent intent = new Intent(getActivity(), ProfileActivity.class);
        intent.putExtra("fullname", response.getFullname());
        intent.putExtra("username", response.getUsername());
        intent.putExtra("email", response.getEmail());
        intent.putExtra("detail", response.getDetail());
        startActivity(intent);

    }

    private void handleError(Throwable error) {

        mProgressBar.setVisibility(View.GONE);

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

    private void goToRegister(){

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        RegisterFragment fragment = new RegisterFragment();
        ft.replace(R.id.mainfragmentFrame,fragment,RegisterFragment.TAG);
        ft.commit();
    }

    /*private void showDialog(){

        ResetPasswordDialog fragment = new ResetPasswordDialog();

        fragment.show(getFragmentManager(), ResetPasswordDialog.TAG);
    }*/

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();
    }


}

package com.hcu.beaconandroid;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.hcu.beaconandroid.fragments.LoginFragment;
import com.hcu.beaconandroid.fragments.ProfileFragment;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

public class ProfileActivity extends AppCompatActivity{

    private ProfileFragment mProfileFragment;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    public static final String TAG = "ProfileActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        /*if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
        }*/

        if (savedInstanceState == null) {
            loadFragment();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("ACCESSING OKAY TAG", "coarse location permission granted");
                } else {
                    Log.d("PERMISSION DENIED", "coarse location permission NOT granted");
                }
            }
        }
    }


    private void loadFragment(){

        if (mProfileFragment == null) {
            Bundle dataBundle = new Bundle();
            Intent getDataIntent = getIntent();
            dataBundle.putString("fullname", getDataIntent.getStringExtra("fullname"));
            dataBundle.putString("username", getDataIntent.getStringExtra("username"));
            dataBundle.putString("email", getDataIntent.getStringExtra("email"));
            dataBundle.putString("detail", getDataIntent.getStringExtra("detail"));

            mProfileFragment = new ProfileFragment();
            mProfileFragment.setArguments(dataBundle);
        }

        getFragmentManager().beginTransaction().replace(R.id.mainfragmentFrame, mProfileFragment, ProfileFragment.TAG).commit();
    }
}

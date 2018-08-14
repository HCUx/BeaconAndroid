package com.hcu.beaconandroid.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hcu.beaconandroid.R;
import com.hcu.beaconandroid.model.Beacon;
import com.hcu.beaconandroid.model.BeaconRecord;
import com.hcu.beaconandroid.model.Response;
import com.hcu.beaconandroid.network.NetworkUtil;
import com.skyfishjy.library.RippleBackground;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class ProfileFragment extends Fragment implements BeaconConsumer {

    public static final String TAG = "ProfileFragment";

    Button  mBtLogout;
    ImageView imageView;
    TextView mTvPersonName, mTvPersonDetail, mTvPersonEmail;
    RippleBackground rippleBackground;
    CompositeSubscription mSubscriptions;
    private BeaconManager beaconManager;
    Set<org.altbeacon.beacon.Beacon> beaconCollection;
    Button btnSignOut, btnSignIn;
    ProgressBar profileprogress;
    String username, fullname, detail, email;
    private String lastlogtype;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        beaconManager = BeaconManager.getInstanceForApplication(getContext());
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        beaconManager.bind(this);
        beaconCollection = new HashSet<>();
        mSubscriptions = new CompositeSubscription();
        initViews(view);
        return view;
    }



    private void initViews(View view) {

        mBtLogout = view.findViewById(R.id.btn_logout);
        mTvPersonName = view.findViewById(R.id.tv_Name);
        mTvPersonEmail = view.findViewById(R.id.tv_Email);
        mTvPersonDetail = view.findViewById(R.id.tv_Description);
        rippleBackground= view.findViewById(R.id.content);
        imageView = view.findViewById(R.id.centerImage);
        profileprogress = view.findViewById(R.id.progressprofile);
        rippleBackground.startRippleAnimation();

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            fullname = bundle.getString("fullname", "Unknown User");
            detail = bundle.getString("detail", "Unknown Detail");
            email = bundle.getString("email", "Unknown Email");
            username = bundle.getString("username", "Unknown User Name");

            mTvPersonName.setText( fullname );
            mTvPersonDetail.setText( detail);
            mTvPersonEmail.setText( email );

        }

        btnSignIn = view.findViewById(R.id.btnSignIn);

        btnSignIn.setOnClickListener(v -> {
            btnSignIn.setEnabled(false);
            profileprogress.setVisibility(View.VISIBLE);
            if (beaconCollection.size() > 0) {
                lastlogtype = "Giriş";
                getbeaconprocess();
            }else {
                Toast.makeText(getContext(), "Çevrede iBeacon Bulunamadı", Toast.LENGTH_SHORT).show();
                btnSignIn.setEnabled(true);
                profileprogress.setVisibility(View.INVISIBLE);
            }


        });

        btnSignOut = view.findViewById(R.id.btnSignOut);

        btnSignOut.setOnClickListener(v -> {
            btnSignOut.setEnabled(false);
            profileprogress.setVisibility(View.VISIBLE);
            if (beaconCollection.size() > 0) {
                lastlogtype = "Çıkış";
                getbeaconprocess();
            }else {
                Toast.makeText(getContext(), "Çevrede iBeacon Bulunamadı", Toast.LENGTH_SHORT).show();
                btnSignOut.setEnabled(true);
                profileprogress.setVisibility(View.INVISIBLE);
            }
        });

    }

    @Override
    public void onBeaconServiceConnect() {


        /*beaconManager.setRangeNotifier((beacons, region) -> {
            if (beacons.size() > 0) {
                Log.d(TAG, "didRangeBeaconsInRegion called with beacon count:  "+beacons.size());
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException ignored) {   }*/



        beaconManager.addRangeNotifier((beacons, region) -> {
            if (beacons.size() > 0) {
                Log.i(TAG, "The first beacon I see is about "+beacons.iterator().next().getDistance()+" meters away.");
                beaconCollection.addAll(beacons);
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) { Log.e("REMOTEEXCEPTION range", e.toString());   }


        /*beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.i(TAG, "I just saw an beacon for the first time!");
            }

            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG, "I no longer see an beacon");
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {

                Log.i(TAG, "I have just switched from seeing/not seeing beacons: "+state);
            }
        });

        try {
            beaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
        } catch (RemoteException e) {  Log.e("REMOTEEXCEPTION monıtorıng", e.toString()); }*/

    }

    private void logToDisplay(final String line) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                Log.i(TAG, line);
            }
        });
    }

    private void getbeaconprocess() {

        mSubscriptions.add(NetworkUtil.getRetrofit().sendBeacon(username)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::getBeaconResponse,this::getBeaconError));
    }

    private void getBeaconResponse(Response response) {
        profileprogress.setVisibility(View.INVISIBLE);
        btnSignIn.setEnabled(true);
        btnSignOut.setEnabled(true);
        boolean exit = false;

        for (int i = 0; i < response.getBeacons().length; i++){
            for(int k = 0; k<beaconCollection.size(); k++){
                if(response.getBeacons()[i].getUuid().equals(beaconCollection.iterator().next().getId1().toString()) ){
                    setBeaconRecord(response.getBeacons()[i].getUuid());
                    exit = true;
                    break;
                }
            }
            if (exit)
                break;
        }

    }

    private void getBeaconError(Throwable error) {
        profileprogress.setVisibility(View.INVISIBLE);
        btnSignIn.setEnabled(true);
        btnSignOut.setEnabled(true);
        showSnackBarMessage(error.getMessage());
    }

    private void setBeaconRecord(String uuid) {

        BeaconRecord beacon = new BeaconRecord();
        beacon.setUuid(uuid);
        beacon.setUsername(username);
        beacon.setLogtype(lastlogtype);

        mSubscriptions.add(NetworkUtil.getRetrofit().sendBeaconRecord(beacon)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::setBeaconRecordResponse,this::setBeaconRecordError));
    }

    private void setBeaconRecordResponse(Response response) {
        profileprogress.setVisibility(View.INVISIBLE);
        btnSignIn.setEnabled(true);
        btnSignOut.setEnabled(true);
        showSnackBarMessage(response.getMessage());
    }

    private void setBeaconRecordError(Throwable error) {
        profileprogress.setVisibility(View.INVISIBLE);
        btnSignIn.setEnabled(true);
        btnSignOut.setEnabled(true);
        showSnackBarMessage(error.getMessage());
    }

    private void showSnackBarMessage(String message) {

        if (getView() != null) {

            Snackbar.make(getView(),message,Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public Context getApplicationContext() {
        return null;
    }

    @Override
    public void unbindService(ServiceConnection serviceConnection) {

    }

    @Override
    public boolean bindService(Intent ıntent, ServiceConnection serviceConnection, int i) {
        return false;
    }

}

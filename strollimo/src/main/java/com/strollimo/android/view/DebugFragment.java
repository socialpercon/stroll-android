package com.strollimo.android.view;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.strollimo.android.R;
import com.strollimo.android.StrollimoApplication;
import com.strollimo.android.StrollimoPreferences;
import com.strollimo.android.controller.AccomplishableController;
import com.strollimo.android.model.ImageComparisonPickupMode;
import com.strollimo.android.model.Location;
import com.strollimo.android.model.Mystery;
import com.strollimo.android.model.Secret;
import com.strollimo.android.network.StrollimoApi;
import com.strollimo.android.network.response.GetMysteriesResponse;
import com.strollimo.android.network.response.GetSecretsResponse;
import com.strollimo.android.network.response.UpdateMysteryResponse;
import com.strollimo.android.network.response.UpdateSecretResponse;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class DebugFragment extends Fragment {
    private View mView;
    private Switch mSwitch;
    private StrollimoPreferences mPrefs;
    private AccomplishableController mAccomplishableController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mPrefs = StrollimoApplication.getService(StrollimoPreferences.class);
        mAccomplishableController = StrollimoApplication.getService(AccomplishableController.class);
        if (mView == null) {
            mView = inflater.inflate(R.layout.debug_layout, container, false);
        } else {
            ViewGroup parentViewGroup = (ViewGroup) mView.getParent();
            if (parentViewGroup != null) {
                parentViewGroup.removeAllViews();
            }
        }

        mSwitch = (Switch) mView.findViewById(R.id.debug_mode_switch);
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                mPrefs.setDebugModeOn(checked);
            }
        });
        mView.findViewById(R.id.sync_data_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                syncData();
            }
        });
        mView.findViewById(R.id.crash_app_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                throw new NullPointerException("Crash test");
            }
        });
        mView.findViewById(R.id.test_something_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testSomething();
            }
        });
        mView.findViewById(R.id.set_env_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setEnv();
            }
        });

        refreshEnvTitle();
        return mView;
    }

    private void syncData() {
        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "", "Please wait...");
        progressDialog.show();
        mAccomplishableController.clearMysteries();
        mAccomplishableController.asyncSyncMysteries(mPrefs.getEnvTag(), new AccomplishableController.OperationCallback() {
            @Override
            public void onSuccess() {
                progressDialog.dismiss();
            }

            @Override
            public void onError(String errorMsg) {
                Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        });
    }

    private void refreshEnvTitle() {
        String env = mPrefs.getEnvTag();
        String envStr = getActivity().getString(R.string.debug_env_title, env);
        ((TextView)mView.findViewById(R.id.current_env_text)).setText(envStr);
    }

    private void setEnv() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

        alert.setTitle("Environment");
        alert.setMessage("Type in an environment");

        final EditText input = new EditText(getActivity());
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mPrefs.setEnvTag(input.getText().toString());
                refreshEnvTitle();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();    }

    private void testSomething() {
        testGetMysteries();
//        testUpdateSecretCall();
//        testUpdateMysteryCall();
//        testGetSecrets();
    }

    private void testGetMysteries() {
        StrollimoApplication.getService(StrollimoApi.class).getMysteries("default", new Callback<GetMysteriesResponse>() {
            @Override
            public void success(GetMysteriesResponse getMysteriesResponse, Response response) {
                Log.i("BB", "success");
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.i("BB", "failure");

            }
        });
    }

    private void testGetSecrets() {
        String mysteryId = "feb1eb63-fca6-4291-99c1-7a6fee31ee05";
        StrollimoApplication.getService(StrollimoApi.class).getSecrets(mysteryId, new Callback<GetSecretsResponse>() {
            @Override
            public void success(GetSecretsResponse getSecretsResponse, Response response) {
                Log.i("BB", "success");
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.i("BB", "failure");

            }
        });
    }

    private void testUpdateSecretCall() {
        Secret secret = new Secret("1", "bb test secret");
        secret.setShortDesc("short desc");
        secret.setImgUrl("test imag url");
        secret.setLocation(new Location(0.4, 0.5, 0.1));
        ImageComparisonPickupMode mode = new ImageComparisonPickupMode();
        mode.addUrl("test url 1");
        secret.addPickupMode(mode);
        StrollimoApplication.getService(StrollimoApi.class).updateSecret(secret, new Callback<UpdateSecretResponse>() {
            @Override
            public void success(UpdateSecretResponse updateSecretResponse, Response response) {
                Log.i("BB", "success");
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.i("BB", "failure");

            }
        });
    }

    private void testUpdateMysteryCall() {
        Mystery mystery = new Mystery("56", "BBTestNew", 0.4, 0.5, "image URL");
        mystery.setShortDesc("something");
        StrollimoApplication.getService(StrollimoApi.class).updateMystery(mystery, new Callback<UpdateMysteryResponse>() {
            @Override
            public void success(UpdateMysteryResponse updateMysteryResponse, Response response) {
                Log.i("BB", "success");
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.i("BB", "failure");

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mSwitch.setChecked(mPrefs.isDebugModeOn());
    }
}

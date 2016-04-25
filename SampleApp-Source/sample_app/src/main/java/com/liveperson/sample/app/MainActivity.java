package com.liveperson.sample.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.liveperson.api.LivePersonCallback;
import com.liveperson.infra.ICallback;
import com.liveperson.infra.InitLivePersonCallBack;
import com.liveperson.messaging.TaskType;
import com.liveperson.messaging.model.AgentData;
import com.liveperson.messaging.sdk.api.LivePerson;
import com.liveperson.messaging.sdk.api.LogoutLivePersonCallback;
import com.liveperson.sample.app.account.AccountStorage;
import com.liveperson.sample.app.account.UserProfileStorage;
import com.liveperson.sample.app.push.RegistrationIntentService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private EditText mAccountTextView;
    private EditText mFirstNameView;
    private EditText mLastNameView;
    private EditText mPhoneNumberView;
    private EditText mAuthCodeView;
    private CheckBox mIdpCheckBox;
    private TextView mSdkVersion;
    private String account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initAccount();
        initOpenConversationButton();
        initStartFragmentButton();

        initSpinner();
    }

    private void initAccount() {
        // Set the default account in the view
        mAccountTextView = (EditText) findViewById(R.id.brand_id);
        mAccountTextView.setText(AccountStorage.getInstance(this).getAccount());

        mFirstNameView = (EditText) findViewById(R.id.first_name);
        mFirstNameView.setText(UserProfileStorage.getInstance(this).getFirstName());

        mLastNameView = (EditText) findViewById(R.id.last_name);
        mLastNameView.setText(UserProfileStorage.getInstance(this).getLastName());

        mPhoneNumberView = (EditText) findViewById(R.id.phone_number);
        mPhoneNumberView.setText(UserProfileStorage.getInstance(this).getPhoneNumber());

        mAuthCodeView = (EditText)findViewById(R.id.auth_code);
        mAuthCodeView.setText(UserProfileStorage.getInstance(this).getAuthCode());

        mIdpCheckBox = (CheckBox) findViewById(R.id.idp_checkbox);

        String sdkVersion = String.format("SDK version %1$s ", LivePerson.getSDKVersion());
        mSdkVersion = (TextView) findViewById(R.id.sdk_version);
        mSdkVersion.setText(sdkVersion);
    }

    private void setCallBack() {
        LivePerson.setCallback(new LivePersonCallback() {
            @Override
            public void onCustomGuiTapped() {

            }

            @Override
            public void onConnectionChanged(boolean isConnected) {
                Toast.makeText(MainActivity.this, "onConnectionChanged : " + isConnected, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(TaskType type, String message) {
                Toast.makeText(MainActivity.this, type.name() + " problem ", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onTokenExpired(String brandId) {
                Toast.makeText(MainActivity.this, "onTokenExpired brand " + brandId, Toast.LENGTH_LONG).show();

                // Change authentication key here
                LivePerson.reconnect(AccountStorage.getInstance(MainActivity.this).getAccount());
            }

            @Override
            public void onConversationStarted() {
                Toast.makeText(MainActivity.this, "onConversationStarted", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onConversationResolved() {
                Toast.makeText(MainActivity.this, "onConversationResolved", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAgentDetailsChanged(AgentData agentData) {
                Toast.makeText(MainActivity.this, "Agent Details Changed " + agentData, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCsatDismissed() {
                Toast.makeText(MainActivity.this, "on Csat Dismissed", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onConversationMarkedAsUrgent() {
                Toast.makeText(MainActivity.this, "Conversation Marked As Urgent", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onConversationMarkedAsNormal() {
                Toast.makeText(MainActivity.this, "Conversation Marked As Normal", Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean checkValidAccount() {
        account = mAccountTextView.getText().toString().trim();
        if (TextUtils.isEmpty(account)) {
            Toast.makeText(this, "No account!", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void saveAccountAndUserSettings() {
        // Get brand ID from UI
        String account = mAccountTextView.getText().toString().trim();
        String firstName = mFirstNameView.getText().toString().trim();
        String lastName = mLastNameView.getText().toString().trim();
        String phoneNumber = mPhoneNumberView.getText().toString().trim();
        String authCode = mAuthCodeView.getText().toString().trim();
        AccountStorage.getInstance(this).setAccount(account);
        UserProfileStorage.getInstance(this).setFirstName(firstName);
        UserProfileStorage.getInstance(this).setLastName(lastName);
        UserProfileStorage.getInstance(this).setPhoneNumber(phoneNumber);
        UserProfileStorage.getInstance(this).setAuthCode(authCode);
    }

    private void initOpenConversationButton() {
        findViewById(R.id.button_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkValidAccount()) {
                    return;
                }

                saveAccountAndUserSettings();
                LivePerson.initialize(MainActivity.this, AccountStorage.getInstance(MainActivity.this).getAccount(), new InitLivePersonCallBack() {
                    @Override
                    public void onInitSucceed() {
                        Log.i(TAG, "onInitSucceed");
                        setCallBack();
                        handleGCMRegistration();
                        openActivity();
                    }

                    @Override
                    public void onInitFailed(Exception e) {
                        Toast.makeText(MainActivity.this, "Init Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            private void openActivity() {
                if (mIdpCheckBox.isChecked()) {
                    // Change authentication key here
                    LivePerson.showConversation(MainActivity.this, UserProfileStorage.getInstance(MainActivity.this).getAuthCode());
                } else {
                    LivePerson.showConversation(MainActivity.this);
                }
                LivePerson.setUserProfile(AccountStorage.SDK_SAMPLE_APP_ID, mFirstNameView.getText().toString(), mLastNameView.getText().toString(), mPhoneNumberView.getText().toString());
            }
        });
    }

    private void initStartFragmentButton() {
        findViewById(R.id.button_start_fragment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkValidAccount()) {
                    return;
                }

                saveAccountAndUserSettings();
                Log.i(TAG, "initStartFragmentButton");
                setCallBack();
                handleGCMRegistration();
                openFragment();
            }

            private void openFragment() {
                Intent in = new Intent(MainActivity.this, CustomActivity.class);
                if (mIdpCheckBox.isChecked()) {
                    in.putExtra(CustomActivity.IS_AUTH, true);
                } else {
                    in.putExtra(CustomActivity.IS_AUTH, false);
                }
                startActivity(in);
            }
        });
    }

    private void handleGCMRegistration() {
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }


    private void initSpinner() {
        final List<String> list = new ArrayList<String>();
        list.add("ShutDown");
        list.add("LogOut");
        list.add("Init");
        list.add("checkActiveConversation");
        list.add("checkConversationIsMarkedAsUrgent");
        list.add("checkAgentID");
        list.add("markConversationAsUrgent");
        list.add("markConversationAsNormal");
        list.add("resolveConversation");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        final Spinner spinner = (Spinner) findViewById(R.id.api_spinner);
        spinner.setAdapter(dataAdapter);

        (findViewById(R.id.api_go)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkValidAccount()){
                    return;
                }
                String selectedItem = (String) spinner.getSelectedItem();
                Log.d(TAG, selectedItem + " button pressed");
                switch (selectedItem){
                    case "ShutDown":
                        LivePerson.shutDown();
                        break;
                    case "LogOut":
                        LivePerson.logOut(MainActivity.this, account, AccountStorage.SDK_SAMPLE_APP_ID, new LogoutLivePersonCallback() {
                            @Override
                            public void onLogoutSucceed() {
                                Toast.makeText(MainActivity.this, "onLogoutSucceed", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onLogoutFailed() {
                                Toast.makeText(MainActivity.this, "onLogoutFailed", Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    case "Init":
                        LivePerson.initialize(MainActivity.this, account, new InitLivePersonCallBack() {
                            @Override
                            public void onInitSucceed() {
                                Toast.makeText(MainActivity.this, "Init Succeed", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onInitFailed(Exception e) {
                                Toast.makeText(MainActivity.this, "Init Failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    case "checkActiveConversation":
                        LivePerson.checkActiveConversation(new ICallback<Boolean, Exception>() {
                            @Override
                            public void onSuccess(Boolean value) {
                                Toast.makeText(MainActivity.this, value + "", Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onError(Exception exception) {
                                Toast.makeText(MainActivity.this, "Error! " + exception.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                    case "checkConversationIsMarkedAsUrgent":
                        LivePerson.checkConversationIsMarkedAsUrgent(new ICallback<Boolean, Exception>() {
                            @Override
                            public void onSuccess(Boolean value) {
                                Toast.makeText(MainActivity.this, value + "", Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onError(Exception exception) {
                                Toast.makeText(MainActivity.this, "Error! " + exception.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                    case "checkAgentID":
                        LivePerson.checkAgentID(new ICallback<AgentData, Exception>() {
                            @Override
                            public void onSuccess(AgentData value) {
                                Toast.makeText(MainActivity.this, value != null ? value.toString() : " No data!", Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onError(Exception exception) {
                                Toast.makeText(MainActivity.this, "Error! " + exception.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                    case "markConversationAsUrgent":
                        LivePerson.markConversationAsUrgent();
                        break;
                    case "markConversationAsNormal":
                        LivePerson.markConversationAsNormal();
                        break;
                    case "resolveConversation":
                        LivePerson.resolveConversation();
                        break;
                }


            }
        });
    }
}

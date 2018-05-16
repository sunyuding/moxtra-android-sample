package com.moxtra.moxiechat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.moxtra.moxiechat.common.PreferenceUtil;
import com.moxtra.moxiechat.model.DummyData;
import com.moxtra.moxiechat.model.MoxieUser;
import com.moxtra.sdk.ChatClient;
import com.moxtra.sdk.client.ChatClientDelegate;
import com.moxtra.sdk.common.ApiCallback;
import com.moxtra.sdk.common.model.MyProfile;

import java.util.List;

/**
 * A login screen that offers login via unique ID.
 */
public class LoginActivity extends Activity {
    private static final String LOG_TAG = LoginActivity.class.getSimpleName();

    /**
     * Here provides a sample to link with Moxtra account with accessToken via Intent.
     */
    private static final String KEY_TOKEN = "token";//the access token which usually generated on the server side.

    private static final String CLIENT_ID = "irej6RlLOBo";
    private static final String CLIENT_SECRET = "uiwE8ZzymRs";
    private static final String ORG_ID = null;
    private static final String BASE_DOMAIN = "sandbox.moxtra.com";//sandbox.moxtra.com for sandbox.

    // UI references.
    private Context mContext;
    private AutoCompleteTextView mUniqueIdView;

    private MyProfile myProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ChatClient.initialize(this.getApplication());

        myProfile = ChatClient.getMyProfile();
        if (myProfile != null) {
            // Already linked to Moxtra account
            startChatListActivity();
            return;
        }

        //Return the context of the single, global Application object of the current process.
        mContext = getApplicationContext();
        //Auto fill sign in for convenience
        mUniqueIdView = (AutoCompleteTextView) findViewById(R.id.unique_id);
        mUniqueIdView.setText(DummyData.UNIQUE_IDS.get(0));
        addUniqueIdsToAutoComplete(DummyData.UNIQUE_IDS);

        Intent intent = getIntent();//Return the intent that started this activity.
        if (intent != null) {
            String token = intent.getStringExtra(KEY_TOKEN);
            Log.d(LOG_TAG, "Token = " + token);
            if (token != null) {
                linkWithAccessToken(token);
                return;
            }
        }

        MoxieUser user = PreferenceUtil.getUser(this);
        if (user != null) {
            linkWithUniqueID(user.uniqueId);
        } else {
            showSelectionDialog();
        }
    }

    private void startChatListActivity() {
        Log.i(LOG_TAG, "startChatListActivity");
        startActivity(new Intent(this, ChatListActivity.class));//launch an Activity
        finish();// Call this when your activity is done and should be closed.
    }

    private void linkWithUniqueID(final String uniqueId) {
        Log.d(LOG_TAG, "Start to linkWithUniqueID...");

        ChatClient.linkWithUniqueId(uniqueId, CLIENT_ID, CLIENT_SECRET, ORG_ID, BASE_DOMAIN,
                new ApiCallback<ChatClientDelegate>() {
                    @Override
                    public void onCompleted(ChatClientDelegate ccd) {
                        Log.i(LOG_TAG, "Linked to Moxtra account successfully.");

                        myProfile = ChatClient.getMyProfile();
                        MoxieUser user = DummyData.findByUniqueId(uniqueId);

                        myProfile.updateMyProfile(user.firstName, user.lastName, new ApiCallback<Void>() {
                            @Override
                            public void onCompleted(Void aVoid) {
                                Log.d(LOG_TAG, "updateMyProfile");
                                PreferenceUtil.saveUser(mContext, uniqueId);
                                Intent intent = new Intent(mContext, GcmRegistrationService.class);
                                startService(intent);
                                startChatListActivity();
                            }

                            @Override
                            public void onError(int i, String s) {

                            }
                        });
                    }

                    @Override
                    public void onError(int errorCode, String errorMsg) {
                        Toast.makeText(mContext, "Failed to link to Moxtra account.", Toast.LENGTH_LONG).show();
                        Log.e(LOG_TAG,
                                "Failed to link to Moxtra account, errorCode=" + errorCode + ", errorMsg=" + errorMsg);
                    }
                });
    }

    private void linkWithAccessToken(final String token) {
        Log.d(LOG_TAG, "Start to linkWithAccessToken...");

        ChatClient.linkWithAccessToken(token, BASE_DOMAIN, new ApiCallback<ChatClientDelegate>() {

            @Override
            public void onCompleted(ChatClientDelegate ccd) {
                Log.i(LOG_TAG, "Linked to Moxtra account successfully.");
                Intent intent = new Intent(mContext, GcmRegistrationService.class);
                startService(intent);
                startChatListActivity();
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                Toast.makeText(mContext, "Failed to link to Moxtra account.", Toast.LENGTH_LONG).show();
                Log.e(LOG_TAG,
                        "Failed to link to Moxtra account, errorCode=" + errorCode + ", errorMsg=" + errorMsg);
            }
        });
    }

    private void showSelectionDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.selectUserTitle)
                .items(DummyData.UNIQUE_IDS)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                        MoxieUser moxieUser = DummyData.USERS.get(i);
                        mUniqueIdView.setText(moxieUser.uniqueId);
                    }
                })
                .show();
    }

    private void addUniqueIdsToAutoComplete(List<String> uniqueIdCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(mContext,
                        android.R.layout.simple_dropdown_item_1line, uniqueIdCollection);

        mUniqueIdView.setAdapter(adapter);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (missing fields, etc.),
     * the errors are presented and no actual login attempt is made.
     */
    public void signIn(View view) {
        // Reset errors.
        mUniqueIdView.setError(null);
        String uniqueId = mUniqueIdView.getText().toString();

        // Check for a valid unique ID.
        if (TextUtils.isEmpty(uniqueId)) {
            mUniqueIdView.setError(getString(R.string.error_field_required));
            mUniqueIdView.requestFocus();
        } else {
            linkWithUniqueID(uniqueId);
        }
    }
}


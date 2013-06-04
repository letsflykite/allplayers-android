package com.allplayers.android;

import org.jasypt.util.text.BasicTextEncryptor;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.allplayers.rest.RestApiV1;

/**
 * Initial activity to handle login.
 *
 * TODO: Replace with AccountManager, loading only as required when an account
 * is needed.
 */
public class Login extends Activity {
    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private TextView mPasswordLabel;
    private TextView mUsernameLabel;
    private Button mLoginButton;
    private Button mNewAccountButton;
    private Button mGroupSearchButton;
    private TextView mAccountCreateSuccess;
    private ProgressBar mLoadingIndicator;
    private AccountManager mAccountManager;
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // TODO - Temporarily disable StrictMode because all networking is
        // currently in the UI thread. Android now throws exceptions when
        // obvious IO happens in the UI thread, which is a good thing.
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mContext = this.getBaseContext();
        mAccountManager = AccountManager.get(mContext);
        mLoginButton = (Button)findViewById(R.id.loginButton);
        mNewAccountButton = (Button)findViewById(R.id.newAccountButton);
        mGroupSearchButton = (Button)findViewById(R.id.groupSearchButton);
        mUsernameEditText = (EditText)findViewById(R.id.usernameField);
        mPasswordEditText = (EditText)findViewById(R.id.passwordField);
        mPasswordLabel = (TextView)findViewById(R.id.passwordLabel);
        mUsernameLabel = (TextView)findViewById(R.id.usernameLabel);
        mAccountCreateSuccess = (TextView)findViewById(R.id.account_create_success);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.ctrlActivityIndicator);

        Account[] accounts = mAccountManager.getAccountsByType("com.allplayers.android");
        // There should only be one allplayers type account in the device at once.
        if (accounts.length == 1) {
            String storedEmail = accounts[0].name;
            String storedPassword = mAccountManager.getPassword(accounts[0]);
            String storedSecretKey = LocalStorage.readSecretKey(mContext);

            if (storedSecretKey == null || storedSecretKey.equals("")) {
                LocalStorage.writeSecretKey(mContext);
                storedSecretKey = LocalStorage.readSecretKey(mContext);
            }

            if (storedEmail != null && !storedEmail.equals("") && storedPassword != null && !storedPassword.equals("")) {
                BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
                textEncryptor.setPassword(storedSecretKey);
                String unencryptedPassword = textEncryptor.decrypt(storedPassword);

                mLoadingIndicator.setVisibility(View.VISIBLE);
                new AttemptLoginTask().execute(storedEmail, unencryptedPassword);
            }
        } else {
            // TODO: Clear user saved data as well
            showLoginFields();

            // Clear any UUID that may be saved from a previous user.
            // @TODO: This is not an elegant solution though is the only apparent one due to the way that
            //  RestApiV1 is currently set up.
            SharedPreferences sharedPreferences = getSharedPreferences("Critical_Data", 0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("UUID", "");
            editor.commit();
        }

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String email = mUsernameEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();

                mLoadingIndicator.setVisibility(View.VISIBLE);
                new AttemptLoginTask().execute(email, password);
            }
        });

        mNewAccountButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = new Intent(Login.this, NewAccountActivity.class);
                startActivityForResult(intent, 0);
            }
        });

        mGroupSearchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(Login.this, FindGroupsActivity.class));
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 || requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                mUsernameEditText.setText(data.getStringArrayExtra("login credentials")[0]);
                mPasswordEditText.setText(data.getStringArrayExtra("login credentials")[1]);
                mAccountCreateSuccess.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_MENU) {
            startActivity(new Intent(Login.this, FindGroupsActivity.class));
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyUp(keyCode, event);
    }

    public void showLoginFields() {
        mLoginButton.setVisibility(View.VISIBLE);
        mNewAccountButton.setVisibility(View.VISIBLE);
        mGroupSearchButton.setVisibility(View.VISIBLE);
        mUsernameEditText.setVisibility(View.VISIBLE);
        mPasswordEditText.setVisibility(View.VISIBLE);
        mPasswordLabel.setVisibility(View.VISIBLE);
        mUsernameLabel.setVisibility(View.VISIBLE);
    }

    /**
     * Attempt a login, if successful, move to the real main activity.
     */
    public class AttemptLoginTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... strings) {

            String email = strings[0];
            String pass = strings[1];

            RestApiV1 client = new RestApiV1();
            try {
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if (activeNetworkInfo == null) {
                    return "noInternetConnection";
                }
                if (RestApiV1.isLoggedIn()) {
                    Intent intent = new Intent(Login.this, GroupsActivity.class);
                    startActivity(intent);
                    finish();
                    return "validLogin";
                }

                String result = client.validateLogin(email, pass);
                JSONObject jsonResult = new JSONObject(result);
                RestApiV1.setCurrentUserUUID(jsonResult.getJSONObject("user").getString("uuid"));

                // If we get to this point, then we encrypt their password and add a new account.
                String key = LocalStorage.readSecretKey(mContext);
                if (key == null || key.equals("")) {
                    LocalStorage.writeSecretKey(mContext);
                    key = LocalStorage.readSecretKey(mContext);
                }

                BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
                textEncryptor.setPassword(key);
                String encryptedPassword = textEncryptor.encrypt(pass);

                Account account = new Account(email, "com.allplayers.android");
                mAccountManager.addAccountExplicitly(account, encryptedPassword, null);

                Intent intent = new Intent(Login.this, GroupsActivity.class);
                startActivity(intent);
                finish();
                return "validLogin";
            } catch (JSONException ex) {
                System.err.println("Login/user_id/" + ex);
                return "invalidLogin";
            }
        }

        protected void onPostExecute(String ex) {
            if (ex.equals("invalidLogin")) {
                Toast invalidLogin = Toast.makeText(getApplicationContext(), "Invalid Login", Toast.LENGTH_LONG);
                invalidLogin.show();
                mLoadingIndicator.setVisibility(View.INVISIBLE);
                showLoginFields();
            } else if (ex.equals("noInternetConnection")) {
                showLoginFields();
                Toast noInternetConnection = Toast.makeText(getApplicationContext(), "No Connection \nCheck Internet Connectivity", Toast.LENGTH_LONG);
                noInternetConnection.show();
                mLoadingIndicator.setVisibility(View.INVISIBLE);
            }
        }
    }
}

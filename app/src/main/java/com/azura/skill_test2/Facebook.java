package com.azura.skill_test2;

import java.util.Arrays;
import java.util.List;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.facebook.widget.LoginButton.UserInfoChangedCallback;

public class Facebook extends FragmentActivity implements OnClickListener, UserInfoChangedCallback {
    private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
    private Dialog share_dialog,user_profile_dialog;
    private LoginButton loginBtn;
    private Button userProfileBtn, updateStatusBtn, shareBtn, okBtn, timelineBtn, photoBtn;
    private TextView userName, id, url, name;
    private ImageView userImage;
    private EditText etShare;
    private UiLifecycleHelper uiHelper;
    private HttpMethod httpMethod;
    private GraphUser user;

    private Session.StatusCallback statusCallback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state,
                         Exception exception) {
            if (state.isOpened()) {
                buttonsEnabled(true);
                Log.d("FacebookSampleActivity", "Facebook session opened");
            } else if (state.isClosed()) {
                buttonsEnabled(false);
                Log.d("FacebookSampleActivity", "Facebook session closed");
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(this, statusCallback);
        uiHelper.onCreate(savedInstanceState);

        setContentView(R.layout.activity_facebook);

        userName = (TextView) findViewById(R.id.user_name);
        loginBtn = (LoginButton) findViewById(R.id.fb_login_button);
        userProfileBtn = (Button) findViewById(R.id.user_profile_btn);
        updateStatusBtn = (Button) findViewById(R.id.update_status);
        timelineBtn = (Button) findViewById(R.id.btn_timeline);
        photoBtn = (Button) findViewById(R.id.btn_photo);

        userProfileBtn.setOnClickListener(this);
        updateStatusBtn.setOnClickListener(this);
        loginBtn.setUserInfoChangedCallback(this);

        buttonsEnabled(false);
    }

    public void buttonsEnabled(boolean isEnabled) {
        userProfileBtn.setEnabled(isEnabled);
        updateStatusBtn.setEnabled(isEnabled);

        /*button saya disable karena fungsinya blm dapat digunakan*/
        timelineBtn.setEnabled(false);
        photoBtn.setEnabled(false);
    }

    public void postStatusMessage(String message) {
        if (checkPermissions()) {
            Request request = Request.newStatusUpdateRequest(
                    Session.getActiveSession(), message,
                    new Request.Callback() {
                        @Override
                        public void onCompleted(Response response) {
                            if (response.getError() == null)
                                Toast.makeText(Facebook.this,
                                        "Status updated successfully",
                                        Toast.LENGTH_LONG).show();
                        }
                    });
            request.executeAsync();
        } else {
            requestPermissions();
        }
    }

    public boolean checkPermissions() {
        Session s = Session.getActiveSession();
//        mengembalikan null jika session kosong
        return s != null && s.getPermissions().contains(PERMISSIONS);
    }

    public void requestPermissions() {
        Session s = Session.getActiveSession();
        if (s != null)
            s.requestNewPublishPermissions(new Session.NewPermissionsRequest(
                    this, PERMISSIONS));
    }


    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
        buttonsEnabled(Session.getActiveSession().isOpened());
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);
        uiHelper.onSaveInstanceState(savedState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.user_profile_btn:
                if (Session.getActiveSession().isOpened()) {
                    try {

                        //                URL img_url = new URL("http://graph.facebook.com/"+user.getId()+"/picture?type=normal");
                        //                Bitmap bmp = BitmapFactory.decodeStream(img_url.openConnection().getInputStream());
                        user_profile_dialog = new Dialog(Facebook.this);
                        user_profile_dialog.setContentView(R.layout.fragment_user_profile);
                        user_profile_dialog.setTitle("User Profile");
                        name = (TextView) user_profile_dialog.findViewById(R.id.get_name);
                        id = (TextView) user_profile_dialog.findViewById(R.id.get_id);
                        url = (TextView) user_profile_dialog.findViewById(R.id.get_url);
                        okBtn = (Button) user_profile_dialog.findViewById(R.id.ok_button);
                        userImage = (ImageView) user_profile_dialog.findViewById(R.id.imgUser);
                        okBtn.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                user_profile_dialog.cancel();
                                Session session = Session.getActiveSession();
                                if (session != null && session.getState().isOpened()) {
                                    Log.i("sessionToken", session.getAccessToken());
                                    Log.i("sessionTokenDueDate", session.getExpirationDate().toLocaleString());
                                }
                            }
                        });
                        //                new RetrievePhoto().execute("http://graph.facebook.com/picture?type=normal");
                        //                imageParser mImageParser = new imageParser();
                        //                mImageParser.execute();

                        //                userImage.setImageBitmap();

                        name.setText(user.getName());
                        id.setText(user.getId());
                        url.setText(user.getLink());
                        user_profile_dialog.show();
                    } catch (Exception e) {
                        Log.e("Error :", "" + e);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please Sign In", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.update_status:
                if (Session.getActiveSession().isOpened()) {
                    share_dialog = new Dialog(Facebook.this);
                    share_dialog.setContentView(R.layout.fragment_share);
                    share_dialog.setTitle("Share Article");
                    etShare = (EditText) share_dialog.findViewById(R.id.textShare);
                    shareBtn = (Button) share_dialog.findViewById(R.id.btnShare);

                    shareBtn.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            postStatusMessage(etShare.getText().toString());
                            share_dialog.cancel();
                        }
                    });
                    share_dialog.show();
                } else {
                    Toast.makeText(getApplicationContext(), "Please Sign In", Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.btn_timeline:
                if (Session.getActiveSession().isOpened()) {
                    Session ss = Session.getActiveSession();
                  /*test parsing timeline 7Langit*/
                    new Request(
                            ss,
                            "/7Langit",
                            null,
                            HttpMethod.GET,
                            new Request.Callback() {
                                @Override
                                public void onCompleted(Response response) {
                                    /*menampilkan apa yang di dapat*/
                                    Log.d("RESPONSE GRAPH OBJECT", "" + response.getGraphObject());
                                    Log.d("RESPONSE ", "" + response);
                                }
                            }
                    ).executeAsync();
                } else {
                    Toast.makeText(getApplicationContext(), "Please Sign In", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.btn_photo:
                break;
        }
    }

    @Override
    public void onUserInfoFetched(GraphUser graphUser) {
        if (graphUser != null) {
            userName.setText("Hello, " + graphUser.getName());
            user = graphUser;
        } else {
            userName.setText("You are not logged");
        }
    }

     /*test retrieve timeline 7Langit menggunakan asynctask*/
//    class imageParser extends AsyncTask<String, Void, Response> {

//        @Override
//        protected Response doInBackground(String... params) {
//            Session ss = Session.getActiveSession();
//            new Request(
//                    ss,
//                    user.getId(),
//                    null,
//                    HttpMethod.GET,
//                    new Request.Callback() {
//                        @Override
//                        public void onCompleted(Response response) {
//                            Log.d("RESPONSE ",""+response);
////                            return response;
//                        }
//                    }
//            ).executeAsync();
////            try {
////                //URL img_url = new URL("https://graph.facebook.com/oauth/access_token?client_id="+getString(R.string.app_id)+"&client_secret=YOUR_APP_SECRET&grant_type=client_credentials");
////                URL img_url = new URL("http://graph.facebook.com/azura.akbar/picture?type=normal");
////                bmp = BitmapFactory.decodeStream(img_url.openConnection().getInputStream());
////                Log.d("bitmap ",""+bmp);
////                return bmp;
////            } catch (IOException e) {
////                e.printStackTrace();
////            }
//            return null;
//        }
//        @Override
//        protected void onPostExecute(Response response) {
//            super.onPostExecute(response);
//        }
//    }
}
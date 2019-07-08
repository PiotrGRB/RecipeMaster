package com.example.recipemaster.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.recipemaster.R;
import com.example.recipemaster.Variables;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Arrays;

public class FacebookFragment extends Fragment {
    private static final String TAG = "FacebookFragment";

    private AsyncTask loadImageTask;
    private TextView tv_email;
    private TextView tv_name;
    private ImageView iv_picture;
    private LoginButton loginButton;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_facebook, container, false);

        tv_email = view.findViewById(R.id.fb_email_tv);
        tv_name = view.findViewById(R.id.fb_name_tv);
        iv_picture = view.findViewById(R.id.fb_picture_iv);

        loginButton = view.findViewById(R.id.login_button);
        loginButton.setPermissions(Arrays.asList("public_profile", "email"));

        loadImageTask = new DownloadImageTask(iv_picture);
       // LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"));
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null) {
            if (!accessToken.isExpired()) {
                loadUserProfile(accessToken);
            }
        }

        return view;
    }


    public void update(String name, String email, String url){
        tv_email.setText(email);
        tv_name.setText(name);
        loadImageTask.execute(url);
        saveToSharedPreferences(name, url);
    }

    public void logout(){
        tv_email.setText("");
        tv_name.setText(getString(R.string.fb_loggedOut));
        iv_picture.setImageResource(android.R.color.transparent);
        //cancel if still running
        loadImageTask.cancel(true);

        removeFromSharedPreferences();
    }

    AccessTokenTracker tokenTracker = new AccessTokenTracker(){
        @Override
        protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
            if(currentAccessToken == null || currentAccessToken.isExpired()){
                //logged out
                {
                    logout();
                }
            }else{
                loadUserProfile(currentAccessToken);
            }
        }

    };

    private void loadUserProfile(AccessToken newAccessToken){
        GraphRequest req = GraphRequest.newMeRequest(newAccessToken, new GraphRequest.GraphJSONObjectCallback(){
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try{
                    String first_name = object.getString("first_name");
                    String last_name = object.getString("last_name");
                    String email = object.getString("email");
                    String id = object.getString("id");
                    String picture = "https://graph.facebook.com/"+id+"/picture?type=large";

                    update( first_name+" "+last_name, email, picture);
                }
                catch(JSONException e){
                    Log.e(TAG, e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        Bundle params = new Bundle();
        params.putString("fields", "first_name, last_name, email, id");
        req.setParameters(params);
        req.executeAsync();
    }

    private void saveToSharedPreferences(String name, String picture) {
        SharedPreferences prefs = getContext().getSharedPreferences(Variables.Shared_Preferences, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Variables.SP_fbName, name);
        editor.putString(Variables.SP_fbPict, picture);

        editor.apply();
    }
    private void removeFromSharedPreferences() {
        SharedPreferences prefs = getContext().getSharedPreferences(Variables.Shared_Preferences, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(Variables.SP_fbName);
        editor.remove(Variables.SP_fbPict);

        editor.apply();
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        private static final String TAG = "DownloadImageTask";
        ImageView imageView;

        public DownloadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }
}
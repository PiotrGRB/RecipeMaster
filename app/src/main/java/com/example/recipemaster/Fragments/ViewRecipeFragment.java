package com.example.recipemaster.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.recipemaster.R;
import com.example.recipemaster.Recipe;
import com.example.recipemaster.Variables;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class ViewRecipeFragment extends Fragment {
    private static final String TAG = "ViewRecipeFragment";
    // File url to download
    private static final String file_url = "https://moodup.team/test/info.php";
    private String downloadedContent = "";

    private TextView title;
    private TextView description;
    private LinearLayout ingredientsList;
    private LinearLayout prepList;
    private LinearLayout imagesList;

    private TextView loggedAs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_viewrecipe, container, false);

        title = view.findViewById(R.id.tv_recipe_title);
        description = view.findViewById(R.id.tv_recipe_desc);
        ingredientsList = view.findViewById(R.id.ll_recipe_ingredientsList);
        prepList = view.findViewById(R.id.ll_recipe_prepList);
        imagesList = view.findViewById(R.id.ll_recipe_imageList);

        loggedAs = view.findViewById(R.id.tv_recipe_logged_as);

        return view;
    }

    public void downloadRecipe() {
        downloadedContent = "";
        ingredientsList.removeAllViews();
        prepList.removeAllViews();
        imagesList.removeAllViews();

        URL url = null;
        try {
            url = new URL(file_url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (url != null) {
            new getTextFromOnlineSource().execute(url);
        }
    }

    public void setLoggedAs(){
        SharedPreferences prefs = getContext().getSharedPreferences(Variables.Shared_Preferences , Context.MODE_PRIVATE);
        String name = prefs.getString(Variables.SP_fbName, getString(R.string.defaultName));
        String picUrl = prefs.getString(Variables.SP_fbPict, null);

        String loggedAsText = getString(R.string.tv_loggedAs);
        loggedAsText += " " + name;

        loggedAs.setText(loggedAsText);
        loggedAs.setCompoundDrawablePadding(16);
        loggedAs.setCompoundDrawables(null, null, null, null);
        if(picUrl != null){
            new DownloadImageTask(loggedAs).execute(picUrl);
        }
    }

    private void saveImageToExternalStorage(Bitmap finalBitmap, int i) {
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File myDir = new File(root + "/pizzaImages");
        if (!myDir.exists() || !myDir.isDirectory())
            myDir.mkdirs();

        String fname = "pizza" + i + ".jpg";
        File file = new File(myDir, fname);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            Toast.makeText(getContext(), getString(R.string.succes_saveImage), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }

        // Tell the media scanner about the new file so that it is
        // immediately available to the user.
        // No need to restart the device!
        MediaScannerConnection.scanFile(getContext(), new String[]{file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });

    }
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        private static final String TAG = "DownloadImageTask";
        TextView tv;

        public DownloadImageTask(TextView tv) {
            this.tv = tv;
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
            BitmapDrawable drawable = new BitmapDrawable(getContext().getResources(), result);
            drawable.setBounds(0, 0,
                    drawable.getIntrinsicWidth() * tv.getMeasuredHeight() / drawable.getIntrinsicHeight(),
                    tv.getMeasuredHeight());
            tv.setCompoundDrawables(null, null, drawable, null);
        }
    }

    private class getTextFromOnlineSource extends AsyncTask<URL, Void, Void> {
        @Override
        protected Void doInBackground(URL... urls) {
            int count = urls.length;
            for (int i = 0; i < count; i++) {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(urls[i].openStream()));
                    String line;
                    while ((line = in.readLine()) != null) {
                        downloadedContent += line;
                    }
                    in.close();
                } catch (MalformedURLException e) {
                    Log.e(TAG, e.getMessage());
                    e.printStackTrace();

                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            Gson gson = new Gson();
            Recipe rcp = gson.fromJson(downloadedContent, Recipe.class);

            String mytitle = rcp.getTitle() + ":";
            title.setText(mytitle);
            description.setText(rcp.getDescription());
            for (String s : rcp.getIngredients()) {
                TextView txt = new TextView(getContext());
                String text = "- " + s;
                txt.setText(text);
                ingredientsList.addView(txt);
            }

            List<String> prep = rcp.getPreparing();
            for (int i = 0; i < prep.size(); i++) {
                TextView txt = new TextView(getContext());
                String text = (i + 1) + ". " + prep.get(i);
                txt.setText(text);
                prepList.addView(txt);
            }

            List<String> imgs = rcp.getImgs();

           // ImageSize size = new ImageSize(imagesList.getWidth()/2, imagesList.getWidth()/2);
           // DisplayImageOptions options = new DisplayImageOptions.Builder().imageScaleType(ImageScaleType.EXACTLY_STRETCHED).build();

            for (int i = 0; i < imgs.size(); i++) {
                ImageView img = new ImageView(getContext());
                //id used for saving image later
                img.setId(i);

                ImageLoader imageLoader = ImageLoader.getInstance();
                imageLoader.displayImage(imgs.get(i), img);
                //Bitmap bm = imageLoader.loadImageSync(imgs.get(i), size, options);
                //img.setImageBitmap(bm);
                /*
                imageLoader.loadImage(imgs.get(i), size, options, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        img.setImageBitmap(loadedImage);
                    }
                });
                 */

                img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final View view = v;
                        new AlertDialog.Builder(getContext())
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle(getString(R.string.saveImageTitle))
                                .setMessage(getString(R.string.saveImageQuestion))
                                .setPositiveButton(getString(R.string.positive), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        BitmapDrawable drawable = (BitmapDrawable) ((ImageView) view).getDrawable();
                                        Bitmap bitmap = drawable.getBitmap();


                                        saveImageToExternalStorage(bitmap, view.getId());
                                    }
                                })
                                .setNegativeButton(getString(R.string.negative), null)
                                .show();
                    }
                });
                imagesList.addView(img);
            }
            super.onPostExecute(v);
        }
    }
}

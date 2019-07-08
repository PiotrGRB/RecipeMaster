package com.example.recipemaster;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.recipemaster.Fragments.FacebookFragment;
import com.example.recipemaster.Fragments.MainFragment;
import com.example.recipemaster.Fragments.ViewRecipeFragment;
import com.example.recipemaster.Views.NonSwipeViewPager;
import com.example.recipemaster.Views.SelectFragmentStatePagerAdapter;
import com.facebook.CallbackManager;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private Toolbar mToolbar;
    private SelectFragmentStatePagerAdapter mFragmentPagerAdapter;
    private ViewPager mViewPager;

    private FloatingActionMenu mFloatingActionMenu;
    private FloatingActionButton facebookFAB;
    private FloatingActionButton getRecipeFAB;

    public CallbackManager mCallbackManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Create global configuration and initialize ImageLoader with this config
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
			.build();
        ImageLoader.getInstance().init(config);

        setupPermissions();
        setupToolbar();
        //setup adapter for fragments
        mFragmentPagerAdapter = new SelectFragmentStatePagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        setupViewPager(mViewPager);
        setupButtons();

        setupFacebookLogin();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "optionsItemSelected launched for " + item.getItemId());
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if(mViewPager.getCurrentItem() == 0){
            super.onBackPressed();
        }else{
            mFloatingActionMenu.showMenu(false);
            toolbarBackArrowVisibility(false);
            setViewPager(0);
        }
    }

    private void setupToolbar(){
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
    }
    private void toolbarBackArrowVisibility(boolean bool){
        getSupportActionBar().setDisplayHomeAsUpEnabled(bool);
        getSupportActionBar().setDisplayShowHomeEnabled(bool);
    }

    private void setupViewPager(ViewPager viewPager){
        mFragmentPagerAdapter = new SelectFragmentStatePagerAdapter(getSupportFragmentManager());

        mFragmentPagerAdapter.addFragment(new MainFragment(), Variables.FRAGMENT_NAME_main);
        mFragmentPagerAdapter.addFragment(new ViewRecipeFragment(), Variables.FRAGMENT_NAME_viewRecipe);
        mFragmentPagerAdapter.addFragment(new FacebookFragment(), Variables.FRAGMENT_NAME_facebook);

        viewPager.setAdapter(mFragmentPagerAdapter);
        //disable swiping using custom ViewPager class
        ((NonSwipeViewPager)findViewById(R.id.viewPager)).setSwipeable(false);
    }

    private void setViewPager(int fragNum){
        mViewPager.setCurrentItem(fragNum);
    }

    private void setupButtons() {
        mFloatingActionMenu = (FloatingActionMenu) findViewById(R.id.menuFAB);
        facebookFAB = (FloatingActionButton) findViewById(R.id.facebookFAB);
        getRecipeFAB = (FloatingActionButton) findViewById(R.id.getRecipeFAB);

        getRecipeFAB.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                mFloatingActionMenu.close(false);
                mFloatingActionMenu.hideMenu(true);
                toolbarBackArrowVisibility(true);
                int index = mFragmentPagerAdapter.getFragmentByName(Variables.FRAGMENT_NAME_viewRecipe);

                ViewRecipeFragment fragment = (ViewRecipeFragment) mFragmentPagerAdapter.getItem
                        (mFragmentPagerAdapter.getFragmentByName(Variables.FRAGMENT_NAME_viewRecipe));
                fragment.downloadRecipe();
                fragment.setLoggedAs();

                setViewPager(index);
            }
        });
        facebookFAB.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                mFloatingActionMenu.close(false);
                mFloatingActionMenu.hideMenu(true);
                toolbarBackArrowVisibility(true);

                int index = mFragmentPagerAdapter.getFragmentByName(Variables.FRAGMENT_NAME_facebook);
                setViewPager(index);
            }
        });
    }


    private void setupFacebookLogin() {
        mCallbackManager = CallbackManager.Factory.create();
    }

    private void setupPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // no permission! gotta ask
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Variables.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Variables.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                } else {
                    Toast.makeText(this, getString(R.string.permissiondenied_toast), Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }
}

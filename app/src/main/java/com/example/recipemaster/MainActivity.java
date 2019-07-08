package com.example.recipemaster;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

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
}

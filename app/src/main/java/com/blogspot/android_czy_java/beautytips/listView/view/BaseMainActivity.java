package com.blogspot.android_czy_java.beautytips.listView.view;

import android.app.DialogFragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;

import com.blogspot.android_czy_java.beautytips.R;
import com.blogspot.android_czy_java.beautytips.appUtils.AnalyticsUtils;
import com.blogspot.android_czy_java.beautytips.appUtils.ExternalStoragePermissionHelper;
import com.blogspot.android_czy_java.beautytips.appUtils.NetworkConnectionHelper;
import com.blogspot.android_czy_java.beautytips.appUtils.SnackbarHelper;
import com.blogspot.android_czy_java.beautytips.listView.viewmodel.ListViewViewModel;
import com.blogspot.android_czy_java.beautytips.listView.firebase.FirebaseLoginHelper;
import com.blogspot.android_czy_java.beautytips.listView.utils.LanguageHelper;
import com.blogspot.android_czy_java.beautytips.listView.view.dialogs.NicknamePickerDialog;
import com.blogspot.android_czy_java.beautytips.notifications.NotificationTokenHelper;
import com.blogspot.android_czy_java.beautytips.welcome.WelcomeActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.navigation.NavigationView;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.blogspot.android_czy_java.beautytips.appUtils.ExternalStoragePermissionHelper.RC_PERMISSION_EXT_STORAGE;
import static com.blogspot.android_czy_java.beautytips.appUtils.ExternalStoragePermissionHelper.RC_PHOTO_PICKER;
import static com.blogspot.android_czy_java.beautytips.listView.model.User.USER_STATE_ANONYMOUS;
import static com.blogspot.android_czy_java.beautytips.listView.model.User.USER_STATE_NULL;
import static com.blogspot.android_czy_java.beautytips.listView.view.MyDrawerLayoutListener.CATEGORY_ALL;
import static com.blogspot.android_czy_java.beautytips.listView.view.MyDrawerLayoutListener.NAV_POSITION_ALL;
import static com.blogspot.android_czy_java.beautytips.listView.view.MyDrawerLayoutListener.NAV_POSITION_LOG_OUT;
import static com.blogspot.android_czy_java.beautytips.listView.view.MyDrawerLayoutListener.RC_NEW_TIP_ACTIVITY;
import static com.blogspot.android_czy_java.beautytips.newTip.view.NewTipActivity.KEY_TIP_NUMBER;
import static com.blogspot.android_czy_java.beautytips.newTip.view.NewTipActivity.RESULT_DATA_CHANGE;
import static com.blogspot.android_czy_java.beautytips.welcome.WelcomeActivity.RC_WELCOME_ACTIVITY;
import static com.blogspot.android_czy_java.beautytips.welcome.WelcomeActivity.RESULT_LOG_IN;
import static com.blogspot.android_czy_java.beautytips.welcome.WelcomeActivity.RESULT_LOG_IN_ANONYMOUSLY;
import static com.blogspot.android_czy_java.beautytips.welcome.WelcomeActivity.RESULT_TERMINATE;

public abstract class BaseMainActivity extends AppCompatActivity
        implements FirebaseLoginHelper.MainViewInterface,
        NicknamePickerDialog.NicknamePickerDialogListener,
        MyDrawerLayoutListener.DrawerCreationInterface {



    public static final String KEY_QUERY = "query";

    public static final String TAG_NICKNAME_DIALOG = "nickname_picker_dialog";
    public static final String TAG_DELETE_TIP_DIALOG = "delete_tip_dialog";


    public static final String KEY_TOKEN_FLAG = "notification_token";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    SearchView mSearchView;

    @BindView(R.id.nav_view)
    NavigationView mNavigationView;

    FirebaseLoginHelper mLoginHelper;

    View mHeaderLayout;
    CircleImageView photoIv;
    TextView nicknameTv;

    MyDrawerLayoutListener mDrawerListener;

    boolean isPhotoSaving;

    private DialogFragment mDialogFragment;

    ListViewViewModel viewModel;

    InterstitialAd interstitialAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LanguageHelper.setLanguageToEnglish(this);

        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        initViewModel();

        mLoginHelper = new FirebaseLoginHelper(this, viewModel);

        if (savedInstanceState == null) {
            MobileAds.initialize(this, getResources().getString(R.string.add_mob_app_id));
        }


        prepareInterstitialAd();

        //it has to be added here to avoid adding it multiple times. It doesn't have to be done on category change,
        //this is why it's not part of prepareNavigationDrawer()
        setListenerToNavigationView();

        viewModel.getCategoryLiveData().observe(this, category -> {
            prepareNavigationDrawer();
            prepareActionBar();
        });

        viewModel.getUserStateLiveData().observe(this, createUserStateObserver());

    }

    abstract void initViewModel();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_search);

        SearchManager searchManager = (SearchManager) BaseMainActivity.
                this.getSystemService(Context.SEARCH_SERVICE);

        if (searchItem != null) {
            mSearchView = (SearchView) searchItem.getActionView();
        }
        if (mSearchView != null && searchManager != null) {
            mSearchView.setSearchableInfo(searchManager.
                    getSearchableInfo(BaseMainActivity.this.getComponentName()));
            prepareSearchView();
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        mLoginHelper = null;
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mDialogFragment != null) {
            mDialogFragment.dismiss();
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {

        //the first and the last screen user sees is all tips to provide proper and predictable
        // navigation
        if (!viewModel.getCategory().equals(CATEGORY_ALL)) {
            viewModel.setNavigationPosition(NAV_POSITION_ALL);
            viewModel.setCategory(CATEGORY_ALL);
        } else super.onBackPressed();
    }

    @NonNull
    private Observer<String> createUserStateObserver() {
        return new Observer<String>() {
            @Override
            public void onChanged(@Nullable String userState) {
                MenuItem logOutItem = mNavigationView.getMenu().getItem(NAV_POSITION_LOG_OUT);
                if (userState.equals(USER_STATE_NULL)) {
                    Intent welcomeActivityIntent = new Intent(BaseMainActivity.this,
                            WelcomeActivity.class);
                    startActivityForResult(welcomeActivityIntent, RC_WELCOME_ACTIVITY);
                }
                //user just logged in anonymously
                else if (userState.equals(USER_STATE_ANONYMOUS)) {
                    logOutItem.setTitle(R.string.nav_log_in);
                    logOutItem.setIcon(R.drawable.ic_login);
                    photoIv.setOnClickListener(null);
                    photoIv.setImageDrawable(getResources().getDrawable(R.drawable.placeholder));
                    nicknameTv.setText(R.string.label_anonymous);
                }
                //user just logged in or logged in user opened the app
                else {
                    logOutItem.setTitle(R.string.nav_log_out);
                    logOutItem.setIcon(R.drawable.ic_logout);
                    mLoginHelper.prepareNavDrawerHeader();

                    //when user clicks photo button_background the photo chooser is opening
                    photoIv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (ExternalStoragePermissionHelper
                                    .isPermissionGranted(BaseMainActivity.this)) {
                                ExternalStoragePermissionHelper.showPhotoPicker(BaseMainActivity.this);
                            } else {
                                ExternalStoragePermissionHelper.askForPermission(
                                        BaseMainActivity.this);
                            }
                        }
                    });

                    if(!tokenWasSaved())
                    NotificationTokenHelper.saveUserNotificationToken();
                }
            }

        };
    }

    private boolean tokenWasSaved() {
        boolean tokenWaSaved = getPreferences(MODE_PRIVATE).getBoolean(KEY_TOKEN_FLAG, false);

        getPreferences(MODE_PRIVATE).edit().putBoolean(KEY_TOKEN_FLAG, true).apply();
        return tokenWaSaved;
    }


    private void prepareActionBar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }
    }

    /*
      here we set navigation drawer actions. There is apparently some bug in recreate(),
      because drawer is not closing but stays open on recreating, so here everything is surrounded
      by onDrawerClosed() from DrawerListener.
    */
    private void prepareNavigationDrawer() {

        mHeaderLayout = mNavigationView.getHeaderView(0);
        photoIv = mHeaderLayout.findViewById(R.id.nav_photo);
        nicknameTv = mHeaderLayout.findViewById(R.id.nav_nickname);


        //make all items in menu unchecked and then check the one that was selected recently
        for (int i = 0; i < mNavigationView.getMenu().size(); i++) {
            mNavigationView.getMenu().getItem(i).setChecked(false);
        }
        mNavigationView.getMenu().getItem(viewModel.getNavigationPosition()).setChecked(true);
    }

    private void setListenerToNavigationView() {
        mNavigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
                        mDrawerLayout.closeDrawers();


                        /*
                        Here we need mDrawerListener to properly close NavigationDrawer when calling
                        recreate(), this is why DrawerListener is added and inside it selection of item
                        is handled.
                         */
                        mDrawerLayout.removeDrawerListener(mDrawerListener);
                        mDrawerListener = new MyDrawerLayoutListener(BaseMainActivity.this, viewModel,
                                item.getItemId());
                        mDrawerLayout.addDrawerListener(mDrawerListener);

                        return true;
                    }
                }
        );
    }

    void prepareSearchView() {

        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                //on close search view, load all the data from chosen category
                mSearchView.clearFocus();
                if (viewModel.getSearchWasConducted()) viewModel.resetSearch();

                return false;
            }
        });


        //when the user submit search, pass it to viewModel
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchView.clearFocus();
                viewModel.search(query);

                AnalyticsUtils.logEventSearch(BaseMainActivity.this, query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }

        });

        if (viewModel.getSearchWasConducted()) {
            mSearchView.setIconified(false);
            mSearchView.setQuery(viewModel.getQuery(), true);
        }

        //if this activity was opened from ingredient activity, make query for ingredient
        if (getIntent() != null && getIntent().getAction() != null &&
                getIntent().getAction().equals(Intent.ACTION_SEARCH)) {
            String query = (getIntent().getStringExtra(KEY_QUERY));
            mSearchView.setIconified(false);
            mSearchView.setQuery(query, true);
            getIntent().setAction(null);

        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mDrawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //This method is called after user signed in or canceled signing in (depending on result code)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_WELCOME_ACTIVITY) {
            if (resultCode == RESULT_TERMINATE) {
                onBackPressed();
            } else if (resultCode == RESULT_LOG_IN) {
                mLoginHelper.showSignInScreen();
            } else if (resultCode == RESULT_LOG_IN_ANONYMOUSLY) {
                mLoginHelper.signInAnonymously();
            }
        }

        if (requestCode == FirebaseLoginHelper.RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                mLoginHelper.signIn();
                showPickNicknameDialog();
                //reload data
                viewModel.notifyRecyclerDataHasChanged();
            } else {
                //if response is null the user canceled sign in flow using back button, so we sign
                //him anonymously.
                if (response == null) {
                    mLoginHelper.signInAnonymously();
                }
            }
        }
        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri photoUri = data.getData();
            if (photoUri != null) {
                if (!NetworkConnectionHelper.isInternetConnection(this)) {
                    SnackbarHelper.showUnableToAddImage(mDrawerLayout);
                } else {
                    Glide.with(this)
                            .load(R.drawable.placeholder)
                            .into(photoIv);
                    SnackbarHelper.showAddImageMayTakeSomeTime(mDrawerLayout);
                }
                if (isPhotoSaving) {
                    mLoginHelper.stopPreviousUserPhotoSaving();
                }
                mLoginHelper.saveUserPhoto(photoUri);
                isPhotoSaving = true;
            }
        }

        if (requestCode == RC_NEW_TIP_ACTIVITY && resultCode == RESULT_DATA_CHANGE) {
            viewModel.waitForAddingImage(data.getStringExtra(KEY_TIP_NUMBER));
            viewModel.setCategory(CATEGORY_ALL);
            SnackbarHelper.showNewTipVisibleSoon(mDrawerLayout);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == RC_PERMISSION_EXT_STORAGE) {
            ExternalStoragePermissionHelper.answerForPermissionResult(this, grantResults,
                    mDrawerLayout);
        }
    }

    private void prepareInterstitialAd() {
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
        interstitialAd.loadAd(new AdRequest.Builder().build());

        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                interstitialAd.loadAd(new AdRequest.Builder().build());
            }

        });
    }


   /*
       Here is the beginning of interfaces methods
     */



    /*
       implementation of MyDrawerLayoutListener.DrawerCreationInterface
     */

    @Override
    public void logOut() {
        mLoginHelper.logOut();
    }

    @Override
    public void removeDrawerListenerFromDrawerLayout() {
        mDrawerLayout.removeDrawerListener(mDrawerListener);
    }

    //This is called by navigation drawer when clicking on "log in"
    @Override
    public void signInAnonymousUser() {
        mLoginHelper.showSignInScreen();
    }

    @Override
    public DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }

    @Override
    public Context getContext() {
        return this;
    }



    /*
      Implementation of FirebaseLoginHelper.MainViewInterface
     */

    @Override
    public void setNickname(String nickname) {
        nicknameTv.setText(nickname);
    }

    @Override
    public void setUserPhoto(String imageUri) {

        if(!isDestroyed())
        Glide.with(this)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.color.bluegray700))
                .load(imageUri)
                .into(photoIv);
    }

    @Override
    public void setIsPhotoSaving(boolean isPhotoSaving) {
        this.isPhotoSaving = isPhotoSaving;
    }

    @Override
    public void showPickNicknameDialog() {
        mDialogFragment = new NicknamePickerDialog();
        mDialogFragment.show(getFragmentManager(), TAG_NICKNAME_DIALOG);
    }

    /*
       Implementation of NicknamePickerDialog.NicknamePickerDialogListener
     */
    @Override
    public void onDialogSaveButtonClick(String nickname) {
        mLoginHelper.saveNickname(nickname);
        setNickname(nickname);
    }

    /*
        End of interfaces
     */

}

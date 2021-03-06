package com.blogspot.android_czy_java.beautytips.newTip.view;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Handler;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.blogspot.android_czy_java.beautytips.R;
import com.blogspot.android_czy_java.beautytips.appUtils.ExternalStoragePermissionHelper;
import com.blogspot.android_czy_java.beautytips.appUtils.SnackbarHelper;
import com.blogspot.android_czy_java.beautytips.appUtils.NetworkConnectionHelper;
import com.blogspot.android_czy_java.beautytips.newTip.firebase.NewTipFirebaseHelper;
import com.blogspot.android_czy_java.beautytips.newTip.view.dialog.ConfirmationDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.farbod.labelledspinner.LabelledSpinner;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

import static com.blogspot.android_czy_java.beautytips.appUtils.ExternalStoragePermissionHelper.RC_PERMISSION_EXT_STORAGE;

public class NewTipActivity extends AppCompatActivity implements NewTipFirebaseHelper.NewTipViewInterface,
        ConfirmationDialog.ConfirmationDialogListener {


    public static final int RESULT_DATA_CHANGE = 10;

    private static final int RC_PHOTO_PICKER = 100;
    public static final String TAG_CONF_DIALOG = "confirmation_dialog";

    public static final String KEY_IMAGE_PATH = "image_path";
    public static final String KEY_CATEGORY = "category";

    public static final String CATEGORY_HAIR = "hair";
    public static final String CATEGORY_FACE = "face";
    public static final String CATEGORY_BODY = "body";

    public static final String KEY_TIP_NUMBER = "tip_number";


    @BindView(R.id.linear_layout)
    LinearLayout mNewTipLayout;

    @BindView(R.id.app_bar)
    Toolbar mToolbar;

    @BindView(R.id.nickname_text_view)
    TextView mNicknameTv;

    @BindView(R.id.author_photo)
    CircleImageView mAuthorPhotoIv;

    @BindView(R.id.category_spinner)
    LabelledSpinner mCategorySpinner;

    @BindView(R.id.image)
    ImageView mImageView;

    @BindView(R.id.title_edit_text)
    EditText mTitleEt;

    @BindView(R.id.ingredient1)
    EditText mIngredient1Et;

    @BindView(R.id.ingredient2)
    EditText mIngredient2Et;

    @BindView(R.id.ingredient3)
    EditText mIngredient3Et;

    @BindView(R.id.ingredient4)
    EditText mIngredient4Et;

    @BindView(R.id.description_edit_text)
    EditText mDescriptionEt;

    @BindView(R.id.source_edit_text)
    EditText mSourceEt;

    @BindView(R.id.subcategory_spinner)
    LabelledSpinner mSubcategorySpinner;

    private NewTipFirebaseHelper mFirebaseHelper;
    private int category;
    private String imagePath;
    private String title;
    private ArrayList<String> ingredients;
    private String description;
    private String newTipNum;
    List<String> subcategories;
    private int subcategoryPosition;

    private int numGeneratingTries;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_tip);

        //on tablet this activity is a dialog - set appropriate width
        if(getResources().getBoolean(R.bool.is_tablet)) {
            int width;
            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                width = getResources().getDisplayMetrics().widthPixels * 2 / 3;
                getWindow().setLayout(width,
                        getResources().getDisplayMetrics().heightPixels);
                this.setFinishOnTouchOutside(false);
            }
        }


        ButterKnife.bind(this);

        overridePendingTransition(R.anim.bottom_to_top, R.anim.fade_out);

        if(savedInstanceState != null) {
            category = savedInstanceState.getInt(KEY_CATEGORY);
            imagePath = savedInstanceState.getString(KEY_IMAGE_PATH, "");
        }

        mFirebaseHelper = new NewTipFirebaseHelper(this);

        prepareToolbar();
        prepareAuthorDesc();
        prepareSpinners();
        prepareImageView();

    }

    /*
      On rotation we have to save chosen category in spinner and local path to chosen tip image.
      Texts from EditTexts are saved automatically.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_CATEGORY, category);
        if(imagePath != null) {
            outState.putString(KEY_IMAGE_PATH, imagePath);
        }
        super.onSaveInstanceState(outState);
    }

    private void prepareToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ripple_back);
        }
    }

    private void prepareAuthorDesc() {
        mFirebaseHelper.setAuthorDetails();
    }

    private void prepareSpinners() {
        mCategorySpinner.setItemsArray(R.array.categories);
        mCategorySpinner.setSelection(category);
        mCategorySpinner.setOnItemChosenListener(new LabelledSpinner.OnItemChosenListener() {
            @Override
            public void onItemChosen(View labelledSpinner, AdapterView<?> adapterView,
                                     View itemView, int position, long id) {
                category = position;
                if(position == 0) subcategories = new ArrayList<>(Arrays.asList(getResources().
                        getStringArray(R.array.hair_chip_labels)));
                if(position == 1) subcategories = new ArrayList<>(Arrays.asList(getResources().
                        getStringArray(R.array.face_chip_labels)));
                if(position == 2) subcategories = new ArrayList<>(Arrays.asList(getResources().
                        getStringArray(R.array.body_chip_labels)));
                subcategories.remove(0);
                mSubcategorySpinner.setItemsArray(subcategories);
                mSubcategorySpinner.setSelection(subcategories.size()-1);
            }

            @Override
            public void onNothingChosen(View labelledSpinner, AdapterView<?> adapterView) {
            }
        });

        mSubcategorySpinner.setOnItemChosenListener(new LabelledSpinner.OnItemChosenListener() {
            @Override
            public void onItemChosen(View labelledSpinner, AdapterView<?> adapterView, View itemView,
                                     int position, long id) {
                subcategoryPosition = position;
            }

            @Override
            public void onNothingChosen(View labelledSpinner, AdapterView<?> adapterView) {

            }
        });
    }

    private void prepareImageView() {
        if(!TextUtils.isEmpty(imagePath)) {
            loadImageWithGlide();
        }

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ExternalStoragePermissionHelper.isPermissionGranted(NewTipActivity.this)) {
                    ExternalStoragePermissionHelper.showPhotoPicker(NewTipActivity.this);
                } else {
                    ExternalStoragePermissionHelper.askForPermission(NewTipActivity.this);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == android.R.id.home) {
            finishWithTransition();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishWithTransition();
    }

    private void finishWithTransition() {
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.top_to_bottom);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri photoUri = data.getData();
            if (photoUri != null) {
                imagePath = photoUri.toString();
                loadImageWithGlide();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if(requestCode == RC_PERMISSION_EXT_STORAGE) {
            ExternalStoragePermissionHelper.answerForPermissionResult(this, grantResults,
                    mNewTipLayout);
        }
    }

    private void loadImageWithGlide() {
        Glide.with(this)
                .setDefaultRequestOptions(RequestOptions.centerCropTransform())
                .load(imagePath)
                .into(mImageView);
    }

    @Override
    public void setAuthorPhoto(String url) {
        Glide.with(NewTipActivity.this)
                .load(url)
                .into(mAuthorPhotoIv);
    }

    @Override
    public void setAuthorNickname(String nickname) {
        mNicknameTv.setText(nickname);
    }

    @Override
    public LinearLayout getLayout() {
        return mNewTipLayout;
    }

    //method to be called when "add tip" button is clicked
    public void addTip(View view) {
        if(!NetworkConnectionHelper.isInternetConnection(this)) {
            SnackbarHelper.showUnableToAddTip(mNewTipLayout);
        } else {
            title = mTitleEt.getText().toString();
            if(TextUtils.isEmpty(title)) {
                SnackbarHelper.showCannotBeEmpty(mNewTipLayout,
                        getResources().getString(R.string.element_title));
                return;
            }

            String ingredient1 = mIngredient1Et.getText().toString();
            String ingredient2 = mIngredient2Et.getText().toString();
            String ingredient3 = mIngredient3Et.getText().toString();
            String ingredient4 = mIngredient4Et.getText().toString();
            ingredients = new ArrayList<>();
            if(!TextUtils.isEmpty(ingredient1)) ingredients.add(ingredient1);
            if(!TextUtils.isEmpty(ingredient2)) ingredients.add(ingredient2);
            if(!TextUtils.isEmpty(ingredient3)) ingredients.add(ingredient3);
            if(!TextUtils.isEmpty(ingredient4)) ingredients.add(ingredient4);
            if(ingredients.isEmpty()) {
                SnackbarHelper.showCannotBeEmpty(mNewTipLayout,
                        getResources().getString(R.string.element_ingredient_list));
                return;
            }

            description = mDescriptionEt.getText().toString();
            if(TextUtils.isEmpty(description)) {
                SnackbarHelper.showCannotBeEmpty(mNewTipLayout,
                        getResources().getString(R.string.element_description));
                return;
            }

            if(TextUtils.isEmpty(imagePath)) {
                SnackbarHelper.showImageCannotBeEmpty(mNewTipLayout);
                return;
            }

            new ConfirmationDialog().show(getFragmentManager(), TAG_CONF_DIALOG);
        }
    }

    private String getCategory() {
        switch (category){
            case 0: return CATEGORY_HAIR;
            case 1: return CATEGORY_FACE;
            case 2: return CATEGORY_BODY;
        }
        return CATEGORY_HAIR;
    }

    private String getSubcategory() {
        return subcategories.get(subcategoryPosition).toLowerCase();
    }

    public void setTipNumber(String tipNumber) {
        newTipNum = tipNumber;
    }

    /*
      ConfirmationDialog.ConfirmationDialogListener interface method
     */

    @Override
    public void onDialogSaveButtonClick() {

        mFirebaseHelper.generateNewTipNum();
        saveDataWhenNumIsGenerated();

    }

    private void saveDataWhenNumIsGenerated() {
        new Handler().postDelayed(() -> {
            if(newTipNum != null && !TextUtils.isEmpty(newTipNum)) {
                Intent data = new Intent();
                data.putExtra(KEY_TIP_NUMBER, newTipNum);
                setResult(RESULT_DATA_CHANGE, data);
                String source = mSourceEt.getText().toString();
                mFirebaseHelper.addTip(title, ingredients, description, getCategory(),
                        getSubcategory(), imagePath, source, newTipNum);
                finishWithTransition();
            } else {
                if(numGeneratingTries > 10) {
                    setResult(RESULT_CANCELED);
                    showSnackbar();
                    finishWithTransition();
                }
                saveDataWhenNumIsGenerated();
                numGeneratingTries++;
            }
        }, 50);
    }

    private void showSnackbar() {
        Snackbar snackbar = Snackbar.make(mToolbar, R.string.message_add_tip_error_try_again,
                Snackbar.LENGTH_LONG).setAction(R.string.label_action_try_again,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        numGeneratingTries = 0;
                        onDialogSaveButtonClick();
                    }
                })
                .setActionTextColor(getResources().getColor(R.color.pink200));
        snackbar.getView().setBackgroundColor(getResources().getColor(R.color.bluegray900));
        snackbar.show();
    }

}

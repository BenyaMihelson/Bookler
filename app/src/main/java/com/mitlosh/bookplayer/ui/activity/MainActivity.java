package com.mitlosh.bookplayer.ui.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.mitlosh.bookplayer.App;
import com.mitlosh.bookplayer.Constants;
import com.mitlosh.bookplayer.R;
import com.mitlosh.bookplayer.audio.AudioServiceManager;
import com.mitlosh.bookplayer.billing.SubscribeManager;
import com.mitlosh.bookplayer.model.Album;
import com.mitlosh.bookplayer.model.Track;
import com.mitlosh.bookplayer.network.RestAPI;
import com.mitlosh.bookplayer.network.response.AlbumContent;
import com.mitlosh.bookplayer.network.response.AlbumList;
import com.mitlosh.bookplayer.network.response.AlbumStat;
import com.mitlosh.bookplayer.network.response.ApiResponse;
import com.mitlosh.bookplayer.network.response.BaseData;
import com.mitlosh.bookplayer.network.response.CategoryList;
import com.mitlosh.bookplayer.network.response.SubscribeResponse;
import com.mitlosh.bookplayer.ui.fragment.AlbumContentFragment;
import com.mitlosh.bookplayer.ui.fragment.AlbumListFragment;
import com.mitlosh.bookplayer.ui.fragment.BaseListFragment;
import com.mitlosh.bookplayer.ui.fragment.CategoriesFragment;
import com.mitlosh.bookplayer.ui.fragment.CategoryAlbumsFragment;
import com.mitlosh.bookplayer.ui.fragment.HistoryFragment;
import com.mitlosh.bookplayer.ui.fragment.InDepelopFragment;
import com.mitlosh.bookplayer.ui.fragment.SupportFragment;
import com.mitlosh.bookplayer.utils.PrefUtils;
import com.mitlosh.bookplayer.utils.Utils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        AlbumListFragment.AlbumListFI, AlbumContentFragment.AlbumContentFI,
        CategoriesFragment.CategoriesFI, CategoryAlbumsFragment.CategoryAlbumsFI,
        SupportFragment.SupportFI {

    private static final int LOAD_COUNT = 10;
    private static final String TAG = "MainActivity";

    @Inject
    RestAPI restAPI;
    @Inject
    PrefUtils prefUtils;
    @Inject
    AudioServiceManager audioServiceManager;

    private NavigationView navigationView;
    private ProgressDialog progress;
    private SubscribeManager subcrMan;

    /**
     * The {@link Tracker} used to record screen views.
     */
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        App.getAppComponent().inject(this);

        mTracker = new App().getDefaultTracker();





        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //select menu item on start app
        selectMenuItem(getStartItemId());

        ((TextView) navigationView.getHeaderView(0).findViewById(R.id.tv_nav_phone)).setText(prefUtils.getPhone());

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

    }

    private void selectMenuItem(int id) {
        navigationView.setCheckedItem(id);
        navigationView.getMenu().performIdentifierAction(id, 0);
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Menu click")
                .build());
    }

    protected int getStartItemId() {
        return R.id.nav_main;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if(getSupportFragmentManager().getBackStackEntryCount() > 0){
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()){
            case R.id.nav_main:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new AlbumListFragment(), AlbumListFragment.TAG).commit();
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory(AlbumListFragment.TAG)
                        .build());
                break;
            case R.id.nav_cat:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new CategoriesFragment(), CategoriesFragment.TAG).commit();
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory(CategoriesFragment.TAG)
                        .build());
                break;
            case R.id.nav_history:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new HistoryFragment(), HistoryFragment.TAG).commit();
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory(HistoryFragment.TAG)
                        .build());
                break;
            case R.id.nav_support:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new SupportFragment(), SupportFragment.TAG)
                        .addToBackStack(null)
                        .commit();
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory(SupportFragment.TAG)
                        .build());
                break;
            case R.id.nav_subscribe:
                subscribe();
                break;
            case R.id.nav_share:
                shareApp();
                break;
            case R.id.nav_news:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, InDepelopFragment.newInstance(getString(R.string.news)), InDepelopFragment.TAG)
                        .addToBackStack(null)
                        .commit();
                break;
            case R.id.nav_settings:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, InDepelopFragment.newInstance(getString(R.string.settings)), InDepelopFragment.TAG)
                        .addToBackStack(null)
                        .commit();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void getAlbumList(final int offset) {
        final List<Album> history = prefUtils.getHistory();
        final int needNew = history.size() == 0 ? 1 : 0;
        restAPI.getAlbums(offset, LOAD_COUNT, needNew, prefUtils.getAuthToken()).enqueue(new Callback<ApiResponse<AlbumList>>() {

            @Override
            public void onResponse(Call<ApiResponse<AlbumList>> call, Response<ApiResponse<AlbumList>> response) {
                AlbumListFragment albumListFragment = getAlbumListFragment();
                if(albumListFragment != null){
                    albumListFragment.showListProgress(false);
                    if(handleResponse(response, response.body(), albumListFragment)){
                        AlbumList res = response.body().getData();
                        boolean hasMore = res.getAlbums().size() == LOAD_COUNT;
                        albumListFragment.updateList(res.getAlbums(), needNew == 1 ? res.getNewAlbums() : history, hasMore, offset == 0);
                        checkFreeSubscription();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AlbumList>> call, Throwable t) {
                AlbumListFragment albumListFragment = getAlbumListFragment();
                if(albumListFragment != null){
                    albumListFragment.showListProgress(false);
                    String error = handleFailure(t);
                    albumListFragment.setEmptyText(getString(R.string.error_and_msg, error));
                    showErrorMessage(error);
                }
            }
        });
    }

    @Override
    public void search(String query, final int offset) {
        restAPI.search(query, offset, LOAD_COUNT, prefUtils.getAuthToken()).enqueue(new Callback<ApiResponse<AlbumList>>() {

            @Override
            public void onResponse(Call<ApiResponse<AlbumList>> call, Response<ApiResponse<AlbumList>> response) {
                AlbumListFragment albumListFragment = getAlbumListFragment();
                if(albumListFragment != null){
                    albumListFragment.showListProgress(false);
                    if(handleResponse(response, response.body(), albumListFragment)){
                        AlbumList res = response.body().getData();
                        boolean hasMore = res.getAlbums().size() == LOAD_COUNT;
                        albumListFragment.updateList(res.getAlbums(), null, hasMore, offset == 0);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AlbumList>> call, Throwable t) {
                AlbumListFragment albumListFragment = getAlbumListFragment();
                if(albumListFragment != null){
                    albumListFragment.showListProgress(false);
                    String error = handleFailure(t);
                    albumListFragment.setEmptyText(getString(R.string.error_and_msg, error));
                    showErrorMessage(error);
                }
            }
        });
    }

    @Override
    public void getAlbumContent(int albumId) {
        restAPI.getContent(albumId, prefUtils.getAuthToken()).enqueue(new Callback<ApiResponse<AlbumContent>>() {

            @Override
            public void onResponse(Call<ApiResponse<AlbumContent>> call, Response<ApiResponse<AlbumContent>> response) {
                AlbumContentFragment albumContentFragment = getAlbumContentFragment();
                if(albumContentFragment != null){
                    albumContentFragment.showProgress(false);
                    String error = null;
                    if (response.isSuccessful()) {
                        if(response.body().isSuccess()){
                            albumContentFragment.onLoadAlbumContent(response.body().getData());
                            if(response.headers().get("Warning") != null){
                                Toast.makeText(getApplicationContext(), R.string.stale_warning, Toast.LENGTH_LONG).show();
                            }
                        }else{
                            error = response.body().getData().getMessage();
                        }
                    } else {
                        error = response.message();
                    }
                    if(error != null){
                        showErrorMessage(error);
                        albumContentFragment.setEmptyText(getString(R.string.error_and_msg, error));
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AlbumContent>> call, Throwable t) {
                AlbumContentFragment albumContentFragment = getAlbumContentFragment();
                if(albumContentFragment != null){
                    albumContentFragment.showProgress(false);
                    String error = handleFailure(t);
                    albumContentFragment.setEmptyText(getString(R.string.error_and_msg, error));
                    showErrorMessage(error);
                }
            }

        });
    }

    @Override
    public void getCategories() {
        restAPI.getCategories(prefUtils.getAuthToken()).enqueue(new Callback<ApiResponse<CategoryList>>() {

            @Override
            public void onResponse(Call<ApiResponse<CategoryList>> call, Response<ApiResponse<CategoryList>> response) {
                CategoriesFragment categoriesFragment = getCategoriesFragment();
                if(categoriesFragment != null){
                    categoriesFragment.showListProgress(false);
                    if(handleResponse(response, response.body(), categoriesFragment)){
                        categoriesFragment.onLoadCategories(response.body().getData().getCategories());
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<CategoryList>> call, Throwable t) {
                CategoriesFragment categoriesFragment = getCategoriesFragment();
                if(categoriesFragment != null){
                    categoriesFragment.showListProgress(false);
                    String error = handleFailure(t);
                    categoriesFragment.setEmptyText(getString(R.string.error_and_msg, error));
                    showErrorMessage(error);
                }
            }

        });
    }

    @Override
    public void getAlbumsByCategory(int categoryId, final int offset) {
        restAPI.getAlbumsByCategory(categoryId, offset, LOAD_COUNT, prefUtils.getAuthToken()).enqueue(new Callback<ApiResponse<AlbumList>>() {

            @Override
            public void onResponse(Call<ApiResponse<AlbumList>> call, Response<ApiResponse<AlbumList>> response) {
                CategoryAlbumsFragment categoryAlbumsFragment = getCategoryAlbumsFragment();
                if(categoryAlbumsFragment != null){
                    categoryAlbumsFragment.showListProgress(false);
                    if(handleResponse(response, response.body(), categoryAlbumsFragment)){
                        AlbumList res = response.body().getData();
                        boolean hasMore = res.getAlbums().size() == LOAD_COUNT;
                        categoryAlbumsFragment.updateList(res.getAlbums(), hasMore, offset == 0);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AlbumList>> call, Throwable t) {
                CategoryAlbumsFragment categoryAlbumsFragment = getCategoryAlbumsFragment();
                if(categoryAlbumsFragment != null){
                    categoryAlbumsFragment.showListProgress(false);
                    String error = handleFailure(t);
                    categoryAlbumsFragment.setEmptyText(getString(R.string.error_and_msg, error));
                    showErrorMessage(error);
                }
            }
        });
    }

    private void subscribe() {
        subcrMan = new SubscribeManager(this);
        subcrMan.subscribe(new SubscribeManager.OnSubscribeResultListener() {
            @Override
            public void onSubscribeSuccess() {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new AlbumListFragment(), AlbumListFragment.TAG).commit();
            }
        });
    }

    private void checkFreeSubscription() {
        restAPI.checkSubscribe(prefUtils.getAuthToken()).enqueue(new Callback<ApiResponse<SubscribeResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<SubscribeResponse>> call, Response<ApiResponse<SubscribeResponse>> response) {
                if(response.isSuccessful() && response.body().isSuccess()){
                    SubscribeResponse subscr = response.body().getData();
                    String message = null;
                    if(prefUtils.isFreeSubscrFirstTime()){
                        if(subscr.getFree_expired() != 0){
                            message = getString(R.string.free_greeting, Utils.getTimeOfDay(MainActivity.this), subscr.getFreeExpiredString());
                        }else{
                            message = getString(R.string.free_greeting_unlim, Utils.getTimeOfDay(MainActivity.this));
                        }
                    }else if(prefUtils.isFreeSubscrExtended(subscr.getFree_expired())){
                        if(subscr.getFree_expired() != 0){
                            message = getString(R.string.free_greeting_ext, Utils.getTimeOfDay(MainActivity.this), subscr.getFreeExpiredString());
                        }else{
                            message = getString(R.string.free_greeting_unlim, Utils.getTimeOfDay(MainActivity.this));
                        }
                    }
                    if(message != null){
                        prefUtils.setFreeSubscrExpired(subscr.getFree_expired());
                        showMessage(message);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<SubscribeResponse>> call, Throwable t) {}
        });
    }

    @Override
    public void sendFeedback(String email, String message) {
        restAPI.sendFeedback(email, message, prefUtils.getAuthToken()).enqueue(new Callback<ApiResponse<BaseData>>() {

            @Override
            public void onResponse(Call<ApiResponse<BaseData>> call, Response<ApiResponse<BaseData>> response) {
                SupportFragment supportFragment = getSupportFragment();
                if(supportFragment != null){
                    String error = null;
                    if (response.isSuccessful()) {
                        if(response.body().isSuccess()){
                            supportFragment.onSendSuccess();
                        }else{
                            error = response.body().getData().getMessage();
                        }
                    } else {
                        error = response.message();
                    }
                    if(error != null){
                        supportFragment.onSendFail(getString(R.string.error_and_msg, error));
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<BaseData>> call, Throwable t) {
                SupportFragment supportFragment = getSupportFragment();
                if(supportFragment != null){
                    String error = handleFailure(t);
                    supportFragment.onSendFail(getString(R.string.error_and_msg, error));
                }
            }

        });
    }

    @Override
    public void addStatistic(int albumId) {
        restAPI.addStatistic(prefUtils.getAuthToken(), albumId).enqueue(new Callback<ApiResponse<AlbumStat>>() {
            @Override
            public void onResponse(Call<ApiResponse<AlbumStat>> call, Response<ApiResponse<AlbumStat>> response) {
                if(response.isSuccessful() && response.body().isSuccess()){
                    Log.i(TAG, "isSuccessful");
                }

            }

            @Override
            public void onFailure(Call<ApiResponse<AlbumStat>> call, Throwable t) {

            }
        });

    }

    @Override
    public void playContent(Album album, List<Track> content, int position) {
        audioServiceManager.playContent(album, content, position);

    }

    @Override
    public void shareApp() {
        startActivity(Utils.createShareAppIntent(getApplicationContext()));
    }


    private Album sharedAlbum;
    private Target shareTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            showProgress(false);
            try {
                File folder = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + Constants.TMP_FOLDER);
                folder.mkdirs();
                File file = new File(folder, "album_" + sharedAlbum.getId() + ".jpg");
                FileOutputStream ostream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                ostream.close();

                Uri uri = Uri.fromFile(file);
                Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                shareIntent.setType("image/*");
                String text = sharedAlbum.getTitle() + ". " + sharedAlbum.getAuthor() + "\n" + sharedAlbum.getDescription() + "\n" + Utils.getPlayMarketLink(getApplicationContext());
                shareIntent.putExtra(Intent.EXTRA_TEXT, text);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
            } catch (Exception e) {
                showErrorMessage(getString(R.string.error_share_album));
                e.printStackTrace();
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            showProgress(false);
            showErrorMessage(getString(R.string.error_load_album_image));
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {}
    };

    @Override
    public void shareAlbum(final Album album) {
        Log.d(TAG, "shareAlbum image="+album.getImage());
        sharedAlbum = album;
        showProgress(true);
        Picasso.with(getApplicationContext()).load(album.getImage()).into(shareTarget);
    }



    private AlbumListFragment getAlbumListFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(AlbumListFragment.TAG);
        if(fragment != null){
            return (AlbumListFragment) fragment;
        }
        return null;
    }

    private AlbumContentFragment getAlbumContentFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(AlbumContentFragment.TAG);
        if(fragment != null){
            return (AlbumContentFragment) fragment;
        }
        return null;
    }

    private CategoriesFragment getCategoriesFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(CategoriesFragment.TAG);
        if(fragment != null){
            return (CategoriesFragment) fragment;
        }
        return null;
    }

    private CategoryAlbumsFragment getCategoryAlbumsFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(CategoryAlbumsFragment.TAG);
        if(fragment != null){
            return (CategoryAlbumsFragment) fragment;
        }
        return null;
    }

    private SupportFragment getSupportFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(SupportFragment.TAG);
        if(fragment != null){
            return (SupportFragment) fragment;
        }
        return null;
    }

    private boolean handleResponse(Response response, ApiResponse body) {
        return handleResponse(response, body, null);
    }

    private boolean handleResponse(Response response, ApiResponse body, BaseListFragment listFragment) {
        String error;
        int code = 0;
        if (response.isSuccessful()) {
            if(body.isSuccess()){
                if(response.headers().get("Warning") != null){
                    Toast.makeText(getApplicationContext(), R.string.stale_warning, Toast.LENGTH_LONG).show();
                }
                return true;
            }else{
                error = body.getData().getMessage();
                code = body.getData().getCode();
            }
        } else {
            error = response.message();
        }
        if(error != null){
            if(code == BaseData.CODE_SUBSCRIPTION_OVER){
                showSubscriptionOverMessage(error);
            }else{
                showErrorMessage(error);
            }
            if(listFragment != null) listFragment.setEmptyText(getString(R.string.error_and_msg, error));
        }
        return false;
    }

    protected String handleFailure(Throwable t) {
        String error;
        if (t instanceof IOException){
            error = getString(R.string.error_connect) ;
        }else{
            error = t.getMessage();
        }
        return error;
    }

    protected void showMessage(String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void showSubscriptionOverMessage(String message) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.error_title)
                .setMessage(message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.subscribe, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        subscribe();
                    }
                })
                .show();
    }

    protected void showErrorMessage(String error) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.error_title)
                .setMessage(error)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void showProgress(boolean show) {
        if(show){
            progress = ProgressDialog.show(this, null, null);
        }else {
            if (progress != null) {
                progress.dismiss();
                progress = null;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == SubscribeManager.REQ_CODE && subcrMan != null){
            subcrMan.onActivityResult(requestCode, resultCode, data);
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        audioServiceManager.onStart();
    }

    @Override
    protected void onStop() {
        audioServiceManager.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if(subcrMan != null){
            subcrMan.dispose();
            subcrMan = null;
        }
        super.onDestroy();
    }
}

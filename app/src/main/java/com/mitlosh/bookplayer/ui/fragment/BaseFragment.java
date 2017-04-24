package com.mitlosh.bookplayer.ui.fragment;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.mitlosh.bookplayer.R;
import com.mitlosh.bookplayer.utils.OnSingleClickListener;

public abstract class BaseFragment extends Fragment {

    public static final String TAG = "BaseFragment";

    private FloatingActionButton floatActionButton;
    private Toolbar toolbar;
    private static boolean hasShowFAB;
    private Button topButton;


    public Toolbar getToolbar() {
        return toolbar;
    }

    public FloatingActionButton getFloatActionButton() {
        return floatActionButton;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        floatActionButton = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
       // topButton = (Button) getActivity().findViewById(R.id.top_button);

    }

    protected void onFABClick() {}

    @Override
    public void onResume() {
        super.onResume();
        setupViewsOnResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    protected int getFABImageResource(){
        return android.R.drawable.ic_dialog_email;
    }

    protected int getFABColor(Resources resources) {
        return resources.getColor(R.color.colorAccent);
    }

    protected void setupViewsOnResume() {
        int titleRes = getTitleRes();
        if(toolbar != null && titleRes != 0){
            if(titleRes != -1){
                toolbar.setTitle(titleRes);
            }else{
                toolbar.setTitle("");
            }
            toolbar.setSubtitle("");
        }
        if(showFABOnResume()){
            showFAB();
        }else{
            hideFAB();
        }

        if(toolbar != null){
            toolbar.post(new Runnable() {
                @Override
                public void run() {
                    if(getActivity() == null) return;
                    int backCount = getActivity().getSupportFragmentManager().getBackStackEntryCount();
                    toolbar.setNavigationIcon(getResources().getDrawable(backCount == 0 ? R.drawable.ic_menu_vector : R.drawable.ic_arrow_back_vector));
                    toolbar.setNavigationOnClickListener(new OnSingleClickListener() {
                        @Override
                        public void onSingleClick(View v) {
                            if(getActivity().getSupportFragmentManager().getBackStackEntryCount() > 0){
                                getFragmentManager().popBackStack();
                            }else{
                                DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
                                drawer.openDrawer(GravityCompat.START);
                            }
                        }
                    });
                }
            });
        }
    }

    protected void setTitle(String title){
        if(toolbar != null) toolbar.setTitle(title);
    }

    protected void setTopButtonText(String text){
         topButton.setText(text);

    }

    protected void hideFAB(){
        hasShowFAB = false;
        if(floatActionButton == null) return;
        floatActionButton.setOnClickListener(null);
        floatActionButton.hide(new FloatingActionButton.OnVisibilityChangedListener() {
            @Override
            public void onHidden(FloatingActionButton fab) {
                super.onHidden(fab);
                if (hasShowFAB) showFAB();
            }
        });
    }

    protected void showFAB(){
        hasShowFAB = true;
        if(floatActionButton == null) return;
        floatActionButton.setImageResource(getFABImageResource());
        floatActionButton.setBackgroundTintList(ColorStateList.valueOf(getFABColor(floatActionButton.getResources())));
        floatActionButton.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                onFABClick();
            }
        });
        floatActionButton.show(new FloatingActionButton.OnVisibilityChangedListener() {
            @Override
            public void onShown(FloatingActionButton fab) {
                super.onShown(fab);
                if (!hasShowFAB) hideFAB();
            }
        });
    }

    protected abstract int getTitleRes();

    protected abstract boolean showFABOnResume();


}

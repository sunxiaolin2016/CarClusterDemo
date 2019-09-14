package com.ad.carclusterdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class MainActivity extends FragmentActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private ViewPager mPager;

    private HashMap<Button, Facet<?>> mButtonToFacet = new HashMap<>();
    private SparseArray<Facet<?>> mOrderToFacet = new SparseArray<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerFacets(
                new Facet<>((Button)findViewById(R.id.btn_nav), 0, NavigationFragment.class),
                new Facet<>((Button)findViewById(R.id.btn_phone), 1, PhoneFragment.class),
                new Facet<>((Button)findViewById(R.id.btn_music), 2, MusicFragment.class),
                new Facet<>((Button)findViewById(R.id.btn_car_info), 3, CarInfoFragment.class));

        mPager = findViewById(R.id.pager);
        mPager.setAdapter(new ClusterPageAdapter(getSupportFragmentManager()));
        mOrderToFacet.get(0).button.requestFocus();
    }

    public class ClusterPageAdapter extends FragmentPagerAdapter {
        public ClusterPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return mButtonToFacet.size();
        }

        @Override
        public Fragment getItem(int position) {
            return mOrderToFacet.get(position).getOrCreateFragment();
        }
    }

    private final View.OnFocusChangeListener mFacetButtonFocusListener =
            new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        mPager.setCurrentItem(mButtonToFacet.get(v).order);
                    }
                }
            };

    private void registerFacets(Facet<?>... facets) {
        for (Facet<?> f : facets) {
            registerFacet(f);
        }
    }

    private <T> void registerFacet(Facet<T> facet) {
        mOrderToFacet.append(facet.order, facet);
        mButtonToFacet.put(facet.button, facet);

        facet.button.setOnFocusChangeListener(mFacetButtonFocusListener);
    }

    private static class Facet<T> {
        Button button;
        Class<T> clazz;
        int order;

        Facet(Button button, int order, Class<T> clazz) {
            this.button = button;
            this.order = order;
            this.clazz = clazz;
        }

        private Fragment mFragment;

        Fragment getOrCreateFragment() {
            if (mFragment == null) {
                try {
                    mFragment = (Fragment) clazz.getConstructors()[0].newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
            return mFragment;
        }
    }

    interface Listener {
        void onKeyEvent(KeyEvent event);
    }
}

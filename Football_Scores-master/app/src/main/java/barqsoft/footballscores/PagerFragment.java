package barqsoft.footballscores;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yehya khaled on 2/27/2015.
 */
public class PagerFragment extends Fragment {
    public static final int NUM_PAGES = 5;
    public ViewPager mPagerHandler;
    private myPageAdapter mPagerAdapter;
    private MainScreenFragment[] viewFragments = new MainScreenFragment[5];
    private TabLayout mTab;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.pager_fragment, container, false);
        mPagerHandler = (ViewPager) rootView.findViewById(R.id.pager);
        mTab = (TabLayout) rootView.findViewById(R.id.tab);
        mPagerAdapter = new myPageAdapter(getChildFragmentManager());

        for (int i = 0; i < NUM_PAGES; i++) {
            Date fragmentdate = new Date(System.currentTimeMillis() + ((i - 2) * 86400000));
            SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
            viewFragments[i] = new MainScreenFragment();
            viewFragments[i].setFragmentDate(mformat.format(fragmentdate));
        }
        //Abhilash:Added Tab layout instead of PagerTabStrip
        mPagerHandler.setAdapter(mPagerAdapter);
        mTab.setupWithViewPager(mPagerHandler);
        mTab.setTabGravity(TabLayout.GRAVITY_FILL);
        mTab.setBackgroundColor(getResources().getColor(R.color.light_green_500));

        mPagerHandler.setCurrentItem(MainActivity.current_fragment);
        return rootView;
    }

    private class myPageAdapter extends FragmentStatePagerAdapter {
        @Override
        public Fragment getItem(int i) {
            return viewFragments[i];
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        public myPageAdapter(FragmentManager fm) {
            super(fm);
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return getDayName(getActivity(), System.currentTimeMillis() + ((position - 2) * 86400000));
        }

        public String getDayName(Context context, long dateInMillis) {
            // If the date is today, return the localized version of "Today" instead of the actual
            // date.
            //Abhilash:Changed the date format from "yesterday","tomorrow" to actual dates
            Time t = new Time();
            t.setToNow();
            int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
            int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);
            if (julianDay == currentJulianDay) {
                return context.getString(R.string.today);
            } else {
                Time time = new Time();
                time.setToNow();
                SimpleDateFormat dayFormat = new SimpleDateFormat("MM/dd");
                return dayFormat.format(dateInMillis);
            }
        }
    }
}

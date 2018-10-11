package com.lab.se.crowdframe;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.SimpleShowcaseEventListener;
import com.lab.se.crowdframe.entity.BottomNavigationViewHelper;
import com.lab.se.crowdframe.entity.MyViewpager;
import com.lab.se.crowdframe.entity.SysApplication;
import com.lab.se.crowdframe.entity.ViewPagerAdapter;

import com.github.amlcurran.showcaseview.targets.ViewTarget;


public class EntryActivity extends AppCompatActivity implements SearchTaskFragment.OnFragmentInteractionListener,
        PublishFragment.OnFragmentInteractionListener, MyTaskFragment.OnFragmentInteractionListener, TemplateFragment.OnFragmentInteractionListener{

    ViewPagerAdapter adapter;
    private MyViewpager viewPager;
    private MenuItem menuItem;
    private BottomNavigationView bottomNavigationView;
    //判断是否第一次登录
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);
        SysApplication.getInstance().addActivity(this);

        setupViewPager();

        preferences = getSharedPreferences("crowdframe", 0);//第一个参数表示对应的preference名称，第二个参数表示模式，一般是private mode
        editor = preferences.edit();
        boolean isFirstRun = preferences.getBoolean("isFirstRun",true);//如果isFirstRun没有对应值，则默认为true

        //引导
        if(isFirstRun){
            editor.putBoolean("isFirstRun",false);
            editor.commit();
            viewPager.setCurrentItem(2);
            ViewTarget target = new ViewTarget(R.id.navigation_template, this);

            //第一个引导：模板浏览及收藏
            ShowcaseView sv = new ShowcaseView.Builder(this)
                    .withMaterialShowcase()
                    .setTarget(target)
                    .setContentTitle(this.getString(R.string.first_title))
                    .setContentText(this.getString(R.string.first_content))
                    .setStyle(R.style.CustomShowcaseTheme2)
                    .setShowcaseEventListener(new SimpleShowcaseEventListener(){

                        //第一个引导隐藏时，显示第二个引导：任务发布。重写onShowcaseViewDidHide方法可实现
                        @Override
                        public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                            viewPager.setCurrentItem(1);
                            ViewTarget target = new ViewTarget(R.id.navigation_publish,EntryActivity.this);
                            ShowcaseView sv = new ShowcaseView.Builder(EntryActivity.this)
                                    .setTarget(target)
                                    .setContentTitle(EntryActivity.this.getString(R.string.second_title))
                                    .setContentText(EntryActivity.this.getString(R.string.second_content))
                                    .setStyle(R.style.CustomShowcaseTheme2)
                                    .replaceEndButton(R.layout.view_custom_button)
                                    .setShowcaseEventListener(new SimpleShowcaseEventListener() {

                                        //第二个引导隐藏时，显示第三个引导：任务浏览和接受。重写onShowcaseViewDidHide方法可实现
                                        @Override
                                        public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                                            viewPager.setCurrentItem(0);
                                            ViewTarget target = new ViewTarget(R.id.navigation_search,EntryActivity.this);
                                            ShowcaseView sv = new ShowcaseView.Builder(EntryActivity.this)
                                                    .setTarget(target)
                                                    .setContentTitle(EntryActivity.this.getString(R.string.third_title))
                                                    .setContentText(EntryActivity.this.getString(R.string.third_content))
                                                    .setStyle(R.style.CustomShowcaseTheme2)
                                                    .replaceEndButton(R.layout.view_custom_button)
                                                    .setShowcaseEventListener(new SimpleShowcaseEventListener() {

                                                        //第三个引导隐藏时，显示最后一个引导：个人页面。重写onShowcaseViewDidHide方法可实现
                                                        @Override
                                                        public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                                                            viewPager.setCurrentItem(3);
                                                            ViewTarget target = new ViewTarget(R.id.navigation_my_task,EntryActivity.this);
                                                            ShowcaseView sv = new ShowcaseView.Builder(EntryActivity.this)
                                                                    .setTarget(target)
                                                                    .setContentTitle(EntryActivity.this.getString(R.string.last_title))
                                                                    .setContentText(EntryActivity.this.getString(R.string.last_content))
                                                                    .setStyle(R.style.CustomShowcaseTheme2)
                                                                    .replaceEndButton(R.layout.view_custom_button)
                                                                    .build();
                                                            //设置button的position
                                                            RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                                            lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                                                            lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                                                            int margin = ((Number) (getResources().getDisplayMetrics().density * 12)).intValue();
                                                            lps.setMargins(margin, margin, margin, margin);
                                                            sv.setButtonPosition(lps);
                                                        }
                                                    })
                                                    .build();
                                        }
                                    })
                                    .build();
                        }
                    })
                    .replaceEndButton(R.layout.view_custom_button)
                    .build();
        }
        else{
            viewPager.setCurrentItem(0);
        }
    }



    private void setupViewPager() {
        viewPager = (MyViewpager) findViewById(R.id.viewpager);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        //默认 >3 的选中效果会影响ViewPager的滑动切换时的效果，故利用反射去掉
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.navigation_search:
                                viewPager.setCurrentItem(0);
                                break;
                            case R.id.navigation_publish:
                                viewPager.setCurrentItem(1);
                                break;
                            case R.id.navigation_template:
                                viewPager.setCurrentItem(2);
                                break;
                            case R.id.navigation_my_task:
                                viewPager.setCurrentItem(3);
                                break;
                        }
                        return false;
                    }
                });
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(SearchTaskFragment.newInstance("search ongoingTask"));
        adapter.addFragment(PublishFragment.newInstance("publish ongoingTask"));
        adapter.addFragment(TemplateFragment.newInstance("manage template"));
        adapter.addFragment(MyTaskFragment.newInstance("my tasks"));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (menuItem != null) {
                    menuItem.setChecked(false);
                } else {
                    bottomNavigationView.getMenu().getItem(0).setChecked(false);
                }
                menuItem = bottomNavigationView.getMenu().getItem(position);
                menuItem.setChecked(true);

//                Fragment ff = adapter.getItem(position);
//                ff.onResume2();

            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        //禁止ViewPager滑动
        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });


        viewPager.setAdapter(adapter);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //communicate with other fragment or activity
    }

    public interface FragmentListener {
        public void onFragmentClickListener(int item);
    }

}

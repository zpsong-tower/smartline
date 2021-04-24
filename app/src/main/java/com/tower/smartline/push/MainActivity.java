package com.tower.smartline.push;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tower.smartline.common.app.Activity;
import com.tower.smartline.common.widget.PortraitView;
import com.tower.smartline.push.frags.main.ContactFragment;
import com.tower.smartline.push.frags.main.GroupFragment;
import com.tower.smartline.push.frags.main.HomeFragment;
import com.tower.smartline.push.helper.NavHelper;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;

import net.qiujuer.genius.ui.Ui;
import net.qiujuer.genius.ui.widget.FloatActionButton;

import butterknife.BindView;
import butterknife.OnClick;

import java.util.Objects;

/**
 * MainActivity
 *
 * @author zpsong-tower <pingzisong2012@gmail.com>
 * @since 2020/10/27 4:00
 */
public class MainActivity extends Activity
        implements BottomNavigationView.OnNavigationItemSelectedListener,
        NavHelper.OnTabChangedListener<Integer> {
    private static final String TAG = MainActivity.class.getName();

    private static final int DEFAULT_NUM = 0;

    private static final float ROTATION_VALUE = 360;

    @BindView(R.id.appbar)
    AppBarLayout mLayAppbar;

    @BindView(R.id.im_portrait)
    PortraitView mPortrait;

    @BindView(R.id.txt_title)
    TextView mTitle;

    @BindView(R.id.lay_container)
    FrameLayout mContainer;

    @BindView(R.id.btn_action)
    FloatActionButton mAction;

    @BindView(R.id.navigation)
    BottomNavigationView mNavigation;

    private NavHelper<Integer> mNavHelper;

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initWidget() {
        super.initWidget();

        // 底部导航栏初始化
        mNavHelper = new NavHelper<>(this, R.id.lay_container,
                getSupportFragmentManager(), this);
        mNavHelper.add(R.id.action_home, new NavHelper.Tab<>(HomeFragment.class, R.string.title_home))
                .add(R.id.action_group, new NavHelper.Tab<>(GroupFragment.class, R.string.title_group))
                .add(R.id.action_contact, new NavHelper.Tab<>(ContactFragment.class, R.string.title_contact));
        mNavigation.setOnNavigationItemSelectedListener(this);

        // 顶部TitleBar加载背景图
        Glide.with(this)
                .load(R.drawable.bg_src_morning)
                .centerCrop()
                .into(new CustomViewTarget<AppBarLayout, Drawable>(mLayAppbar) {
                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    }

                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        this.view.setBackground(resource);
                    }

                    @Override
                    protected void onResourceCleared(@Nullable Drawable placeholder) {
                    }
                });
    }

    @Override
    protected void initData() {
        super.initData();

        // 底部导航栏默认选中首页
        Menu menu = mNavigation.getMenu();
        menu.performIdentifierAction(R.id.action_home, DEFAULT_NUM);
    }

    @OnClick(R.id.im_portrait)
    void onPortraitClick() {
        Log.i(TAG, "onPortraitClick");
        Toast.makeText(this, "onPortraitClick", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.im_search)
    void onSearchClick() {
        Log.i(TAG, "onSearchClick");
        Toast.makeText(this, "onSearchClick", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.btn_action)
    void onActionClick() {
        Log.i(TAG, "onActionClick");
        Toast.makeText(this, "onActionClick", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (!TextUtils.isEmpty(item.getTitle())) {
            Log.i(TAG, "onNavigationItemSelected: " + item.getTitle());
        }

        // 转接事件流到辅助类中
        return mNavHelper.performClickMenu(item.getItemId());
    }

    @Override
    public void onTabChanged(@NonNull NavHelper.Tab<Integer> newTab, NavHelper.Tab<Integer> oldTab) {
        if (getResources() == null) {
            return;
        }
        try {
            // 更改标题栏文字
            String title = getResources().getString(newTab.getExtra());
            if (!TextUtils.isEmpty(title)) {
                mTitle.setText(title);
            }

            // 浮动按钮动画
            if (oldTab == null) {
                return;
            }
            float translationValue = DEFAULT_NUM;
            float rotationValue = DEFAULT_NUM;
            if (Objects.equals(newTab.getClx(), HomeFragment.class)) {
                translationValue = Ui.dipToPx(getResources(), getResources().getDimension(R.dimen.len_84));
            }
            if (Objects.equals(newTab.getClx(), GroupFragment.class)) {
                mAction.setImageResource(R.drawable.ic_group_add);
                mAction.setRotation(ROTATION_VALUE);
                rotationValue = -ROTATION_VALUE;
            }
            if (Objects.equals(newTab.getClx(), ContactFragment.class)) {
                mAction.setImageResource(R.drawable.ic_contact_add);
                mAction.setRotation(-ROTATION_VALUE);
                rotationValue = ROTATION_VALUE;
            }
            if (Objects.equals(newTab.getClx(), HomeFragment.class)
                    || Objects.equals(oldTab.getClx(), HomeFragment.class)) {
                mAction.animate().translationY(translationValue).start();
            } else {
                mAction.animate().rotation(rotationValue).start();
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "onTabChanged: Exception");
        }
    }
}
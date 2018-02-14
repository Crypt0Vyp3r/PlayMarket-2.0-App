package com.blockchain.store.playmarket.ui.main_list_screen;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.blockchain.store.playmarket.R;
import com.blockchain.store.playmarket.data.entities.App;
import com.blockchain.store.playmarket.data.entities.Category;
import com.blockchain.store.playmarket.interfaces.AppListCallbacks;
import com.blockchain.store.playmarket.ui.account_management_activity.AccountManagementActivity;
import com.blockchain.store.playmarket.ui.app_detail_screen.AppDetailActivity;
import com.blockchain.store.playmarket.ui.navigation_view.NavigationViewFragment;
import com.blockchain.store.playmarket.ui.search_screen.SearchActivity;
import com.blockchain.store.playmarket.utilities.ToastUtil;
import com.blockchain.store.playmarket.utilities.ViewPagerAdapter;
import com.blockchain.store.playmarket.utilities.data.ClipboardUtils;
import com.blockchain.store.playmarket.utilities.drawable.HamburgerDrawable;
import com.blockchain.store.playmarket.utilities.net.APIUtils;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.BehaviorSubject;

import static com.blockchain.store.playmarket.ui.main_list_screen.MainMenuContract.Presenter;

public class MainMenuActivity extends AppCompatActivity implements AppListCallbacks, MainMenuContract.View, MaterialSearchView.OnQueryTextListener {
    private static final String TAG = "MainMenuActivity";
    private static final int DEBOUNCE_INTERVAL_MILLIS = 1000;

    @BindView(R.id.tab_layout) TabLayout tabLayout;
    @BindView(R.id.app_bar_layout) AppBarLayout appBarLayout;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;
    @BindView(R.id.view_pager) ViewPager viewPager;
    @BindView(R.id.progress_bar) ProgressBar progressBar;
    @BindView(R.id.error_holder) View errorHolder;
    @BindView(R.id.nav_view) NavigationView navigationView;
    @BindView(R.id.search_view) MaterialSearchView searchView;

    private BehaviorSubject<String> userInputSubject = BehaviorSubject.create();
    private ArrayList<App> searchListResult = new ArrayList<>();
    private Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        ButterKnife.bind(this);
        attachPresenter();
        initViews();
        attachFragment();
        setSearchViewDebounce();
    }

    private void attachPresenter() {
        presenter = new MainMenuPresenter();
        presenter.init(this);
        presenter.loadCategories();

    }

    private void attachFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.navigation_view_holder, new NavigationViewFragment()).commitAllowingStateLoss();
    }

    private void initViews() {
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.setDrawerArrowDrawable(new HamburgerDrawable(this));
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();
        searchView.setOnItemClickListener((parent, view, position, id) -> {
            String query = (String) parent.getItemAtPosition(position);
            onSuggestionClicked(query);
            searchView.closeSearch();
        });
        searchView.setOnQueryTextListener(this);

    }


    private void setSearchViewDebounce() {
        userInputSubject.debounce(DEBOUNCE_INTERVAL_MILLIS, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .filter(text -> !text.isEmpty())
                .subscribe(text -> {
                    presenter.searchQuery(text);
                });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            //todo back twice to quit
        }
    }

    @Override
    public void setProgress(boolean isShow) {
        progressBar.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onCategoryLoaded(ArrayList<Category> categories) {
        errorHolder.setVisibility(View.GONE);
        initViewPager(categories);

    }

    private void initViewPager(ArrayList<Category> categories) {
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        for (Category category : categories) {
            viewPagerAdapter.addFragment(MainMenuFragment.newInstance(category), category.name);
        }
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

    }

    @Override
    public void onCategoryLoadFailed(Throwable throwable) {
        errorHolder.setVisibility(View.VISIBLE);
        ToastUtil.showToast("Category load failed! " + throwable.getMessage());
    }

    @Override
    public void onSearchResultReady(ArrayList<App> apps) {
        searchListResult = apps;
    }

    @Override
    public void onSearchResultFail(Throwable throwable) {
        ToastUtil.showToast(throwable.getMessage());
    }

    @Override
    public void onSearchSuggestionReady(String[] strings) {
//        searchView.setSuggestions(strings);

    }

    @OnClick(R.id.error_view_repeat_btn)
    void onRepeatBtnClicked() {
        errorHolder.setVisibility(View.GONE);
        presenter.loadCategories();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu_drawer, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ToastUtil.showToast(item.getTitle().toString());
        return super.onOptionsItemSelected(item);
    }


    public void showAddFundsDialog() {
        final Dialog d = new Dialog(this);
        d.setContentView(R.layout.show_address_dialog);

        final TextView addressTextView = (TextView) d.findViewById(R.id.addressTextView);
//        try {
//            addressTextView.setText(keyManager.getAccounts().get(0).getAddress().getHex());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        TextView balanceTextView = (TextView) d.findViewById(R.id.balanceText);
        balanceTextView.setText(APIUtils.api.balance.getDisplayPrice(true));

        Button close_btn = (Button) d.findViewById(R.id.continueButton);
        close_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                d.dismiss();
            }
        });

        Button copyAddressButton = (Button) d.findViewById(R.id.copyAddressButton);
        copyAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardUtils.copyToClipboard(getApplicationContext(), addressTextView.getText().toString());
                showCopiedAlert();
            }
        });

        d.show();
    }

    private void showCopiedAlert() {
        ToastUtil.showToast(R.string.address_copied);
    }


    @Override
    public void onAppClicked(App app) {
        AppDetailActivity.start(this, app);

    }

    private void onSuggestionClicked(String query) {
        for (App app : searchListResult) {
            if (app.nameApp.equals(query)) {
                AppDetailActivity.start(this, app);
                return;
            }
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (query.trim().length() > 0) {
            SearchActivity.start(this, query);
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (newText.isEmpty() /*|| searchText.equals(newText)*/) return false;
//        searchText = newText;
        userInputSubject.onNext(newText);
        return false;
    }


}

package derpibooru.derpy.ui;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import derpibooru.derpy.R;
import derpibooru.derpy.data.server.DerpibooruUser;
import derpibooru.derpy.server.QueryHandler;
import derpibooru.derpy.server.providers.UserDataProvider;
import derpibooru.derpy.server.requesters.LogoutRequester;

class NavigationDrawer implements NavigationView.OnNavigationItemSelectedListener {
    private static final int LOGIN_ACTIVITY_REQUEST_CODE = 1;

    private NavigationDrawerActivity mParent;
    private int mParentNavigationId;

    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;
    private View mDrawerHeader;

    private UserDataProvider mUserProvider;

    NavigationDrawer(NavigationDrawerActivity parent, DrawerLayout drawer, Toolbar toolbar, NavigationView menu) {
        mParent = parent;
        setActivityMenuItemId();

        mDrawerLayout = drawer;
        mNavigationView = ((NavigationView) mDrawerLayout.findViewById(R.id.navigationView));
        mDrawerHeader = mNavigationView.getHeaderView(0);

        mDrawerHeader.findViewById(R.id.buttonRefreshUserData)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        refreshUserData();
                    }
                });

        initDrawerToggle(parent, toolbar, drawer, menu);

        mUserProvider = new UserDataProvider(parent, new UserQueryHandler());
        mUserProvider.fetch();
    }

    private void setActivityMenuItemId() {
        if (mParent instanceof MainActivity) {
            mParentNavigationId = R.id.navigationHome;
        } else if (mParent instanceof SearchActivity) {
            mParentNavigationId = R.id.navigationSearch;
        } else if (mParent instanceof FiltersActivity) {
            mParentNavigationId = R.id.navigationFilters;
        }
    }

    public void refreshUserData() {
        mDrawerHeader.findViewById(R.id.buttonRefreshUserData)
                .setVisibility(View.INVISIBLE);
        ((TextView) mDrawerHeader.findViewById(R.id.textHeaderFilter))
                .setText("Loading...");
        mUserProvider.refreshUserData();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == mParentNavigationId) {
            closeDrawer();
            return true;
        }
        switch (id) {
            case (R.id.navigationHome):
                mParent.startActivity(new Intent(mParent, MainActivity.class));
                mParent.finish();
                break;
            case (R.id.navigationSearch):
                mParent.startActivity(new Intent(mParent, SearchActivity.class));
                mParent.finish();
                break;
            case (R.id.navigationFilters):
                mParent.startActivity(new Intent(mParent, FiltersActivity.class));
                mParent.finish();
                break;
            case (R.id.navigationLogin):
                mParent.startActivityForResult(new Intent(mParent, LoginActivity.class),
                                               LOGIN_ACTIVITY_REQUEST_CODE);
                mNavigationView.getMenu()
                        .findItem(mParentNavigationId).setChecked(false);
                break;
            case (R.id.navigationLogout):
                logout();
                return true; /* do not hide the drawer */
        }
        closeDrawer();
        return true;
    }

    private void logout() {
        new LogoutRequester(mParent, new QueryHandler<Boolean>() {
            @Override
            public void onQueryExecuted(Boolean result) {
                mUserProvider.refreshUserData();
            }

            @Override
            public void onQueryFailed() { }
        }).fetch();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case (LOGIN_ACTIVITY_REQUEST_CODE):
                if (resultCode == Activity.RESULT_OK) {
                    mUserProvider.refreshUserData();
                }
                mNavigationView.getMenu().findItem(R.id.navigationLogin).setChecked(false);

                mNavigationView.getMenu()
                        .findItem(mParentNavigationId).setChecked(false);
                break;
        }
    }

    private void displayUserData(DerpibooruUser user) {
        mDrawerHeader.findViewById(R.id.buttonRefreshUserData)
                .setVisibility(View.VISIBLE);
        if (!user.isLoggedIn()) {
            onUserLoggedOut(user);
        } else {
            onUserLoggedIn(user);
        }
        ((TextView) mDrawerHeader.findViewById(R.id.textHeaderFilter))
                .setText("Filter: " + user.getCurrentFilter().getName());
        /* ! copied from ImageCommentsAdapter; perhaps it should be made into a separate class? */
        if (!user.getAvatarUrl().endsWith(".svg")) {
            Glide.with(mParent).load(user.getAvatarUrl()).diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .dontAnimate().into((ImageView) mDrawerHeader.findViewById(R.id.imageAvatar));
        } else {
            Glide.with(mParent).load(R.drawable.no_avatar).dontAnimate()
                    .into((ImageView) mDrawerHeader.findViewById(R.id.imageAvatar));
        }
    }

    private void onUserLoggedIn(DerpibooruUser user) {
        ((TextView) mDrawerHeader.findViewById(R.id.textHeaderUser))
                .setText(user.getUsername());
        mNavigationView.getMenu().clear();
        mNavigationView.inflateMenu(R.menu.menu_navigation_drawer_logged_in);
        mNavigationView.getMenu()
                .findItem(mParentNavigationId).setChecked(true);
    }

    private void onUserLoggedOut(DerpibooruUser user) {
        ((TextView) mDrawerHeader.findViewById(R.id.textHeaderUser))
                .setText("Not logged in");
        mNavigationView.getMenu().clear();
        mNavigationView.inflateMenu(R.menu.menu_navigation_drawer_logged_out);
        mNavigationView.getMenu()
                .findItem(mParentNavigationId).setChecked(true);
    }

    private void initDrawerToggle(Activity parent, Toolbar toolbar,
                                  DrawerLayout drawer, NavigationView menu) {
        ActionBarDrawerToggle toggle =
                new ActionBarDrawerToggle(parent, mDrawerLayout, toolbar,
                                          R.string.open_drawer, R.string.close_drawer) {
                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {
                        /* Disable the hamburger icon animation (for more info refer to
                         * https://medium.com/android-news/navigation-drawer-styling-according-material-design-5306190da08f#.9wrzhczd8 ) */
                        super.onDrawerSlide(drawerView, 0);
                    }
                };
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        menu.setNavigationItemSelectedListener(this);
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }

    public void closeDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    private class UserQueryHandler implements QueryHandler<DerpibooruUser> {
        @Override
        public void onQueryExecuted(DerpibooruUser result) {
            mParent.onUserDataRefreshed();
            displayUserData(result);
        }

        @Override
        public void onQueryFailed() { }
    }
}

package derpibooru.derpy.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;

import butterknife.Bind;
import butterknife.ButterKnife;
import derpibooru.derpy.R;
import derpibooru.derpy.data.server.DerpibooruImageDetailed;
import derpibooru.derpy.data.server.DerpibooruImageThumb;
import derpibooru.derpy.data.server.DerpibooruUser;
import derpibooru.derpy.server.QueryHandler;
import derpibooru.derpy.server.providers.ImageDetailedProvider;
import derpibooru.derpy.ui.fragments.ImageActivityMainFragment;
import derpibooru.derpy.ui.fragments.ImageActivityTagFragment;
import derpibooru.derpy.ui.fragments.ImageListFragment;

public class ImageActivity extends AppCompatActivity {
    public static final String EXTRAS_IMAGE_DETAILED = "derpibooru.derpy.ImageDetailed";
    public static final String EXTRAS_IMAGE_ID = "derpibooru.derpy.ImageId";

    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.toolbarLayout) View toolbarLayout;
    @Bind(R.id.fragmentLayout) FrameLayout contentLayout;

    private DerpibooruImageDetailed mImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) throws IllegalStateException {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        ButterKnife.bind(this);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setTitle(R.string.loading);
        if ((savedInstanceState != null) && (savedInstanceState.containsKey(EXTRAS_IMAGE_DETAILED))) {
            mImage = savedInstanceState.getParcelable(EXTRAS_IMAGE_DETAILED);
            toolbar.setTitle("#" + Integer.toString(mImage.getThumb().getId()));
            restoreCallbackHandlers();
        } else if (getIntent().hasExtra(EXTRAS_IMAGE_ID)) {
            fetchDetailedInformation(getIntent().getIntExtra(EXTRAS_IMAGE_ID, 0));
        } else if (getIntent().hasExtra(ImageListFragment.EXTRAS_IMAGE_THUMB)) {
            DerpibooruImageThumb thumb = getIntent().getParcelableExtra(ImageListFragment.EXTRAS_IMAGE_THUMB);
            displayMainFragment(thumb);
            fetchDetailedInformation(thumb.getId());
        }
    }

    private void restoreCallbackHandlers() {
        if (getCurrentFragment() instanceof ImageActivityMainFragment) {
            ((ImageActivityMainFragment) getCurrentFragment())
                    .setActivityCallbacks(new MainFragmentCallbackHandler());
            /* the fragment's view is not created yet; remove the thumb from arguments so
             * the fragment will initialize from the detailed image */
            getCurrentFragment().getArguments()
                    .remove(ImageListFragment.EXTRAS_IMAGE_THUMB);
        } else if (getCurrentFragment() instanceof ImageActivityTagFragment) {
            ((ImageActivityTagFragment) getCurrentFragment())
                    .setActivityCallbacks(new TagFragmentCallbackHandler());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (mImage != null) {
            savedInstanceState.putParcelable(EXTRAS_IMAGE_DETAILED, mImage);
        }
    }

    @Override
    public void onBackPressed() {
        if (!getSupportFragmentManager().popBackStackImmediate()) {
            if (mImage != null) {
                setResult(Activity.RESULT_OK,
                          new Intent().putExtra(ImageListFragment.EXTRAS_IMAGE_THUMB, mImage.getThumb()));
            } else {
                setResult(Activity.RESULT_OK);
            }
            super.onBackPressed();
        } else {
            if ((getCurrentFragment() instanceof ImageActivityMainFragment) && (mImage != null)) {
                ((ImageActivityMainFragment) getCurrentFragment())
                        .setActivityCallbacks(new MainFragmentCallbackHandler());
                ((ImageActivityMainFragment) getCurrentFragment())
                        .resetView();
                ((ImageActivityMainFragment) getCurrentFragment())
                        .onDetailedImageFetched();
            }
        }
    }

    private void fetchDetailedInformation(int imageId) {
        ImageDetailedProvider provider = new ImageDetailedProvider(
                this, new QueryHandler<DerpibooruImageDetailed>() {
            @Override
            public void onQueryExecuted(DerpibooruImageDetailed info) {
                mImage = info;
                displayMainFragment(null);
            }

            @Override
            public void onQueryFailed() {
            /* TODO: handle failed request */
            }
        });
        provider.id(imageId).fetch();
    }

    private void displayMainFragment(@Nullable DerpibooruImageThumb placeholderThumb) {
        if (placeholderThumb != null) {
            initializeMainFragmentWithPlaceholderThumb(placeholderThumb);
        } else if (toolbar.getTitle().equals(getString(R.string.loading))) {
            initializeMainFragmentWithDetailed();
        } else if (getCurrentFragment() instanceof ImageActivityMainFragment) {
            /* the main fragment has already been instantiated with a placeholder thumb */
            ((ImageActivityMainFragment) getCurrentFragment())
                    .onDetailedImageFetched();
        }
    }

    private void initializeMainFragmentWithDetailed() {
        toolbar.setTitle("#" + Integer.toString(mImage.getThumb().getId()));
        if (getCurrentFragment() instanceof ImageActivityMainFragment) {
            /* on configuration change, fragmentmanager restores the fragment — no need to instantiate it again */
            ((ImageActivityMainFragment) getCurrentFragment())
                    .setActivityCallbacks(new MainFragmentCallbackHandler());
            getCurrentFragment().getArguments().remove(ImageListFragment.EXTRAS_IMAGE_THUMB);
        } else {
            instantiateMainFragment(null);
        }
    }

    private void initializeMainFragmentWithPlaceholderThumb(DerpibooruImageThumb thumb) {
        toolbar.setTitle("#" + Integer.toString(thumb.getId()));
        if (getCurrentFragment() instanceof ImageActivityMainFragment) {
            /* on configuration change, fragmentmanager restores the fragment — no need to instantiate it again */
            ((ImageActivityMainFragment) getCurrentFragment())
                    .setActivityCallbacks(new MainFragmentCallbackHandler());
        } else {
            instantiateMainFragment(thumb);
        }
    }

    private void instantiateMainFragment(@Nullable DerpibooruImageThumb placeholderThumb) {
        ImageActivityMainFragment mainFragment = new ImageActivityMainFragment();
        mainFragment.setActivityCallbacks(new MainFragmentCallbackHandler());
        mainFragment.setArguments(getMainFragmentArguments(placeholderThumb));

        getSupportFragmentManager()
                .beginTransaction()
                .replace(contentLayout.getId(), mainFragment)
                .commit();
    }

    private Bundle getMainFragmentArguments(@Nullable DerpibooruImageThumb thumb) {
        boolean isUserLoggedIn =
                ((DerpibooruUser) getIntent().getParcelableExtra(MainActivity.EXTRAS_USER)).isLoggedIn();
        Bundle args = new Bundle();
        args.putBoolean(ImageActivityMainFragment.EXTRAS_IS_USER_LOGGED_IN, isUserLoggedIn);
        if (thumb != null) {
            args.putParcelable(ImageListFragment.EXTRAS_IMAGE_THUMB, thumb);
        }
        return args;
    }

    @Nullable
    protected Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(contentLayout.getId());
    }

    private void displayTagFragment(int tagId) {
        Bundle args = new Bundle();
        args.putInt(ImageActivityTagFragment.EXTRAS_TAG_ID, tagId);

        ImageActivityTagFragment fragment = new ImageActivityTagFragment();
        fragment.setActivityCallbacks(new TagFragmentCallbackHandler());
        fragment.setArguments(args);

        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.image_activity_tag_enter,
                                     R.anim.image_activity_tag_exit,
                                     R.anim.image_activity_tag_back_stack_pop_enter,
                                     R.anim.image_activity_tag_back_stack_pop_exit)
                .addToBackStack(null)
                .replace(contentLayout.getId(), fragment)
                .commit();
    }

    private class MainFragmentCallbackHandler implements ImageActivityMainFragment.ImageActivityMainFragmentHandler {
        @Override
        public boolean isToolbarVisible() {
            return toolbarLayout.getVisibility() == View.VISIBLE;
        }

        @Override
        public void setToolbarVisible(boolean visible) {
            toolbarLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
        }

        @Override
        public DerpibooruImageDetailed getImage() {
            return mImage;
        }

        @Override
        public void hideProgress() {
            findViewById(R.id.progressImage).setVisibility(View.GONE);
        }

        @Override
        public void openTagInformation(int tagId) {
            displayTagFragment(tagId);
        }
    }

    private class TagFragmentCallbackHandler implements ImageActivityTagFragment.ImageActivityTagFragmentHandler {
        @Override
        public void onTagSearchRequested(String tagName) {

        }

        @Override
        public void hideProgress() {

        }
    }
}

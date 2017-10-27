package nc.opt.mobile.optmobile.activity;

import android.Manifest;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import nc.opt.mobile.optmobile.R;
import nc.opt.mobile.optmobile.fragment.ActualiteFragment;
import nc.opt.mobile.optmobile.interfaces.AttachToPermissionActivity;
import nc.opt.mobile.optmobile.provider.OptProvider;
import nc.opt.mobile.optmobile.provider.ProviderObserver;
import nc.opt.mobile.optmobile.utils.NoticeDialogFragment;
import nc.opt.mobile.optmobile.utils.RequestQueueSingleton;
import nc.opt.mobile.optmobile.utils.Utilities;

import static nc.opt.mobile.optmobile.provider.services.AgenceService.populateContentProviderFromAsset;
import static nc.opt.mobile.optmobile.provider.services.ColisService.count;

public class MainActivity extends AttachToPermissionActivity
        implements NavigationView.OnNavigationItemSelectedListener, NoticeDialogFragment.NoticeDialogListener, ProviderObserver.ProviderObserverListener {

    private static final String TAG = MainActivity.class.getName();

    public static final String DIALOG_TAG_EXIT = "DIALOG_TAG_EXIT";

    public static final String SAVED_ACTUALITE_FRAGMENT = "SAVED_ACTUALITE_FRAGMENT";

    public static final int RC_PERMISSION_LOCATION = 100;
    public static final int RC_PERMISSION_CALL_PHONE = 200;
    public static final int RC_PERMISSION_INTERNET = 300;
    public static final int RC_SIGN_IN = 300;

    private static final String PREF_POPULATED = "POPULATE_CP";

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private Drawable mDrawablePhoto;

    private ImageView mImageViewProfile;
    private Button mButtonConnexion;
    private TextView mProfilName;

    private ActualiteFragment mActualiteFragment;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;

    @BindView(R.id.nav_view)
    NavigationView navigationView;

    private void signIn() {
        // User is signed out
        signOut();

        List<AuthUI.IdpConfig> listProviders = new ArrayList<>();
        listProviders.add(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());
        listProviders.add(new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build());

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(false)
                        .setAvailableProviders(listProviders)
                        .build(),
                RC_SIGN_IN);
    }

    private void signOut() {
        mButtonConnexion.setText(R.string.login);
        mProfilName.setText(null);
        mFirebaseUser = null;
        mDrawablePhoto = null;
        mImageViewProfile.setImageResource(R.drawable.ic_person_white_48dp);
        invalidateOptionsMenu();
    }

    private void defineAuthListener() {
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mFirebaseUser = firebaseAuth.getCurrentUser();
                if (mFirebaseUser != null) {
                    mButtonConnexion.setText(R.string.logout);
                    mProfilName.setText(mFirebaseUser.getDisplayName());
                    createAsyncTaskGetPhoto().execute();
                } else {
                    signOut();
                }
            }
        };
    }

    private AsyncTask<Void, Void, Drawable> createAsyncTaskGetPhoto() {
        // On définit une tache pour recuperer la photo de la personne connectee
        return new AsyncTask<Void, Void, Drawable>() {
            @Override
            protected Drawable doInBackground(Void... voids) {
                try {
                    return Glide.with(MainActivity.this)
                            .asDrawable()
                            .load(mFirebaseUser.getPhotoUrl())
                            .apply(RequestOptions.circleCropTransform())
                            .submit()
                            .get();
                } catch (InterruptedException | ExecutionException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Drawable drawable) {
                if (drawable != null) {
                    mDrawablePhoto = drawable;
                    mImageViewProfile.setImageDrawable(mDrawablePhoto);
                    invalidateOptionsMenu();
                }
            }
        };
    }

    private boolean isInternetPermited() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED;
    }

    private void updateBadge() {
        // Récupération du textView présent dans le menu
        Menu menu = navigationView.getMenu();
        TextView suiviColisBadgeCounter = (TextView) menu.findItem(R.id.nav_suivi_colis).getActionView();
        suiviColisBadgeCounter.setGravity(Gravity.CENTER_VERTICAL);
        suiviColisBadgeCounter.setTypeface(null, Typeface.BOLD);
        suiviColisBadgeCounter.setTextColor(getResources().getColor(R.color.colorPrimary));
        suiviColisBadgeCounter.setText(String.valueOf(count(this)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(SAVED_ACTUALITE_FRAGMENT)) {
                mActualiteFragment = (ActualiteFragment) getSupportFragmentManager().getFragment(savedInstanceState, SAVED_ACTUALITE_FRAGMENT);
            }
        }
        if (mActualiteFragment == null) {
            mActualiteFragment = ActualiteFragment.newInstance();
        }

        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        View headerLayout = navigationView.getHeaderView(0);
        mImageViewProfile = headerLayout.findViewById(R.id.image_view_profile);
        mButtonConnexion = headerLayout.findViewById(R.id.button_connexion);
        mProfilName = headerLayout.findViewById(R.id.text_profil_name);

        mFirebaseAuth = FirebaseAuth.getInstance();

        defineAuthListener();

        mButtonConnexion.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                if (mFirebaseUser != null) {
                    signOut();
                } else {
                    signIn();
                }
            }
        });

        // Define the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null)

        {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        // Si la permission Internet n'a pas été accordée on va la demander
        if (!

                isInternetPermited())

        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, RC_PERMISSION_INTERNET);
        }

        // Populate the contentProvider with assets, only the first time
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        if (!sharedPreferences.getBoolean(PREF_POPULATED, false))

        {
            populateContentProviderFromAsset(this);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(PREF_POPULATED, true);
            editor.apply();
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        updateBadge();

        // Appel de la premiere instance
        RequestQueueSingleton.getInstance(this.

                getApplicationContext());

        // Enregistrement d'un observer pour écouter les modifications sur le ContentProvider
        ArrayList<Uri> uris = new ArrayList<>();
        uris.add(OptProvider.ListColis.LIST_COLIS);
        ProviderObserver providerObserver = ProviderObserver.getInstance();
        providerObserver.observe(this, this, uris);

        // Création du premier fragment
        getSupportFragmentManager().

                beginTransaction().

                replace(R.id.frame_main, mActualiteFragment).

                commit();

    }

    @Override
    public void onBackPressed() {
        // Fermeture du drawer latéral s'il est ouvert
        if (drawer != null) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    // on demande avant de quitter l'application
                    Utilities.SendDialogByActivity(this, getString(R.string.want_you_quit), NoticeDialogFragment.TYPE_BOUTON_YESNO, NoticeDialogFragment.TYPE_IMAGE_INFORMATION, DIALOG_TAG_EXIT);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_sign_out) {
            mFirebaseAuth.signOut();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_geo_agence:
                startActivity(new Intent(this, AgenceActivity.class));
                break;
            case R.id.nav_suivi_colis:
                startActivity(new Intent(this, GestionColisActivity.class));
                break;
            default:
                break;
        }

        // Après avoir cliquer sur un menu, on ferme le menu latéral
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mFirebaseAuth != null) {
            mFirebaseAuth.addAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mFirebaseAuth != null) {
            if (mAuthStateListener != null) {
                mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActualiteFragment != null) {
            getSupportFragmentManager().putFragment(outState, SAVED_ACTUALITE_FRAGMENT, mActualiteFragment);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                mFirebaseUser = mFirebaseAuth.getCurrentUser();
                Toast.makeText(this, R.string.welcome, Toast.LENGTH_LONG).show();

                // On appelle la tache pour aller recuperer la photo
                createAsyncTaskGetPhoto().execute();

            }
            if (resultCode == RESULT_CANCELED) {
                // Sign in was canceled by the user, finish the activity
                Toast.makeText(this, R.string.sign_in_cancelled, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_PERMISSION_INTERNET && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(this, R.string.internet_needed, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        String s = dialog.getTag();
        if (s.equals(DIALOG_TAG_EXIT)) {
            finish();
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
    }

    @Override
    public void onProviderChange() {
        updateBadge();
    }
}

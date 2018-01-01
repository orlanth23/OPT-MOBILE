package nc.opt.mobile.optmobile.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import nc.opt.mobile.optmobile.R;
import nc.opt.mobile.optmobile.job.task.ParamSyncTask;
import nc.opt.mobile.optmobile.job.task.SyncTask;
import nc.opt.mobile.optmobile.provider.entity.ColisEntity;
import nc.opt.mobile.optmobile.provider.services.ActualiteService;
import nc.opt.mobile.optmobile.provider.services.ColisService;
import nc.opt.mobile.optmobile.service.FirebaseService;
import nc.opt.mobile.optmobile.utils.Utilities;

public class AddColisFragment extends Fragment {

    @BindView(R.id.edit_id_parcel)
    EditText editIdParcel;

    @BindView(R.id.edit_description_parcel)
    EditText editDescriptionParcel;

    private AppCompatActivity mActivity;

    public AddColisFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (AppCompatActivity) context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_colis, container, false);
        ButterKnife.bind(this, rootView);
        mActivity.setTitle(getString(R.string.add_colis));
        return rootView;
    }

    private void createColisAndSaveIt(String idColis, String description, View view) {
        // Add the colis to our ContentProvider
        ColisEntity colis = new ColisEntity();
        colis.setIdColis(idColis);
        colis.setDescription(description);
        colis.setDeleted(0);
        if (ColisService.save(mActivity, colis)) {

            // Launch asyncTask to query the server
            ParamSyncTask paramSyncTask = new ParamSyncTask();
            paramSyncTask.setContext(mActivity);
            paramSyncTask.setIdColis(idColis);
            SyncTask syncTask = new SyncTask(SyncTask.TypeTask.SOLO);
            syncTask.execute(paramSyncTask);

            Snackbar.make(view, String.format(getString(R.string.colis_added), idColis), Snackbar.LENGTH_LONG).show();

            // Ajout d'une actualité
            ActualiteService.insertActualite(mActivity,
                    String.format(getString(R.string.colis_added), idColis),
                    String.format(getString(R.string.insert_contenu_actualite), idColis),
                    true);

            // Try to send to the remote DB
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null && getContext() != null) {
                FirebaseService.createRemoteDatabase(getContext(), ColisService.listFromProvider(getActivity(), true), null);
            }
        }
    }

    @OnClick(R.id.fab_search_parcel)
    public void searchParcel(View view) {
        if (!editIdParcel.getText().toString().isEmpty()) {

            // On cache le clavier.
            Utilities.hideKeyboard(getActivity());

            // Get the idColis from the view
            String idColis = editIdParcel.getText().toString().toUpperCase();
            String description = editDescriptionParcel.getText().toString();

            // Query our ContentProvider to avoid duplicate
            if (ColisService.exist(mActivity, idColis, true)) {
                Snackbar.make(view, R.string.colis_already_added, Snackbar.LENGTH_LONG).show();
            } else {
                // Colis was already in our local DB but marked as deleted. We call real delete and reinsert it.
                if (ColisService.exist(mActivity, idColis, false)) {
                    ColisService.realDelete(mActivity, idColis);
                }

                createColisAndSaveIt(idColis, description, view);

                mActivity.finish();
            }
        }
    }
}

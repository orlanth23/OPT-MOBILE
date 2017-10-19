package nc.opt.mobile.optmobile.adapter;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import nc.opt.mobile.optmobile.R;
import nc.opt.mobile.optmobile.activity.MainActivity;
import nc.opt.mobile.optmobile.entity.ColisEntity;
import nc.opt.mobile.optmobile.entity.EtapeAcheminementEntity;
import nc.opt.mobile.optmobile.fragment.HistoriqueColisFragment;
import nc.opt.mobile.optmobile.provider.ProviderUtilities;

/**
 * Created by orlanth23 on 05/10/2017.
 */

public class ColisAdapter extends RecyclerView.Adapter<ColisAdapter.ViewHolderStepParcel> {

    private final List<ColisEntity> mColisList;
    private Context mContext;

    public ColisAdapter(Context context, List<ColisEntity> colisList) {
        mContext = context;
        mColisList = colisList;
    }

    @Override
    public ViewHolderStepParcel onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_colis, parent, false);
        return new ViewHolderStepParcel(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolderStepParcel holder, int position) {
        holder.mColis = mColisList.get(position);
        holder.mIdColis.setText(holder.mColis.getIdColis());
        holder.parcelDescription.setText(holder.mColis.getDescription());
        if (!holder.mColis.getEtapeAcheminementArrayList().isEmpty()) {
            // On prend la dernière étape
            EtapeAcheminementEntity etapeAcheminement = holder.mColis.getEtapeAcheminementArrayList().get(holder.mColis.getEtapeAcheminementArrayList().size() - 1);
            holder.mStepLastDate.setText(etapeAcheminement.getDate());
            holder.mStepLastPays.setText(etapeAcheminement.getPays());
            holder.mStepLastDescription.setText(etapeAcheminement.getDescription());
        } else {
            holder.mStepLastDate.setText(null);
            holder.mStepLastPays.setText(null);
            holder.mStepLastDescription.setText(null);
        }
    }

    @Override
    public int getItemCount() {
        return mColisList.size();
    }

    public List<ColisEntity> getmColisList() {
        return mColisList;
    }

    class ViewHolderStepParcel extends RecyclerView.ViewHolder {
        final View mView;

        @BindView(R.id.text_id_colis)
        TextView mIdColis;

        @BindView(R.id.step_last_date)
        TextView mStepLastDate;

        @BindView(R.id.step_last_pays)
        TextView mStepLastPays;

        @BindView(R.id.step_last_description)
        TextView mStepLastDescription;

        @BindView(R.id.fab_delete_colis)
        Button mDeleteButton;

        @BindView(R.id.parcel_description)
        TextView parcelDescription;

        @BindView(R.id.constraint_detail_colis_layout)
        ConstraintLayout constraintDetailColisLayout;

        ColisEntity mColis;

        boolean mDeleteMode;

        private void changeDeleteVisibility() {
            mDeleteButton.setVisibility(mDeleteMode ? View.VISIBLE : View.GONE);
        }

        ViewHolderStepParcel(View view) {
            super(view);
            mView = view;
            mDeleteMode = false;
            ButterKnife.bind(this, mView);

            constraintDetailColisLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // If we aren't in delete mode we call the parcel result search fragment
                    // Otherwise we deactivate the delete mode and make the delete button invisible
                    if (!mDeleteMode) {
                        HistoriqueColisFragment historiqueColisFragment = HistoriqueColisFragment.newInstance(mColis.getIdColis());
                        ((AppCompatActivity) mContext).getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.frame_main, historiqueColisFragment, MainActivity.TAG_PARCEL_RESULT_SEARCH_FRAGMENT)
                                .addToBackStack(null)
                                .commit();
                    } else {
                        mDeleteMode = !mDeleteMode;
                        changeDeleteVisibility();
                    }
                }
            });

            // When we long click on the view, we display Delete button
            constraintDetailColisLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mDeleteMode = !mDeleteMode;
                    changeDeleteVisibility();
                    return true;
                }
            });

            mDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Delete from the memory list
                    mColisList.remove(mColis);

                    // Remove from the content provider
                    ProviderUtilities.deleteColis(mContext, mColis.getIdColis());

                    // Change visibility
                    mDeleteMode = false;
                    changeDeleteVisibility();

                    Snackbar snackbar = Snackbar.make(mView, mColis.getIdColis().concat(" supprimé du suivi"), Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            });
        }
    }
}

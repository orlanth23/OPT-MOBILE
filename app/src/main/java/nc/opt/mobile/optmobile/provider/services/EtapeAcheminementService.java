package nc.opt.mobile.optmobile.provider.services;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import org.chalup.microorm.MicroOrm;

import java.util.ArrayList;
import java.util.List;

import nc.opt.mobile.optmobile.R;
import nc.opt.mobile.optmobile.domain.suivi.EtapeAcheminementDto;
import nc.opt.mobile.optmobile.provider.OptProvider;
import nc.opt.mobile.optmobile.provider.entity.ColisEntity;
import nc.opt.mobile.optmobile.provider.entity.EtapeEntity;
import nc.opt.mobile.optmobile.provider.interfaces.ColisInterface;
import nc.opt.mobile.optmobile.provider.interfaces.EtapeAcheminementInterface;
import nc.opt.mobile.optmobile.utils.DateConverter;

import static nc.opt.mobile.optmobile.provider.interfaces.EtapeAcheminementInterface.COMMENTAIRE;
import static nc.opt.mobile.optmobile.provider.interfaces.EtapeAcheminementInterface.DATE;
import static nc.opt.mobile.optmobile.provider.interfaces.EtapeAcheminementInterface.DESCRIPTION;
import static nc.opt.mobile.optmobile.provider.interfaces.EtapeAcheminementInterface.ID_COLIS;
import static nc.opt.mobile.optmobile.provider.interfaces.EtapeAcheminementInterface.LOCALISATION;
import static nc.opt.mobile.optmobile.provider.interfaces.EtapeAcheminementInterface.PAYS;
import static nc.opt.mobile.optmobile.provider.interfaces.EtapeAcheminementInterface.STATUS;

/**
 * Created by 2761oli on 23/10/2017.
 */

public class EtapeAcheminementService {

    private EtapeAcheminementService() {
    }

    private static final MicroOrm uOrm = new MicroOrm();

    private static final String AND = "=? AND ";

    // Check existence
    private static String mWhereEtapeExistenceWhere = ID_COLIS.concat(AND)
            .concat(DATE).concat(AND)
            .concat(DESCRIPTION).concat(AND)
            .concat(COMMENTAIRE).concat(AND)
            .concat(LOCALISATION).concat(AND)
            .concat(PAYS).concat("=?");

    /**
     * @param context
     * @param idColis
     * @return
     */
    public static List<EtapeEntity> listFromProvider(Context context, String idColis) {
        List<EtapeEntity> etapeList = new ArrayList<>();

        // Query the content provider to get a cursor of Etape
        Cursor cursorListEtape = context.getContentResolver().query(OptProvider.ListEtapeAcheminement.withIdColis(idColis), null, null, null, EtapeAcheminementInterface.DATE + " DESC");

        if (cursorListEtape != null) {
            while (cursorListEtape.moveToNext()) {
                EtapeEntity etapeEntity = getFromCursor(cursorListEtape);
                etapeList.add(etapeEntity);
            }
            cursorListEtape.close();
        }
        return etapeList;
    }

    /**
     * @param context
     * @param idColis
     * @return
     */
    public static boolean delete(Context context, String idColis) {
        // Suppression des étapes d'acheminement
        int result = context.getContentResolver().delete(OptProvider.ListEtapeAcheminement.LIST_ETAPE, ColisInterface.ID_COLIS.concat("=?"), new String[]{idColis});

        return result >= 1;
    }

    /**
     * @param context
     * @param etape
     * @param colis
     * @return
     */
    public static long insert(Context context, EtapeEntity etape, ColisEntity colis) {
        Uri uri = context.getContentResolver().insert(OptProvider.ListEtapeAcheminement.LIST_ETAPE, putToContentValues(etape, colis.getIdColis()));
        return ContentUris.parseId(uri);
    }

    /**
     * Vérification si cette étape était déjà enregistrée, sinon on la créé.
     *
     * @param context
     * @param colis
     * @return
     */
    public static boolean save(Context context, ColisEntity colis) {
        boolean creation = false;
        for (EtapeEntity etape : colis.getEtapeAcheminementArrayList()) {
            if (!exist(context, colis.getIdColis(), etape)) {
                creation = true;
                insert(context, etape, colis);
            }
        }
        return creation;
    }

    /**
     * Return true if a new etape is detected in the list
     *
     * @param context
     * @param colis
     * @return
     */
    public static boolean shouldInsertNewEtape(Context context, ColisEntity colis) {
        for (EtapeEntity etape : colis.getEtapeAcheminementArrayList()) {
            if (!exist(context, colis.getIdColis(), etape)) {
                return true;
            }
        }
        return false;
    }

    private static ContentValues putToContentValues(EtapeEntity etapeEntity, String idColis) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(PAYS, etapeEntity.getPays());
        contentValues.put(LOCALISATION, etapeEntity.getLocalisation());
        contentValues.put(COMMENTAIRE, etapeEntity.getCommentaire());
        contentValues.put(DESCRIPTION, etapeEntity.getDescription());
        contentValues.put(DATE, etapeEntity.getDate());
        contentValues.put(STATUS, etapeEntity.getStatus());
        contentValues.put(ID_COLIS, idColis);
        return contentValues;
    }

    private static EtapeEntity getFromCursor(Cursor cursor) {
        return uOrm.fromCursor(cursor, EtapeEntity.class);
    }

    private static boolean exist(Context context, String idColis, EtapeEntity etape) {
        String[] args = new String[]{idColis,
                String.valueOf(etape.getDate()),
                etape.getDescription(),
                etape.getCommentaire(),
                etape.getLocalisation(),
                etape.getPays()};

        Cursor cursor = context.getContentResolver()
                .query(OptProvider.ListEtapeAcheminement.LIST_ETAPE, null, mWhereEtapeExistenceWhere, args, null);
        if ((cursor != null) && cursor.getCount() > 0) {
            cursor.close();
            return true;
        }
        return false;
    }

    static EtapeAcheminementDto convertToDto(EtapeEntity entity) {
        EtapeAcheminementDto dto = new EtapeAcheminementDto();
        dto.setDate(DateConverter.convertDateEntityToDto(entity.getDate()));
        dto.setCommentaire(entity.getCommentaire());
        dto.setDescription(entity.getDescription());
        dto.setLocalisation(entity.getLocalisation());
        dto.setPays(entity.getPays());
        return dto;
    }

    static EtapeEntity convertToEntity(EtapeAcheminementDto dto) {
        EtapeEntity entity = new EtapeEntity();
        entity.setDate(DateConverter.convertDateDtoToEntity(dto.getDate()));
        entity.setCommentaire(dto.getCommentaire());
        entity.setDescription(dto.getDescription());
        entity.setLocalisation(dto.getLocalisation());
        entity.setPays(dto.getPays());
        return entity;
    }

    public static int getStatusDrawable(EtapeEntity etapeEntity) {
        if (etapeEntity != null && etapeEntity.getStatus() != null) {
            switch (etapeEntity.getStatus()) {
                case "InfoReceived":
                    return R.drawable.ic_status_info_receive;
                case "AttemptFail":
                    return R.drawable.ic_status_attemptfail;
                case "Delivered":
                    return R.drawable.ic_status_delivered;
                case "Exception":
                    return R.drawable.ic_status_exception;
                case "Expired":
                    return R.drawable.ic_status_expired;
                case "InTransit":
                    return R.drawable.ic_status_in_transit;
                case "OutForDelivery":
                    return R.drawable.ic_status_out_for_delivery;
                case "Pending":
                    return R.drawable.ic_status_pending;
                default:
                    return R.drawable.ic_status_pending;
            }
        }
        return R.drawable.ic_status_pending;
    }
}

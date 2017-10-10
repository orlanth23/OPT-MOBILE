package nc.opt.mobile.optmobile.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.google.gson.Gson;

import org.chalup.microorm.MicroOrm;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import nc.opt.mobile.optmobile.domain.Agency;
import nc.opt.mobile.optmobile.domain.Colis;
import nc.opt.mobile.optmobile.domain.EtapeAcheminement;
import nc.opt.mobile.optmobile.domain.Feature;
import nc.opt.mobile.optmobile.domain.FeatureCollection;
import nc.opt.mobile.optmobile.utils.Utilities;

import static nc.opt.mobile.optmobile.provider.EtapeAcheminementInterface.COMMENTAIRE;
import static nc.opt.mobile.optmobile.provider.EtapeAcheminementInterface.DATE;
import static nc.opt.mobile.optmobile.provider.EtapeAcheminementInterface.DESCRIPTION;
import static nc.opt.mobile.optmobile.provider.EtapeAcheminementInterface.ID_COLIS;
import static nc.opt.mobile.optmobile.provider.EtapeAcheminementInterface.LOCALISATION;
import static nc.opt.mobile.optmobile.provider.EtapeAcheminementInterface.PAYS;

/**
 * Created by orlanth23 on 12/08/2017.
 */

public class ProviderUtilities {

    private static final MicroOrm uOrm = new MicroOrm();

    // Check existence
    private static String mWhereEtapeExistenceWhere = ID_COLIS.concat("=? AND ")
            .concat(DATE).concat("=? AND ")
            .concat(DESCRIPTION).concat("=? AND ")
            .concat(COMMENTAIRE).concat("=? AND ")
            .concat(LOCALISATION).concat("=? AND ")
            .concat(PAYS).concat("=?");

    private ProviderUtilities() {

    }

    public static int updateLastUpdate(Context context,@NonNull String idColis, boolean successful) {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Calendar cal = Calendar.getInstance();

        ContentValues contentValues = new ContentValues();
        contentValues.put(ColisInterface.LAST_UPDATE, dateFormat.format(cal.getTime()));
        if (successful) {
            contentValues.put(ColisInterface.LAST_UPDATE_SUCCESSFUL, dateFormat.format(cal.getTime()));
        }

        String where = ColisInterface.ID_COLIS.concat("=?");

        return context.getContentResolver().update(OptProvider.ListColis.LIST_COLIS, contentValues, where, new String[]{idColis});
    }

    private static boolean checkExistence(Context context, String idColis, EtapeAcheminement etape) {
        String[] args = new String[]{idColis,
                etape.getDate(),
                etape.getDescription(),
                etape.getCommentaire(),
                etape.getLocalisation(),
                etape.getPays()};

        Cursor cursor = context.getContentResolver()
                .query(OptProvider.ListEtapeAcheminement.LIST_ETAPE, null, mWhereEtapeExistenceWhere, args, null);
        if ((cursor != null) && (cursor.moveToFirst())) {
            cursor.close();
            return true;
        }
        return false;
    }

    /**
     * Vérification si cette étape était déjà enregistrée, sinon on la créé.
     * @param context
     * @param idColis
     * @param listEtape
     * @return true if at least one etape has been created, false instead.
     */
    public static boolean checkAndInsertEtape(Context context, String idColis, List<EtapeAcheminement> listEtape) {
        boolean creation = false;
        for (EtapeAcheminement etape : listEtape) {
            if (!checkExistence(context, idColis, etape)) {
                // Création
                creation = true;
                context.getContentResolver().insert(OptProvider.ListEtapeAcheminement.LIST_ETAPE, putEtapeToContentValues(etape, idColis));
            }
        }
        return creation;
    }

    public static boolean deleteColis(Context context, String idColis) {
        // Suppression des étapes d'acheminement
        context.getContentResolver().delete(OptProvider.ListEtapeAcheminement.LIST_ETAPE, ID_COLIS.concat("=?"), new String[]{idColis});

        // Suppression du colis
        int result = context.getContentResolver().delete(OptProvider.ListColis.LIST_COLIS, ColisInterface.ID_COLIS.concat("=?"), new String[]{idColis});

        return result == 1;
    }

    public static ContentValues putColisToContentValues(Colis colis) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ColisInterface.ID_COLIS, colis.getIdColis());
        return contentValues;
    }

    private static ContentValues putEtapeToContentValues(EtapeAcheminement etapeAcheminement, String idColis) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(PAYS, etapeAcheminement.getPays());
        contentValues.put(LOCALISATION, etapeAcheminement.getLocalisation());
        contentValues.put(COMMENTAIRE, etapeAcheminement.getCommentaire());
        contentValues.put(DESCRIPTION, etapeAcheminement.getDescription());
        contentValues.put(DATE, etapeAcheminement.getDate());
        contentValues.put(ID_COLIS, idColis);
        return contentValues;
    }

    private static Agency getAgencyFromCursor(Cursor cursor) {
        return uOrm.fromCursor(cursor, Agency.class);
    }

    private static Colis getColisFromCursor(Cursor cursor) {
        return uOrm.fromCursor(cursor, Colis.class);
    }

    private static EtapeAcheminement getEtapeFromCursor(Cursor cursor) {
        return uOrm.fromCursor(cursor, EtapeAcheminement.class);
    }

    public static void populateContentProviderFromAsset(Context context) {
        String json = Utilities.loadStringFromAsset(context, "opt_agencies.json");
        Gson gson = new Gson();

        // Okay we 've got the list in a json let's just getParcelResultFromHtml to an arrayList and send it to the content provider.
        FeatureCollection featureCollection = gson.fromJson(json, FeatureCollection.class);
        ProviderUtilities.populateContentProviderFromFeatureCollection(context, featureCollection);
    }

    private static void populateContentProviderFromFeatureCollection(Context context, FeatureCollection featureCollection) {

        // Delete all the content
        context.getContentResolver().delete(OptProvider.ListAgency.LIST_AGENCY, null, null);

        ContentValues contentValues = new ContentValues();
        Agency agency;
        for (Feature feature : featureCollection.getFeatures()) {
            contentValues.clear();
            agency = feature.getProperties();
            agency.setTYPE_GEOMETRY(feature.getType());
            // Pour le moment seul les points sont gérés.
            if (feature.getGeometry().getType().equals("Point")) {
                agency.setLONGITUDE(feature.getGeometry().getCoordinates()[0]);
                agency.setLATITUDE(feature.getGeometry().getCoordinates()[1]);
            }
            context.getContentResolver().insert(OptProvider.ListAgency.LIST_AGENCY, uOrm.toContentValues(agency));
        }
    }

    /**
     * Query the ContentProvider from the context to get the agency list
     *
     * @param context
     * @return List of recipe
     */
    public static List<Agency> getListAgencyFromContentProvider(Context context) {
        ArrayList<Agency> agencyList = null;

        // Query the content provider to get a cursor of Agency
        Cursor cursorListAgency = context.getContentResolver().query(OptProvider.ListAgency.LIST_AGENCY, null, null, null, null);

        if (cursorListAgency != null) {
            agencyList = new ArrayList<>();
            while (cursorListAgency.moveToNext()) {
                Agency agency = ProviderUtilities.getAgencyFromCursor(cursorListAgency);
                agencyList.add(agency);
            }
            cursorListAgency.close();
        }
        return agencyList;
    }

    public static List<Colis> getListColisFromContentProvider(Context context) {
        List<Colis> colisList = null;

        // Query the content provider to get a cursor of Colis
        Cursor cursorListColis = context.getContentResolver().query(OptProvider.ListColis.LIST_COLIS, null, null, null, null);

        if (cursorListColis != null) {
            colisList = new ArrayList<>();
            while (cursorListColis.moveToNext()) {
                Colis colis = ProviderUtilities.getColisFromCursor(cursorListColis);
                List<EtapeAcheminement> listEtape = ProviderUtilities.getListEtapeFromContentProvider(context, colis.getIdColis());
                colis.setEtapeAcheminementArrayList(listEtape);
                colisList.add(colis);
            }
            cursorListColis.close();
        }
        return colisList;
    }

    public static List<EtapeAcheminement> getListEtapeFromContentProvider(Context context, String idColis) {
        List<EtapeAcheminement> etapeList = null;

        // Query the content provider to get a cursor of Etape
        Cursor cursorListEtape = context.getContentResolver().query(OptProvider.ListEtapeAcheminement.withIdColis(idColis), null, null, null, null);

        if (cursorListEtape != null) {
            etapeList = new ArrayList<>();
            while (cursorListEtape.moveToNext()) {
                EtapeAcheminement etape = ProviderUtilities.getEtapeFromCursor(cursorListEtape);
                etapeList.add(etape);
            }
            cursorListEtape.close();
        }
        return etapeList;
    }
}

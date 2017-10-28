package nc.opt.mobile.optmobile.utils;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by 2761oli on 23/10/2017.
 */

public class DateConverter {
    private static final String TAG = DateConverter.class.getName();
    private static final SimpleDateFormat simpleDtoDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.FRANCE);
    private static final SimpleDateFormat simpleUiDateFormat = new SimpleDateFormat("dd MMM yyyy à HH:mm", Locale.FRANCE);
    private static final SimpleDateFormat simpleEntityDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.FRANCE);

    /**
     * Transformation d'une date de type yyyyMMddHHmmss vers le format dd MMM yy HH:mm
     * @param dateEntity
     * @return
     */
    public static String convertDateEntityToUi(Long dateEntity) {
        try {
            return simpleUiDateFormat.format(simpleEntityDateFormat.parse(String.valueOf(dateEntity)));
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    /**
     * Transformation d'une date de type dd/MM/yyyy HH:mm:ss vers le format yyyyMMddHHmmss en Long
     *
     * @param dateDto
     * @return Long
     */
    public static Long convertDateDtoToEntity(String dateDto) {
        try {
            String dateConverted = simpleEntityDateFormat.format(simpleDtoDateFormat.parse(dateDto));
            return Long.parseLong(dateConverted);
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (NullPointerException e1) {
            Log.e(TAG, e1.getMessage(), e1);
        }
        return 0L;
    }

    /**
     * Transformation d'une date de type yyyyMMddHHmmss vers le format dd/MM/yyyy HH:mm:ss en Long
     *
     * @param dateEntity
     * @return String
     */
    public static String convertDateEntityToDto(Long dateEntity) {
        try {
            Date dateConverted = simpleEntityDateFormat.parse(String.valueOf(dateEntity));
            return simpleDtoDateFormat.format(dateConverted);
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    /**
     * Va renvoyer la date du jour au format yyyyMMddHHmmss en Long
     *
     * @return Long
     */
    public static Long getNowEntity() {
        Calendar cal = Calendar.getInstance();
        return Long.parseLong(simpleEntityDateFormat.format(cal.getTime()));
    }

    /**
     * Va renvoyer la date du jour au format dd/MM/yyyy HH:mm:ss en String
     *
     * @return String
     */
    public static String getNowDto() {
        Calendar cal = Calendar.getInstance();
        return simpleDtoDateFormat.format(cal.getTime());
    }
}

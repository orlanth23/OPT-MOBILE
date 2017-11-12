package nc.opt.mobile.optmobile.domain.suiviColis;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by 2761oli on 05/10/2017.
 */

public class ColisDto implements Parcelable {

    private String idColis;
    private String description;
    private List<EtapeAcheminementDto> etapeAcheminementDtoArrayList;

    public ColisDto() {
    }

    public ColisDto(String idColis, List<EtapeAcheminementDto> etapeAcheminementDtoArrayList) {
        this.idColis = idColis;
        this.etapeAcheminementDtoArrayList = etapeAcheminementDtoArrayList;
    }

    public String getIdColis() {
        return idColis;
    }

    public void setIdColis(String idColis) {
        this.idColis = idColis;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<EtapeAcheminementDto> getEtapeAcheminementDtoArrayList() {
        return etapeAcheminementDtoArrayList;
    }

    public void setEtapeAcheminementDtoArrayList(List<EtapeAcheminementDto> etapeAcheminementDtoArrayList) {
        this.etapeAcheminementDtoArrayList = etapeAcheminementDtoArrayList;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.idColis);
        dest.writeString(this.description);
        dest.writeTypedList(this.etapeAcheminementDtoArrayList);
    }

    protected ColisDto(Parcel in) {
        this.idColis = in.readString();
        this.description = in.readString();
        this.etapeAcheminementDtoArrayList = in.createTypedArrayList(EtapeAcheminementDto.CREATOR);
    }

    public static final Creator<ColisDto> CREATOR = new Creator<ColisDto>() {
        @Override
        public ColisDto createFromParcel(Parcel source) {
            return new ColisDto(source);
        }

        @Override
        public ColisDto[] newArray(int size) {
            return new ColisDto[size];
        }
    };
}

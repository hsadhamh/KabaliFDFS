package factor.app.fdfs.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by hassanhussain on 7/16/2016.
 */
public class CinemaInfo implements Parcelable{
    @JsonIgnoreProperties(ignoreUnknown=true)
    @JsonProperty("theatre-name")
    String CinemaName;
    @JsonIgnoreProperties(ignoreUnknown=true)
    @JsonProperty("theatre-link")
    String CinemaLink;
    @JsonIgnoreProperties(ignoreUnknown=true)
    @JsonProperty("theatre-status")
    String CinemaStatus;
    @JsonIgnoreProperties(ignoreUnknown=true)
    @JsonProperty("theatre-open")
    boolean bookingOpen;

    public String getCinemaStatus() {
        return CinemaStatus;
    }

    public void setCinemaStatus(String cinemaStatus) {
        CinemaStatus = cinemaStatus;
    }

    public boolean isBookingOpen() {
        return bookingOpen;
    }

    public void setBookingOpen(boolean bookingOpen) {
        this.bookingOpen = bookingOpen;
    }

    protected CinemaInfo(Parcel in) {
        CinemaName = in.readString();
        CinemaLink = in.readString();
        CinemaStatus = in.readString();
        bookingOpen = false;
    }

    public static final Creator<CinemaInfo> CREATOR = new Creator<CinemaInfo>() {
        @Override
        public CinemaInfo createFromParcel(Parcel in) {
            return new CinemaInfo(in);
        }

        @Override
        public CinemaInfo[] newArray(int size) {
            return new CinemaInfo[size];
        }
    };

    public CinemaInfo() {
        CinemaName = "";
        CinemaLink = "";
        CinemaStatus = "";
    }

    public String getCinemaLink() {
        return CinemaLink;
    }

    public void setCinemaLink(String cinemaLink) {
        CinemaLink = cinemaLink;
    }

    public String getCinemaName() {
        return CinemaName;
    }

    public void setCinemaName(String cinemaName) {
        CinemaName = cinemaName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(CinemaName);
        dest.writeString(CinemaLink);
        dest.writeString(CinemaStatus);
    }
}

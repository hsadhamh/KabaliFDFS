package factor.app.fdfs.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hassanhussain on 7/16/2016.
 */
public class MovieInTheatresInfo implements Parcelable {
    @JsonIgnoreProperties(ignoreUnknown=true)
    @JsonProperty("movie-info")
    MovieInfo movie;
    @JsonIgnoreProperties(ignoreUnknown=true)
    @JsonProperty("cinemas")
    List<CinemaInfo> cinemas = new ArrayList<>();
    @JsonIgnoreProperties(ignoreUnknown=true)
    @JsonProperty("booking-date")
    String date;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @JsonIgnoreProperties(ignoreUnknown=true)
    @JsonProperty("city-name")
    String city;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    protected MovieInTheatresInfo(Parcel in) {
        movie = in.readParcelable(MovieInfo.class.getClassLoader());
        cinemas = in.createTypedArrayList(CinemaInfo.CREATOR);
        date = in.readString();
        city = in.readString();
    }

    public static final Creator<MovieInTheatresInfo> CREATOR = new Creator<MovieInTheatresInfo>() {
        @Override
        public MovieInTheatresInfo createFromParcel(Parcel in) {
            return new MovieInTheatresInfo(in);
        }

        @Override
        public MovieInTheatresInfo[] newArray(int size) {
            return new MovieInTheatresInfo[size];
        }
    };

    public MovieInTheatresInfo() { }

    public MovieInfo getMovie() {
        return movie;
    }

    public void setMovie(MovieInfo movie) {
        this.movie = movie;
    }

    public List<CinemaInfo> getCinemas() {
        return cinemas;
    }

    public void setCinemas(List<CinemaInfo> cinemas) {
        this.cinemas = cinemas;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(movie, flags);
        dest.writeTypedList(cinemas);
        dest.writeString(date);
        dest.writeString(city);
    }
}

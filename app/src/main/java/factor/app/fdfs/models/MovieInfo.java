package factor.app.fdfs.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class MovieInfo implements Parcelable {
	@JsonIgnoreProperties(ignoreUnknown=true)
	@JsonProperty("movie-name")
	String Name;
	@JsonIgnoreProperties(ignoreUnknown=true)
	@JsonProperty("movie-url")
	String relativeURL;
	@JsonIgnoreProperties(ignoreUnknown=true)
	@JsonProperty("movie-id")
	String MovieID;
	@JsonIgnoreProperties(ignoreUnknown=true)
	@JsonProperty("movie-release")
	String releaseDate;
	@JsonIgnoreProperties(ignoreUnknown=true)
	@JsonProperty("movie-languages")
	List<String> languages;
	@JsonIgnoreProperties(ignoreUnknown=true)
	@JsonProperty("running")
	boolean isRunning;

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public String getRelativeURL() {
		return relativeURL;
	}

	public void setRelativeURL(String relativeURL) {
		this.relativeURL = relativeURL;
	}

	public String getMovieID() {
		return MovieID;
	}

	public void setMovieID(String movieID) {
		MovieID = movieID;
	}

	public String getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(String releaseDate) {
		this.releaseDate = releaseDate;
	}

	public List<String> getLanguages() {
		return languages;
	}

	public void setLanguages(List<String> languages) {
		this.languages = languages;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean running) {
		isRunning = running;
	}

	public MovieInfo(){
		Name = "";
		relativeURL = "";
		MovieID = "";
		releaseDate = "";
		languages = new ArrayList<String>();
	}

	protected MovieInfo(Parcel in) {
		Name = in.readString();
		relativeURL = in.readString();
		MovieID = in.readString();
		releaseDate = in.readString();
		languages = in.createStringArrayList();
		isRunning = in.readByte() != 0;
	}

	public static final Creator<MovieInfo> CREATOR = new Creator<MovieInfo>() {
		@Override
		public MovieInfo createFromParcel(Parcel in) {
			return new MovieInfo(in);
		}

		@Override
		public MovieInfo[] newArray(int size) {
			return new MovieInfo[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(Name);
		dest.writeString(relativeURL);
		dest.writeString(MovieID);
		dest.writeString(releaseDate);
		dest.writeStringList(languages);
		dest.writeByte((byte) (isRunning ? 1 : 0));
	}
}

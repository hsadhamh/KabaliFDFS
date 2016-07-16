package factor.app.fdfs;

import java.util.ArrayList;
import java.util.List;

public class MovieInfo {
	String Name;
	String relativeURL;
	String MovieID;
	String releaseDate;
	List<String> languages;
	boolean isRunning;
	
	MovieInfo(){
		Name = "";
		relativeURL = "";
		MovieID = "";
		releaseDate = "";
		languages = new ArrayList<String>();
	}

}

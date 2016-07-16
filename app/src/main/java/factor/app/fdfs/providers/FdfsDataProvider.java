package factor.app.fdfs.providers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import factor.app.fdfs.models.CinemaInfo;
import factor.app.fdfs.models.MovieInTheatresInfo;
import factor.app.fdfs.models.MovieInfo;
import factor.app.fdfs.receivers.AlarmReceiver;

public class FdfsDataProvider {
	
    public static String mStrBookMyShow = "https://in.bookmyshow.com/";

	public static List<MovieInfo> getListRunningMovies(String URL) {
        List<MovieInfo> listMovies = new ArrayList<MovieInfo>();
		Document doc = null;
		try {
			doc = Jsoup
					.connect(URL)
					.header("Accept-Encoding", "gzip, deflate")
				    .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
				    .maxBodySize(0)
				    .timeout(600000)
				    .get();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		if(doc != null)
        {
			String title = doc.title();  

            Elements mMovieSection = doc.getElementsByClass("now-showing");
			if(mMovieSection.isEmpty()) { return listMovies; }

			Element firstSection = mMovieSection.get(0);
			if(firstSection == null) { return listMovies; }

            Element sectionRow = firstSection.select("div.__col-now-showing").first();
            if(sectionRow == null) { return listMovies; }

            Element moviesDiv = sectionRow.select("div.mv-row").first();
            if(moviesDiv == null) { return listMovies; }

            Elements divsList = moviesDiv.getElementsByClass("movie-card");
            int i = 1;

            for(Element movie : divsList)
            {
                Element detail = movie.getElementsByClass("detail").first();
                if(detail == null)
                    continue;

                Element movInfo= detail.select("div.__name > a").first();
                if(movInfo == null)
                    continue;

                MovieInfo movieObj = new MovieInfo();
                movieObj.setRunning(true);

                Elements liList = detail.select("ul.language-list > li");
                List<String> langsList = new ArrayList<>();
                for(Element li : liList)
                {
                    langsList.add(li.text());
                }
                movieObj.setLanguages(langsList);
                movieObj.setRelativeURL(movInfo.attr("href"));
                movieObj.setName(movInfo.attr("title"));
                movieObj.setMovieID(movieObj.getRelativeURL().substring(movieObj.getRelativeURL().lastIndexOf('/')+1));
                listMovies.add(movieObj);
                i++;
            }
        }
        return listMovies;
	}

	public static List<MovieInfo> getListUpcomingMovies(String URL){
        List<MovieInfo> listMovies = new ArrayList<>();
		Document doc = getHtmlDocument(URL);
		if(doc != null)
        {
			Element mSection = doc.getElementsByClass("release-calandar").first();
            if(mSection == null){ return listMovies; }

			Elements mMovieSection = mSection.getElementsByClass("movie-card-container");
			if(mMovieSection.isEmpty()) { return listMovies; }

            int i =1;
            for(Element movie : mMovieSection)
            {
                Element release = movie.select(".release-info").first();
                String year = movie.attr("data-year");
                Element details  = movie.getElementsByClass("detail").first();
                if(details != null)
                {
                    Element anchor = details.select(".__name > a").first();
                    String rel_string = release.text();
                    String href = anchor.attr("href");
                    String movieName = anchor.text();

                    Element langs = details.getElementsByClass("languages").first();
                    String slang = langs.text();
                    List<String> langList  = new ArrayList<>();

                    MovieInfo movInfo = new MovieInfo();
                    movInfo.setName(movieName);
                    movInfo.setRunning(false);
                    movInfo.setLanguages(langList);
                    movInfo.setRelativeURL(href);
                    movInfo.setMovieID(movInfo.getRelativeURL().substring(movInfo.getRelativeURL().lastIndexOf('/')+1));
                    movInfo.setReleaseDate(rel_string + " " + year);
                    listMovies.add(movInfo);
                    i++;
                }
            }
        }
        return listMovies;
	}

    public static List<MovieInfo> getFullMoviesList(String sCity){
        String strNowShwoingLink = mStrBookMyShow + sCity + "/movies/nowshowing";
        String strComingSoonLink = mStrBookMyShow + sCity + "/movies/comingsoon";
        List<MovieInfo> mMovies = new ArrayList<>();
        mMovies.addAll(getListRunningMovies(strNowShwoingLink));
        mMovies.addAll(getListUpcomingMovies(strComingSoonLink));
        return mMovies;
    }
	
	public static List<CinemaInfo> getCinemasList(String sCity){
        String URL = mStrBookMyShow+""+sCity+"/cinemas";
        List<CinemaInfo> listCinemas = new ArrayList<>();
		Document doc = getHtmlDocument(URL);
		if(doc != null)
        {
			Element mSection = doc.getElementsByClass("cinema-brand-list").first();
			Elements cinemaSection = mSection.getElementsByClass("__cinema-tiles");
			if(!cinemaSection.isEmpty())
			{
				int i =1;
				for(Element cinema : cinemaSection)
				{
					Element a = cinema.select(".__cinema-text > a").first();
					String sCineName = a.text();
					String sCineURL = a.attr("href");

                    CinemaInfo cine = new CinemaInfo();
                    cine.setCinemaLink(sCineURL);
                    cine.setCinemaName(sCineName);
                    listCinemas.add(cine);
				}
			}
        }
        return listCinemas;
	}

    public static boolean isBookingOpenForTheDay(String URL, String date){
        Document doc = getHtmlDocument(URL);
        if(doc != null) {
            Elements eles = doc.select("ul#showDates > li");
            if(eles == null) {
                return false;
            }
            for(Element e : eles)
            {
                Element a = e.select("a").first();
                if(a == null)
                    return false;
                String href = a.attr("href");
                if(href.contains(date)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isBookingOpenForTheMovie(String URL, String date, String movieID){
        String URL_Date = URL + "/" + date;
        Document doc = getHtmlDocument(URL_Date);
        if(doc != null) {
            Elements eles = doc.select("ul#showDates > li");
            if(eles == null) {
                return false;
            }
            for(Element e : eles)
            {
                Element a = e.select("a").first();
                if(a == null)
                    return false;
                String href = a.attr("href");
                boolean b = e.hasClass("_active");
                if(href.contains(date) && b) {
                    return doc.html().contains(movieID);
                }
            }
        }
        return false;
    }

    public static Document getHtmlDocument(String URL){
        Document doc = null;
        try {
            doc = Jsoup
                    .connect(URL)
                    .header("Accept-Encoding", "gzip, deflate")
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                    .maxBodySize(0)
                    .timeout(600000)
                    .get();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return doc;
    }

    public static void writeToFile(Context context, String data) {
        try {
            OutputStreamWriter outputStreamWriter =
                    new OutputStreamWriter(context.openFileOutput("saved_config.info", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public static String getSavedInfoString(MovieInTheatresInfo ticket) throws JSONException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(ticket);
        return json;
    }

    public static MovieInTheatresInfo getSavedInfoObject(String json) throws JSONException, IOException {
        MovieInTheatresInfo dataHolder = new ObjectMapper().readValue(json, MovieInTheatresInfo.class);
        return dataHolder;
    }

    public static String readFromFile(Context context) {
        String ret = "";
        try {
            InputStream inputStream = context.openFileInput("saved_config.info");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }
                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }
        return ret;
    }

    public static boolean doesFileExists(Context context){
        String sJson = FdfsDataProvider.readFromFile(context);
        return (sJson != null && sJson.length() > 0);
    }

    public static boolean deleteFile(Context context){
        context.deleteFile("saved_config.info");
        return true;
    }

    // Setup a recurring alarm every half hour
    public static void scheduleAlarm(Context context) {
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(context, AlarmReceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(context, AlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Setup periodic alarm every 5 seconds
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                5 * 60 * 1000, pIntent);
    }

    public static void cancelAlarm(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(context, AlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }

    public static boolean appInstalledOrNot(Context context, String uri) {
        PackageManager pm = context.getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        }
        catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }
}

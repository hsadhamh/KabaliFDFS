package factor.app.fdfs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class FdfsDataProvider {
	
    public static String mStrBookMyShow = "https://in.bookmyshow.com/";
	/*	String url = "https://in.bookmyshow.com/bengaluru/cinemas/pvr-market-city-bengaluru/PVBM/20160716";
		String url1 = "https://in.bookmyshow.com/bengaluru/cinemas/pvr-market-city-bengaluru/PVBM/20160717";
			doc = Jsoup.connect(url).header("Accept-Encoding", "gzip, deflate")
				    .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
				    .maxBodySize(0)
				    .timeout(600000)
				    .get();
			doc1 = Jsoup.connect(url1).header("Accept-Encoding", "gzip, deflate")
				    .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
				    .maxBodySize(0)
				    .timeout(600000)
				    .get();
			
			Elements eles = doc.select("ul#showDates > li");
			if(eles != null)
			{
				for(Element e : eles)
				{
					Element a = e.select("a").first();
					String href = a.attr("href");
					boolean b = e.hasClass("_active");
					
					System.out.println("Href ["+href+"] : " + b);
				}
			}
			
			eles = doc1.select("ul#showDates > li");
			if(eles != null)
			{
				for(Element e : eles)
				{
					Element a = e.select("a").first();
					String href = a.attr("href");
					boolean b = e.hasClass("_active");
					
					System.out.println("Href ["+href+"] : " + b);
				}
	}*/

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
			System.out.println("title is: " + title);
			
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
                movieObj.isRunning = true;

                Elements liList = detail.select("ul.language-list > li");
                String langList="";
                for(Element li : liList)
                {
                    movieObj.languages.add(li.text());
                }

                movieObj.relativeURL = movInfo.attr("href");
                movieObj.Name = movInfo.attr("title");

                movieObj.MovieID = movieObj.relativeURL.substring(movieObj.relativeURL.lastIndexOf('/')+1);

                String stringDisplay = "["+movieObj.Name+"]" + "["+movieObj.MovieID+"]"
                                    +"["+movieObj.relativeURL+"]";
                System.out.println(i + "] Movie is: " + stringDisplay);
                listMovies.add(movieObj);
                i++;
            }
        }
        return listMovies;
	}

	public static List<MovieInfo> getListUpcomingMovies(String URL){
        List<MovieInfo> listMovies = new ArrayList<>();
		Document doc = null;
		try {
			doc = Jsoup.connect(URL).header("Accept-Encoding", "gzip, deflate")
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

                    MovieInfo movInfo = new MovieInfo();
                    movInfo.Name = movieName;
                    movInfo.isRunning = false;
                    movInfo.languages.add(slang);
                    movInfo.relativeURL = href;
                    movInfo.MovieID = movInfo.relativeURL.substring(movInfo.relativeURL.lastIndexOf('/')+1);
                    movInfo.releaseDate = rel_string + " " + year;

                    String display = "["+ movInfo.Name +"]" + "["+movInfo.MovieID+"]" + "["+movInfo.relativeURL+"]" + "["+movInfo.releaseDate+"]";

                    System.out.println(i+"] Movie is :" + display);
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
		Document doc = null;
		try {
			doc = Jsoup.connect(URL).header("Accept-Encoding", "gzip, deflate")
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
					
					System.out.println(i++ +"] " + sCineName + "("+sCineURL+")");
				}
			}
        }
        return listCinemas;
	}
}

package webcrawler;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import movies.*;

public class IMDBMovieCatalog {
	public MovieCatalog movCatalog;

	public IMDBMovieCatalog() {
		movCatalog = new MovieCatalog();
	}
	  
	public String getTitle(Element link){
		return link.getElementsByAttribute("title").attr("title");//we get the title
	}
	
	public void getGenre(Element link, List<String> list){
		Elements genres = link.getElementsByClass("cert-runtime-genre").get(0).getElementsByTag("span");//the genre list
		for(int j=0;j<genres.size();j+=2)
		{
			list.add(genres.get(j).ownText());
		}
	}
	
	public BigInteger getScore(Element link){
		Elements rating = link.getElementsByClass("metascore");
		if(rating.size() != 0)
		{
			return BigInteger.valueOf(Integer.parseInt(rating.get(0).getElementsByTag("strong").get(0).ownText()));
		}
		else
			return null;
	}
	
	public void getPeople(Element link, List<String> director_list, List<String> star_list){
		Elements people = link.getElementsByClass("txt-block");//the director + stars

		Elements directors = people.get(0).getElementsByTag("a");//stars
		for(int j=0;j<directors.size();j++)
		{
			director_list.add(directors.get(j).ownText());
		}

		Elements stars = people.get(1).getElementsByTag("a");//stars
		for(int j=0;j<stars.size();j++)
		{
			star_list.add(stars.get(j).ownText());
		}
	}
	
	public void fetchImdbCatalog(String url) throws IOException
	{
		
		Document doc;
		System.out.println("Teste02 ");
		doc = Jsoup.connect(url).get(); //Gets IMDB HTML
		System.out.println("Teste03");	
		Element main = doc.getElementById("main");//Gets the "main" part
		Elements links = main.getElementsByClass("overview-top");//Gets the "overview-top" part that contains info about all the movies

		for(int i=0;i<links.size();i++)//for each movie
		{
			Movie m = new Movie();
			m.setDirectorList(new DirectorList());
			m.setGenresList(new GenresList());
			m.setStarList(new StarList());
			
			m.setTitle(getTitle(links.get(i)));

			getGenre(links.get(i),m.getGenresList().getGenre());
			
			m.setScore(getScore(links.get(i)));

			getPeople(links.get(i),m.getDirectorList().getDirector(),m.getStarList().getStar());
			//And we put it all into objects
			movCatalog.getMovie().add(m);

		}
	}

}

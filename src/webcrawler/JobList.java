package webcrawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;


public class JobList extends Observable{
	private ArrayList<Job<String,String>> list;
	
	public JobList() {
		list = new ArrayList<Job<String, String>>();
	}
	
	public int size()
	{
		return list.size();
	}
	
	public void addJob(String job) {
		if (job.toLowerCase().startsWith("http://") || job.toLowerCase().startsWith("https://")) {
			synchronized (list) {
				list.add(new Job<String, String>(Job.url, job));
			}
			
			this.setChanged();
			this.notifyObservers(null);
		} else {
			//list.add(new Job<String, String>(Job.file, job));
		}
	}
	
	public void addJobBatch(String fileBatch) throws FileNotFoundException, IOException{
		
			String job = null;
			File file = new File(fileBatch);
			BufferedReader reader;
			
			reader = new BufferedReader(new FileReader(file));
			
			while((job = reader.readLine()) != null) {
				addJob(job);
			}
			reader.close();
			file.delete();
	}
	
	/**
	 * Removes a job from the list.
	 * @return String job, or null if the list is empty
	 */
	public String removeJob(){
		synchronized (list) {
			return (list.isEmpty()) ? null : list.remove(0).content;
		}
	}

	public class Job <TYPE,CONTENT> {
		public static final String url = "URL";
		public static final String file = "FILE";
		public final String type;
		public final String content;
		public Job(String type, String content) {
			this.type = type;
			this.content = content;
		}
		public boolean isURLJob() {
			return (type.equals(Job.url) ? true : false );
		}
		public boolean isFILEJob() {
			return (type.equals(Job.file) ? true : false );
		}
	}
}

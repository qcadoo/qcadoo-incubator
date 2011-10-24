import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.*;

public class Archive {

	String folder;
	static File file;
	public Archive(String link) throws IOException
	{
		URL url = new URL(link);
		file = DownloadZip(url);
		this.folder = ReadZip(file);
	}
	public static File DownloadZip(URL url) throws IOException{

			File dir = new File("downloads/");
				if (!dir.exists()) {
			          dir.mkdirs();
			      }
				
				InputStream in = new BufferedInputStream(url.openStream(), 1024);
				File file = File.createTempFile("plugin", ".zip", dir);
			      OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
			      byte[] buffer = new byte[1024];
			      int len = in.read(buffer);
			      while (len >= 0) {
			          out.write(buffer, 0, len);
			          len = in.read(buffer);
			      }
			      in.close();
			      out.close();
				return file;
	}
	
	
	public static ArrayList<String> GetDescriptor(String link) throws ZipException, IOException{
		URL url = new URL(link);
		ArrayList<String> descriptorList = new ArrayList<String>();
		ZipFile zipFile = new ZipFile(Archive.file);
	      Pattern pattern = Pattern.compile(".*/qcadoo.plugin.xml$");
	      Enumeration<?> entries = zipFile.entries();
	      while(entries.hasMoreElements()) {
		        ZipEntry entry = (ZipEntry)entries.nextElement();
		        Matcher matcher = pattern.matcher(entry.getName());
	        	boolean bool = matcher.matches();
	        	if(bool){
	        		File dir1 = new File(".");
	        		descriptorList.add(dir1.getCanonicalPath() +"/downloads/" +entry.getName());

		        }
		       
	      }
	      return descriptorList;
	}

	 public static final void copyInputStream(InputStream in, OutputStream out)
			  throws IOException
			  {
			    byte[] buffer = new byte[1024];
			    int len;

			    while((len = in.read(buffer)) >= 0)
			      out.write(buffer, 0, len);

			    in.close();
			    out.close();
			  }
	 public static String ReadZip(File file){
		 ArrayList<String> linkList = new ArrayList<String>();
		 String[] dirName=null;
		 Enumeration<?> entries;
		    ZipFile zipFile;
		 try {
			 
		      zipFile = new ZipFile(file);

		      entries = zipFile.entries();
		      Pattern pattern = Pattern.compile(".*/qcadoo.plugin.xml$");
		        
	        	
		      while(entries.hasMoreElements()) {
		        ZipEntry entry = (ZipEntry)entries.nextElement();
		        Matcher matcher = pattern.matcher(entry.getName());
	        	boolean bool = matcher.matches();
		        if(entry.isDirectory()) {
		          (new File("downloads/"+entry.getName())).mkdir();
		          continue;
		        }
		        if(bool){
		        	linkList.add(entry.getName());
		        	dirName = entry.getName().split("/");
		        	copyInputStream(zipFile.getInputStream(entry),
		        	new BufferedOutputStream(new FileOutputStream("downloads/"+entry.getName())));
		        }
		      }
		      zipFile.close();
		    } catch (IOException E) {
		      E.printStackTrace();
		      
		    }
		 return dirName[0];
		 
	 }
}


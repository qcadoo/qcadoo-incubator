import java.io.*;
import java.util.ArrayList;
import javax.xml.parsers.*;

import org.w3c.dom.*;

public class Parser {
	public static String returnCanonicalPath() throws IOException{
		File dir = new File(".");
		return dir.getCanonicalPath();
	}
	public static void splitPath(String path) throws IOException{
		path.replaceFirst(returnCanonicalPath(), "sd");
		//System.out.println(path + " "+returnCanonicalPath());
		//System.out.println("zmiana PATH: " +path);
	}
   public static ArrayList<Descriptor> ParserDescriptorsFile() 
      {
          NodeList list = null;
          ArrayList<Descriptor> listOfDescriptors = new ArrayList<Descriptor>();
          ArrayList<String> excludeArr = new ArrayList<String>();
          
          File dir1 = new File(".");
          try 
          {
              
              File descriptors = new File(dir1.getCanonicalFile()+"/descriptors.xml");
              if (descriptors.exists()) 
              {
                  DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                  DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                  Document doc = dBuilder.parse(descriptors);
                  doc.getDocumentElement().normalize();
                  list = doc.getElementsByTagName("group");
                  
                  for (int i = 0; i < list.getLength(); i++) 
                  {
                      
                      Element listElement = (Element) list.item(i);
                      NodeList list2 = listElement.getElementsByTagName("descriptor");
                      NodeList list3 = listElement.getElementsByTagName("archive");
                      
                      
                      String group = listElement.getAttribute("name");
                      
                      for(int j=0;j<list2.getLength();j++)
                      {
                          Element listElementMes = (Element) list2.item(j);
                                     
                          String link = listElementMes.getAttribute("url");
                          Descriptor descriptorObject = new Descriptor(link, group);
                          listOfDescriptors.add(descriptorObject);
                         // System.out.println(link);
                      }
                      for(int j=0; j<list3.getLength();j++){
                    	  
                    	  Element exclude = (Element) list3.item(j);
                    	  
                    	  NodeList excludeList = exclude.getElementsByTagName("exclude");
                    	  String link = exclude.getAttribute("url");
                    	  Archive archive = new Archive(link);
                    	  
                    	  
                    	  for(int k=0; k<excludeList.getLength();k++){
                    		  Element exclude2 = (Element) excludeList.item(k);
                    		  String exludePath = exclude2.getAttribute("path");
                    		  excludeArr.add(dir1.getCanonicalPath() + "/downloads/"+ archive.folder+"/" + exludePath);
                    	  }
                    	  
                    	 
                    	  ArrayList<String> s = Archive.GetDescriptor(link);
                    	  String[] sArray = new String[s.size()];
                    	  s.toArray(sArray);
                    	  for (int k=0; k<sArray.length; k++){
                    		  
                    		  if (!excludeArr.contains(sArray[k])){
                    			  Descriptor archiveDescriptor = new Descriptor(sArray[k], group);
                    			  listOfDescriptors.add(archiveDescriptor);
                    		  }
                    		  
                    	  }
                      }
                  }
              } 
              else 
              {
                  System.out.println("plik nie zostal wczytany");
              }
          }
          catch (Exception e) 
          {
              e.printStackTrace();
          }
          return listOfDescriptors;
      }
   

   public static ArrayList<Plugin> ParserPluginFiles(){
       ArrayList<Descriptor> list = ParserDescriptorsFile();
       ArrayList<Plugin> listOfPlugin = new ArrayList<Plugin>();
       String url, id, group = null;
       ArrayList<String> listOfDependencies = new ArrayList<String>();
       
       try{
           for(int i = 0; i < list.size(); i++){
               listOfDependencies = new ArrayList<String>();
               url = list.get(i).link;
               group = list.get(i).group;
               DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
               DocumentBuilder builder = factory.newDocumentBuilder();
               Document document = builder.parse(url);
               document.getDocumentElement().normalize();
               NodeList pluginList = document.getElementsByTagName("plugin");
               Element element = (Element) pluginList.item(0);
               id = element.getAttribute("plugin");
               int j=1;

               if((Element) pluginList.item(1)==null)
               {
                   listOfDependencies.add(null);
               }else{
               while((Element) pluginList.item(j) != null)
               {
                   
                   
                   element = (Element) pluginList.item(j);
                   
                       listOfDependencies.add(element.getTextContent());
                       j++;
               }    
               
               }
               String[] dependencies = new String[listOfDependencies.size()];
               listOfDependencies.toArray(dependencies);
               
               Plugin pluginObject = new Plugin(id, dependencies, group);
               listOfPlugin.add(pluginObject);
           }
       }catch (Exception e){
               e.printStackTrace();
           }
       return listOfPlugin;
   }
}
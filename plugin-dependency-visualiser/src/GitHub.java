import org.idlesoft.libraries.ghapi.GitHubAPI;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class GitHub {
public static String[] ParserCommitFile() throws SAXException, IOException, ParserConfigurationException{
		
		GitHubAPI gapi = new GitHubAPI();
	    gapi.goStealth();
	    
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
       
        Repository[] array = GitHub.ParserRepoFile().clone();
	    String[] url = new String[array.length];
	    for(int i=0; i<array.length; i++){
	    	url[i] = gapi.commits.list(array[i].userName, array[i].repositoryName, "master").url;
		       url[i] = url[i].replaceAll("json", "xml");
		       
	    	
	    }
        String[] sha = new String[url.length];
         for (int i=0; i<url.length; i++){
        	Document document = builder.parse(url[i]) ;
        	document.getDocumentElement().normalize();
        	NodeList commitList = document.getElementsByTagName("commit");
        	
        	Element element = (Element) commitList.item(0);
        	NodeList tree = element.getElementsByTagName("tree");
        	Element elementTree = (Element) tree.item(0);
        	sha[i] = elementTree.getTextContent();
        	//System.out.println(i+ " " +sha[i]);
        }
         return sha;
	}
	public static Repository[] ParserRepoFile(){
	       ArrayList<Repository> listOfRepositories = new ArrayList<Repository>();
	       GitHubAPI gapi = new GitHubAPI();
	       gapi.goStealth();
	       
	       String url = gapi.repo.search("Qcadoo").url;
	       url = url.replaceAll("json", "xml");
	       //System.out.println(url);
	       
	       Repository[] arrayRepository = null;
	       try{
	           DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	           DocumentBuilder builder = factory.newDocumentBuilder();
	           Document document = builder.parse(url);
	           document.getDocumentElement().normalize();
               NodeList repoList = document.getElementsByTagName("repository");
               
               for (int i = 0; i<repoList.getLength(); i++){
            	   Element element = (Element) repoList.item(i);
            	   NodeList usernameList = element.getElementsByTagName("username");
            	   Element elementUser = (Element) usernameList.item(0);
            	   String userName = elementUser.getTextContent();
            	   
            	   
            	   NodeList name = element.getElementsByTagName("name");
            	   Element elementName = (Element) name.item(0);
            	   String repositoryName = elementName.getTextContent();
            	   
            	   NodeList descriptionList = element.getElementsByTagName("description");
            	   Element elementDescription = (Element) descriptionList.item(0);
            	   String description = elementDescription.getTextContent();
            	   
            	   Repository repository = new Repository(userName, repositoryName, description);
            	   listOfRepositories.add(repository);
               }
               arrayRepository = new Repository[listOfRepositories.size()];
               listOfRepositories.toArray(arrayRepository);
	           
               
	       }catch(Exception e){
	    	   System.out.print("test");
	       }
	       
	       return arrayRepository;
	   }
	
	public static void GetBlob() throws SAXException, IOException, ParserConfigurationException{
		GitHubAPI gapi = new GitHubAPI();
	    gapi.goStealth();
	    GitHubAPI gapi2 = new GitHubAPI();
	    gapi2.goStealth();
	    
	    String[] url = new String[GitHub.ParserCommitFile().length];
	    Repository[] repoArray = GitHub.ParserRepoFile().clone();
	    String[] shaArrayTree = GitHub.ParserCommitFile().clone();
	    
	    
	    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        NodeList treeArray = null;
        Element elementTree;
        String[] nameArray = null;
        String[] typeArray = null;
        String[] shaArray = null;
        String shaUrl = null;
        
        
	    for(int i=0; i<url.length; i++){
	    	url[i] = gapi.object.tree(repoArray[i].userName, repoArray[i].repositoryName, shaArrayTree[i]).url;
	    	url[i] = url[i].replaceAll("json", "xml");

	    	
            Document doc = dBuilder.parse(url[i]);
            doc.getDocumentElement().normalize();
            NodeList tree = doc.getElementsByTagName("tree");
            Element element = (Element) tree.item(0);
            treeArray = element.getElementsByTagName("tree");
            nameArray = new String[treeArray.getLength()];
            typeArray = new String[treeArray.getLength()];
            shaArray = new String[treeArray.getLength()];
            
            //elementTree = (Element) treeArray.item(i);
            for(int j=0; j<treeArray.getLength(); j++){
            	//array[j] = treeArray.item(j).getTextContent();
            	elementTree = (Element) treeArray.item(j);
            	
            	NodeList name = elementTree.getElementsByTagName("name");
            	Element nameElement = (Element) name.item(0);
            	nameArray[j] = nameElement.getTextContent();
            	//System.out.println(nameArray[j]);
            	
            	NodeList type = elementTree.getElementsByTagName("type");
            	Element typeElement = (Element) type.item(0);
            	typeArray[j] = typeElement.getTextContent();
            	//System.out.println(typeArray[j]);
            	
            	NodeList sha = elementTree.getElementsByTagName("sha");
            	Element shaElement = (Element) sha.item(0);
            	shaArray[j] = shaElement.getTextContent();
            	
            	
            	if (nameArray[j].equals("qcadoo-plugin.xml")){
            		//String qcadoo = gapi.object.blob(repoArray[i].userName, repoArray[i].repositoryName, shaArray[j], "").url;
            		//System.out.println(qcadoo);
            		
            	}else{
            		if(typeArray[j].equals("tree")){
            			//System.out.println(typeArray[j]);
            			
            			shaUrl = gapi2.object.tree(repoArray[i].userName, repoArray[i].repositoryName, shaArray[j]).url;	
            			shaUrl = shaUrl.replaceAll("json", "xml");
            			
            			System.out.println(shaUrl);
            		}
            	}
            }
	    }

	 	    //System.out.println(url[i]);

	}
}

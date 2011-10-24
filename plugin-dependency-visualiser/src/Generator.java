import java.io.*;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class Generator {
       public static ArrayList<Plugin> list;
       public static FileWriter fstream;
       public static BufferedWriter output;
       public static ArrayList<String> checkList;

       public Generator() throws IOException {
               this.list = Parser.ParserPluginFiles();
               this.fstream = new FileWriter("qcadoo-plugin.gv");
               this.output = new BufferedWriter(fstream);
               this.checkList = new ArrayList<String>();
       }

       public static void Foo(String s) throws IOException {
               Plugin[] arrayOfPlugin = new Plugin[list.size()];
               list.toArray(arrayOfPlugin);
               String str = null;
               Group group = null;
               
               for (int i = 0; i < list.size(); i++) {
                       for (int j = 0; j < arrayOfPlugin[i].dependencies.length; j++) {
                               if (arrayOfPlugin[i].dependencies[j] != null) {
                                       if (arrayOfPlugin[i].dependencies[j].equals(s)) {
                                               str = arrayOfPlugin[i].id + "->" + s  + ":" + arrayOfPlugin[i].group;
                                               if (!checkList.contains(str)) {
                                                       checkList.add(str);
                                               }
                                               Foo(arrayOfPlugin[i].id);
                                       }
                               } else {
                                       str = arrayOfPlugin[i].id + ":" + arrayOfPlugin[i].group;
                                       if (!checkList.contains(str)) {
                                               checkList.add(str);
                                               Foo(arrayOfPlugin[i].id);
                                       }
                                       
                               }
                       }
               }
       
       }

       
       public static String[] GetGroups() {
               Plugin[] arrayOfPlugin = new Plugin[list.size()];
               list.toArray(arrayOfPlugin);
               ArrayList<String> groupList = new ArrayList<String>();
               for (int i = 0; i < arrayOfPlugin.length; i++) {
                       if (!groupList.contains(arrayOfPlugin[i].group)) {
                               groupList.add(arrayOfPlugin[i].group);
                       }
               }
               String[] array = new String[groupList.size()];
               groupList.toArray(array);
               return array;
       }

       public static void GenerateFile(String[] array) throws IOException {
               String[] tablica = new String[Generator.checkList.size()];
               Generator.checkList.toArray(tablica);
               
               Group[] groupArray = new Group[Generator.checkList.size()];
               for (int i = 0; i < tablica.length; i++) {
                       
                       String[] tokens = tablica[i].split(":");
                       Group group = new Group(tokens[0], tokens[1]);
                       groupArray[i] = group;
               }
               for (int i = 0; i < array.length; i++) {
                       Generator.output.write("subgraph cluster" + (i + 1) + "\n{\n");
                       Generator.output.write("label = \"" +array[i]+  "\";\n");
                       
                       for (int j = 0; j < groupArray.length; j++) {
                               if (groupArray != null)
                                       if (groupArray[j].group.equals(array[i])) {
                                               output.write(groupArray[j].id + "\n");
                                       }
                       }
                       Generator.output.write("}\n");
               }
       }
       public static void deleteDir(File dir) {
           if (dir.isDirectory()) {
                   String[] children = dir.list();
                   for (int i = 0; i < children.length; i++) {
                           deleteDir(new File(dir, children[i]));  
                   }
           }
           dir.delete();
   }
       public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
               Generator generator = new Generator();
               Plugin[] arrayOfPlugin = new Plugin[generator.list.size()];
               generator.list.toArray(arrayOfPlugin);
               generator.output.write("digraph qcadoo\n{\n");
               for (int i = 0; i < arrayOfPlugin.length; i++) {
                       if (arrayOfPlugin[i].dependencies[0] == null)
                               Foo(arrayOfPlugin[i].id);
               }
               
               GenerateFile(GetGroups());
               generator.output.write("}");
               generator.output.close();
               File file = new File("./downloads");
               deleteDir(file);
               
       }
}
import com.google.gson.JsonObject;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        //System.out.println("Test bruitage et débruitage avec filtre median");
        //Charger une image en memoire
        /*Image test= ImageLoader.exec("src\\img\\eiffel.jpg");
        Viewer2D.exec(test);*/
        //bruiter une image
        /*Image noisyImage=NoiseTools.addNoise(test,0.2);
        noisyImage.setColor(true); //si false => affichage de chaque canal, si true => affichage d'une image couleur
        Viewer2D.exec(noisyImage);*/

        //application du filtre median pour debruiter
        /*Image imgFiltree = Traitement.filtreMedian(noisyImage);
        imgFiltree.setColor(true); //si false => affichage de chaque canal, si true => affichage d'une image couleur
        Viewer2D.exec(imgFiltree);*/

        /*
        //creer les histo des 3 couleurs
        double[][] histo = HistogramTools.creerHistoRGB(test);
        HistogramTools.plotHistogram(histo[0],"red");
        HistogramTools.plotHistogram(histo[1],"green");
        HistogramTools.plotHistogram(histo[2],"blue");*/

        //scanner to read the theme of images(broad or motos)
        Scanner myObj = new Scanner(System.in);  // Create a Scanner object
        String theme;
        do{
            System.out.println("motos or broad");
            theme = myObj.nextLine();  // Read user input
            System.out.println("Theme : " + theme);  // Output user input
        }while(!(theme.equals("motos") || theme.equals("broad")|| theme.equals("img")));
        String imgName = myObj.nextLine();  // Read user input
        System.out.println("Image : " + imgName);  // Output user input
        String path = "src\\"+theme;
        indexation(path);
        File imgFileScan;
        do{
            System.out.println("Entrer un nom d'image");
            imgFileScan = new File(path+"\\"+imgName);
        }while(!imgFileScan.exists());

        System.out.println("OK wait now!");
        File dir  = new File(path);
        File[] liste = dir.listFiles();

        //create JSON

            Image imgScan= ImageLoader.exec(path+"\\"+imgName);
            double[][] histoSource = Traitement.TraitementHisto(imgScan);
            JSONObject jsonObject = (JSONObject) readJson("src/index.json");
            JSONArray jsonArray = (JSONArray) jsonObject.get("images");

            //create a map or tab to stock the similarities

            for(int i = 0;i<jsonArray.size();i++){
                JSONObject obj = (JSONObject) jsonArray.get(i);
                if(!obj.get("name").equals(imgName)){

                    //not working
                    JSONArray jsonHisto = (JSONArray) obj.get("histo");
                    HistogramTools.plotHistogram(HistogramTools.getHisto(jsonHisto)[0]);
                    HistogramTools.plotHistogram(HistogramTools.getHisto(jsonHisto)[1]);
                    HistogramTools.plotHistogram(HistogramTools.getHisto(jsonHisto)[2]);

                    //use the func similarity or cos between the img source and this one
                    //stock the name and similarities in the map or tab

                }

            }
            //display the 10 first

        //System.out.println(obj.toJSONString());
        /*writeJson("src/test.json",obj);
        JSONObject jsonObject = (JSONObject) readJson("src/test.json");
        System.out.println("aaa");
        System.out.println(jsonObject);
        System.out.println(jsonObject.get("images"));
        JSONArray jsonArray = (JSONArray) jsonObject.get("images");*/

    }
    public static void indexation(String path) throws Exception {
        File dir  = new File(path);
        File[] liste = dir.listFiles();
        //create JSON
        JSONObject obj = new JSONObject();
        JSONArray arrayImg = new JSONArray();

        for(File item : liste){
            if(item.isFile()){
                System.out.format("Nom du fichier: %s%n", item.getName());
                Image img= ImageLoader.exec(path+"\\"+item.getName());
//                    Viewer2D.exec(test);

                JSONObject objImg = new JSONObject();
                objImg.put("name", item.getName());
                JSONArray arrayHisto = new JSONArray();

                double[][] histo = Traitement.TraitementHisto(img);
                //List<String> histoList = new ArrayList<>();
                for(int i = 0;i<histo.length;i++){
                    List<String> list = new ArrayList<>();
                    for(int j = 0;j<histo[i].length;j++){
                        list.add(Double.toString(histo[i][j]));
                    }
                    arrayHisto.add(list.toString());
                }
                //arrayHisto.add(histoList);
                objImg.put("histo",arrayHisto);
                arrayImg.add(objImg);
            }
        }

        obj.put("images",arrayImg);
        writeJson("src/index.json",obj);
    }

    public static Object readJson(String filename) throws IOException, ParseException {
        FileReader reader = new FileReader(filename);
        JSONParser jsonParser = new JSONParser();
        return jsonParser.parse(reader);
    }


    public static void writeJson(String filename,JSONObject json) throws Exception {
        Files.write(Paths.get(filename), json.toJSONString().getBytes());
    }

}


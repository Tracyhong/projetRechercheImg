import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.io.ImageLoader;

import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

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
        String path = "src\\"+theme;

        //indexation
        long tempsDebut = System.currentTimeMillis();
        indexation(path);
        long tempsFin = System.currentTimeMillis();
        double seconds = (tempsFin - tempsDebut) / 1000F;
        System.out.println("Indexation effectuée en: "+ Double.toString(seconds) + " secondes.");

        File imgFileScan;
        String imgName;
        do{
            System.out.println("Entrer un nom d'image");
             imgName = myObj.nextLine();  // Read user input
            System.out.println("Image : " + imgName);  // Output user input
            imgFileScan = new File(path+"\\"+imgName);
        }while(!imgFileScan.exists());

        System.out.println("OK wait now!");
        File dir  = new File(path);
        File[] liste = dir.listFiles();

            Image imgSource= ImageLoader.exec(path+"\\"+imgName);
            double[][] histoSource = Traitement.traitementHisto(imgSource);
            JSONObject jsonObject = (JSONObject) readJson("src/index.json");
            JSONArray jsonArray = (JSONArray) jsonObject.get("images");

            //create a map or tab to stock the similarities
            Map<Double,String> map= new TreeMap<>();

            for(int i = 0;i<jsonArray.size();i++){
                JSONObject obj = (JSONObject) jsonArray.get(i);
                String name = (String) obj.get("name");
                if(!name.equals(imgName)){

                    JSONArray jsonHisto = (JSONArray) obj.get("histo");
                    double[][] histo = HistogramTools.getHistoJson(jsonHisto);
                    //HistogramTools.plotHistogram(HistogramTools.getHistoJson(jsonHisto)[0]); //R
                    //HistogramTools.plotHistogram(HistogramTools.getHistoJson(jsonHisto)[1]); //G
                    //HistogramTools.plotHistogram(HistogramTools.getHistoJson(jsonHisto)[2]); //B

                    //use the func similarity or cos between the img source and this one
                    //System.out.println(name);
                    double similarity = Traitement.similarite(histoSource,histo,false);
                    //stock the name and similarities in the map or tab
                    map.put(similarity,name);
                    for (Map.Entry<Double, String> entry : map.entrySet()) {
                        System.out.println(entry.getKey() + " => " + entry.getValue());
                    }
                }

            }
            //display the 10 first
        System.out.println("BEST 10 :");
            int max = 0;
        for (Map.Entry<Double, String> entry : map.entrySet()) {
            if(max<10) {
                System.out.println(entry.getKey() + " => " + entry.getValue());
                Image test= ImageLoader.exec(path+"\\"+entry.getValue());
                Viewer2D.exec(test);
            }
            max++;
        }

        //System.out.println(obj.toJSONString());
        /*writeJson("src/index.json",obj);
        JSONObject jsonObject = (JSONObject) readJson("src/index.json");
        System.out.println("aaa");
        System.out.println(jsonObject);
        System.out.println(jsonObject.get("images"));
        JSONArray jsonArray = (JSONArray) jsonObject.get("images");*/

    }
    public static void indexation(String path) throws Exception {
        System.out.println("Chargement en cours ...");
        File dir  = new File(path);
        File[] liste = dir.listFiles();
        //create JSON
        JSONObject obj = new JSONObject();
        JSONArray arrayImg = new JSONArray();
        int nbImg = liste.length;
        int index = 1;
        for(File item : liste){
            System.out.println(index+"/"+nbImg);
            if(item.isFile()){
                //System.out.format("Nom du fichier: %s%n", item.getName());
                Image img= ImageLoader.exec(path+"\\"+item.getName());
//                    Viewer2D.exec(test);

                JSONObject objImg = new JSONObject();
                objImg.put("name", item.getName());
                JSONArray arrayHisto = new JSONArray();

                double[][] histo = Traitement.traitementHisto(img);
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
            index++;

        }

        obj.put("images",arrayImg);
        writeJson("src/index.json",obj);
        System.out.println("Fin du chargement !");
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


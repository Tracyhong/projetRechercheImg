import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.io.ImageLoader;

import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        //Indexation
        /*indexation("src/motos","RGB","motos");
        indexation("src/motos","HSV","motos");
        indexation("src/broad","RGB","broad");
        indexation("src/broad","HSV","broad"); //non
        */

        //scanner
        Scanner sc = new Scanner(System.in);  // Create a Scanner object

        String indexer; //demande si recréer l'indexation
        do{
            System.out.println("Regénérer l'indexation ? (oui/non)");
            indexer = sc.nextLine();  // Read user input
        }while(!(indexer.equals("oui") || indexer.equals("non")));


        String theme; //demande le theme des image entre motos ou broad
        do{
            System.out.println("Choisissez le thème d'images : motos ou broad");
            theme = sc.nextLine();  // Read user input
            System.out.println("Theme : " + theme);  // Output user input
        }while(!(theme.equals("motos") || theme.equals("broad")));
        String path = "src\\"+theme;

        String methode; // demande la méthode de comparaison : rgb ou hsv
        do {
            System.out.print("Veuillez choisir la méthode de comparaison (RGB ou HSV) : ");
            methode = sc.nextLine().toUpperCase(); // RGB || HSV
        } while(!(methode.equals("RGB") || methode.equals("HSV")));

        File imgFileScan;
        String imgName; //demande le nom de l'image source à comparer avec le reste
        do{
            System.out.println("Entrer un nom d'image");
            imgName = sc.nextLine();  // Read user input
            System.out.println("Image : " + imgName);  // Output user input
            imgFileScan = new File(path+"\\"+imgName);
        }while(!imgFileScan.exists());

        //indexation si demandée
        if(indexer.equals("oui")){
            long tempsDebut = System.currentTimeMillis();
            indexation(path,methode,theme);
            long tempsFin = System.currentTimeMillis();
            double seconds = (tempsFin - tempsDebut) / 1000F;
            System.out.println("Indexation effectuée en: "+ Double.toString(seconds) + " secondes.");
        }

        System.out.println("OK !");

        //créer histo source
        Image imgSource= ImageLoader.exec(path+"\\"+imgName);
        double[][] histoSource = Traitement.traitementHisto(imgSource,methode);
        //lire le json index
        JSONObject jsonObject = (JSONObject) readJson("src/index"+theme+methode+".json");
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
                //afficher toutes les similarités des images dans la treemap dans l'ordre croissant
                /*for (Map.Entry<Double, String> entry : map.entrySet()) {
                    System.out.println(entry.getKey() + " => " + entry.getValue());
                }*/
            }
        }
        //display the 10 first
        JFrame frame=new JFrame();
        frame.setSize(800, 500);
        // image source
        Viewer2D.exec(imgSource);
        //images similaires
        DefaultListModel listModel = new DefaultListModel();
        System.out.println("BEST 10 :");
            int max = 0;
        for (Map.Entry<Double, String> entry : map.entrySet()) {
            if(max<10) {
                System.out.println(entry.getKey() + " => " + entry.getValue());
                /*Image test= ImageLoader.exec(path+"\\"+entry.getValue());
                Viewer2D.exec(test);*/
                ImageIcon ii = new ImageIcon(ImageIO.read(new File(path+"\\"+entry.getValue())));
                listModel.add(max, ii);
            }
            max++;
        }
        JList lsm=new JList(listModel);
        lsm.setVisibleRowCount(1);
        JScrollPane js = new JScrollPane(lsm);
        frame.add(js);

        //frame.pack();
        frame.setVisible(true);


    }
    public static void indexation(String path, String methode,String theme) throws Exception {
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
//              Viewer2D.exec(test);

                JSONObject objImg = new JSONObject();
                objImg.put("name", item.getName());
                JSONArray arrayHisto = new JSONArray();

                double[][] histo = Traitement.traitementHisto(img,methode);
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
        writeJson("src/index"+theme+methode+".json",obj);
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


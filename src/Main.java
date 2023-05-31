import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {

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
        JSONObject obj = new JSONObject();

        Scanner myObj = new Scanner(System.in);  // Create a Scanner object
        System.out.println("Entrer un nom d'image");

        String imgName = myObj.nextLine();  // Read user input
        System.out.println("Image : " + imgName);  // Output user input
        String path = "src\\img";
        File dir  = new File(path);
        File[] liste = dir.listFiles();

        File imgScan = new File(path+"\\"+imgName);
        if(imgScan.exists() && imgScan.isFile()){//if file scan exist
            for(File item : liste){
                if(item.isFile()&&!item.getName().equals(imgName)){
                    System.out.format("Nom du fichier: %s%n", item.getName());
                    Image img= ImageLoader.exec(path+"\\"+item.getName());
//                    Viewer2D.exec(test);
                    double[][] histo = Traitement.TraitementHisto(img);
                    obj.put("image", item.getName());
                    obj.put("histo", histo);
                }
                else if(item.isDirectory())
                {
                    System.out.format("Nom du répertoire: %s%n", item.getName());
                }
            }
        }
        else{
            System.out.println("Cette image n'existe pas !");
        }
        String json = obj.toJSONString();
        System.out.println(json);
    }
}
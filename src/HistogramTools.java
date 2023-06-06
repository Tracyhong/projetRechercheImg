import fr.unistra.pelican.Image;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.json.simple.JSONArray;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class HistogramTools {

    /**
     * Afficher dans une frame l'histogramme
     * @param histogram
     * @throws IOException
     */
    public static void plotHistogram(double [] histogram) throws IOException{
        XYSeries myseries = new XYSeries("Nombre de pixels");
        for(int i=0;i<histogram.length;i++){
            myseries.add(new Double(i), new Double(histogram[i]));
        }
        XYSeriesCollection myseriescollection = new XYSeriesCollection(myseries);

        JFreeChart jfreechart = ChartFactory.createXYBarChart("Histogramme de l'image : " , "Valeur pixel", false, "Nombre de pixels", myseriescollection, PlotOrientation.VERTICAL, true, false, false);
        jfreechart.setBackgroundPaint(Color.white);
        XYPlot xyplot = jfreechart.getXYPlot();

        xyplot.setBackgroundPaint(Color.lightGray);
        xyplot.setRangeGridlinePaint(Color.white);
        NumberAxis axis = (NumberAxis) xyplot.getDomainAxis();

        axis.setLowerMargin(0);
        axis.setUpperMargin(0);

        // create and display a frame...
        ChartFrame frame = new ChartFrame("LPIOT - Projet Traitement image", jfreechart);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Sauvegarde l'histogramme en image
     * @param histogram
     * @param pathToSave
     * @throws IOException
     */
    public static void saveHistogram(double [] histogram, String pathToSave) throws IOException{

        XYSeries myseries = new XYSeries("Nombre de pixels");
        for(int i=0;i<histogram.length;i++){
            myseries.add(new Double(i), new Double(histogram[i]));
        }
        XYSeriesCollection myseriescollection = new XYSeriesCollection(myseries);

        JFreeChart jfreechart = ChartFactory.createXYBarChart("Histogramme de l'image", "Niveaux de gris", false, "Nombre de pixels", myseriescollection, PlotOrientation.VERTICAL, true, false, false);
        jfreechart.setBackgroundPaint(Color.white);
        XYPlot xyplot = jfreechart.getXYPlot();

        xyplot.setBackgroundPaint(Color.lightGray);
        xyplot.setRangeGridlinePaint(Color.white);
        NumberAxis axis = (NumberAxis) xyplot.getDomainAxis();

        axis.setLowerMargin(0);
        axis.setUpperMargin(0);

        if(pathToSave!=null)
            ChartUtilities.saveChartAsPNG(new File(pathToSave), jfreechart, 900, 600);
    }

    /**
     * Créer l'histogramme d'une image
     * @param img
     * @param show
     * @return
     * @throws IOException
     */
    public static double[][] createHistoRGB(Image img, boolean show) throws IOException {

        double[][] histo = new double[img.getBDim()][256];
        //System.out.println(ImageRead.getBDim());
        for(int c=0; c < img.getBDim(); c++){
            Arrays.fill(histo[c], 0);
        }

        for(int x=0; x<img.getXDim();x++){
            for(int y=0; y<img.getYDim();y++){
                for(int c=0; c < img.getBDim(); c++){
                    histo[c][img.getPixelXYBByte(x, y, c)]++;
                }
            }
        }

        if(show) {
            for(int c=0; c < img.getBDim(); c++){
                HistogramTools.plotHistogram(histo[c]);
            }
        }
        return histo;
    }

    /**
     * Discrétise l'histogramme
     * @param histo
     * @param division
     * @param show
     * @return
     * @throws IOException
     */
    public static double[][] ReduceHisto (double[][] histo, int division, boolean show) throws IOException {
        int newLength = (int) (histo[0].length / division);
        double[][] histoReduit = new double[histo.length][newLength];
        for(int c=0; c < histoReduit.length; c++){
            for(int x=0; x < histoReduit[c].length; x++){
                for(int y=x*division; y < (x*division) + division; y++){
                    histoReduit[c][x] = histoReduit[c][x] + histo[c][y] ;
                }
            }
        }

        if(show) {
            for(int c=0; c < histoReduit.length; c++){
                HistogramTools.plotHistogram(histoReduit[c]);
            }
        }
        return histoReduit;
    }

    /**
     * Convertis un histogramme en histogramme de valeur en pourcentage
     * @param histo
     * @param show
     * @return histogramm de pourcentage
     * @throws IOException
     */
    public static double[][] PourcentageHisto (double[][] histo, boolean show) throws IOException {

        double[][] histoPourc = new double[histo.length][histo[0].length];
        int nbPixels = (int) Arrays.stream(histo[0]).sum();
        for(int c=0; c < histoPourc.length; c++){
            for(int x=0; x < histoPourc[c].length; x++){
                histoPourc[c][x] = histo[c][x] / nbPixels;
            }
        }

        if(show) {
            for(int c=0; c < histoPourc.length; c++){
                HistogramTools.plotHistogram(histoPourc[c]);
            }
        }
        return histoPourc;
    }

    public static double[][] createHistoHSV(Image img, boolean show) throws IOException {
        int largeur = img.getXDim();
        int hauteur = img.getYDim();

        double[][] histo = new double[3][];
        histo[0] = new double[361];
        histo[1] = new double[101];
        histo[2] = new double[101];

        for (int x = 0; x < largeur; x++) {
            for (int y = 0; y < hauteur; y++) {
                int r = img.getPixelXYBByte(x, y, 0);
                int g = img.getPixelXYBByte(x, y, 1);
                int b = img.getPixelXYBByte(x, y, 2);

                double max = Math.max(Math.max(r, g), b);
                double min = Math.min(Math.min(r, g), b);

                int v = (int) ((max / 255) * 100);
                int s = 0;
                if(max > 0)
                    s = (int) ((1 - min / max) * 100);
                int h = (int) (Math.acos((r - (1/2) * g - (1/2) * b) / Math.sqrt(r*r + g*g + b*b + r*g + r*b + g*b)) * 100);
                if(b > g)
                    h = 360 - h;

                ++histo[0][h];
                ++histo[1][s];
                ++histo[2][v];
            }
        }
        if(show) {
            for(int c=0; c < img.getBDim(); c++){
                plotHistogram(histo[c]);
            }
        }
        return histo;
    }

    public static double[][] ImageHistoHSV (Image ImageRead, boolean show) throws IOException {

        double[][] histo = new double[3][101];


        for(int x=0; x<ImageRead.getXDim();x++){
            for(int y=0; y<ImageRead.getYDim();y++){
                float[] hsv = null;
                hsv = Color.RGBtoHSB(ImageRead.getPixelXYBByte(x, y, 0), ImageRead.getPixelXYBByte(x, y, 1), ImageRead.getPixelXYBByte(x, y, 2), hsv);


                for(int c=0; c < ImageRead.getBDim(); c++){
                    //System.out.println( "hsv[+"+c+"+]" + (int) ( hsv[c] * 100 ));
                    histo[c][ (int) (hsv[c] * 100 )]++;
                }
            }
        }

        if(show) {
            for(int c=0; c < ImageRead.getBDim(); c++){
                HistogramTools.plotHistogram(histo[c]);
            }
        }
        return histo;
    }









    /**
     * Convertis un jsonArray contenant un String en histogramme
     * @param json
     * @return un histogramme
     */
   public static double[][] getHistoJson(JSONArray json){
       //System.out.println(json);
       double[][] histo = new double[json.size()][];
       int index = 0;
       for (Object tab :json) {
           //System.out.println(tab.toString());
           String[] histoString = tab.toString().replace("[","").replace("]","").split(",");
           //System.out.println(tab.toString().split(","));
           //histo = new double[json.size()][histoString.length];
           double[] histoColor = new double[histoString.length];
           for(int i =0;i<histoString.length;i++){
               histoColor[i]=Double.parseDouble(histoString[i]);
           }
           histo[index] = histoColor;
           index++;
       }
       //affichage
       /*for(int i =0;i<histo.length;i++){
           for(int j =0;j<histo[i].length;j++){
               System.out.print(histo[i][j]);
           }
           System.out.println();
       }*/
       return histo;
    }
}
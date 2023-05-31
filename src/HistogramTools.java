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

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class HistogramTools {

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
     * Calcule l'histo d'une image en niveau de gris 
     * @param img l'image
     * @return histo
     */
    public static double[] createHisto(Image img) throws IOException {
        double[] histo = new double[256];
        for(int i = 0;i<256;i++){
            histo[i] = 0;
        }
        for (int x = 0; x < img.getXDim(); x++) {
            for (int y = 0; y < img.getYDim(); y++) {
                histo[img.getPixelXYBByte(x,y,0)] +=1;
                //System.out.println(img.getPixelXYBByte(x,y,0));
            }
        }
        //plotHistogram(histo);
        return histo;
    }

    /**
     * Calcule l'histo RGB d'une image
     * @param img l'image
     * @return histoRGB
     */
    public static double[][] creerHistoRGB(Image img){
        int largeur = img.getXDim();
        int hauteur = img.getYDim();
        double[][] histo = new double[3][256];

        // init histo à 0
        /*for(int i = 0; i < 3; ++i){
            for(int j = 0; j < histo[0].length; ++j)
                histo[i][j] = 0;
        }*/

        for(int i = 0; i < 3; ++i) {  //canaux
            for (int x = 0; x < largeur; x++) {
                for (int y = 0; y < hauteur; y++) {
                    int val = img.getPixelXYBByte(x, y, i);
                    histo[i][val] += 1;
                }
            }
        }
        return histo;
    }

    //Histogramme d'une image
    public static double[][] ImageHisto (Image ImageRead, boolean show) throws IOException {

        double[][] histo = new double[3][256];

        for(int c=0; c < ImageRead.getBDim(); c++)
        {
            Arrays.fill(histo[c], 0);
        }

        for(int x=0; x<ImageRead.getXDim();x++)
        {
            for(int y=0; y<ImageRead.getYDim();y++)
            {
                for(int c=0; c < ImageRead.getBDim(); c++)
                {
                    histo[c][ImageRead.getPixelXYBByte(x, y, c)]++;
                }
            }
        }

        if(show) {
            for(int c=0; c < ImageRead.getBDim(); c++)
            {
                HistogramTools.plotHistogram(histo[c]);
            }
        }

        return histo;

    }


    public static double[][] ReduceHisto (double[][] histo, int division, boolean show) throws IOException {

        int newLength = (int) (histo[0].length / division);

        double[][] histoReduit = new double[histo.length][newLength];

        for(int c=0; c < histoReduit.length; c++)
        {
            for(int x=0; x < histoReduit[c].length; x++)
            {
                for(int y=x*division; y < (x*division) + division; y++)
                {
                    histoReduit[c][x] = histoReduit[c][x] + histo[c][y] ;
                }
            }
        }

        if(show) {
            for(int c=0; c < histoReduit.length; c++)
            {
                HistogramTools.plotHistogram(histoReduit[c]);
            }
        }

        return histoReduit;

    }

    public static double[][] PourcentageHisto (double[][] histo, boolean show) throws IOException {

        double[][] histoPourc = new double[histo.length][histo[0].length];
        int nbPixels = (int) Arrays.stream(histo[0]).sum();

        for(int c=0; c < histoPourc.length; c++)
        {
            for(int x=0; x < histoPourc[c].length; x++)
            {
                histoPourc[c][x] = histo[c][x] / nbPixels;
            }
        }

        if(show) {
            for(int c=0; c < histoPourc.length; c++)
            {
                HistogramTools.plotHistogram(histoPourc[c]);
            }
        }

        return histoPourc;

    }

}
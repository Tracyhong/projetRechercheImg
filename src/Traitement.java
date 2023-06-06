import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

import java.io.IOException;
import java.util.Arrays;

public class Traitement {
	/**
	 * Créer l'histogramme d'une image après traitement (filtre médian, discrétisation, normalisation )
	 * @param ImageRead
	 * @return
	 * @throws IOException
	 */
    public static double[][] traitementHisto(Image ImageRead, String methode) throws IOException {

        Image ImageMedian = medianFilter(ImageRead,false);

        double[][] histo, histo2, histo3;
		if(methode.equals("HSV")){
			//histo = HistogramTools.createHistoHSV(ImageMedian, false);
			histo = HistogramTools.ImageHistoHSV(ImageMedian, false);
			System.out.println("HSV");
			return histo;
		}
		else{
			histo = HistogramTools.createHistoRGB(ImageMedian, false);
			System.out.println("RGB");
			histo2 = HistogramTools.ReduceHisto(histo,10,false);

			histo3 = HistogramTools.PourcentageHisto(histo2,false);
			return histo3;
		}/*
		histo2 = HistogramTools.ReduceHisto(histo,10,false);

		histo3 = HistogramTools.PourcentageHisto(histo2,false);
		return histo3;*/

    }

    /**
     * Appliquer un filtre médian sur une image
     * @param ImageRead
     * @return l'image débruitée
     */
    public static Image medianFilter(Image ImageRead, boolean show) {
		
		Image ImageCustom = ImageRead;
		
		for(int x=0; x < ImageRead.getXDim(); x++){
			for(int y=0; y < ImageRead.getYDim(); y++){
				int[][][] pixelsVoisins = getPixelsVoisins(ImageRead, x, y);
				int[][] hasValueFilter = hasValueVoisins(ImageRead, x, y);	
				
				int [][] TrierPixelsVoisins = new int[ImageRead.getBDim()][pixelsVoisins[0].length * pixelsVoisins[0][0].length];
				int NbVoisins = 0;
				
				for(int c=0; c < ImageRead.getBDim(); c++){
					int index = 0;
					for(int pvX=0; pvX < pixelsVoisins[0].length; pvX++){
						for(int pvY=0; pvY < pixelsVoisins[0][0].length; pvY++){
							TrierPixelsVoisins[c][index] =  pixelsVoisins[c][pvX][pvY];
							index++;
						}
					}
				}
				
				for(int pvX=0; pvX < hasValueFilter.length; pvX++){
					for(int pvY=0; pvY < hasValueFilter[0].length; pvY++){
						NbVoisins++;
					}
				}
				
				//Trier les valeurs par ordre croissant
				for(int c=0; c < ImageRead.getBDim(); c++){
					Arrays.sort(TrierPixelsVoisins[c]);
					
					int medianValue = TrierPixelsVoisins[c][ (int) (NbVoisins/2)];
					ImageCustom.setPixelXYBByte(x, y, c, medianValue);
					
				}
			}
		}
		
		if(show){
			Viewer2D.exec(ImageCustom);
		}
		return ImageCustom;
	}

	/**
	 * récupère tous les pixels voisins d'un pixel d'une image
	 * @param ImageRead
	 * @param x
	 * @param y
	 * @return un tableau à 3 entrées pour chaque canaux
	 */
	private static int[][][] getPixelsVoisins(Image ImageRead, int x, int y) {
		
		int[][][] pixelsVoisins = new int[ImageRead.getBDim()][3][3];
	
		//Parcourir les voisins
		for(int voisinX = -1; voisinX <= 1; voisinX++){
			for(int voisinY = -1; voisinY <= 1; voisinY++){
				int currentX = x + voisinX;
				int currentY = y + voisinY;
	
				for(int c=0; c < ImageRead.getBDim(); c++){
					pixelsVoisins[c][voisinX+1][voisinY+1] = 255;
	
					if (currentX >= 0 && currentX < ImageRead.getXDim()-1 && currentY >= 0 && currentY < ImageRead.getYDim()-1){
						pixelsVoisins[c][voisinX+1][voisinY+1] = ImageRead.getPixelXYBByte(currentX, currentY, c);
					}
				}
			}
		}
		return pixelsVoisins;
		
	}
	
	private static int[][] hasValueVoisins(Image ImageRead, int x, int y) {
	
		int[][] hasValueVoisins = new int[3][3];
	
		//Parcourir les voisins
		for(int voisinX = -1; voisinX <= 1; voisinX++)
		{
			for(int voisinY = -1; voisinY <= 1; voisinY++)
			{
				int currentX = x + voisinX;
				int currentY = y + voisinY;
	
				if (currentX >= 0 && currentX < ImageRead.getXDim()-1 && currentY >= 0 && currentY < ImageRead.getYDim()-1)
				{
					hasValueVoisins[voisinX+1][voisinY+1] = 1;
				}
				else {
					hasValueVoisins[voisinX+1][voisinY+1] = 0;
				}
			}
		}
		
		return hasValueVoisins;
	}

	/**
	 * Compare deux histogramme
	 * @param histoSource
	 * @param histoAComparer
	 * @param show
	 * @return une valeur de similarité (petit = très similaire, grand = moins similaire)
	 */
	public static double similarite( double[][] histoSource, double[][] histoAComparer, boolean show) {
		
		double distance = 0.0;
		
		for(int c=0; c < histoSource.length; c++)
		{
			for(int y=0; y < histoSource[c].length; y++)
			{
				distance = distance + Math.sqrt( Math.pow( (histoSource[c][y] - histoAComparer[c][y]), 2 ) );
			}
		}
		
		if(show) {
			System.out.println("Distance : " + distance);
		}
		
		return distance;
	}
	
	
	//License from project: Open Source License 
	public static double cosineSim(double[][] histoSource, double[][] histoAComparer, boolean show) {
        if (histoSource == null || histoAComparer == null || histoSource.length < 1 || histoAComparer.length < 1 || histoSource.length != histoAComparer.length)
            return Double.NaN;

        double sum = 0.0, sum_a = 0, sum_b = 0, distance = 0.0;
        
        for(int c=0; c < histoSource.length; c++)
		{
	        for (int i = 0; i < histoSource.length; i++) {
	            sum += histoSource[c][i] * histoAComparer[c][i];
	            sum_a += histoSource[c][i] * histoSource[c][i];
	            sum_b += histoAComparer[c][i] * histoAComparer[c][i];
	        }
		}

        double val = Math.sqrt(sum_a) * Math.sqrt(sum_b);

        distance = sum / val;
        
        if(show) {
			System.out.println("Distance : " + distance);
		}
        
        return distance;
    }

	//test func
	public static void main(String[] args) throws IOException {

        Image test= ImageLoader.exec("src\\motos\\000.jpg");
        Image test1= ImageLoader.exec("src\\motos\\243.jpg");
		double[][] histoTest = traitementHisto(test,"HSV");
		//double[][] histoTest1 = traitementHisto(test1,"RGB");
		//similarite(histoTest,histoTest,true); //1.0
		//similarite(histoTest,histoTest1,true); //0.9
	}


}
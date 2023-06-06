import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FileViewer extends JFrame {

    private DefaultListModel<String> fileListModel;
    private DefaultListModel<String> methodeListModel;
    private JList<String> fileList;
    private JList<String> methodeList;
    private JButton compareBouton;
    private JButton indexationBouton;
    private JTable imageTable;
    private JScrollPane tableScrollPane;
    private JLabel resultLabel;
    private ImageTableModel imageTableModel;
    private static String path = "src/motos";

    // Créatio du JFrame
    public FileViewer() {
        setTitle("File Viewer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Liste des images
        fileListModel = new DefaultListModel<>();
        fileList = new JList<>(fileListModel);
        JScrollPane listScrollPane = new JScrollPane(fileList);
        add(listScrollPane, BorderLayout.WEST);
        
        // Liste des methodes
        methodeListModel = new DefaultListModel<>();
        methodeListModel.addElement("RGB");
        methodeListModel.addElement("HSV");
        methodeList = new JList<>(methodeListModel);
        JScrollPane methodelistScrollPane = new JScrollPane(methodeList);
        add(methodelistScrollPane, BorderLayout.EAST);

        // Bouton de comparaison
        compareBouton = new JButton("Image Similaire");
        compareBouton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedFile = fileList.getSelectedValue();
                String selectedmethode = methodeList.getSelectedValue();
                if (selectedFile != null || selectedmethode != null) {
                    // Appeler la fonction 1 avec le fichier choisi
                    try {
						performFunction1(selectedFile,selectedmethode);
					} catch (IOException | ParseException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
                } else {
                    JOptionPane.showMessageDialog(FileViewer.this, "Veuillez sélectionner un fichier et une méthode.");
                }
            }
        });
        add(compareBouton, BorderLayout.NORTH);

        // Bouton d'indexation
        indexationBouton = new JButton("Indexation");
        indexationBouton.addActionListener(new ActionListener() {
        String selectedmethode = methodeList.getSelectedValue();
            @Override
            public void actionPerformed(ActionEvent e) {
                // Appeler la fonction 2
            	if (selectedmethode != null) {
            		try {
    					performFunction2(selectedmethode);
    				} catch (Exception e1) {
    					// TODO Auto-generated catch block
    					e1.printStackTrace();
    				}
	            } else {
	                JOptionPane.showMessageDialog(FileViewer.this, "Veuillez sélectionner une méthode.");
	            }
                
            }
        });
        add(indexationBouton, BorderLayout.SOUTH);
        
        // Tableau des images
        imageTableModel = new ImageTableModel();
        imageTable = new JTable(imageTableModel);
        //imageTable.getColumnModel().getColumn(0).setCellRenderer(new ImageTableCellRenderer());
        JScrollPane tableScrollPane = new JScrollPane(imageTable);
        add(tableScrollPane, BorderLayout.CENTER);

        pack();
        setVisible(true);
    }

    // Récupère la liste des fichiers d'un répertoire
    private List<String> getFileList() {
        List<String> fileList = new ArrayList<>();
        File folder = new File(path);
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            for (File file : files) {
                if (file.isFile()) {
                    fileList.add(file.getName());
                }
            }
        }
        return fileList;
    }

    // Met à jour la liste par rapport aux fichiers du répertoire
    private void updateFileList() {
        List<String> files = getFileList();
        fileListModel.clear();
        for (String file : files) {
            fileListModel.addElement(file);
        }
    }

    //Lancer la fonction du bouton Comparaison
    private void performFunction1(String selectedFile, String selectedmethode) throws IOException, ParseException {
    	imageTableModel.removeImage(0);
    	
    	/*
    	BufferedImage image = loadImageFromFile(selectedFile);
    	imageTableModel.addImage(image);
    	*/
    	imageTableModel.addImage(selectedFile);
        Simil(selectedFile, selectedmethode);
    }

    //Lancer la fonction du bouton Indexation
    private void performFunction2(String selectedmethode) throws Exception {
        // Implémenter la fonction 2 ici
    	compareBouton.setEnabled(false);
    	indexationBouton.setEnabled(false);
    	indexation(path,selectedmethode);
    	compareBouton.setEnabled(true);
    	indexationBouton.setEnabled(true);
    }
    
    private void indexation(String path, String methode) throws Exception {
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
        writeJson("src/index"+methode+".json",obj);
        System.out.println("Fin du chargement !");

    }
    
    private Object readJson(String filename) throws IOException, ParseException {
        FileReader reader = new FileReader(filename);
        JSONParser jsonParser = new JSONParser();
        return jsonParser.parse(reader);
    }


    private void writeJson(String filename,JSONObject json) throws Exception {
        Files.write(Paths.get(filename), json.toJSONString().getBytes());
    }
    
    
    private void Simil(String selectedFile, String selectedmethode) throws IOException, ParseException {
    	
    	Image imgSource= ImageLoader.exec(path+"\\"+selectedFile);
        double[][] histoSource = Traitement.traitementHisto(imgSource,selectedmethode);
        JSONObject jsonObject = (JSONObject) readJson("src/index.json");
        JSONArray jsonArray = (JSONArray) jsonObject.get("images");

        //create a map or tab to stock the similarities
        Map<Double,String> map= new TreeMap<>();

        for(int i = 0;i<jsonArray.size();i++){
            JSONObject obj = (JSONObject) jsonArray.get(i);
            String name = (String) obj.get("name");
            if(!name.equals(selectedFile)){
                JSONArray jsonHisto = (JSONArray) obj.get("histo");
                double[][] histo = HistogramTools.getHistoJson(jsonHisto);
                double similarity = Traitement.similarite(histoSource,histo,false);
                map.put(similarity,name);
            }
        }
        
        int max = 0;
        imageTableModel.removeAllImage();
        for (Map.Entry<Double, String> entry : map.entrySet()) {
            if(max<10) {
            	/*
                System.out.println(entry.getKey() + " => " + entry.getValue());
                Image test= ImageLoader.exec(path+"\\"+entry.getValue());
                Viewer2D.exec(test);
                */
            	/*
            	BufferedImage image = loadImageFromFile(entry.getValue());
            	imageTableModel.addImage(image);
            	*/
            	imageTableModel.addImage(entry.getValue());
            }
            max++;
        }
    	
    }
    
    private BufferedImage loadImageFromFile(String filename) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(path+"/"+filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }
    
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                FileViewer fileViewer = new FileViewer();
                fileViewer.updateFileList();
            }
        });
    }
    
}



class ImageTableModel extends AbstractTableModel {
    
	//private List<BufferedImage> images;
	private List<String> images;

    public ImageTableModel() {
        images = new ArrayList<>();
    }

    
    /*
    public void addImage(BufferedImage image) {
        images.add(image);
        fireTableDataChanged(); // Actualiser l'affichage du tableau
    }
    */
    public void addImage(String image) {
        images.add(image);
        fireTableDataChanged(); // Actualiser l'affichage du tableau
    }
    
    public void removeImage(int index) {
        if (index >= 0 && index < images.size()) {
            images.remove(index);
            fireTableDataChanged(); // Actualiser l'affichage du tableau
        }
    }
    
    public void removeAllImage() {
    	for(int i = 0; i < images.size(); i++){
            images.remove(i);
            
        }
    	fireTableDataChanged(); // Actualiser l'affichage du tableau
    }

    @Override
    public int getRowCount() {
        return images.size();
    }

    @Override
    public int getColumnCount() {
        return 1; // Une seule colonne pour afficher les images
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return images.get(rowIndex);
    }
}
/*
class ImageTableCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value instanceof Image) {
            Image image = (Image) value;
            setIcon(new ImageIcon());
            setText("");
        }
        return this;
    }
}
*/

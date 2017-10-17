package uniba.warpingtester.view;


import java.io.File;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;

import dtw.TimeWarpInfo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import timeseries.TimeSeries;
import uniba.warpingtester.utility.Calcolo;
import uniba.warpingtester.utility.Coordinates;
import uniba.warpingtester.utility.DTW;
import util.DistanceFunction;
import util.DistanceFunctionFactory;
import uniba.warpingtester.DTW.DynamicTimeWrapping1D;
import uniba.warpingtester.DTW.DynamicTimeWrapping2D;
import uniba.warpingtester.model.WavFile;
import dtw.DTWPro;

public class MainController {
	
	@FXML
	private ListView listMediaFile;
	
	@FXML
	private Label resultLabel;
		
	private ObservableList<WavFile> mediaFiles = FXCollections.observableArrayList();
	
	private HashMap<String, String> fileMap = new HashMap<String, String>();
	
	public MainController() {}
	
	public void initialize() {
		listMediaFile.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		resultLabel.setVisible(false);
	}
	
	@FXML
	private void addMediaFile() {
		FileChooser fc = new FileChooser();
		fc.setInitialDirectory(new File("."));
		fc.setTitle("Add Audio File");
		fc.getExtensionFilters().clear();
		fc.getExtensionFilters().add(new ExtensionFilter("WAV file","*.wav"));
		
		List<File> files = fc.showOpenMultipleDialog(null);
		
		if (files != null) {
			for(File f : files) fileMap.putIfAbsent(f.getName(), f.getPath());
			
			List<String> fileNames = new ArrayList<String>();
			for (String s : fileMap.keySet()) fileNames.add(s);		
			ObservableList names = FXCollections.observableArrayList(fileNames);
			Collections.sort(names);
			listMediaFile.setItems(names);
		}
		
	}
	
	@FXML
	private void removeMediaFile() {
		int index = listMediaFile.getSelectionModel().getSelectedIndex();
		
		if(index >= 0) {
			listMediaFile.getItems().remove(index);	
		} else {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Warning");
	        alert.setHeaderText("Error select files.");
	        alert.setContentText("You must selection 2 file.");
	        alert.showAndWait();
		}
		
	}
	
	@FXML
	private void calculateDistance() {
		List<String> selectedFileNames = (List<String>)listMediaFile.getSelectionModel().getSelectedItems();
				
		if (selectedFileNames.size() != 2) {
			Alert alert = new Alert(AlertType.WARNING);
	        alert.setTitle("Warning");
	        alert.setHeaderText("Error selecting files.");
	        alert.setContentText("You must selection 2 file.");
	        alert.showAndWait();
		} else {
			String pathFirstFile = fileMap.get(selectedFileNames.get(0));
			String pathSecondFile = fileMap.get(selectedFileNames.get(1));
			
			Calcolo computedFirstFile= new Calcolo(pathFirstFile);
			XYSeries seriesFirstFile = computedFirstFile.AnalisiFrequenze(); 
			
			Calcolo computedSecondFile= new Calcolo(pathSecondFile);
			XYSeries seriesSecondFile = computedSecondFile.AnalisiFrequenze();
			
			//create a list of XYSeries to calculate sable Areas between each other
			
			ArrayList<XYSeries> listOfComputedFiles = new ArrayList<XYSeries>();
			
			for(int i = 0; i < selectedFileNames.size(); i++) {
				String pathFile = fileMap.get(selectedFileNames.get(i));
				Calcolo computedFile = new Calcolo (pathFile);
				XYSeries computedSeriesFile = computedFile.AnalisiFrequenze();
				listOfComputedFiles.add(computedSeriesFile);
			}
			
			//calculateStableAreas(listOfComputedFiles);
            correlationWithoutZones(listOfComputedFiles);
			
			/*
			 * Creazione di array (serie) float e double per diversi algoritmi DTW
			 */
			
			float[] firstFileMatrix = new float[seriesFirstFile.getItemCount()];
			float[] secondFileMatrix = new float[seriesSecondFile.getItemCount()];
			
			double[] firstFileMatrixd = new double[seriesFirstFile.getItemCount()];
			double[] secondFileMatrixd = new double[seriesSecondFile.getItemCount()];
			
			
			for(int i = 0; i < seriesFirstFile.getItemCount();i++) {
				firstFileMatrix[i] = seriesFirstFile.getY(i).floatValue();
				firstFileMatrixd[i] = seriesFirstFile.getY(i).doubleValue();
				}
						
			for(int i = 0; i < seriesSecondFile.getItemCount(); i++) {
				secondFileMatrix[i] = seriesSecondFile.getY(i).floatValue();
				secondFileMatrixd[i] = seriesSecondFile.getY(i).doubleValue();
				}
			

			/*
			 * DTW Maestre
			 */
						
			TimeSeries a1 = new TimeSeries(firstFileMatrixd);
	    	TimeSeries a2 = new TimeSeries(secondFileMatrixd);
	    	
	      final DistanceFunction distFn = DistanceFunctionFactory.getDistFnByName("EuclideanDistance"); 
	      
	      final TimeWarpInfo info = DTWPro.getWarpInfoBetween(a1, a2, distFn);
	     
	      System.out.println("Warp Distance Mestre: " + info.getDistance());


		/*
		 * DTW secondo algoritmo
		 */
			
			DynamicTimeWrapping1D distance = new DynamicTimeWrapping1D(firstFileMatrixd, secondFileMatrixd);
			
			System.out.println("Wraping secondo " + distance.calDistance());
			
			resultLabel.setText("Warp Distance: " + info.getDistance());
			resultLabel.setVisible(true);	
					

			calculateStableAreas(listOfComputedFiles);

			//autoGenerateStableAreas(listOfComputedFiles);
		}
		
	}

	private double[] getMinimumDistance(XYSeries first, XYSeries second){

		double min = 0;
		double hz = 0;
		double position = 0;
		int i = 8;
		double sum = 0;
		int count = 0;
		while(first.getX(i).doubleValue() < 5500 && first.getX(i).doubleValue() > 50) {
			double dist = Math.sqrt(Math.pow((first.getX(i).doubleValue() - second.getX(i).doubleValue()), 2) + Math.pow((first.getY(i).doubleValue() - second.getY(i).doubleValue()), 2));
			if (i == 8) {
				min = dist;
				hz = first.getX(i).doubleValue();
				position = i;

			} else {
				if (dist < min) {
					min = dist;
					hz = first.getX(i).doubleValue();
					position = i;
				}
			}
			sum += dist;
			count++;
			i++;
		}
		double average = sum/count;
		System.out.println(average + "************************************************");

		double[] result = new double[4];
		result[0] = min;
		result[1] = position;
		result[2] = hz;
		result[3] = average;

		return result;
	}

	private void autoGenerateStableAreas(ArrayList<XYSeries> listOfFiles){

		XYSeries first = listOfFiles.get(0);
		XYSeries second = listOfFiles.get(1);

		double[] result = getMinimumDistance(first, second);

		double[][] area = new double[3][3];
		area [0][0] = first.getX((int) result[1] -1).doubleValue();
		area [0][1] = first.getX((int) result[1] ).doubleValue();
		area [0][2] = first.getX((int) result[1] +1).doubleValue();

		area [1][0] = first.getY((int) result[1] -1).doubleValue();
		area [1][1] = first.getY((int) result[1]).doubleValue();
		area [1][2] = first.getY((int) result[1] + 1).doubleValue();

		area [2][0] = second.getY((int) result[1] -1 ).doubleValue();
		area [2][1] = second.getY((int) result[1]).doubleValue();
		area [2][2] = second.getY((int) result[1] +1).doubleValue();



		System.out.println(area[0][0] + "\t" + area[0][1] + "\t" + area[0][2]);
		System.out.println(area[1][0] + "\t" + area[1][1] + "\t" + area[1][2]);
		System.out.println(area[2][0] + "\t" + area[2][1] + "\t" + area[2][2]);

	}

	
	private void calculateStableAreas(ArrayList<XYSeries> listOfFiles) {

        /**
         * Metodo per calcolare zone stabili
          */

		XYSeries first = listOfFiles.get(0);
		XYSeries second = listOfFiles.get(1);
		
		double[] distances = new double[56];
		
		int counter;
		int aaa = 0;
		int i = 1;
		while(i < 5500) {
			double[] s1 = new double[13];
			double[] s2 = new double[13];
			counter = 0;
			for(int j = 0; j < first.getItemCount(); j++) {
				if(first.getX(j).doubleValue() >= i && first.getX(j).doubleValue() <= i+99) {
					s1[counter] = first.getY(j).doubleValue();
					s2[counter]= second.getY(j).doubleValue();
					counter++;
				}
			}
			
			final TimeWarpInfo info = DTWPro.getWarpInfoBetween(new TimeSeries(s1), new TimeSeries(s2), (DistanceFunctionFactory.getDistFnByName("EuclideanDistance")));
			distances[aaa] = info.getDistance();
			aaa++;
			i = i+100;
		}

		double sum = 0;
		
		for(double dist : distances) {
			sum+=dist;
		}

        /**
         * Calcola media distanza media tra tutte le zone
         */

        double average = sum/distances.length;

		int n = 1;
		double areas[][] = new double [100][3];

        /*
		 * Calcola la seguente matrice
		 *
		 * 			|LowerBound | HigherBound |Distance|
		 * 			| 0         |   100       | 12     |
		 * 			| 101       |   200       | 0      |
		 *
		 */

		for (int x = 0; x < distances.length; x++) {
			areas[x][0] = n;
			areas[x][1] = n+99;
			areas[x][2] = distances[x];
			n+=100;
		}
		
		double stableAreas[][] = new double [100][3];

         /*
		 * Calcola la matrice delle zone stabili
		 *
		 * 			|LowerBound | HigherBound |Distance|
		 * 			| 0         |   100       | 12     |
		 * 			| 101       |   200       | 0      |
		 *
		 */

		n = 1;
		int index = 0;
		for (int x = 0; x < distances.length; x++) {
			if(distances[x] < average) {
				stableAreas[index][0] = n;
				stableAreas[index][1] = n+99;
				stableAreas[index][2] = distances[x];
				n+=100;
				index++;
			} else {
				n +=100;
			}
		}

		System.out.println("**********************" + average);
		System.out.println("************************************************************************ ZONE");

		for(int p = 0; p < 50; p++) {
			System.out.println(areas[p][0] + " | " +areas[p][1] + " | " + areas[p][2]);
		}

		System.out.println("************************************************************************* ZONE STABILI");

		for(int p = 0; p < 50; p++) {
			System.out.println(stableAreas[p][0] + " | " +stableAreas[p][1] + " | " + stableAreas[p][2]);
		}
	}

	private void correlationWithoutZones(ArrayList<XYSeries> listOfFiles) {

        /**
         * Correlazione tra tutti i file.
         * 1. Calcola la correlazione tra i due punti per ogni punto Y
         * 2. Se la correlazione compresa nell'intervallo [-1, -0.95] U [0.95, 1] "salva" i valori
         * 3. Calcola la correlazione tra i valori compresi all'interno di quel range.
         *
         *
         */

        XYSeries first = listOfFiles.get(0);
		XYSeries second = listOfFiles.get(1);

		double[] firstValue = new double[2];
		double[] secondValue = new double[2];
		ArrayList<Double> s1_arr = new ArrayList<>();
		ArrayList<Double> s2_arr = new ArrayList<>();
		PearsonsCorrelation p = new PearsonsCorrelation();


		for(int j = 0; j < first.getItemCount(); j++) {
            if(first.getX(j).doubleValue() < 5500) {
                firstValue[0] = first.getY(j).doubleValue();
                secondValue[0] = second.getY(j).doubleValue();
                double corr = p.correlation(firstValue, secondValue);
                if (corr < -0.95 || corr > 0.95) {
                    s1_arr.add(firstValue[0]);
                    s2_arr.add(secondValue[0]);
                }
            }
		}

        double[] s1_official = new double[s1_arr.size()];
        double[] s2_official = new double[s2_arr.size()];

        for(int i = 0; i < s1_arr.size(); i++){
            s1_official[i] = s1_arr.get(i);
            s2_official[i] = s2_arr.get(i);
        }

        System.out.println("Indice di correlazione " + p.correlation(s1_official, s2_official));

	}
	
}


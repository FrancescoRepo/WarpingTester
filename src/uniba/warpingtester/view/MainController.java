package uniba.warpingtester.view;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

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
		
		if (files.size() > 0) {
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
			
			calculateStableAreas(listOfComputedFiles);
			
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
			 * DTW primo algoritmo (Chee Youl Gun).
			 */
			
		
			DTW dtw = new DTW(firstFileMatrix, secondFileMatrix);
			
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
					
						
		}
		
	}
	
	
	private void calculateStableAreas(ArrayList<XYSeries> listOfFiles) {
		
		/*
		 * This method creates an array of 42 distance every 100 Hz
		 * 
		 * 			|LowerBound | HigherBound |Distance|
		 * 			| 0         |   100       | 12     |
		 * 			| 101       |   200       | 0      |
		 * 			
		 */
		
		XYSeries first = listOfFiles.get(0);
		XYSeries second = listOfFiles.get(1);
		
		double[] distances = new double[43];
		
		int counter;
		int aaa = 0;
		int i = 1;
		while(i < 4300) {
			double[] s1 = new double[50];
			double[] s2 = new double[50];
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
		
		double average = sum/distances.length;
		int n = 1;
		double areas[][] = new double [50][3];
				
		for (int x = 0; x < distances.length; x++) {
			areas[x][0] = n;
			areas[x][1] = n+99;
			areas[x][2] = distances[x];
			n+=100;
		}
		
		double stableAreas[][] = new double [50][3];
		
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
		
		for(int p = 0; p < 50; p++) {
			System.out.println(stableAreas[p][0] + " | " +stableAreas[p][1] + " | " +stableAreas[p][2]);
		}
			
	}
	
}


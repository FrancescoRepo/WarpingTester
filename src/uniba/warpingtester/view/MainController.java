package uniba.warpingtester.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.jfree.data.xy.XYSeries;

import dtw.TimeWarpInfo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser.*;
import timeseries.TimeSeries;
import uniba.warpingtester.utility.Calcolo;
import util.DistanceFunction;
import util.DistanceFunctionFactory;
import dtw.DTWPro;

/**
 * @Class MainController.
 */


public class MainController {
	
	@FXML
	private ListView listMediaFile;
	
	@FXML
	private Label resultLabel;

	@FXML
	private TextField stableFileName;


	private HashMap<String, String> fileMap = new HashMap<String, String>();

	public MainController() {}
	
	public void initialize() {
		listMediaFile.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		resultLabel.setVisible(false);


	}

	/**
	 * Add file using a FileChooser in the software to be analyzed.
	 */

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
			
			List<String> fileNames = new ArrayList<>();
			for (String s : fileMap.keySet()) fileNames.add(s);		
			ObservableList names = FXCollections.observableArrayList(fileNames);
			Collections.sort(names);
			listMediaFile.setItems(names);
		}
		
	}


	/**
	 * Remove selected file from the listMediaFile view.
	 */

	@FXML
	private void removeMediaFile() {
		int index = listMediaFile.getSelectionModel().getSelectedIndex();
		Object fileToRemove = listMediaFile.getSelectionModel().getSelectedItem();

		if(index >= 0) {
			listMediaFile.getItems().remove(index);
			fileMap.remove(fileToRemove);
		} else {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Warning");
	        alert.setHeaderText("Error select files.");
	        alert.setContentText("You must selection 2 file.");
	        alert.showAndWait();
		}
		
	}

	/**
	 * Calculates distances between two spectrum using DTW.
	 * It analyze two selected file and prints the distance.
	 * @throws FileNotFoundException
	 */

	@FXML
	private void calculateDistance() throws FileNotFoundException {
		List<String> selectedFileNames = (List<String>)listMediaFile.getSelectionModel().getSelectedItems();

		FileChooser fc = new FileChooser();
		fc.setInitialDirectory(new File("."));
		fc.setTitle("Add Stable Areas File");
		fc.getExtensionFilters().clear();
		fc.getExtensionFilters().add(new ExtensionFilter("Text file","*.txt"));

		List<File> files = fc.showOpenMultipleDialog(null);

				
		if (selectedFileNames.size() != 2) {
			showMessage(AlertType.WARNING, "Warning", "Error creating file","You must select two files" );
		}else {
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
			

			/*
			 * Creazione di array (serie) float e double per diversi algoritmi DTW
			 */
			

			double[] firstFileMatrix = new double[seriesFirstFile.getItemCount()];
			double[] secondFileMatrix = new double[seriesSecondFile.getItemCount()];
			
			
			for(int i = 0; i < seriesFirstFile.getItemCount();i++) {
				firstFileMatrix[i] = seriesFirstFile.getY(i).doubleValue();
				}
						
			for(int i = 0; i < seriesSecondFile.getItemCount(); i++) {
				secondFileMatrix[i] = seriesSecondFile.getY(i).doubleValue();
				}


			/*
			 * DTW Maestre
			 */

			double[][] stableMatrix = readFromFile(files.get(0));

			ArrayList<Double> firstRealMatrix = new ArrayList<>();
			ArrayList<Double> secondRealMatrix = new ArrayList<>();
			for(int i = 0; i < stableMatrix.length; i++){
				for(int j = 0; j < seriesFirstFile.getItemCount(); j++){
					if(seriesFirstFile.getX(j).doubleValue() >= stableMatrix[i][0] && seriesFirstFile.getX(j).doubleValue() <= stableMatrix[i][1]){
						firstRealMatrix.add(seriesFirstFile.getY(i).doubleValue());
					}
					if(seriesSecondFile.getX(j).doubleValue() >= stableMatrix[i][0] && seriesSecondFile.getX(j).doubleValue() <= stableMatrix[i][1]){
						firstRealMatrix.add(seriesSecondFile.getY(i).doubleValue());
					}
				}
			}

			double[] firstSeries =  new double[firstRealMatrix.size()];
			double[] secondSeries = new double[firstRealMatrix.size()];
			for(int i = 0; i < firstRealMatrix.size();i++){
				firstSeries[i] = firstRealMatrix.get(i).doubleValue();
			}
			for(int i = 0; i < secondRealMatrix.size();i++){
				secondSeries[i] = secondRealMatrix.get(i).doubleValue();
			}


			TimeSeries a1 = new TimeSeries(firstSeries);
	    	TimeSeries a2 = new TimeSeries(secondSeries);

	    	TimeSeries b1 = new TimeSeries(firstFileMatrix);
	    	TimeSeries b2 = new TimeSeries(secondFileMatrix);
	    	
	      final DistanceFunction distFn = DistanceFunctionFactory.getDistFnByName("EuclideanDistance"); 
	      
	      final TimeWarpInfo info = DTWPro.getWarpInfoBetween(a1, a2, distFn);

	      final TimeWarpInfo withoutStableAreas = DTWPro.getWarpInfoBetween(b1, b2, distFn);
	     
	      System.out.println("Warp Distance Mestre: " + withoutStableAreas.getDistance());

			resultLabel.setText(String.format("Warp Distance: " + info.getDistance(), "%.4d"));
			resultLabel.setVisible(true);	

		}
		
	}

	/**
	 * Analyze spectrum to identify stable zone.
	 * Call method calculateStableAreas to calculate stable areas of spectrum
	 * Call method writeOnFile to save those zone in file.
	 *
	 * @throws FileNotFoundException
	 */

	@FXML
	private void createStableAreaFile() throws IOException, InterruptedException {
		List<String> selectedFileNames = (List<String>)listMediaFile.getSelectionModel().getSelectedItems();
		if(stableFileName.getText().equals("")){
			showMessage(AlertType.WARNING, "Warning", "Error creating file","Insert Surname and Name." );
		} else if(selectedFileNames.isEmpty()) {
			showMessage(AlertType.WARNING, "Warning", "Error creating file", "You must select two file.");
		} else if(selectedFileNames.size() < 2) {
			showMessage(AlertType.WARNING, "Warning", "Error creating file", "You must select two file.");
		} else{
			ArrayList<XYSeries> listOfComputedFiles = new ArrayList<>();
			for (int i = 0; i < selectedFileNames.size(); i++) {
				String pathFile = fileMap.get(selectedFileNames.get(i));
				Calcolo computedFile = new Calcolo(pathFile);
				XYSeries computedSeriesFile = computedFile.AnalisiFrequenze();
				listOfComputedFiles.add(computedSeriesFile);
			}

			double [][] stableAreaMatrix = calculateStableAreas(listOfComputedFiles);
			writeOnFile(stableAreaMatrix);
		}

	}


	/**
	 * Writes on file
	 * @param stableAreaMatrix
	 * @throws FileNotFoundException
	 */

	/**
	 * Save on file stable zone with the following format:
	 *
	 * 		LOWER BOUND | HIGHER BOUND | DISTANCE
	 * 			1		|	  100	   |  6.89..
	 * 		   1201		|	 1301	   |  9.09..
	 *
	 * @param stableAreaMatrix
	 * @throws FileNotFoundException
	 */

	private void writeOnFile(double[][] stableAreaMatrix) throws IOException {
		String name = stableFileName.getText();
		File stableAreasFile = new File("stablefiles\\" +name + ".txt");
		stableAreasFile.getParentFile().mkdir();
		PrintWriter printWriter = new PrintWriter(stableAreasFile);
		printWriter.flush();
		for(double[] array: stableAreaMatrix){
			printWriter.println(array[0] + "|" + array[1] + "|" + array[2]);
		}
		printWriter.close();

		if(stableAreasFile.exists()){
			showMessage(AlertType.INFORMATION, "File created", "","File created and saved in \n" + stableAreasFile.getCanonicalPath() );
		}
	}

	/**
	 * Reads from file and convert data into matrix of stable zone.
	 * @param stableFile
	 * @return
	 * @throws FileNotFoundException
	 */

	private double[][] readFromFile(File stableFile) throws FileNotFoundException {
		Scanner countScanner = new Scanner(stableFile);
		int lines = 0;
		while(countScanner.hasNext()){
			String string = countScanner.nextLine();
			if (!string.equals("0.0|0.0|0.0")){
				lines++;
			}
		}
		countScanner.close();
		Scanner realScanner = new Scanner(stableFile);
		double [][] stableMatrix = new double[lines][3];
		for(int i = 0; i< lines; i++){
			String string = realScanner.nextLine();
			String array[] = string.split("\\|");
			stableMatrix[i][0] = Double.parseDouble(array[0]);
			stableMatrix[i][1] = Double.parseDouble(array[1]);
			stableMatrix[i][2] = Double.parseDouble(array[2]);
		}
		return stableMatrix;
	}

	/**
	 * Calculate stable areas of two spectrums.
	 * It devides the spectrum in 100 Hz areas and calculate the distance within those areas.
	 * Next calculate the average distance and identify as stable zone those who have the distance lower than the average.
	 * @param listOfFiles
	 * @return
	 */

	private double[][] calculateStableAreas(ArrayList<XYSeries> listOfFiles) throws InterruptedException {

		XYSeries first = listOfFiles.get(0);
		XYSeries second = listOfFiles.get(1);
		
		double[] distances = new double[55];
		
		int counter;
		int k = 0;
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
			distances[k] = info.getDistance();
			k++;
			i = i+100;
		}

		double sum = 0;
		
		for(double dist : distances) {
			sum+=dist;
		}

        double average = sum/distances.length;
		int cntStableZone = 0;

		for(i = 0; i < distances.length; i++) {
			if(distances[i] < average) cntStableZone++;
		}

		double stableAreas[][] = new double [cntStableZone][3];
		int n = 1;
		int index = 0;

		for (i = 0; i < distances.length; i++) {
			if(distances[i] < average) {
				stableAreas[index][0] = n;
				stableAreas[index][1] = n+99;
				stableAreas[index][2] = distances[i];
				n+=100;
				index++;
			} else {
				n +=100;
			}
		}

		// print result on console
		//testMethod(average, areas, stableAreas);

		return stableAreas;
	}

	/**
	 * Prints all results in console.
	 * @param average
	 * @param areas
	 * @param stableAreas
	 */

	private void testMethod(double average, double[][] areas, double[][] stableAreas) {
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

	private void showMessage(AlertType type, String title, String headerText, String contentText) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(headerText);
		alert.setContentText(contentText);
		alert.showAndWait();
	}
}


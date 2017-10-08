package uniba.warpingtester.utility;

import java.io.File;
import org.jfree.data.xy.XYSeries;
import com.musicg.wave.Wave;
import com.musicg.wave.WaveHeader;

/**
 * Classe per effettuare tutte le operazioni di conteggio e calcolo della durata.
 * @author Donato De Benedictis
 * 
 */
public class Calcolo {
    public double[] real;
    double[] values;
    double[] valuesdouble;
	public double[] realvalues;
	double numCampioni; //frequenza di campionamento
	private double durata;
	
	
	public double[] ampiezze;          //tagliando 1/2 secondo all'inizio e 1/2 secondo alla fine
	
	double[] abs;
	double[] average;
	double[] avg;
	double[] xVal;
	double[] yVal;
	double[] counting;
	
	private static int freqCamp;
	
	
	String file;
	
	public Calcolo(String file){
		
		//nome del file
		this.file = file;
		
		//Ritorna un vettore di ampiezze reali(comprende quindi anche le ampiezze negative)
		//Qui nel getValuesReal ho messo anche l'assegnazione a  un vettore real che prene proprio le ampiezze pure 
		//come le da il java,l'ho fatto per fare stampare il grafico(Ampiezze-tempo) proprio uguale a quello di audacity
        realvalues = getValuesReal();
		
		//Ritorna un Vettore di ampiezze reali in valore assoluto prendendo un valore ogni tot secondi;
		this.abs = getValues();
		
		//prima media su ampiezze reali in valore assoluto
		this.average = windows(abs);
		
		//Seconda media sulla prima media di sopra
	    this.avg = windowsAvg(average);
		
	}
	
	public Calcolo() {
		
	}
	
	//Ritorna i valori reali delle ampiezze,quindi anche quelli negativi
	private double[] getValuesReal() {

		Wave wave = new Wave(this.file);
		
		
		//l'ho fatto perch� lo stampo nel grafico ampiezza tempo e ho proprio quello che otteniamo in audacity
		real= wave.getNormalizedAmplitudes();
		realvalues = wave.getNormalizedAmplitudes();
		//System.out.println(real.length+" "+realvalues.length);

		//frequenza di campionamento in base alle modalit� di registrazioni.
				try {
//				numCampioni = real.length / Double.valueOf(wave.timestamp());
				numCampioni = real.length / Double.valueOf(wave.length());
				freqCamp = (int) (realvalues.length / wave.length());
				} catch (Exception e) {
					System.out.println("error: " + e.getMessage() + ", error code: " + e.getCause());
					System.out.println("wave timestamp " + wave.length());
					System.out.println("wrapped wave length " + Double.valueOf(wave.length()));
					System.out.println("real.length " + real.length);
					System.out.println("numCampioni " + numCampioni);
				}
				//Tempo.setTempo(wave.timestamp());
				Tempo.setTempo(wave.length());
				
				
				double[] support;
				try{
//					support = new double[(int) (Double.parseDouble(wave.timestamp()) * 100*2) + 1];
					support = new double[(int) (wave.length() * 100*2) + 1];
				}catch(Exception e){
					support = new double[1];
					System.out.println("error: " + e.getMessage() + ", error code: " + e.getCause());
					System.out.println("wave length " + wave.length());
					System.out.println("wrapped wave length " + Double.valueOf(wave.length()));
					System.out.println("real.length " + real.length);
					System.out.println("numCampioni " + numCampioni);
				}
				try{
//					valuesdouble=new double[(int) (Double.parseDouble(wave.timestamp()) * 100*2) + 1];
					valuesdouble=new double[(int) (wave.length() * 100*2) + 1];

				}catch(Exception e){
					support = new double[1];
					System.out.println("error: " + e.getMessage() + ", error code: " + e.getCause());
					System.out.println("wave length " + wave.length());
					System.out.println("wrapped wave length " + Double.valueOf(wave.length()));
					System.out.println("real.length " + real.length);
					System.out.println("numCampioni " + numCampioni);
				}
		
		int numCampTagl = (int)numCampioni/2;
		ampiezze=new double[real.length-2*numCampTagl];
		for (int x=numCampTagl; x<real.length-numCampTagl; x++)ampiezze[x-numCampTagl]=real[x];
		//System.out.println("Campioni Tagliati"+numCampTagl*2+" real.length"+real.length+" real2.length"+real2.length);
		
		int j = 0;
		//frequenza a 16000hz
		if(numCampioni >= 15000 && numCampioni <= 17000) {
			
			for (int i = 0; i < realvalues.length-80; i += 80) {
				
				support[j] = realvalues[i] * 100;
				
				valuesdouble[j]=realvalues[i] * 100;
				j++;
			}

		//frequenza a 44100hz
		}else if(numCampioni >= 43000 && numCampioni <= 45000) {
			for (int i = 0; i < realvalues.length -222; i += 222) {
				
				support[j] = realvalues[i] * 100;
				
				valuesdouble[j]=realvalues[i] * 100;
				j++;
			}
		}
		
		//frequenza a circa 88000hz
		else if(numCampioni >= 87000 && numCampioni <= 89000) {
			for(int i = 0; i < realvalues.length-441; i+=441) {
				
				support[j] = realvalues[i] * 100;
				
				valuesdouble[j]=realvalues[i] * 100;
				j++;
			}
		}
		realvalues = support;
		//System.out.println(real.length + " " + realvalues.length);
		return realvalues;
		
		
	}
	
	
	//Ritorna i valori delle ampiezze in valore assoluto
	private double[] getValues() {

		Wave wave = new Wave(this.file);

		values = wave.getNormalizedAmplitudes();

		int j = 0;
		//qualsiasi sia la frequenza di campionamento, prendo 200 valori al secondo.
		double[] support;
		
		try{
//			support = new double[(int) (Double.parseDouble(wave.timestamp()) * 100*2) + 1];
			support = new double[(int) (wave.length() * 100*2) + 1];
		}catch(Exception e){
			support = new double[1];
			System.out.println("error: " + e.getMessage() + ", error code: " + e.getCause());
			System.out.println("wave length " + wave.length());
			System.out.println("wrapped wave length " + Double.valueOf(wave.length()));
			System.out.println("real.length " + real.length);
			System.out.println("numCampioni " + numCampioni);
		}
		if(numCampioni >= 15000 && numCampioni <= 17000) {
			for (int i = 0; i < values.length-80; i += 80) {
				support[j] = Math.abs(values[i] * 100);
				
				j++;
			}
		}else if(numCampioni >= 43000 && numCampioni <= 45000) {
			for (int i = 0; i < values.length - 222; i += 222) {
				support[j] = Math.abs(values[i] * 100);
				
				j++;
			}
		}
		else if(numCampioni >= 87000 && numCampioni <= 89000) {
			for(int i = 0; i < values.length-441; i+=441) {
			
				support[j] = Math.abs(values[i] * 100);
				
				
				j++;
			}
		}

		values = support;
		
		return values;
	
	}

	//finestra per conteggiare le sillabe
	private double[] windows(double[] values) {
		counting = new double[values.length/2];
		int j = 0;
		for(int i = 0; i < values.length-2; i+=2) {
			counting[j] = values[i];
			j++;
		}
		average = new double[counting.length];
		for (int i = 0; i < counting.length - 10; i++) {
			double count = 0;
			for (int k = 0; k < 10; k++) {
				count = count + counting[i + k];
			}
			average[i] = (count / 10);
		}
		return average;
	}
	
	

	//media su ogni finestra creata.
	private double[] windowsAvg(double[] average2) {
		avg = new double[average2.length];
		for (int i = 0; i < average2.length - 10; i++) {
			double count = 0;
			for (int k = 0; k < 10; k++) {
				count = count + average2[i + k];
			}
			avg[i] = count / 10;
		}
		for(int i = 0; i < avg.length; i++) {
			
		}
		return avg;
	}
	
	/**
	 * Metodo per calcolare la matrice ampiezze/frequenze di un file wav
	 *
	 */
	public XYSeries AnalisiFrequenze(){
		//Calcola le frequenze con l'algoritmo DFT passando il vettore delle ampiezze sul quale calcolarlo
		double frequenze[] = new double[1024];
		TestDFT6.dft(real,frequenze);
		
		Wave wave = new Wave(this.file);
		WaveHeader wh=wave.getWaveHeader();
		float mul = wh.getSampleRate()*0.9765625f/(2*1000);
		
		XYSeries series = getSeries(file, mul, frequenze);
		
		return series;
	}
	
	private XYSeries getSeries(String file,float mul, double[] frequenze) {
        
        final XYSeries series = new XYSeries(file);
        
        double variable;
                        
        for(int i = 0; i < frequenze.length; i++){
        	series.add(i*mul, frequenze[i]);
            }
       
        return series;
        
        /* 
        for(int i = 0; i < frequenze.length; i++){
        	variable = i*mul;
        	if((variable >= 1 && variable <= 800) || (variable >= 3400 && variable <= 4500)) {
        		series.add(i*mul, frequenze[i]);
        	}
        }
        
        */
    }
	
}
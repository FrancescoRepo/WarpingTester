package uniba.warpingtester.utility;

import javax.sound.sampled.*;

import java.util.*;


public class SignalProcUtil {

    // Play the given sampled sound at the given rate. Good numbers to use for
    // samplesPerSecond are 400 (low quality, but easy to compute with) and
    // 16000 (high-quality, but long computations).

    public static void playSoundBytes (byte[] samples, int samplesPerSecond) 
    {
	// Modified from code at www.jsresources.org
        // Make an AudioFormat instance that specifies:
        //   sampleRate, #bits/sample=8, #channels=1 (mono), signed-PCM.
        AudioFormat audioFormat = new AudioFormat (samplesPerSecond, 8, 1, true, true);

        //System.out.println (audioFormat);
        
        // I don't (yet) understand the rest of the code below. It's all peculiar
	// to how Java does sound. Some of it is explained at jsresources.org.

        SourceDataLine line = null;
        DataLine.Info info = new DataLine.Info (SourceDataLine.class, audioFormat);
        try {
            line = (SourceDataLine) AudioSystem.getLine (info);
            line.open (audioFormat);
        }
        catch (LineUnavailableException e)
        {
            e.printStackTrace ();
            System.exit (1);
        }
        catch (Exception e)
        {
            e.printStackTrace();

            System.exit (1);
        }
        
        line.start();
        line.write (samples, 0, samples.length);
        line.drain();
        line.close();
    }


    // Create sampled sound at a desired frequency, such as 256Hz. For example, use
    // freq=440.0 to get the sound for middle-A on the piano, the standard by which
    // all notes are created on musical instruments.

    public static byte[] makeSampledSound (double freq, int samplesPerSecond, int playTimeSeconds)
    {
        // Total number of samples:
        int numSamples = samplesPerSecond * playTimeSeconds;

        // Build the array of bytes (one byte per sample).
        byte[] samples = new byte [numSamples];

        // The samples/second determines the #samples per cycle.
        int samplesPerCycle = (int) Math.floor (samplesPerSecond / freq);

        // We'll advance the angle by #samples/cycle:
        double delTheta = 2*Math.PI / samplesPerCycle;

        // Start at angle=0.
        double theta = 0;

        // Now build all the samples.
        for (int i=0; i<numSamples; i++) {

            // Sample value at theta. Amplitude is in [-127, 127].
            double y = 127 * Math.sin (theta);

            // Convert to byte and assign. NOTE: we are actually forcing
            // b into the range [-126, 126]. For whatever reason, there's
            // a problem in Java with values outside this range.
            byte b = 0;
            if (y > 0) {
                b = (byte) Math.floor (y);
            }
            if (y < 0) {
                b = (byte) Math.ceil (y);
            }
            samples[i] = b;

            theta += delTheta;

        }

        return samples;
    }
    


    // Here, lowNotes and highNotes need to be strings like "CCEE" 
    // (two C notes, followed by two E notes). The two strings must be the same length.
    // The method returns sampled sound bytes at the given # samples per note.

    public static byte[] makeMusic (String lowNotes, String highNotes, int samplesPerNote)
    {
        // First, check validity: (1) strLen, (2) note letters
        if (lowNotes.length() != highNotes.length()) {
            System.out.println ("ERROR: makeMusic(): must have same length");
            return null;
        }
        int numNotes = lowNotes.length ();
        byte[] samples = new byte [numNotes * samplesPerNote];
        for (int i=0; i<numNotes; i++) {
            double freq = getFrequency (lowNotes.charAt(i), true);
            byte[] lowSamples = makeSampledSound (freq, samplesPerNote, 1);    // 1 = one second.
            freq = getFrequency (highNotes.charAt(i), false);
            byte[] highSamples = makeSampledSound (freq, samplesPerNote, 1);

            byte[] combined = addSounds (lowSamples, highSamples);

            for (int j=0; j<samplesPerNote; j++) {
                samples[i*samplesPerNote + j] = combined[j];
            }
            
            /*
            for (int j=0; j<samplesPerNote; j++) {
                double avg = 0.5 * (lowSamples[j] + highSamples[j]);
                byte b = (byte) avg;
                if (b < -126) {
                    b = -126;
                }
                else if (b > 126) {
                    b = 126;
                }
                samples[i*samplesPerNote + j] = b;
             }*/
        }

        return samples;
    }
    

    // To store frequencies. We are using the octave below middle C for the low
    // range, and the middle-C octave for the high range.
    static HashMap<Character, Double> lowNoteFrequencies, highNoteFrequencies;

    static double getFrequency (char note, boolean isLow)
    {
        if ((lowNoteFrequencies == null) || (highNoteFrequencies == null)) {
            lowNoteFrequencies = new HashMap<Character, Double> ();
            lowNoteFrequencies.put ('C', 130.81);
            lowNoteFrequencies.put ('D', 146.83);
            lowNoteFrequencies.put ('E', 164.81);
            lowNoteFrequencies.put ('F', 174.61);
            lowNoteFrequencies.put ('G', 196.00);
            lowNoteFrequencies.put ('A', 220.00);
            lowNoteFrequencies.put ('B', 246.94);

            highNoteFrequencies = new HashMap<Character, Double> ();
            highNoteFrequencies.put ('C', 261.63);
            highNoteFrequencies.put ('D', 293.66);
            highNoteFrequencies.put ('E', 329.63);
            highNoteFrequencies.put ('F', 349.23);
            highNoteFrequencies.put ('G', 392.00);
            highNoteFrequencies.put ('A', 440.00);
            highNoteFrequencies.put ('B', 493.88);
        }

        if (isLow) {
            return lowNoteFrequencies.get (note);
        }

        return highNoteFrequencies.get (note);
    }


    public static byte[] addSounds (byte[] signal1, byte[] signal2)
    {
	if (signal1.length != signal2.length) {
	    System.out.println ("ERROR: SignalProcUtil.addSounds(): input arrays of different length");
	    return null;
	}
	double [] combinedInt = new double [signal1.length];
	double max = Double.MIN_VALUE,  min = Double.MAX_VALUE;
	for (int i=0; i<combinedInt.length; i++) {
	    combinedInt[i] = signal1[i] + signal2[i];
	    if (combinedInt[i] < min) {
		min = combinedInt[i];
	    }
	    if (combinedInt[i] > max) {
		max = combinedInt[i];
	    }
	}

	// Get the larger of the two absolute values.
	max = Math.abs (max);
	min = Math.abs (min);
	if (min > max) {
	    max = min;
	}

	// Now scale
	byte[] combined = new byte [combinedInt.length];
	for (int i=0; i<combined.length; i++) {
	    double v = combinedInt[i];
	    v = v * 126.0 / max;
	    byte b = (byte) v;
	    if (b < -126) {
		b = -126;
	    }
	    else if (b > 126) {
		b = 126;
	    }
	    combined[i] = b;
	}	

	return combined;
    }


    // The real-part of the signal is scaled to the [-126,126] range. 

    public static byte[] convertComplexSignalToBytes (Complex[] signal)
    {
        byte[] samples = new byte [signal.length];

        // First find min, max.
        double min=Double.MAX_VALUE, max=Double.MIN_VALUE;
        for (int i=0; i<signal.length; i++) {
            if (signal[i].re < min) {
                min = signal[i].re;
            }
            if (signal[i].re > max) {
                max = signal[i].re;
            }
        }

        // Now convert with a simple scaling: using the min and max values.
        for (int i=0; i<signal.length; i++) {
            double d = -126.0 + 2*126*(signal[i].re - min) / (max - min);
            byte b = (byte) d;
            if (b < -126) {
                b = -126;
            }
            else if (b > 126) {
                b = 126;
            }
            samples[i] = b;
        }

        return samples;
    }


    public static Complex[] convertBytesToComplex (byte[] samples)
    {
        Complex[] signal = new Complex [samples.length];
        for (int i=0; i<signal.length; i++) {
            signal[i] = new Complex (samples[i], 0);
        }
        return signal;
    }
    


    // Create a Complex sinusoid (either sin or cos) with given frequency and 
    // amplitude over N samples. 

    public static Complex[] makeComplexSinusoid (int N, int freq, double amplitude, boolean isSinPhase)
    {
	if (isSinPhase) {
	    return makeComplexSinusoid (N, freq, amplitude, 0);
	}
	else {
	    return makeComplexSinusoid (N, freq, amplitude, Math.PI/2.0);
	}
    }


    // Create a Complex sinusoid with given frequency, phase, and
    // amplitude over N samples. 

    public static Complex[] makeComplexSinusoid (int N, int freq, double amplitude, double phase)
    {
	double[] dSignal = makeSinusoid (N, freq, amplitude, phase);
	Complex[] signal = new Complex [N];
	for (int i=0; i<N; i++) {
	    signal[i] = new Complex (dSignal[i], 0);
	}
	return signal;
    }


    // Make N byte samples of a given sinusoid. The amplitude is assumed to be 126.

    public static byte[] makeSampledSinusoid (int N, int f, double phase)
    {
	Complex[] signal = makeComplexSinusoid (N, f, 126, phase);
	return convertComplexSignalToBytes (signal);
    }

    // Make a sinusoid over N samples with given frequency, amplitude and phase.

    static double[] makeSinusoid (int N, int f, double a, double phase)
    {
	// f=0 => DC,   f=1 => full cycle starting at 0.
	// f =2 => two full cycles .. etc
	// f = N/2 => N/2 full cycles.

	double[] signal = new double [N];
	
	if (f == 0) {
	    // DC signal.
	    for (int i=0; i<N; i++) {
		signal[i] = a;
	    }
	    return signal;
	}
	
	for (int i=0; i<N; i++) {
	    signal[i] = a* Math.sin (((2*Math.PI*i*f) / (double)N) + phase);
	}

	return signal;
    }

    /**
     * Algoritmo che implementa la Discrete Fourier Transform DFT 
     * @param signal, campioni che compongono il segnale in ingresso
     * @param N, numero di campioni sul quale esguire la DFT 
     * @return spectrum, array di numeri complessi che costituiscono 
     * 					 lo spettro del segnale in ingresso
     */

    public static Complex[] DFT (Complex[] signal, int N) 
    {
	// Make the space for the resulting DFT (the spectrum).
	Complex[] spectrum = new Complex [N];

	// Compute the DFT
	for (int k=0; k<N; k++) {
	    Complex sum = new Complex (0,0);
	    for (int j=0; j<N; j++) {
		
	    	// Compute W*_N^kj:
	    	double cosTerm = Math.cos (2*Math.PI*k*j / (double)N);
	    	double sinTerm = -Math.sin (2*Math.PI*k*j / (double)N);
	    	Complex WN_star_pow_kj = new Complex (cosTerm, sinTerm);
		
	    	// Multiply by f(j):
	    	Complex product = signal[j].mult (WN_star_pow_kj);
	    	
	    	// Accumulate:
	    	sum = sum.add (product);
	    }
	    // Scale by N:
	    spectrum[k] = sum.mult (1.0/N);
	}

	return spectrum;
    }

    /**
     * Algoritmo che implementa la DFT Inversa
     * @param spectrum, spettro del segnale da ricostruire
     * @param N, numero di campioni dello spettro da utilizzare nella IDFT
     * @return signal, segnale ricostruito dallo spettro
     */
    public static Complex[] inverseDFT (Complex[] spectrum, int N) 
    {
	// Make the space for the resulting invDFT (the signal).
	Complex[] signal = new Complex [N];

	// Compute the inverse.
	for (int j=0; j<N; j++) {
	    Complex sum = new Complex (0,0);
	    for (int k=0; k<N; k++) {
		
	    	// Compute W_N^jk:
	    	double cosTerm = Math.cos (2*Math.PI*k*j / (double)N);
	    	double sinTerm = Math.sin (2*Math.PI*k*j / (double)N);
	    	Complex WN_pow_jk = new Complex (cosTerm, sinTerm);
		
	    	// Multiply by F(k):
	    	Complex prod = spectrum[k].mult (WN_pow_jk);
		
	    	// Accumulate:
	    	sum = sum.add (prod);
	    }
	    // Do NOT scale by N.
	    signal[j] = sum;
	}

	return signal;
    }


    /**
     * Algoritmo che calcola la Fast Fourier Transform (usa recursivaFFT)
     * e raffina i valori relativi allo spettro
     * @param signal, campioni che compongono il segnale in ingresso
     * @param N, numero di campioni su cui eseguire la FFT
     * @return spectrum, spettro del segnale
     */
    public static Complex[] FFT (Complex[] signal, int N) 
    {
	// First, compute the FFT recursively:
        Complex[] spectrum = recursiveFFT (signal, N);
	// Simply scale by 1/N in a single O(N) sweep:
        for (int k=0; k<N; k++) {
            spectrum[k] = spectrum[k].mult (1.0/N);
        }
        return spectrum;
    }
    
    /**
     * Algoritmo ricorsivo che calcola la Fast Fourier Transform
     * @param signal, campioni che compongono il segnale da trasformare
     * @param N, numero di campioni su cui eseguire la FFT
     * @return result, risultato della 
     */
    static Complex[] recursiveFFT (Complex[] signal, int N) 
    {
	// Bottom-out case:
        if (N == 1) {
	    return new Complex[] { signal[0] }; 
	}

	// Error checking:
        if (N % 2 != 0) { 
	    System.out.println ("ERROR: N not divisible by 2");
	    return null;
	}

        // FFT of even terms
        Complex[] even = new Complex [N/2];
        for (int k = 0; k < N/2; k++) {
            even[k] = signal[2*k];
        }
        Complex[] evenFFT = recursiveFFT (even, N/2);

        // FFT of odd terms
        Complex[] odd  = new Complex [N/2];
        for (int k = 0; k < N/2; k++) {
            odd[k] = signal[2*k + 1];
        }
        Complex[] oddFFT = recursiveFFT (odd, N/2);

        // Combine: first, the F[0], ... F[N/2-1] terms:
        Complex[] result = new Complex[N];
        for (int k = 0; k < N/2; k++) {
            double angle = -2 * k * Math.PI / N;
            Complex w = new Complex (Math.cos(angle), Math.sin(angle));
            result[k] = evenFFT[k].add (w.mult(oddFFT[k]));
        }
	// Next, the second half: the F[N/2], ... F[N-1] terms:
	for (int k=N/2; k<N; k++) {
            double angle = -2 * k * Math.PI / N;
            Complex w = new Complex (Math.cos(angle), Math.sin(angle));
            result[k] = evenFFT[k-N/2].add(w.mult(oddFFT[k-N/2]));
	}

        return result;
    }

    /**
     * Algoritmo che implementa la FFT Inversa
     * @param spectrum, lo spettro del segnale da ricostruire
     * @param N, numero di campioni dello spettro
     * @return result, campioni di segnale ricostruito
     */
    public static Complex[] inverseFFT (Complex[] spectrum, int N) 
    {
	// Bottom-out case:
        if (N == 1) {
	    return new Complex[] { spectrum[0] };
	}

	// Error checking:
        if (N % 2 != 0) { 
	    System.out.println ("ERROR: N not divisible by 2");
	    return null;
	}

        // invFFT of even terms
        Complex[] even = new Complex [N/2];
        for (int k = 0; k < N/2; k++) {
            even[k] = spectrum[2*k];
        }
        Complex[] evenFFT = inverseFFT (even, N/2);

        // FFT of odd terms
        Complex[] odd  = new Complex [N/2];
        for (int k = 0; k < N/2; k++) {
            odd[k] = spectrum[2*k + 1];
        }
        Complex[] oddFFT = inverseFFT (odd, N/2);

        // Combine: first, the f[0], ... f[N/2-1] terms:
        Complex[] result = new Complex[N];
        for (int k = 0; k < N/2; k++) {
            double angle = 2 * k * Math.PI / N;
            Complex w = new Complex (Math.cos(angle), Math.sin(angle));
            result[k] = evenFFT[k].add(w.mult(oddFFT[k]));
        }
	// Next, the second half: the f[N/2], ... f[N-1] terms:
	for (int k=N/2; k<N; k++) {
            double angle = 2 * k * Math.PI / N;
            Complex w = new Complex (Math.cos(angle), Math.sin(angle));
            result[k] = evenFFT[k-N/2].add(w.mult(oddFFT[k-N/2]));
	}

        return result;
    }


    // Plot the complex signal or spectrum, as the case may be.

    //manca plot complex


}



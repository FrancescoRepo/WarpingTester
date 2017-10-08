package uniba.warpingtester.utility;

public class TestDFT6 {

	/**
	 * Funzione che calcola lo spettro di un segnale attraverso la DFT
	 * @param samples, campioni del segnale di input
	 * @param realspectrum, lo spettro del segnale
	 */
	public static void dft (double []samples, double realspectrum[])
    {
	// 8192 = 2^13 samples per second. 
	//int samplesPerSecond = 16384;
	//byte[] samples = SignalProcUtil.makeMusic ("CCCC", "CCGG", samplesPerSecond);
	
	// Window size of N=1024
	//System.out.println(realspectrum.length);
	int N = 2048;
	for (int i=0; i<N/2; i++)realspectrum[i]=0;

	int numWindows = samples.length / N;


  	//byte[] filteredBytes = new byte [samples.length];

        // Process each window separately.
        for (int w=0; w<numWindows; w++) {
	    // Make space for the complex signal and fill it with N samples in window.
            Complex[] signal = new Complex [N];
            for (int i=0; i<N; i++) {
                signal[i] = new Complex ((double)samples[w*N+i], 0);
            }

	    // Get DFT of signal.
	    Complex[] spectrum = SignalProcUtil.FFT (signal, N);

	    for (int i=0; i<N/2; i++) realspectrum[i] += (Math.abs(spectrum[i].re));
	    
	    // Convert back to signal domain.
	    //Complex[] filteredSignal = SignalProcUtil.inverseFFT (spectrum, N);

        // Copy to filtered bytes.
        // byte[] filteredWindow = SignalProcUtil.convertComplexSignalToBytes (filteredSignal);
        //    for (int i=0; i<N; i++) {
        //        filteredBytes[w*N+i] = filteredWindow[i];
        //    }
	    //System.out.println ("Processed window w= " + w);
        }
        
        for (int i=0; i<N/2; i++) realspectrum[i] /=numWindows;
        for (int i=0; i<N/2; i++) realspectrum[i] = Math.log10(realspectrum[i]);
        for (int i=0; i<N/2; i++) realspectrum[i] *=20;
        for (int i=0; i<N/2; i++) realspectrum[i] +=20;
        //SignalProcUtil.playSoundBytes (filteredBytes, samplesPerSecond);
	
    }
	
}
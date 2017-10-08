package uniba.warpingtester.utility;
/**
 * Classe per la gestione della parte reale e immaginaria
 * @author Luca Adesso
 *
 */
public class Complex {

    // Real and imaginary parts
    double re, im;

    public Complex (double re, double im)
    {
        this.re = re;
        this.im = im;
    }

    public double magnitude ()
    {
	return Math.sqrt (re*re + im*im);
    }

    public Complex add (Complex c)
    {
        return new Complex (re + c.re, im + c.im);
    }

    public Complex sub (Complex c)
    {
	return new Complex (re - c.re, im - c.im);
    }
    
    public Complex mult (Complex c)
    {
        return new Complex (re*c.re - im*c.im, re*c.im + im*c.re);
    }

    public Complex mult (double a)
    {
        return new Complex (re*a, im*a);
    }
    

    public Complex pow (int n)
    {
        Complex result = new Complex (re, im);
        for (int i=2; i<=n; i++) {
            result = result.mult (this);
        }
        return result;
    }

    public String toString ()
    {
	return "(" + re + "," + im + ")";
    }
    

}


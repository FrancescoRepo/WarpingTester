package uniba.warpingtester.utility;

/**
 * Classe che gestisce le coordinate x e y da rappresentare nel grafico.
 * @author Donato De Benedictis
 */
public class Coordinates {
	
	private double x;
	
	private double y;
	
	public Coordinates(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Getter della coordinata x(ascissa).
	 * @return la coordinata x.
	 */
	public double getX() {
		return x;
	}
	
	/**
	 * Setter della coordinata x(ascissa).
	 * @param x - coordinata x.
	 */
	public void setX(double x) {
		this.x = x;
	}
	
	/**
	 * Getter della coordinata y(ordinata).
	 * @return la coordinata y.
	 */
	public double getY() {
		return y;
	}
	
	/**
	 * Setter della coordinata y(ordinata).
	 * @param y - coordinata y.
	 */
	public void setY(double y) {
		this.y = y;
	}
	
	@Override
	public String toString() {
		return "Coordinata X: " + String.valueOf(x) + "\nCoordinata Y: " + String.valueOf(y);
	}
	
}
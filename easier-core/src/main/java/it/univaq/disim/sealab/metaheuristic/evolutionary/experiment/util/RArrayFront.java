package it.univaq.disim.sealab.metaheuristic.evolutionary.experiment.util;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.point.Point;
import org.uma.jmetal.util.point.impl.ArrayPoint;

import java.io.*;
import java.util.*;

/**
 * This class implements the {@link Front} interface by using an array of
 * {@link Point} objects
 *
 * @author Antonio J. Nebro
 */
@SuppressWarnings("serial")
public class RArrayFront implements Front {
	protected Point[] points;
	protected int numberOfPoints;
	private int pointDimensions;

	/** Constructor */
	public RArrayFront() {
		points = null;
		numberOfPoints = 0;
		pointDimensions = 0;
	}

	/** Constructor */
	public RArrayFront(List<? extends Solution<?>> solutionList) {
		if (solutionList == null) {
			throw new JMetalException("The list of solutions is null");
		} else if (solutionList.size() == 0) {
			throw new JMetalException("The list of solutions is empty");
		}

		numberOfPoints = solutionList.size();
		pointDimensions = solutionList.get(0).getNumberOfObjectives();
		points = new Point[numberOfPoints];

		points = new Point[numberOfPoints];
		for (int i = 0; i < numberOfPoints; i++) {
			Point point = new ArrayPoint(pointDimensions);
			for (int j = 0; j < pointDimensions; j++) {
				point.setValue(j, solutionList.get(i).getObjective(j));
			}
			points[i] = point;
		}
	}

	/** Copy Constructor */
	public RArrayFront(Front front) {
		if (front == null) {
			throw new JMetalException("The front is null");
		} else if (front.getNumberOfPoints() == 0) {
			throw new JMetalException("The front is empty");
		}
		numberOfPoints = front.getNumberOfPoints();
		pointDimensions = front.getPoint(0).getDimension();
		points = new Point[numberOfPoints];

		points = new Point[numberOfPoints];
		for (int i = 0; i < numberOfPoints; i++) {
			points[i] = new ArrayPoint(front.getPoint(i));
		}
	}

	/** Constructor */
	public RArrayFront(int numberOfPoints, int dimensions) {
		this.numberOfPoints = numberOfPoints;
		pointDimensions = dimensions;
		points = new Point[this.numberOfPoints];

		for (int i = 0; i < this.numberOfPoints; i++) {
			Point point = new ArrayPoint(pointDimensions);
			for (int j = 0; j < pointDimensions; j++) {
				point.setValue(j, 0.0);
			}
			points[i] = point;
		}
	}

	/**
	 * Constructor
	 * 
	 * @param fileName File containing the data. Each line of the file is a list of
	 *                 objective values
	 * @throws FileNotFoundException
	 */
	public RArrayFront(String fileName) throws FileNotFoundException {
		this();
		// TODO: investigate why no exception is raised if fileName == ""
		InputStream inputStream = createInputStream(fileName);

		InputStreamReader isr = new InputStreamReader(inputStream);
		BufferedReader br = new BufferedReader(isr);

		List<Point> list = new ArrayList<>();
		int numberOfObjectives = 0;
		String aux;
		try {
			aux = br.readLine();

			while (aux != null) {
				StringTokenizer tokenizer = new StringTokenizer(aux);

				int i = 0;
				if (numberOfObjectives == 0) {
					numberOfObjectives = tokenizer.countTokens();
				} else if (numberOfObjectives != tokenizer.countTokens()) {
					throw new JMetalException("Invalid number of points read. " + "Expected: " + numberOfObjectives
							+ ", received: " + tokenizer.countTokens());
				}

				boolean first = true;
				Point point = new RArrayPoint(numberOfObjectives-1); // TODO must skip the first token
				while (tokenizer.hasMoreTokens()) {
					if (first) {
						first = false;
//						int ID = new Integer(tokenizer.nextToken());
						int ID = Integer.parseInt(tokenizer.nextToken());
						((RArrayPoint)point).setID(ID);
					} else {
//						double value = new Double(tokenizer.nextToken());
						double value = Double.parseDouble(tokenizer.nextToken());
						point.setValue(i, value);
						i++;
					}
				}
				list.add(point);
				aux = br.readLine();
			}
			br.close();
		} catch (IOException e) {
			throw new JMetalException("Error reading file", e);
		} catch (NumberFormatException e) {
			throw new JMetalException("Format number exception when reading file", e);
		}

		numberOfPoints = list.size();
		points = new Point[list.size()];
		points = list.toArray(points);
		if (numberOfPoints == 0) {
			pointDimensions = 0;
		} else {
			pointDimensions = points[0].getDimension();
		}
		for (int i = 0; i < numberOfPoints; i++) {
			points[i] = list.get(i);
		}
		System.out.println(points.length);
	}

	public InputStream createInputStream(String fileName) throws FileNotFoundException {
		InputStream inputStream = getClass().getResourceAsStream(fileName);
		if (inputStream == null) {
			inputStream = new FileInputStream(fileName);
		}

		return inputStream;
	}

	@Override
	public int getNumberOfPoints() {
		return numberOfPoints;
	}

	@Override
	public int getPointDimensions() {
		return pointDimensions;
	}

	@Override
	public Point getPoint(int index) {
		if (index < 0) {
			throw new JMetalException("The index value is negative");
		} else if (index >= numberOfPoints) {
			throw new JMetalException("The index value (" + index + ") is greater than the number of " + "points ("
					+ numberOfPoints + ")");
		}
		return points[index];
	}

	@Override
	public void setPoint(int index, Point point) {
		if (index < 0) {
			throw new JMetalException("The index value is negative");
		} else if (index >= numberOfPoints) {
			throw new JMetalException("The index value (" + index + ") is greater than the number of " + "points ("
					+ numberOfPoints + ")");
		} else if (point == null) {
			throw new JMetalException("The point is null");
		}
		points[index] = point;
	}

	@Override
	public void sort(Comparator<Point> comparator) {
		// Arrays.sort(points, comparator);
		Arrays.sort(points, 0, numberOfPoints, comparator);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		RArrayFront that = (RArrayFront) o;

		if (numberOfPoints != that.numberOfPoints)
			return false;
		if (pointDimensions != that.pointDimensions)
			return false;
		if (!Arrays.equals(points, that.points))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = Arrays.hashCode(points);
		result = 31 * result + numberOfPoints;
		result = 31 * result + pointDimensions;
		return result;
	}

	@Override
	public String toString() {
		return Arrays.toString(points);
	}

	@Override
	public double[][] getMatrix() {
		// TODO Auto-generated method stub
		return null;
	}
}

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import Jama.Matrix;

public class Driver {

	//this will be the table/tableaux (pg 230in book) in the simplex algorithm, and the b
	static Matrix A;
	static char[] types;
	static Matrix constraints;
	static int[] objectiveFunction;
	static Matrix temp;
	static Matrix directions;
	//these will be supplied in first two numbers of the file (will just hardcode for now)
	static int m = 0; //number of constraints
	static int n = 0; //number of variables
	static int numNonBasics;
	static int numBasics;
	static int totalVariables;
	static boolean isDone = false;
	static boolean isUnbounded = false;
	static boolean isFeasible = true;
	static double value = 0;
	
	public Driver( String filename ) throws IOException {
		//initialize all of the global variables
		int line;
		try {
			//create a filereader to read in the filename
			FileReader fr = new FileReader( filename );
			//always wrap the filereader in a buffer??? can't remember exactly
			BufferedReader br = new BufferedReader( fr );
			m = br.read();
			System.out.println( m );
		}
		catch( FileNotFoundException e ) {
			System.out.println("Error, file not found!");
			
		}
		
		
	}

	public static void main(String[] args) {
		// Eventually another method will do all of the initializing for me
		/*
		double stepSize = 0;
		objectiveFunction = new int[2];
		objectiveFunction[0] = 12;
		objectiveFunction[1] = 9;
		double[][] Atest = {{1,0,1,0,0,0},{0,1,0,1,0,0},{1,1,0,0,1,0},{4,2,0,0,0,1}};
		double[] btest = {1000,1500,1750,4800};
		A = new Matrix(Atest);
		constraints = new Matrix(btest,4);
		numNonBasics = 2;
		numBasics = 4;
		totalVariables = 6;
		types = new char[totalVariables];
		//Eventally another method will do the initializing above

		//----------------------------------------------------------------------------------
		//find our initial solution
		double[] x = initialize( A, btest );
		directions = simplexDirections();
		directions = optimalityCheck( directions );
		
		while( isDone == false ) {
			stepSize = stepSize(x, directions);
			x = updateAndAdvance( x, directions, stepSize );
			directions = simplexDirections();
			directions = optimalityCheck( directions );
		}
		
		System.out.println("Optimal Values for Objective Variables:");
		for( int i = 0; i < objectiveFunction.length; i++ ) {
			System.out.print("x" + i + ":" + x[i] + " ");
		}
		System.out.println("\n");
		System.out.println("Objective Value: ");
		System.out.println(value);
		*/
		
		System.out.println(args[0]);
	}

	/*
	 * Identify n-m non-basic variables to set to 0
	 * Identify m variables to set = b
	 */
	//Step 0: given a standardized matrix A return the initial feasible solution, and solutions b
	static double[] initialize( Matrix A, double b[] ) {
		//this will be our initial solution that we want to return
		double x[] = new double[totalVariables];
		//initialize a starting Basis matrix B, which will be all Basic variables
		double B[] = new double[totalVariables]; 
		int count = 0;
		int idx = numNonBasics;
		//we want to set our initial nonBasics to 0
		for( int i = numNonBasics; i < totalVariables /*numConstraints*/; i++ ) {
			B[count] = A.get(count,idx);
			idx++;
			count++;
		}
		count = 0;
		//set the non basic variables to be 0
		for( int i = 0; i < numNonBasics; i++) {
			x[i] = 0;
			types[i] = 'N';
		}
		//set the basic variables to what equals it to b
		for( int i = numNonBasics; i < totalVariables; i++ ) {
			x[i] = b[count]/B[count];
			types[i] = 'B';
			count++;
		}
		//construct corresponding basic solution x(0)
		return x;
	}
	//For now the above just sets every slack variable to the non basics by default, is this an ok
	// way to do this?

	//step 1, return an array of each simplex direction using our current solution and basic variables
	static Matrix simplexDirections( ) {	
		int count = 0;
		int col = 0;

		//this will be used to return a matrix of all possible simplex directions
		Matrix deltaX = new Matrix( numNonBasics, totalVariables, 0.0 );
		//this will be used to solve a system of Basics to get the direction for d
		Matrix basics = new Matrix( A.getRowDimension(), numBasics, 0.0);

		//This matrix we will negate and set the basics equal to it to get the direction values for the basics
		Matrix negateEqual;
		Matrix basicDirections;

		//go through and build a matrix of just Basics
		for( int i = 0; i < totalVariables; i++ ) {
			
			if( types[i] == 'B' ) {
				
				for( int j = 0; j < A.getRowDimension(); j++ ) {
					basics.set(j, col, A.get(j, i));
				}
				
				col++;
			}
		}
		//System.out.println("THIS IS THE MATRIX OF BASICS");
		basics.print(0, 0);
		//now that we have a matrix of just basics we exchange the nonbasics 
		for( int i = 0; i < totalVariables; i++ ) {
			if( types[i] == 'N' ) {
				col = 0;
				
				negateEqual = A.getMatrix(0, A.getRowDimension()-1, i, i);
				negateEqual = negateEqual.times(-1);
				
				basicDirections = basics.solve(negateEqual);
				basicDirections = basicDirections.transpose();
				
				//now that we have the basic directions set the nonBasic directions in our return matrix
				for( int j = 0; j < totalVariables; j++ ) {
					if( types[j] == 'N' ) {
						if( i == j ) {
							deltaX.set(count, j, 1);
						}
						else {
							deltaX.set(count, j, 0);
						}
					}
					else {//variable is a basic, set it to the basic direction
						deltaX.set(count, j, basicDirections.get(0, col));
						col++;
					}
				}
				count++;
			}
		}
		return deltaX;
	}

	//step 2
	//check our array of possible directions and find whether any are optimal, and if so, find the best
	//direction to go
	static Matrix optimalityCheck( Matrix directions ) {
		Matrix direction = new Matrix(1, totalVariables);
		double values[] = new double[directions.getRowDimension()];
		int bestIndex = -1;
		double maxVal = 0;

		//go through each direction and find the values for the objective function
		for( int i = 0; i < directions.getRowDimension(); i++ ) {
			for( int j = 0; j < objectiveFunction.length; j++ ) {
				values[i] = values[i] + (directions.get(i, j)*objectiveFunction[j]);
			}
			//System.out.println(values[i]);
		}
		//check and see if they are optimal and which one is the best (I think this only factors max for now
		for( int i = 0; i < values.length; i++ ) {
			if( values[i] > 0 && values[i] > maxVal) {
				bestIndex = i;
				maxVal = values[i];
			}
		}
		//if we found an improving optimal direction return it
		if( bestIndex >= 0 ) {
			//this is the optimal direction
			direction = directions.getMatrix(bestIndex, bestIndex, 0, totalVariables-1);
		}
		//otherwise we have already found an optimal solution
		else {
			//signal that we are currently at an optimal solution
			isDone = true;
		}
		return direction;
	}

	//step 3
	static double stepSize( double[] x, Matrix directions ) {
		int idx = 0;
		double[] stepSizes = new double[numBasics];
		//create an array of potential step sizes
		for( int i = 0; i < totalVariables; i++ ) {
			if( types[i] == 'B' && directions.get(0, i) != 0 ) {
				double temp = x[i] / (directions.get(0, i)*-1);

				if (temp > 0 ) {
					stepSizes[idx] = temp;
				}

				idx++;
			}
		}
		//find which step size is the greatest
		double min = stepSizes[0];
		for( int i = 1; i < stepSizes.length; i++ ) {
			if( stepSizes[i] < min  && stepSizes[i] != 0) {
				min = stepSizes[i];
			}
		}
		return min;
	}

	//step 4
	static double[] updateAndAdvance( double[] x, Matrix direction, double stepSize) {
		double[] newSolution = new double[totalVariables];
		double temp = 0;
		//calculate the new solution
		for( int i = 0; i < totalVariables; i++ ) {
			newSolution[i] = x[i] + (direction.get(0, i) * stepSize);
		}
		//change the type of each solution
		for( int i = 0; i < totalVariables; i++ ) {
			if( newSolution[i] != 0 ) {
				types[i] = 'B';
			}
			else {
				types[i] = 'N';
			}
		}
		for( int i = 0; i < objectiveFunction.length; i++ ) {
			temp = temp + (newSolution[i] * objectiveFunction[i]);
		}
		value = temp;
		return newSolution;
	}
}

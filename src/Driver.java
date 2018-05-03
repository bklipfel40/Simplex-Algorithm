//import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
//import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

import Jama.Matrix;

public class Driver {
	static double[] x;
	static int iteration;
	static Matrix A;
	static char[] types;
	static Matrix constraints;
	static double[] objectiveFunction;
	static Matrix directions;
	static String maxMin;
	//these will be supplied in first two numbers of the file (will just hardcode for now)
	static int m = 0; //number of constraints
	static int n = 0; //number of variables
	static int numNonBasics;
	static int numBasics;
	static int totalVariables;
	static boolean isDone = false;
	static boolean isUnbounded = false;
	static double value = 0;
	static double stepSize;

	public Driver( String filename ) throws IOException {
		//initialize all of the global variables
		int constraintCount = 0;
		String temp;
		int numSlacks = 0;
		try {
			//create a filereader to read in the filename
			File fr = new File( filename );
			//always wrap the filereader in a buffer??? can't remember exactly
			//BufferedReader br = new BufferedReader( fr );
			Scanner input = new Scanner( fr );
			stepSize = 0;
			iteration = 0;
			//The first two numbers are the number of constraints and number of variables
			m = input.nextInt();
			n = input.nextInt();
			//set the number of basics, number of non-basics, and the number of total variables
			numBasics = m;
			numNonBasics = n;
			totalVariables = n+m;
			types = new char[totalVariables];
			//create a constraints matrix that will equal the number of constraints
			constraints = new Matrix(1,m);
			//create our objective function
			objectiveFunction = new double[n];
			//read in whether it is a max or min
			maxMin = input.next();
			//create our objective function array
			for( int i = 0; i < n; i++ ) {
				objectiveFunction[i] = input.nextDouble();
			}
			if( maxMin.toUpperCase().equals("MIN") ) {
				for( int i = 0; i < n; i++ ) {
					objectiveFunction[i] = (objectiveFunction[i] * -1);
				}
			}
			//now we have to construct A
			A = new Matrix( m, totalVariables, 0);
			while( constraintCount < m ) {
				int j = 0;
				//wait until we know we have read every constraint
				while( input.hasNextDouble() ) {
					A.set(constraintCount, j, input.nextDouble());
					j++;
				}
				temp = input.next().toString();
				//System.out.println("TEMP " + temp + " equals <= is " + (temp == "<=") );
				//if we come accross a <= constraint set the slack to be positive
				//WOW I FORGOT ABOUT .equals() THIS TOOK WAY TO LONG TO FIGURE OUT
				if( temp.equals("<=") ) {
					A.set(constraintCount, n+numSlacks, 1);
					numSlacks++;
				}
				//if we come accross a >= constraint set the slack to be negative
				else if( temp.equals(">=")) {
					A.set( constraintCount, n+numSlacks, -1);
					numSlacks++;
				}
				else {
					//it is bad coding to leave this blank but i will leave this logically
					//if it is an = constraint we do not need a slack variable
				}
				constraints.set(0, constraintCount, input.nextDouble());
				constraintCount++;
			}
			//DEBUGGING STUFF
			//System.out.println("Number of constraints " + m );
			//System.out.println("Number of variables " + n );
			//System.out.print( maxMin + " ");
			//for( int i = 0; i < n; i++ ) {
			//	System.out.print(objectiveFunction[i] + " ");
			//}
			//A.print(1, 1);
			//constraints.print(1, 1);
			input.close();

		}
		catch( FileNotFoundException e ) {
			System.out.println("ERROR: file not found!");
		}


	}

	public static void main(String[] args) throws IOException {
		//read in the file and initialize 
		Driver dr = new Driver( args[0] );

		System.out.println("Beginning Phase II...\n");
		System.out.print(maxMin + " c ");
		for( int i = 0; i < n; i++ ) {
			System.out.print(objectiveFunction[i] + " ");
		}
		for( int i = n; i < totalVariables; i++ ) {
			System.out.print(" 0.00");
		}
		System.out.println("\n");
		System.out.println("=============== A ===============");
		A.print(3, 0);
		System.out.println("=================================\n");

		System.out.println("===============b^T===============");
		constraints.print(0, 0);
		System.out.println("=================================\n");

		x = initialize( );
		printIteration();
		directions = simplexDirections();
		directions = optimalityCheck( directions );

		while( isDone == false ) {
			stepSize = stepSize();
			if( stepSize == 0 ) {
				//all djk were positive
				isUnbounded = true;
				break;
			}
			x = updateAndAdvance(directions, stepSize );
			printIteration();
			directions = simplexDirections();
			directions = optimalityCheck( directions );
		}

		if( isUnbounded == false ) {
			System.out.println("===============Optimal Values for X===============\n");
			System.out.print("X : ");
			for( int i = 0; i < totalVariables; i++ ) {
				System.out.print("x["+ i + "]:" + x[i] + " ");
			}
			System.out.println("\n");
			System.out.println("===================================================\n");

			System.out.println("Finished Phase II ( " + iteration + " iteration(s) )\n");

			System.out.println("Optimal Solution : " + value + "\n");
		}
		else {
			System.out.println("Unbounded\n");
		}



	}

	static void printIteration() {
		System.out.print("Iteration " + iteration + ": ");
		for( int i = 0; i < totalVariables; i++ ) {
			System.out.print(types[i] + " ");
		}
		System.out.println(" ");
		System.out.print("Values for x : ");
		for( int i = 0; i < totalVariables; i++ ) {
			System.out.print(x[i] + " ");
		}
		System.out.println(" ");
		iteration++;

	}

	/*
	 * Identify n-m non-basic variables to set to 0
	 * Identify m variables to set = b
	 */
	//Step 0: given a standardized matrix A return the initial feasible solution, and solutions b
	static double[] initialize( ) {
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
			x[i] = constraints.get(0, count)/B[count];
			types[i] = 'B';
			count++;
		}

		//construct corresponding basic solution x(0)
		return x;
	}

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
		//basics.print(0, 0);
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
				System.out.println("d for x" + i + ":");
				deltaX.getMatrix(count, count, 0, totalVariables-1).print(0,0);
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
		double bestVal = 0;
		//go through each direction and find the values for the objective function
		for( int i = 0; i < directions.getRowDimension(); i++ ) {
			for( int j = 0; j < objectiveFunction.length; j++ ) {
				values[i] = values[i] + (directions.get(i, j)*objectiveFunction[j]);
			}
			//System.out.println(values[i]);
		}
		//check and see if they are optimal and which one is the best (I think this only factors max for now)
		if( maxMin.toUpperCase().equals("MAX") ){
			for( int i = 0; i < values.length; i++ ) {
				if( values[i] > 0 && values[i] > bestVal) {
					bestIndex = i;
					bestVal = values[i];
				}	
			}
		}
		//else if( maxMin.toUpperCase().equals("MIN") ) {
		//	for( int i = 0; i < values.length; i++ ) {
		//		if( values[i] < 0 && values[i] < bestVal) {
		//			bestIndex = i;
		//			bestVal = values[i];
		//		}
		//	}

		//}
		//Console printing=======================
		System.out.println("cbars:");
		for(int i = 0; i < values.length; i++ ) {
			System.out.print(values[i] + " ");
		}
		//========================================
		System.out.println("\n");
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
	static double stepSize() {
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
		System.out.println("Lambda: " + min + "\n");
		return min;
	}

	//step 4
	static double[] updateAndAdvance( Matrix direction, double stepSize) {
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
		System.out.println("Obj : " + value );
		System.out.println("-------------------------------------");
		return newSolution;
	}

	//static boolean isUnbounded( Matrix directions ) {

	//}
}

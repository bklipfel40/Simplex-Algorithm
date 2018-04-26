import Jama.Matrix;

public class Driver {
	
	//this will be the table/tableaux (pg 230in book) in the simplex algorithm, and the b
	static Matrix A;
	static char[] types;
	static Matrix constraints;
	static int[] objectiveFunction;
	
	//these will be supplied in first two numbers of the file (will just hardcode for now)
	static int m = 0; //number of constraints
	static int n = 0; //number of variables
	static int numNonBasics;
	static int numBasics;
	static int totalVariables;
	boolean isOptimal;
	boolean isUnbounded = false;
	boolean isFeasible = true;

	public static void main(String[] args) {
		// See if I can get git hub working
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
		
		double[] x0 = initialize( Atest, btest );
		
		for( int i = 0; i < totalVariables; i++ ) {
			System.out.print(types[i]);
		}
		System.out.print("\n");
		
		System.out.println("HARDCODED STANDARD FORM MATRIX TO TEST");
		System.out.println("========= A =========");
		System.out.print("  x1  x2   x3  x4  x5  x6");
		A.print(2, 0);
		
		System.out.println("HARDCODED b CONSTRAINTS");
		System.out.println("==== b ====");
		printSingleMatrix(btest);
		
		System.out.print("\n");
		System.out.println("INITAL FEASIBLE SOLUTION");
		System.out.println(" x1  x2   x3     x4     x5     x6");
		printSingleMatrix( x0 );
		
		Matrix test2 = simplexDirections();
		
		Matrix direction = optimalityCheck(test2);
		
		direction.print(0, 0);
	}
	
	/*
	 * Identify n-m non-basic variables to set to 0
	 * Identify m variables to set = b
	 */
	//Step 0: given a standardized matrix A return the initial feasible solution, and solutions b
	static double[] initialize( double[][] A, double b[] ) {
		//this will be our initial solution that we want to return
		double x[] = new double[totalVariables];
		//initialize a starting Basis matrix B, which will be all Basic variables
		double B[] = new double[totalVariables]; 
		int count = 0;
		int idx = numNonBasics;
		//we want to set our initial nonBasics to 0
		for( int i = numNonBasics; i < totalVariables /*numConstraints*/; i++ ) {
			B[count] = A[count][idx];
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
		int row = 0;
		int col = 0;
		
		//this will be used to return a matrix of all possible simplex directions
		Matrix deltaX = new Matrix( numNonBasics, totalVariables, 0 );
		//this will be used to solve a system of Basics to get the direction for d
		Matrix basics = new Matrix( A.getRowDimension(), numBasics, 0);
		
		//This matrix we will negate and set the basics equal to it to get the direction values for the basics
		Matrix negateEqual;
		Matrix basicDirections;
		
		//go through and build a matrix of just Basics
		for( int i = 0; i < A.getRowDimension(); i++ ) {
			for( int j = 0; j < totalVariables; j++ ) {
				if( types[j] == 'B' ) {
					basics.set(row, col, A.get(i, j));
					col = (col+1)%numBasics;
				}
			}
			row++;
		}
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
		deltaX.print(0, 0);
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
			return null;
		}
		return direction;
	}
	
	//step 3
	static void stepSize() {
		
	}
	
	//step 4
	static void updateAndAdvance() {
		
	}
	//Turns out the library covers both of these
	static void printDoubleMatrix( double[][] matrix ) {
		for( int i = 0; i < matrix.length; i++ ) {
			for( int j = 0; j < matrix[i].length; j++ ) {
				System.out.print( matrix[i][j] + " ");
			}
			System.out.println("");
		}
	}
	
	static void printSingleMatrix( double[] matrix ) {
		for( int i = 0; i < matrix.length; i++ ) {
			System.out.print( matrix[i] + " ");
		}
		System.out.println(" ");
	}
}

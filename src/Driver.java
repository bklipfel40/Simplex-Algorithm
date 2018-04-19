import Jama.Matrix;

public class Driver {
	
	//this will be the table/tableaux (pg 230in book) in the simplex algorithm, and the b
	static Matrix A;
	static double[] b;
	static char[] types;
	static Matrix constraints;
	
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
		Matrix zeroConstraints = new Matrix(constraints.getRowDimension(), 1);
		//this will be used to return a matrix of all possible simplex directions
		Matrix deltaX = new Matrix( numNonBasics, totalVariables, 0 );
		//this will be used to solve a system of equations to get the direction for d
		Matrix temp = new Matrix( A.getRowDimension(), totalVariables, 0);
		//go through and find each non-basic, set the non-basic to 1 and then 		
		for( int i = 0; i < totalVariables; i++ ) {
			//we have come across a Basic
			if( types[i] == 'N' ) {
				//solve the system of equations 
				for( int j = 0; j < A.getRowDimension(); j++ ) {
					for( int idx = 0; idx < totalVariables; idx++ ) {
						if( types[idx] == 'N' && idx == i ) {//set the current basic to 1
							temp.set(j, idx, A.get(j, idx));
						}
						else if( types[idx] == 'N' && idx != i) {//set every other basic to 0
							temp.set(j, idx, 0);
						}
						else if( types[idx] == 'B' ){
							temp.set(j, idx, A.get(j, idx));
						}
					}
				}
				temp.print(0, 0);
				zeroConstraints.print(0, 0);
				Matrix ret = temp.solve(zeroConstraints);
				ret.print(0, 0);
			}
		}
		return deltaX;
	}
	
	//step 2
	static void optimalityCheck() {
		
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

import Jama.Matrix;

public class Driver {
	
	//this will be the table/tableaux (pg 230in book) in the simplex algorithm, and the b
	static double[][] table;
	static double[] limits;
	//keep track of how many slack variables we have
	static int[] slacks;
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
		double[][] Atest = {{1,2,0,0,0},{1,1,1,0,0},{2,1,0,1,0},{0,1,0,0,1}};
		double[] btest = {2,3,1};
		Matrix A = new Matrix(Atest);
		numNonBasics = 2;
		numBasics = 3;
		totalVariables = 5;
		double [] x0 = initialize( Atest, btest );
		
		System.out.println("max  x + 2y \n"
				+ "s.t. x + y + s1 = 2\n"
				+ "    2x + y + s2 = 3\n"
				+ "         y + s3 = 1\n");
		
		System.out.println("HARDCODED STANDARD FORM MATRIX TO TEST");
		System.out.println("========= A =========");
		System.out.print("  x1  x2  x3  x4  x5");
		A.print(2, 0);
		
		System.out.println("HARDCODED b CONSTRAINTS");
		System.out.println("==== b ====");
		printSingleMatrix(btest);
		
		System.out.print("\n");
		System.out.println("INITAL FEASIBLE SOLUTION");
		System.out.println(" x1  x2  x3  x4  x5");
		printSingleMatrix( x0 );
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
		//we want to set our initial nonBasics to 0
		for( int i = numNonBasics; i < totalVariables /*numConstraints*/; i++ ) {
			B[count] = A[i-1][i];
			count++;
		}
		count = 0;
		//set the non basic variables to be 0
		for( int i = 0; i < numNonBasics; i++) {
			x[i] = 0;
		}
		//set the basic variables to what equals it to b
		for( int i = numNonBasics; i < totalVariables; i++ ) {
			x[i] = b[count]/B[count];
			count++;
		}
	//construct corresponding basic solution x(0)
		return x;
	}
	//For now the above just sets every slack variable to the non basics by default, is this an ok
	// way to do this?
	
	//step 1
	static void simplexDirections( double[][] A, double[] b, double[] c) {	
		
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

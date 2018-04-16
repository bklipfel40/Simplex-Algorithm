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
	int numNonBasics;
	int numBasics;
	static int totalVariables;
	boolean isOptimal;
	boolean isUnbounded = false;

	public static void main(String[] args) {
		// See if I can get github working
		int numNonBasics = n-m;
		int numBasics = m;
		totalVariables = n+m;
	}
	
	//Step 0: given a standardized matrix A return the initial feasible solution
	double[] initialize( double[][] A ) {
		//this will be our initial solution that we want to return
		int x[] = new int[totalVariables];
		//initialize a starting Basis matrix B, which will be all Basic variables
		double B[][] = new double[m][m]; 
		int count = 0;
		//we want to set our initial 
		for( int i = numNonBasics; i < m /*numConstraints*/; i++ ) {
			B[count][count] = A[i][i+1];
			count++;
		}
		
	//construct corresponding basic solution x(0)
		
	}
	
	//step 1
	void simplexDirections( double[][] A, double[] b, double[] c) {	
		
	}
	
	//step 2
	void optimalityCheck() {
		
	}
	
	//step 3
	void stepSize() {
		
	}
	
	//step 4
	void updateAndAdvance() {
		
	}
	
	
	
	

}

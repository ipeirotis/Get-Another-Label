package com.ipeirotis.gal;


public class Stat {

    /**
     * Computes the value of the hypergeometric distribution for the passed
     * values. To avoid overflows when computing the factorials, we use a small
     * trick by taking initially the logarithms and then returning the exponent.
     * 
     * log(N!) = log (1*2*...*N = log1 + log2 + .... + log N = Sum(log(i),
     * i=1..N);
     * 
     * @param D
     *            Size of database
     * @param gt
     *            Token degree
     * @param S
     *            Sample size
     * @return
     */
    private static double hg(long D, long gt, long S) {

        double dgt = 0;
        for (int i = 1; i <= D - gt; i++) {
            dgt += Math.log(i);
        }

        double ds = 0;
        for (int i = 1; i <= D - S; i++) {
            ds += Math.log(i);
        }

        double dgts = 0;
        for (int i = 1; i <= D - gt - S; i++) {
            dgts += Math.log(i);
        }

        double d = 0;
        for (int i = 1; i <= D; i++) {
            d += Math.log(i);
        }

        double P = Math.exp(dgt + ds - dgts - d);

        return P;
    }

    /**
     * Computes the value of the hypergeometric distribution for the passed
     * values. To avoid overflows when computing the factorials, we use a small
     * trick by taking initially the logarithms and then returning the exponent.
     * 
     * log(N!) = log (1*2*...*N = log1 + log2 + .... + log N = Sum(log(i),
     * i=1..N);
     * 
     * @param D
     *            Size of database
     * @param gt
     *            Token degree
     * @param S
     *            Sample size
     * @return
     */
    public static double hgapprox(long D, long gt, int S) {

        double dgt = logNfact(D - gt);

        double ds = logNfact(D - S);

        double dgts = logNfact(D - gt - S);

        double d = logNfact(D);

        double P = Math.exp(dgt + ds - dgts - d);

        return P;
    }

    public static double beta_distribution_CDF(double x, int a, int b) {
        return Ix(x, a, b);
    }

    public static double incompleteBeta(double x, int a, int b) {
        return beta_function(a, b) * Ix(x, a, b);
    }

    public static double beta_function(int a, int b) {
        return Math.exp(logNfactExact(a - 1) + logNfactExact(b - 1) - logNfactExact(a + b - 1));
    }

    private static double Ix(double x, int a, int b) {

        double result = 0;
        for (int j = a; j <= a + b - 1; j++) {
            double m = Math.exp(logNfact(a + b - 1) - logNfact(j) - logNfact(a + b - 1 - j));
            double n = Math.pow(x, j) * Math.pow(1 - x, a + b - 1 - j);
            result += m * n;
        }
        return result;

    }
    
    /**
     * 
     * @param p Number of positive labels
     * @param n Number of negative labels
     * @param P Prior for positive class
     * @return
     */
    public static double LU_ProbPositive(int p, int n, double P) {
        
        boolean invert = (n>p);
        
        if (invert) {
            int t=p;
            p=n;
            n=t;
            P = 1-P;
        }

        double result = 1 + ((1-P)/P)* Math.exp( 2*logNfact(n + p)-logNfact(2*n)-logNfact(2*p)   );
        
        if (invert)
            return 1-1.0/result;
        else
            return 1.0/result;

    }

    /**
     * Computing log(n!) using Gosper's approximation of n! when n is large (i.e., >100) and exact computation otherwise
     */
    public static double logNfact(long n) {

        if (n <= 0)
            return 0;

        if (n < 100)
            return logNfactExact(n);

        // Stirling's approximation:
        // double P = Math.log(2*Math.PI*n)/2 + n*Math.log(n/Math.E);

        // Gosper's approximation
        double P = Math.log((2 * n + 1.0 / 3) * Math.PI) / 2 + n * Math.log(n / Math.E);

        return P;
    }

    /**
     * Computing log(n!) using exact calculation
     */
    private static double logNfactExact(long n) {

        if (n <= 0)
            return 0;

        double s = 0;
        for (int i = 1; i <= n; i++) {
            s += Math.log(i);
        }

        return s;
    }
    
    /**
     * Computing n!
     * 
     */
    public static long NfactExact(long n) {

        if (n == 0)
            return 1;

        long s = 1;
        for (int i = 1; i <= n; i++) {
            s *= i;
        }

        return s;
    }

    /**
     * Computing the binomial coefficient
     */
    public static double binomial_coefficient(int n, int i) {

        return 1.0*NfactExact(n)/(NfactExact(i)*NfactExact(n-i));

    }
    
    
    /**
     * Computing the multinomial coefficient
     */
    public static double multinomial_coefficient_exact(int n, int[] x) {

    		double prod = 1;
    		for (int i=0; i<x.length; i++) {
    			prod *= NfactExact(x[i]);
    		}
        return 1.0*NfactExact(n)/prod;

    }
    
    /**
     * Computing the multinomial coefficient
     */
    public static double multinomial_coefficient(int n, Integer[] x) {

    		double prod = logNfactExact(n);
    		for (int i=0; i<x.length; i++) {
    			prod -= logNfactExact(x[i]);
    		}
        return Math.exp(prod);

    }
    
    /**
     * Computing the binomial coefficient
     */
    private static double binomDivBy2N(int n, int i) {

        return Math.exp( logNfactExact(n)-logNfactExact(i)-logNfactExact(n-i) - Math.log(2)*n );

    }
    
    /**
     * Returns the significance level for an one-tailed sign-test
     * @param np Number of positive occurrences
     * @param nm Number of negative occurrences
     * @return Level of statistical significance
     */
    public static double signtest(int np, int nm) {
        int n = np+nm;
        
        int k = Math.min(np, nm);
        
        
        double sum = 0;
        for (int i = 0; i<=k ; i++) {
            sum += binomDivBy2N(n,i);
        }
        
        double p = sum;
        return p;
    }
    

    public static void main(String[] args) {
        // Testing IncompleteBeta

        /*
         * for (int a =1 ; a<10; a++) for (int b =1 ; b<10; b++)
         * System.out.println("a="+a+" b="+b+" Beta(a,b)="+Beta(a,b));
         */

        double x = 0.5;
        for (int a = 0; a <= 20; a++)
            for (int b = 0; b <= 20; b++)
                System.out.println("x=" + x + " pos=" + a + " neg=" + b + " Beta_CDF(x;pos,neg)=" + beta_distribution_CDF(x, a+1, b+1));

    }

}

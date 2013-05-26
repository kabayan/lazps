package jp.adlibjapan.android.lib.matrix;

public class Matrix{
    private int i, j;
    private double[][] matrix;
    Matrix(int n, int m){
        matrix = new double[n][m];
        for(i=0;i<m;i++){
            for(j=0;j<n;j++){
                matrix[j][i]=0.0;
            }
        }
    }

    public void set(double value, int x, int y){
        matrix[x][y]=value;
    }

    public double get(int x, int y){
        return(matrix[x][y]);
    }
}
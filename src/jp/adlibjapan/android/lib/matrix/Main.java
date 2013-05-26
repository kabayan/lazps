package jp.adlibjapan.android.lib.matrix;

import java.util.ArrayList;
import java.util.Iterator;

public class Main {
	ArrayList<Double> XMatrix;
	ArrayList<Double> YMatrix;

	public void add(ArrayList<Double> time, ArrayList<Double> val) {
		XMatrix = time;
		YMatrix = val;
	}

	public double calc(double time) {
		return calc(time, 4);
		// System.out.println(AMatrix1);
	}

	private double calc(double time, int dim) {
		double ans = 0;
		if (XMatrix.size() == 0) {
			return 0;
		}

		int m =0;
		ArrayList<Double> AMatrix1 = MinR2.gettest(XMatrix, YMatrix, dim);
		for (Iterator iterator = AMatrix1.iterator(); iterator.hasNext();) {
			ans += (Double) iterator.next() * Math.pow(time, m);
			m++;
		}
		return ans;
		// System.out.println(AMatrix1);
	}
}

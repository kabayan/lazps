package jp.adlibjapan.android.lib.matrix;

import java.util.ArrayList;

public final class MinR2 {
	private static ArrayList<Double> ZMatrix;
	private static int i, j, length, rank, t;
	private static Matrix CMatrix;

	static ArrayList<Double> gettest(ArrayList<Double> x, ArrayList<Double> y,
			int number) {
		if (x.size() > y.size()) {
			length = y.size();
		} else {
			length = x.size();
		}
		CMatrix = new Matrix(number, number);
		ZMatrix = new ArrayList<Double>();
		for (i = 0; i < number; i++) {
			for (j = 0; j < number; j++) {
				CMatrix.set(sum(x, i + j), j, i);
			}
			ZMatrix.add(sum(x, y, i, 1));
		}
		// 掃き出し法
		SweepOutMethod(number);

		return (ZMatrix);
	}

	private static void SweepOutMethod(int number) {
		for (rank = 0; rank < number; rank++) {
			if (CMatrix.get(rank, rank) == 0)
				changeRow(number);
			// 対角係数を１となるようにrank行を割る。
			double dummy = CMatrix.get(rank, rank);
			for (t = 0; t < number; t++) {
				CMatrix.set(CMatrix.get(t, rank) / dummy, t, rank);
			}
			ZMatrix.set(rank, ZMatrix.get(rank) / dummy);
			// 他の行に数倍にしたもので引きrank列を０にする。
			for (i = 0; i < number; i++) {
				if (rank != i) {
					dummy = CMatrix.get(rank, i);
					for (j = rank; j < number; j++) {
						CMatrix.set(CMatrix.get(j, i) - CMatrix.get(j, rank)
								* dummy, j, i);
					}
					ZMatrix.set(i, ZMatrix.get(i) - ZMatrix.get(rank) * dummy);
				}
			}
		}
	}

	// ０とならない行と交換
	private static boolean changeRow(int number) {
		int count = rank;
		while (CMatrix.get(rank, count) == 0) {
			if (count == number)
				break;
			count++;
		}
		for (int chx = rank; chx < number; chx++) {
			double dummy = CMatrix.get(chx, rank);
			CMatrix.set(CMatrix.get(chx, count), chx, rank);
			CMatrix.set(dummy, chx, count);
		}
		double dummy = ZMatrix.get(rank);
		ZMatrix.set(rank, ZMatrix.get(count));
		ZMatrix.set(count, dummy);
		if (CMatrix.get(rank, count) == 0) {
			System.out.println("解くこと不可");
			return (false);
		} else {
			return (true);
		}
	}

	private static double sum(ArrayList<Double> element, int m) {
		double sum1 = 0;
		for (int sumi = 0; sumi < length; sumi++) {
			sum1 += Math.pow(element.get(sumi), m);
		}
		return (sum1);
	}

	private static double sum(ArrayList<Double> element1,
			ArrayList<Double> element2, int m1, int m2) {
		double sum2 = 0;
		for (int sumi = 0; sumi < length; sumi++) {
			sum2 += Math.pow(element1.get(sumi), m1)
					* Math.pow(element2.get(sumi), m2);
		}
		return (sum2);
	}
}
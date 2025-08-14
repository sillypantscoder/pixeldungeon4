package com.sillypantscoder.utils;

import java.util.ArrayList;

/**
 * Get all the points that a line passes through.
 * Used for lighting.
 */
public class LinePoints {
	public static int[][] get_line(int[] start, int[] end) {
		/*
			Bresenham's Line Algorithm
			Produces a list of coordinate points from start and end
		*/
		// Setup initial conditions
		int x1 = start[0];
		int y1 = start[1];
		int x2 = end[0];
		int y2 = end[1];
		int dx = x2 - x1;
		int dy = y2 - y1;

		// Determine how steep the line is
		boolean is_steep = Math.abs(dy) > Math.abs(dx);

		// Rotate line
		if (is_steep) {
			int _x1 = x1;
			x1 = y1;
			y1 = _x1;
			int _x2 = x2;
			x2 = y2;
			y2 = _x2;
		}

		// Swap start and end points if necessary and store swap state
		boolean swapped = false;
		if (x1 > x2) {
			int _x1 = x1;
			x1 = x2;
			x2 = _x1;
			int _y1 = y1;
			y1 = y2;
			y2 = _y1;
			swapped = true;
		}

		// Recalculate differentials
		dx = x2 - x1;
		dy = y2 - y1;

		// Calculate error
		int error = (int)(dx / 2.0);
		int ystep = -1;
		if (y1 < y2) ystep = 1;

		// Iterate over bounding box generating points between start and end
		int y = y1;
		ArrayList<int[]> points = new ArrayList<int[]>();
		for (var x = x1; x < x2 + 1; x++) {
			int[] coord = new int[] { x, y };
			if (is_steep) coord = new int[] { y, x };
			points.add(coord);
			error -= Math.abs(dy);
			if (error < 0) {
				y += ystep;
				error += dx;
			}
		}

		// Reverse the list if the coordinates were swapped
		int[][] ret = new int[points.size()][2];
		if (swapped) {
			for (int i = 0; i < points.size(); i++) {
				ret[points.size() - (i + 1)][0] = points.get(i)[0];
				ret[points.size() - (i + 1)][1] = points.get(i)[1];
			}
		} else {
			for (int i = 0; i < points.size(); i++) {
				ret[i][0] = points.get(i)[0];
				ret[i][1] = points.get(i)[1];
			}
		}
		return ret;
	}
}

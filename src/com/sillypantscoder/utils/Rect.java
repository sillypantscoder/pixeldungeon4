package com.sillypantscoder.utils;

public class Rect {
	public int x, y, w, h;
	public Rect(int x, int y, int w, int h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}
	public int left() { return this.x; }
	public int right() { return this.x + this.w; }
	public int top() { return this.y; }
	public int bottom() { return this.y + this.h; }
	public boolean collidePoint(int x, int y) {
		return  left() <= x && x <= right() &&
				top() <= y && y <= bottom();
	}
	public boolean collideRect(Rect other) {
		boolean xOverlap =
			this.intervalContains(this.left(), this.right(), other.left()) ||	// \ ____ Check whether the left or right of the other
			this.intervalContains(this.left(), this.right(), other.right()) ||	// /		rect is inside of this rect
			this.intervalContains(other.left(), other.right(), this.left()) ||	// \ ____ Check whether the left or right of this rect
			this.intervalContains(other.left(), other.right(), this.right());	// /		is inside the other rect
		boolean yOverlap =
			this.intervalContains(this.top(), this.bottom(), other.top()) ||
			this.intervalContains(this.top(), this.bottom(), other.bottom()) ||
			this.intervalContains(other.top(), other.bottom(), this.top()) ||
			this.intervalContains(other.top(), other.bottom(), this.bottom());
		return xOverlap && yOverlap;
	}
	protected boolean intervalContains(int start, int end, int point) {
		return start <= point && point <= end;
	}
	public boolean containsRect(Rect other) {
		return this.left() < other.left() && other.right() < this.right() &&
				this.top() < other.top() && other.bottom() < this.bottom();
	}
	public Rect square() {
		// Center a square inside of this rect
		int size = Math.min(this.w, this.h);
		int x = this.x + ((this.w - size) / 2);
		int y = this.y + ((this.h - size) / 2);
		return new Rect(x, y, size, size);
	}
	public Rect translate(int bx, int by, int bw, int bh) {
		return new Rect(x + bx, y + by, w + bw, h + bh);
	}
	public Rect translate(int bx, int by) {
		return translate(bx, by, 0, 0);
	}
	public Rect[] subdivide() {
		boolean doSplitVertical = false;
		if (w > h) {
			doSplitVertical = true;
		} else if (w < h) {
			doSplitVertical = false;
		} else {
			doSplitVertical = Math.random() < 0.5;
		}
		if (doSplitVertical) {
			int split = Random.randint(2, w - 2);
			return new Rect[] {
				new Rect(x, y, split, h),
				new Rect(x + split, y, w - split, h)
			};
		} else {
			int split = Random.randint(2, h - 2);
			return new Rect[] {
				new Rect(x, y, w, split),
				new Rect(x, y + split, w, h - split)
			};
		}
	}
	public String toString() {
		return "Rect [" + x + ", " + y + ", " + w + ", " + h + "]";
	}
	@Override
	public boolean equals(Object j) {
		if (j instanceof Rect) {
			Rect jr = (Rect)(j);
			return x == jr.x && y == jr.y && w == jr.w && h == jr.h;
		}
		return false;
	}
}

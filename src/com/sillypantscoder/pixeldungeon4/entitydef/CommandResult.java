package com.sillypantscoder.pixeldungeon4.entitydef;

public class CommandResult<T> {
	public T result;
	public String debugInfo;
	public CommandResult(T result, String debugInfo) {
		this.result = result;
		this.debugInfo = debugInfo;
	}
	public CommandResult<T> setResult(T result) {
		this.result = result;
		this.addSubResult("Result: " + result.toString());
		return this;
	}
	public void addSubResult(String subResult) {
		this.debugInfo += "\n\t" + subResult.replace("\n", "\n\t");
	}
	public<V> V addSubResult(String name, V subResult) {
		this.addSubResult(name + ": " + subResult.toString());
		return subResult;
	}
	public<V> V addSubResult(String name, CommandResult<V> subResult) {
		this.addSubResult(name + ": " + subResult.debugInfo);
		return subResult.result;
	}
}

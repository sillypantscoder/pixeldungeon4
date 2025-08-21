package com.sillypantscoder.pixeldungeon4.entitydef;

public class CommandResult<T> {
	public T result;
	public String debugInfo;
	public CommandResult(T result, String debugInfo) {
		this.result = result;
		this.debugInfo = debugInfo;
	}
	public void addSubResult(String subResult) {
		this.debugInfo += "\n\t" + subResult.replace("\n", "\n\t");
	}
}

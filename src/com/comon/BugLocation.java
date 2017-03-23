package com.comon;

import java.util.ArrayList;

import com.comon.Constants.BUG_LOCATION_REGION_TYPE;

public class BugLocation {
	String className;                   //ʵ����class���ڵ�fileName
	Integer startLine;
	Integer endLine;
	BUG_LOCATION_REGION_TYPE region;    //�Ǽ�¼��ʲôλ��
	ArrayList<String> codeInfoList;
	String relatedMethodName;    //����ǰ�����method�У�����Ļ�������Ϊ��
	
	public BugLocation ( String className, Integer startLine, Integer endLine, BUG_LOCATION_REGION_TYPE region, String relatedMethodName ){
		this.className = className;
		this.startLine = startLine;
		this.endLine = endLine;
		this.region = region;
		this.codeInfoList = new ArrayList<String>();
		this.relatedMethodName = relatedMethodName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public Integer getStartLine() {
		return startLine;
	}

	public void setStartLine(Integer startLine) {
		this.startLine = startLine;
	}

	public Integer getEndLine() {
		return endLine;
	}

	public void setEndLine(Integer endLine) {
		this.endLine = endLine;
	}

	public BUG_LOCATION_REGION_TYPE getRegion() {
		return region;
	}

	public void setRegion(BUG_LOCATION_REGION_TYPE region) {
		this.region = region;
	}

	public ArrayList<String> getCodeInfoList() {
		return codeInfoList;
	}

	public void setCodeInfoList(ArrayList<String> codeInfoList) {
		this.codeInfoList = codeInfoList;
	}

	public String getRelatedMethodName() {
		return relatedMethodName;
	}

	public void setRelatedMethodName(String relatedMethodName) {
		this.relatedMethodName = relatedMethodName;
	}
	
}

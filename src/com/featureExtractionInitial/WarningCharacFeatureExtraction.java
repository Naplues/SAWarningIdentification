package com.featureExtractionInitial;

import java.util.ArrayList;
import java.util.HashMap;

import com.comon.BugInfo;
import com.comon.ProjectInfo;
import com.comon.StaticWarning;

public class WarningCharacFeatureExtraction {
	
	public HashMap<String, Object> extractWarningInfo_F20_to_F23 ( StaticWarning warning ){
		HashMap<String, Object> result = new HashMap<String, Object>();
		
		result.put( "F20", warning.getBugInfo().getType() );
		result.put( "F21", warning.getBugInfo().getCategory() );
		result.put( "F22", warning.getBugInfo().getPriority() );
		result.put( "F23", warning.getBugInfo().getRank() );
		
		return result;
	}
	
	/*F82Ӧ���Ǹ�project������revision�����warning count
	public Integer extractWarningCountProject_F82 ( ProjectInfo projectInfo ){
		int count = projectInfo.getTotalWarningCount();
		
		return count;
	}
	*/
	
	/*
	 * ��Ҫ�����еõ�projectInfo
	 */
	public HashMap<String, Object> extractWarningCountPackage_F94_F111 ( String packageName, ProjectInfo projectInfo ){
		HashMap<String, Object> result = new HashMap<String, Object>();
		
		Integer warningCount = projectInfo.getWarningNumForPackage().get( packageName );
		double percent = (1.0*warningCount) / (1.0* projectInfo.getTotalWarningCount());
		
		result.put( "F94", warningCount);
		result.put("F111", percent);
		
		return result;
	}
	
	public HashMap<String, Object> extractWarningCountFile_F95_F112 ( String fileName, ProjectInfo projectInfo ){
		Integer warningCount = projectInfo.getWarningNumForFile().get( fileName );
		double percent = (1.0*warningCount) / (1.0* projectInfo.getTotalWarningCount());
		
		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put( "F95", warningCount);
		result.put("F112", percent);
		
		return result;
	}
	
	
	//��Ҫ������WarningHistoryFeatureExtraction�е�obtainWarningStatus���õ�warningStatusList
	public HashMap<String, HashMap<String, Integer>> obtainWarningAccordingType ( ArrayList<StaticWarning> warning, ArrayList<String> warningStatusList ){
		HashMap<String, Integer> warningTypeCountMap = new HashMap<String, Integer>();
		HashMap<String, Integer> closeTypeCountMap = new HashMap<String, Integer>();
		
		for ( int i = 0;  i< warning.size(); i++ ){
			BugInfo bugInfo = warning.get(i).getBugInfo();
			String type = bugInfo.getType();
			int count =1;
			if ( warningTypeCountMap.containsKey( type )){
				count += warningTypeCountMap.get( type );
			}
			warningTypeCountMap.put( type, count );
			
			String status = warningStatusList.get( i );
			if ( status.equals( "close")){
				int closeCount = 1;
				if ( closeTypeCountMap.containsKey( type )){
					closeCount += closeTypeCountMap.get( type );
				}
				closeTypeCountMap.put(type, closeCount );
			}
		}
		
		HashMap<String, HashMap<String, Integer>> result = new HashMap<String, HashMap<String, Integer>>();
		result.put( "number", warningTypeCountMap);
		result.put( "percent", closeTypeCountMap);
		
		return result;
	}
	
	public HashMap<String, HashMap<String, Double>> obtainWarningAccordingCategory ( ArrayList<StaticWarning> warning, 
			ArrayList<String> warningStatusListInBugFix, ArrayList<String> warningStatusListInNoBugFix ){
		
		HashMap<String, Integer> closeCategoryCountBugFix = new HashMap<String, Integer>();
		HashMap<String, Integer> closeCategoryCountNoBugFix = new HashMap<String, Integer>();
		for ( int i =0; i < warning.size(); i++ ){
			BugInfo bugInfo = warning.get(i).getBugInfo();
			String category = bugInfo.getCategory();
			
			String status = warningStatusListInBugFix.get( i );
			if ( status.equals( "close")){
				int closeCount = 1;
				if ( closeCategoryCountBugFix.containsKey( category )){
					closeCount += closeCategoryCountBugFix.get( category );
				}
				closeCategoryCountBugFix.put( category, closeCount );
			}
			
			status = warningStatusListInNoBugFix.get( i );
			if ( status.equals( "close")){
				int closeCount = 1;
				if ( closeCategoryCountNoBugFix.containsKey( category )){
					closeCount += closeCategoryCountNoBugFix.get( category );
				}
				closeCategoryCountNoBugFix.put( category, closeCount );
			}
		}
		
		HashMap<String, HashMap<String, Double>> result = new HashMap<String, HashMap<String, Double>>();
		int size = warning.size();
		HashMap<String, Double> closeCategoryPercentBugFix = new HashMap<String, Double>();
		for ( String category: closeCategoryCountBugFix.keySet() ){
			double value = (1.0* closeCategoryCountBugFix.get( category ) ) / (1.0*size);
			closeCategoryPercentBugFix.put( category, value);		
		}
		HashMap<String, Double> closeCategoryPercentNoBugFix = new HashMap<String, Double>();
		for ( String category: closeCategoryCountNoBugFix.keySet() ){
			double value = (1.0* closeCategoryCountNoBugFix.get( category ) ) / (1.0*size);
			closeCategoryPercentNoBugFix.put( category, value);		
		}
		
		result.put( "bugfix", closeCategoryPercentBugFix);
		result.put( "nobugfix", closeCategoryPercentNoBugFix);
		return result;
	}
	
	
	
	//������obtainWarningAccordingType��Ȼ�����и÷������õ�warningPercent���Ӷ���extractWarningPercentType_F109����
	public HashMap<String, Double> obtainWarningPercentAccordingType ( HashMap<String, Integer> warningTypeCountMap , ArrayList<StaticWarning> warning){
		int totalCount = warning.size();
		HashMap<String, Double> warningTypePercentMap = new HashMap<String, Double>();
		for ( String type: warningTypeCountMap.keySet()){
			int value = warningTypeCountMap.get( type );
			warningTypePercentMap.put( type , (1.0*value)/(1.0*totalCount));
		}
		
		return warningTypePercentMap;
	}
	
	/*
	 * ��Ҫ��������WarningParser.obtainWarningNumberForMethod(), �õ�warningNumberForMethod
	 * ������������һ�μ��ɣ���extractWarningCountMethod_F79��Ҫ���ж�λ�ȡÿ������ʵ����feature
	 */	
	public HashMap<String, Object> extractWarningPercentMethod_F79_F113 ( ProjectInfo projectInfo, String fileName, String methodName, HashMap<String, Integer> warningNumberForMethod ){
		String fullName = fileName + "-" + methodName;
		int warningCount = 0;
		if ( warningNumberForMethod.containsKey( fullName ))
			warningCount = warningNumberForMethod.get( fullName );
		
		double percent = (1.0*warningCount) / (1.0* projectInfo.getTotalWarningCount());
		
		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put( "F79", warningCount);
		result.put("F113", percent);
		
		return result;
	}

	//��Ҫ��������һ��obtainWarningAccordingType�����ܵ��øú����õ�ÿ�����͵�count
	public Double extractWarningPercentType_F109 ( String type, HashMap<String, Double> warningTypePercentMap ){
		double result = 0.0;
		if ( warningTypePercentMap.containsKey( type ))
			result = warningTypePercentMap.get( type );
	
		return result;
	}	
	
}

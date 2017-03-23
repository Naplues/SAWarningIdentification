package com.git;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.comon.Constants;
import com.database.DBOperation;


/*
 * ������LogParser�����ݱ�commit_info��commit_content�Ѿ�������
 * ���иó��򣬶����е����ݽ���������֯
 */

public class DatabaseReOrganization {
	
	private DBOperation dbOperation;
	
	public DatabaseReOrganization ( ){
		dbOperation = new DBOperation();
	}
	
	/*
	 * ����Ҫ�ˣ���logParser���Ѿ�ʵ����
	 */
	public void reOrganizationCommitTime ( ){
		String sql = "SELECT * FROM " + Constants.COMMIT_CONTENT_TABLE;
		//System.out.println( sql );
		ResultSet rs = dbOperation.DBSelect(sql);
		
		ArrayList<String> commitIdList = new ArrayList<String>();
		ArrayList<String> contentIdList = new ArrayList<String>();
		try {
			while ( rs.next() ){
				String commitId = rs.getString( "commitId");
				commitIdList.add( commitId );
				
				String contentId = rs.getString( "contentId");
				contentIdList.add( contentId );
			}
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//contentIdList ��  commitIdList ��ͬ����С��
		for ( int i = 0; i < contentIdList.size(); i++ ){
			String commitId = commitIdList.get( i );
			int commitIdInt = Integer.parseInt( commitId );
			
			String sqlCommitId = "SELECT * FROM " + Constants.COMMIT_INFO_TABLE + " where commitAutoId = " + commitIdInt;
			ResultSet rsId = dbOperation.DBSelect(sqlCommitId);
			String time = "";
			try {
				if ( rsId.next() ){
					time = rsId.getString( "commitTime");
				}
				rsId.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String contentId = contentIdList.get( i );
			int contentIdInt = Integer.parseInt( contentId );
			String sqlTime = "update " + Constants.COMMIT_CONTENT_TABLE + " set commitTime = \"" + time + 
					"\" where contentId = " + contentIdInt;
			System.out.println( sqlTime );
			dbOperation.DBUpdate(sqlTime);
		}
	}
	
	//��LogParserʱ��û�н�issueId��ȡ��������������ʽ��ȡ
	//����Cass��Ŀ 
	public void retrieveIssueIdFromCommit ( ){
		String sql = "SELECT * FROM " + Constants.COMMIT_INFO_TABLE;
		//System.out.println( sql );
		ResultSet rs = dbOperation.DBSelect(sql);
		
		ArrayList<String> commitIdList = new ArrayList<String>();
		ArrayList<String> issueIdList = new ArrayList<String>();
		
		try {
			while ( rs.next() ){
				String commitId = rs.getString( "commitAutoId");
				commitIdList.add( commitId );
				String issueName = rs.getString( "issueName");
				issueName = issueName.toLowerCase();
				
				String issueId = "0";
				Pattern pattern = Pattern.compile(  "CASSANDRA-\\d+");
				Matcher matcher = pattern.matcher( issueName );
				if ( matcher.find() ){
					issueId =  matcher.group(0);
				}	
				issueIdList.add( issueId );
			}
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for ( int i = 0; i < commitIdList.size(); i++ ){
			String commitId = commitIdList.get( i );
			String issueId = issueIdList.get( i );
			
			String sqlTime = "update " + Constants.COMMIT_INFO_TABLE + " set issueId = \"" + issueId + 
					"\" where commitAutoId = " + commitId;
			System.out.println( sqlTime );
			dbOperation.DBUpdate(sqlTime);
		}		
	}
	/*
	 * ����commit message�еĹؼ��� ȷ��issueType
	 * maven ����jira��Ҳ��Ҫ�����
	 */
	public void reOrganizationIssueTypeFromCommitTomcat ( ){
		String[] notContainTerm = { "spelling", "typo", "javadoc", "typos", "docs", "indentation", "indent"};
		
		String sql = "SELECT * FROM " + Constants.COMMIT_INFO_TABLE;
		//System.out.println( sql );
		ResultSet rs = dbOperation.DBSelect(sql);
		
		ArrayList<String> commitIdList = new ArrayList<String>();
		ArrayList<String> issueIdList = new ArrayList<String>();
		ArrayList<String> issueTypeList = new ArrayList<String>();
		ArrayList<String> commitMessageList = new ArrayList<String>();
		
		try {
			while ( rs.next() ){
				String commitId = rs.getString( "commitAutoId");
				commitIdList.add( commitId );
				
				String issueId = rs.getString( "issueId").trim();
				issueIdList.add( issueId );
				commitMessageList.add( rs.getString( "issueName"));
				
				issueTypeList.add( rs.getString( "issueType"));
			}
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//contentIdList ��  commitIdList ��ͬ����С��
		for ( int i = 0; i < commitIdList.size(); i++ ){
			String commitId = commitIdList.get( i );
			String issueId = issueIdList.get( i );
			
			String issueType = issueTypeList.get( i );
			String commitMessage = commitMessageList.get( i );
			commitMessage = commitMessage.toLowerCase();
			
			boolean isNotContanTerm = false;
			for ( int j = 0 ; j < notContainTerm.length; j++ ){
				String temp = notContainTerm[j].toLowerCase();
				if ( commitMessage.contains( temp )){
					isNotContanTerm = true;
				}
			}
			
			if ( commitMessage.contains( "fix") && !isNotContanTerm && issueId.length() <= 1 ){
				issueType = "BUG";
			}
			
			/*
			 * ����ant��Ŀ,����pattern�� bug 50217, Bug-60628, Bugzilla-60349, /bugzilla/show_bug.cgi?id=60172
			 */
			/*
			Pattern pattern = Pattern.compile(  "bug \\d+");
			Matcher matcher = pattern.matcher( commitMessage );
			if ( matcher.find() ){
				issueType = "BUG";
			}	
			pattern = Pattern.compile(  "bugzilla\\w+\\d+");
			matcher = pattern.matcher( commitMessage );
			if ( matcher.find() ){
				issueType = "BUG";
			}	
			*/
			/*
			 * ����aspectJ, bug id�ţ�����5λ���ϵ����� -------- ��������̫��,��bugzilla��Ҳ�����improvement
			 * Fix 485055, Bug 467415
			 * ����eclipse.jdt.core��Ҳ��������
			 */
			Pattern pattern = Pattern.compile(  "bug \\d+");
			Matcher matcher = pattern.matcher( commitMessage );
			if ( matcher.find() ){
				issueType = "BUG";
			}
			
			String sqlTime = "update " + Constants.COMMIT_INFO_TABLE + " set issueType = \"" + issueType + 
					"\" where commitAutoId = " + commitId;
			System.out.println( sqlTime );
			dbOperation.DBUpdate(sqlTime);
		}
	}
	
	
	public void reOrganizationIssueType ( ){
		String sql = "SELECT * FROM " + Constants.COMMIT_INFO_TABLE;
		//System.out.println( sql );
		ResultSet rs = dbOperation.DBSelect(sql);
		
		ArrayList<String> commitIdList = new ArrayList<String>();
		ArrayList<String> issueIdList = new ArrayList<String>();
		try {
			while ( rs.next() ){
				String commitId = rs.getString( "commitAutoId");
				commitIdList.add( commitId );
				
				String issueId = rs.getString( "issueId").trim();
				issueIdList.add( issueId );
			}
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//contentIdList ��  commitIdList ��ͬ����С��
		for ( int i = 0; i < commitIdList.size(); i++ ){
			String commitId = commitIdList.get( i );
			int commitIdInt = Integer.parseInt( commitId );
			String issueId = issueIdList.get( i );
			
			//���û�������ţ�ȫ������"Task"���
			String issueType = "Task";
			if ( !issueId.equals( "")){
				String sqlCommitId = "SELECT * FROM " + Constants.ISSUE_TABLE + " where issueId = '" + issueId + "'";
				ResultSet rsId = dbOperation.DBSelect(sqlCommitId);
				try {
					if ( rsId.next() ){
						issueType = rsId.getString( "issueType");
					}
					rsId.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
			String sqlTime = "update " + Constants.COMMIT_INFO_TABLE + " set issueType = \"" + issueType + 
					"\" where commitAutoId = " + commitId;
			System.out.println( sqlTime );
			dbOperation.DBUpdate(sqlTime);
		}
	}
	
	
	public void reOrganizationIssueTypeContentTable ( ){
		String sql = "SELECT * FROM " + Constants.COMMIT_INFO_TABLE;
		//System.out.println( sql );
		ResultSet rs = dbOperation.DBSelect(sql);
		
		ArrayList<String> commitIdList = new ArrayList<String>();
		ArrayList<String> issueTypeList = new ArrayList<String>();
		try {
			while ( rs.next() ){
				String commitId = rs.getString( "commitAutoId");
				commitIdList.add( commitId );
				
				String issueId = rs.getString( "issueType");
				issueTypeList.add( issueId );
			}
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//contentIdList ��  commitIdList ��ͬ����С��
		for ( int i = 0; i < commitIdList.size(); i++ ){
			String commitId = commitIdList.get( i );
			int commitIdInt = Integer.parseInt( commitId );
			String issueType = issueTypeList.get( i );
			
			//���û�������ţ�ȫ������"Task"���
			String sqlCommitId = "SELECT * FROM " + Constants.COMMIT_CONTENT_TABLE + " where commitId = " + commitIdInt;
			ResultSet rsId = dbOperation.DBSelect(sqlCommitId);
			ArrayList<Integer> contentIdList = new ArrayList<Integer>();
			try {
				while ( rsId.next() ){
					int contentId = Integer.parseInt( rsId.getString( "contentId") );
					contentIdList.add( contentId );
				}
				rsId.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			for ( int j=0; j < contentIdList.size(); j++ ){
				String sqlTime = "update " + Constants.COMMIT_CONTENT_TABLE + " set issueType = \"" + issueType + 
						"\" where contentId = " + contentIdList.get( j );
				System.out.println( sqlTime );
				dbOperation.DBUpdate(sqlTime);
			}
		}
	}
	
	//����Ҫִ��reOrganizationIssueTypeContentTable,
	//�����Ƿ����issueType������ִ��reOrganizationIssueType(����issueType��) ���� reOrganizationIssueTypeFromCommit(������issueType��)
	//��Щ��Ŀ��Ҫ����reOrganizationIssueType(����issueType��)������ reOrganizationIssueTypeFromCommit����Ϊ��Щʱ��jira�м�¼����Ϣ��ȫ
	
	//����Cass��Ŀ����ִ��retrieveIssueIdFromCommit������ĺ�������һ��
	public static void main ( String args[] ){
		DatabaseReOrganization operation = new DatabaseReOrganization();
		//����Ҫ�ˣ���logParser���Ѿ�ʵ����
		//operation.reOrganizationCommitTime();
		
		//only for Cass ��Ŀ
		//operation.retrieveIssueIdFromCommit ();
		
		//operation.reOrganizationIssueType();
		//operation.reOrganizationIssueTypeFromCommitTomcat();
		
		operation.reOrganizationIssueTypeContentTable(); 
		operation.dbOperation.DBClose();
	}
}

package com.featureExtractionInitial;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import com.comon.BugLocation;
import com.comon.CodeInfo;
import com.comon.Constants;
import com.comon.MethodBodyLocation;
import com.comon.StaticWarning;
import com.comon.StringTool;
import com.featureExtractionInitial.SourceCodeParserExample.TypeFinderVisitor;

import ca.uwaterloo.ece.qhanam.slicer.Slicer;

public class SourceCodeSlicer {
	public SourceCodeSlicer() {
		
	}
	
	public ArrayList<CodeInfo> analyzeMethod( MethodDeclaration method, String methodName, Integer seedLine ) {
		ArrayList<CodeInfo> slicesCodeList = new ArrayList<CodeInfo>();
		
		/* Check that we are analyzing the correct method. */
		//System.out.println( method.getName() );
		if ( method == null )
			return slicesCodeList;
		if( method.getName().toString().equals( methodName )){
			System.out.println("Generating intra-procedural slice...");
			System.out.flush();
			
			Slicer slicer = new Slicer( Constants.SLICER_DIRECTION, Constants.SLICER_TYPE, Constants.SLICER_OPTIONS );
			List<ASTNode> statements;
			try{
				statements = slicer.sliceMethod( method , seedLine);
			}
			catch(Exception e){
				System.out.println(e.getMessage());
				return null;
			}
			
			//���slicer��������Ļ���ֻ�õ�ǰ�Ĵ���
			if ( statements == null )
				return slicesCodeList;
			/* Print slice statements. */
			System.out.println("\nNodes in slice:" + statements.size() );
			for(ASTNode node : statements){	
				CodeInfo codeInfo = new CodeInfo ( Slicer.getLineNumber( node), node );
				slicesCodeList.add( codeInfo );
				
				//System.out.print(Slicer.getLineNumber(node) + ": " + node.toString());
			}
			
			System.out.println("Finished generating intra-procedural slice.");	
			System.out.flush();
		}
		
		return slicesCodeList;
	}
	
	public HashMap<String, MethodBodyLocation> obtainMethodInfo ( String fileName ){
		//����һ��warning�У���ǳ�����λ�ö���λ��һ��file��
		//String fileName = warning.getBugLocationList().get(0).getClassName();
		
		String content = this.readJavaFile(fileName);
		ASTParser parser = ASTParser.newParser( AST.JLS4 );
		parser.setKind( ASTParser.K_COMPILATION_UNIT);
		
		parser.setSource( content.toCharArray() );
		CompilationUnit result = (CompilationUnit) parser.createAST( null );
		
		List types = result.types();
		HashMap<String, MethodBodyLocation> methodNameMap = new HashMap<String, MethodBodyLocation>();
		//dup����洢����������ͬ���ֺ���������
		Set<String> methodNameDup = new HashSet<String>();
		
		Slicer slicer = new Slicer( Constants.SLICER_DIRECTION, Constants.SLICER_TYPE, Constants.SLICER_OPTIONS );
		int index = 1;
		// ȡ����������
		if ( types.size() > 0 ){
			TypeDeclaration typeDec = (TypeDeclaration) types.get(0);

			MethodDeclaration methodDec[] = typeDec.getMethods();
			for (MethodDeclaration method : methodDec) {
				String methodName = method.getName().toString();
				//System.out.println( "-----------------------------------------" + methodName );
				int lineNumber = slicer.getLineNumber( method );
				MethodBodyLocation methodInfo = new MethodBodyLocation ( method, lineNumber );
				
				if ( methodNameDup.contains( methodName ) ){
					if ( methodNameMap.containsKey( methodName )){
						MethodBodyLocation methodInfoTemp = new MethodBodyLocation ( methodNameMap.get( methodName ).getMethod(), methodNameMap.get( methodName ).getStartLine() );
						methodNameMap.remove( methodName );
					
						methodNameMap.put( methodName + "-" + index, methodInfoTemp );
						index++;
					}
					methodNameMap.put( methodName + "-" + index, methodInfo);
					index++;
				}else{
					methodNameMap.put( methodName, methodInfo );
				}				
				methodNameDup.add( methodName );
				//������Ĺ��캯��������methodName�õ����Ǻ��������֣�Ҳ���Ǻ�������ͬ������ByteVector��������warning�ļ��У��õ�����<init>��������Ҫ����ת����
				//���Ҷ��ڴ��ڶ�����캯���������ֱ��put����ɸ��ǣ���Ҫ����ÿ�����캯������ֹδ֪���Ա������о�ȷget
				//���˹��캯������������Ҳ������������
				//System.out.println ( "================================================" + methodName );
			}	
		}		
		
		return methodNameMap;
	}
	
	//��
	public HashMap<String, MethodBodyLocation> obtainMethodInfoSimplify ( String fileName ){
		//����һ��warning�У���ǳ�����λ�ö���λ��һ��file��
		//String fileName = warning.getBugLocationList().get(0).getClassName();
		
		String content = this.readJavaFile(fileName);
		ASTParser parser = ASTParser.newParser( AST.JLS4 );
		parser.setKind( ASTParser.K_COMPILATION_UNIT);
		
		parser.setSource( content.toCharArray() );
		CompilationUnit result = (CompilationUnit) parser.createAST( null );
		
		List types = result.types();
		HashMap<String, MethodBodyLocation> methodNameMap = new HashMap<String, MethodBodyLocation>();
		//dup����洢����������ͬ���ֺ���������
		Set<String> methodNameDup = new HashSet<String>();
		
		Slicer slicer = new Slicer( Constants.SLICER_DIRECTION, Constants.SLICER_TYPE, Constants.SLICER_OPTIONS );
		int index = 1;
		// ȡ����������
		if ( types.size() > 0 ){
			TypeDeclaration typeDec = (TypeDeclaration) types.get(0);

			MethodDeclaration methodDec[] = typeDec.getMethods();
			for (MethodDeclaration method : methodDec) {
				String methodName = method.getName().toString();
				//System.out.println( "-----------------------------------------" + methodName );
				int lineNumber = slicer.getLineNumber( method );
				MethodBodyLocation methodInfo = new MethodBodyLocation ( method, lineNumber );
				
				if ( methodNameDup.contains( methodName ) ){
					if ( methodNameMap.containsKey( methodName )){
						MethodBodyLocation methodInfoTemp = new MethodBodyLocation ( methodNameMap.get( methodName ).getMethod(), methodNameMap.get( methodName ).getStartLine() );
						methodNameMap.remove( methodName );
					
						methodNameMap.put( methodName + "-" + index, methodInfoTemp );
						index++;
					}
					methodNameMap.put( methodName + "-" + index, methodInfo);
					index++;
				}else{
					methodNameMap.put( methodName, methodInfo );
				}				
				methodNameDup.add( methodName );
				//������Ĺ��캯��������methodName�õ����Ǻ��������֣�Ҳ���Ǻ�������ͬ������ByteVector��������warning�ļ��У��õ�����<init>��������Ҫ����ת����
				//���Ҷ��ڴ��ڶ�����캯���������ֱ��put����ɸ��ǣ���Ҫ����ÿ�����캯������ֹδ֪���Ա������о�ȷget
				//���˹��캯������������Ҳ������������
				//System.out.println ( "================================================" + methodName );
			}	
		}		
		
		return methodNameMap;
	}
	
	public ArrayList<CodeInfo> obtainWarningStatementSlices ( StaticWarning warning, String fileName  ){
		HashMap<String, MethodBodyLocation> methodNameMap = this.obtainMethodInfo(fileName );
		
		ArrayList<CodeInfo> slicesCodeListMethod = new ArrayList<CodeInfo>();
		ArrayList<CodeInfo> slicesCodeListOthers = new ArrayList<CodeInfo>();
		
		for ( int i = 0; i < warning.getBugLocationList().size(); i++ ){
			BugLocation bugLoc = warning.getBugLocationList().get(i);
			
			//û�ж�λ��method��������field��type��class���֣�ֵ����seed line��������slicer
			if ( bugLoc.getRelatedMethodName().equals( "")){
				for ( int j = 0;  j < bugLoc.getCodeInfoList().size() ; j++ ){
					CodeInfo codeInfo = new CodeInfo ( bugLoc.getStartLine()+j, null );
					slicesCodeListOthers.add( codeInfo );			
				}
			}
			else{
				for ( int j = 0;  j < bugLoc.getCodeInfoList().size(); j++ ){
					Integer seedLine = bugLoc.getStartLine() + j;
					String methodName = bugLoc.getRelatedMethodName();
					
					String className = StringTool.obtainClassNameShort( fileName );
					if ( methodName.equals( "<init>"))
						methodName = className;
					MethodDeclaration method = this.obtainMethodDeclaration(methodNameMap, methodName, seedLine);
					if ( method == null )
						continue;
					
					System.out.println( "method to be sliced: " + fileName + "\n" + method + "\n" + seedLine + "\n" + bugLoc.getCodeInfoList().get( j ));
					//System.out.println ( "--------------------------------------------" + method.getName().toString() );
					//Ŀǰ���캯���е��������������
					ArrayList<CodeInfo> codeInfoList = this.analyzeMethod ( method, method.getName().toString(), seedLine );
					//����û����Ƭ�����ģ�ֻ�����������
					if ( codeInfoList == null ){
						CodeInfo codeInfo = new CodeInfo ( seedLine, null );
						codeInfoList = new ArrayList<CodeInfo>();
						codeInfoList.add( codeInfo );
					}
						
					slicesCodeListMethod.addAll( codeInfoList );
				}
			}
		}
		
		//�����ô�method�еõ���
		ArrayList<CodeInfo> refinedSlicesCodeList = new ArrayList<CodeInfo>();
		Set<Integer> codeLineSet = new HashSet<Integer>();
		for ( int i = 0; i < slicesCodeListMethod.size() && i < Constants.MAX_SLICES; i++ ){
			CodeInfo temp = slicesCodeListMethod.get( i );
			if ( codeLineSet.contains( temp.getCodeLine() ))
				continue;
			
			CodeInfo codeInfo = new CodeInfo ( temp.getCodeLine(), temp.getCodeContent() );
			refinedSlicesCodeList.add( codeInfo );
			codeLineSet.add( codeInfo.getCodeLine() );
		}
		for ( int i = 0; i < slicesCodeListOthers.size() && refinedSlicesCodeList.size() <  Constants.MAX_SLICES; i++ ){
			CodeInfo temp = slicesCodeListOthers.get( i );
			if ( codeLineSet.contains( temp.getCodeLine() ))
				continue;
			
			CodeInfo codeInfo = new CodeInfo ( temp.getCodeLine(), temp.getCodeContent() );
			refinedSlicesCodeList.add( codeInfo );
			codeLineSet.add( codeInfo.getCodeLine() );
		}
		
		/*
		ArrayList<CodeInfo> refinedSlicesCodeList = new ArrayList<CodeInfo> (new LinkedHashSet<CodeInfo>(slicesCodeList));
		if ( refinedSlicesCodeList.size() > Constants.MAX_SLICES )
			refinedSlicesCodeList.subList( Constants.MAX_SLICES , refinedSlicesCodeList.size() ).clear();
		*/
		return refinedSlicesCodeList;
	}

	//��Ժ��������ǹ��캯�������
	public MethodDeclaration obtainMethodDeclaration ( HashMap<String, MethodBodyLocation>  methodNameMap, String methodName, int seedLine ){
		MethodDeclaration method = null;
		//�ҵ���seedLineС����������method
		int selectedLine  = 0;
		String selectedMethod = "";
		for ( String methodNameTemp: methodNameMap.keySet() ){
			MethodBodyLocation methodInfo = methodNameMap.get( methodNameTemp);
			
			if ( methodNameTemp.contains( "-")){
				int index = methodNameTemp.indexOf( "-");
				String methodNameTrue = methodNameTemp.substring( 0, index );
				
				if ( methodName.equals( methodNameTrue) && methodInfo.getStartLine() <= seedLine && methodInfo.getStartLine() > selectedLine ){
					selectedLine = methodInfo.getStartLine();
					selectedMethod = methodNameTemp;
				}
			}
			else{
				if ( methodNameTemp.equals( methodName )){
					selectedMethod = methodNameTemp;
				}
			}
		}
		
		if ( !selectedMethod.equals( "")){
			method = methodNameMap.get( selectedMethod ).getMethod();
			System.out.println( "======================================" +selectedMethod );
		}
		
		/*
		if ( methodName.equals( "<init>")){
			for ( String methodNameTemp: methodNameMap.keySet() ){
				if ( methodNameTemp.contains( "-")){
					MethodBodyLocation methodInfo = methodNameMap.get( methodNameTemp);
					if ( methodInfo.getStartLine() <= seedLine && methodInfo.getStartLine() > selectedLine ){
						selectedLine =  methodInfo.getStartLine();
						selectedMethod = methodNameTemp;
					}
				}
			}
			
			if ( selectedLine != 0 )
				method = methodNameMap.get( selectedMethod ).getMethod();
		}
		else{
			//System.out.println ( methodNameMap.keySet().toString() );
			if ( methodNameMap.containsKey( methodName ))
				method = methodNameMap.get( methodName ).getMethod();
		}
		*/
		return method;
	}
	
	public String readJavaFile( String filename)  {
		FileInputStream reader;
		try {
			reader = new FileInputStream ( new File(filename) );
			byte[] b = new byte[reader.available()];
			reader.read( b, 0, reader.available());
			String javaCode = new String (b);
			
			return javaCode;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}	
	
	
	public static void main ( String args[] ){
		
		//String javaFile = "test_files/Test2.java";
		//slicer.obtainMethodInfo( javaFile );
	}
}

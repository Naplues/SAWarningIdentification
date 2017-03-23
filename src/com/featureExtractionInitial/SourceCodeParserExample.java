package com.featureExtractionInitial;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import ca.uwaterloo.ece.qhanam.slicer.Slicer;


public class SourceCodeParserExample {
	public void obtainMethodDetails() {
		String path = "D:\\java-workstation\\FeatureRequest\\src\\com\\feature\\priority\\PrepareNodeData.java";
		String str = this.readJavaFile( path );

		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setResolveBindings(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		parser.setBindingsRecovery(true);

		Map options = JavaCore.getOptions();
		parser.setCompilerOptions(options);

		String unitName = "PrepareNodeData.java";
		parser.setUnitName(unitName);

		String[] sources = { "D:\\java-workstation\\FeatureRequest\\src\\com\\feature\\priority" };
		String[] classpath = { "C:\\Program Files\\Java\\jre1.8.0_91\\lib\\\rt.jar" };

		parser.setEnvironment( classpath, sources, new String[] { "UTF-8" }, true);
		parser.setSource(str.toCharArray());

		CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		if (cu.getAST().hasBindingsRecovery()) {
			System.out.println("Binding activated.");
		}

		TypeFinderVisitor v = new TypeFinderVisitor();
		cu.accept(v);
	}
	
	class TypeFinderVisitor extends ASTVisitor{
		 
		//VariableDeclarationStatement: "ResultSet rs = dbOperation.DBSelect( sql );", ����ResultSet
		public boolean visit(VariableDeclarationStatement node){
			System.out.println (  Slicer.getLineNumber( node) );
			for (Iterator iter = node.fragments().iterator(); iter.hasNext();) {
				System.out.println("------------------");
	 
				VariableDeclarationFragment fragment = (VariableDeclarationFragment) iter.next();
				IVariableBinding binding = fragment.resolveBinding();
				
				System.out.println( node.toString() );
				System.out.println("binding variable declaration: " +binding.getVariableDeclaration());
				System.out.println("binding name: " +  binding.getName() );
				System.out.println("binding method name: " +  binding.getDeclaringMethod().getName() );
				System.out.println("binding name: " +  binding.getName() );
				
				//System.out.println("binding: " +binding);
			}
			return true;
		}
		
		/*
		methodBind[]�õ��������еķ�������������ֵ�Ͳ���������<init>()������ public void setAuthorNodeList(Map<java.lang.Integer,java.lang.Integer>) 
		fieldBind[]�õ���������������е����ԣ�����private DBOperation dbOperation;
		*/
		public boolean visit ( TypeDeclaration declaration ){
			ITypeBinding bind = declaration.resolveBinding();
			//System.out.println( "declared fields in TypeDeclaration: " +  bind.toString()  );
			IMethodBinding[] methodBind = bind.getDeclaredMethods() ;
			for ( int i = 0; i < methodBind.length; i++ ){
				System.out.println( "type declaration: " + methodBind[i].toString() );
			}
			
			IVariableBinding[] fieldBind = bind.getDeclaredFields();
			for ( int i = 0; i < fieldBind.length; i++ ){
				System.out.println ( "filed declaration: " + fieldBind[i].toString() );
			}
			return true;
		}
		
		/*bind.getQualifiedName()���õ�����new instance������, ����com.feature.priority.PrepareNodeData�� F6
		bind.getBinaryName()���õ����Ǽ򻯵����������磬java.util.HashMap��Map���������ʲôû�б�ǣ�
		��QualifiedName�õ�����java.util.HashMap<java.lang.Integer,java.lang.Integer>
		getName�Ǹ��򻯵İ汾
		.getSuperclass().getName(), �õ�AbstractMap<Integer,Node>��Object
		.getSuperclass().getBinaryName(),�õ�java.util.AbstractMap��F5
		*/
		public boolean visit ( ClassInstanceCreation instanceCreation ){
			ITypeBinding bind = instanceCreation.resolveTypeBinding();
			//ITypeBinding declare = bind.getDeclaringClass();     //��ʾΪnull
			System.out.println( "class instance creation: " + bind.getSuperclass().getBinaryName() );
			return true;
		}
		
		/*
		 .getBody �õ� ����catch��䣬����ĳһ�У���Ҫ�ٴν��н�����F10
		 catch {
		  e.printStackTrace();
		}
		*/
		public boolean visit(CatchClause node) {
			if ( node.getNodeType() == ASTNode.CATCH_CLAUSE )
				System.out.println ( "catch clause!");
            System.out.println( "catch " + node.getBody().toString() );
            return true;
        }

		
		/*ֻ�ܵõ�this.nodeList,���������fieldAccessû�л�ȡ��
		 * ����newNode.nodeId������ʽ����Щʱ������������Щʱ�������QualifiedName���
		 * ���Ƕ��ڳ�����QualifiedName����ģ�����һЩ������������ҵò���declaringClass,ֻ����ƥ��ķ�ʽ
		 * bind.getDeclaringClass().getName(), F8
		 * bind.getName(), F9
		 */
		public boolean visit ( FieldAccess node){
			System.out.println ( "content:" + node.toString() );
			IVariableBinding bind = node.resolveFieldBinding();
			System.out.println ( bind.getVariableDeclaration().toString() );
			System.out.println ( "name: " + bind.getName() );
			System.out.println ( "class:" + bind.getDeclaringClass().getName()  );
			//System.out.println( node.toString() );
			return true;
		}
		
		/*
		 * An expression like "foo.bar" can be represented either as a qualified name (QualifiedName) 
		 * or as a field access expression (FieldAccess) containing simple names. 
		 * Either is acceptable, and there is no way to choose between them without information 
		 * about what the names resolve to (ASTParser may return either).
		 * ����newNode.nodeId������������ǵò������õ�����
		 */
		
		public boolean visit ( QualifiedName node){
			/*û������
			IBinding bind = node.resolveBinding();
			if ( bind != null && bind.getJavaElement() != null)
				System.out.println ( bind.getJavaElement().getElementName() );
				*/
			System.out.println ( "qualified name: " + node.toString());
			
			/*û������
			ITypeBinding bindType = node.resolveTypeBinding();
			if ( bindType != null ){
				System.out.println ( bindType.getDeclaredFields().toString() );
			}
			*/
			return true;
		}
	
		
		/*�������filed���壬
		fragements�õ����Ǹ��������е�field
		bind.getName(), F11
		bind.getType().getQualifiedName(), F12
		bind.getVariableDeclaration(), �õ�private Map<java.lang.Integer,java.lang.Integer> commentNodeList�����Եõ��Ƿ�Ϊprivate��F13,F14ֱ������ƥ��õ�
		*/	
		public boolean visit ( FieldDeclaration node){
			List temp = node.fragments();
			for ( int i = 0; i < temp.size(); i++ ) {
				VariableDeclarationFragment frag = (VariableDeclarationFragment) temp.get(i);
				IVariableBinding bind = frag.resolveBinding();
				System.out.println( bind.getVariableDeclaration() );
			}
			//System.out.println( node.getType().toString() );
			return true;
		}
		
		/*
		 * �᷵�غܶ��expressionStatement����Ҫ����ƥ�䣬ƥ�����+,-,*,/,����ѧ���ŵģ�F7
		 */		
		public boolean visit ( ExpressionStatement node ){	
			System.out.println("expression name: " +  node.toString());
			return true;
		}
			
		public boolean visit ( Assignment node ){	
			System.out.println("======================================== " +  node.toString());
			ITypeBinding bind = node.resolveTypeBinding();
			if ( bind != null )
				System.out.println ( "=======================================" + bind.getName() );
			
			return true;
		}
		
		/*"ResultSet rs = dbOperation.DBSelect(sql);"
		 * .toString�õ�dbOperation.DBSelect(sql);�����Եõ�����ĸ������Ϣ��
		 * ǰ���ResultSet rs�Ǵ�VariableDeclarationStatement�����ȡ
		 * getName(), F1
		 * getExpression() , F2
		 * arguments(),��Ҫ�����������һ�ֿ���ֱ�ӵõ����ͣ�����int����һ��ֻ�ܵõ�newNode.getNodeId(), F3
		 * bind.getReturnType().getName()��F4
		 */
		public boolean visit ( MethodInvocation invoc ){
			//IMethodBinding bind = invoc.resolveMethodBinding();
			System.out.println( "methodInvocation : " + invoc.toString() );
			System.out.println( "methodInvocation expression: " + invoc.getExpression()  );
			
			List<Expression> arguments = invoc.arguments();
			for ( int i = 0; i < arguments.size() ; i++ ){
				ITypeBinding bind = arguments.get( i ).resolveTypeBinding();
				if ( bind != null )
					System.out.println( "methodInvocation argument: " + bind.getName() );
				else 
					System.out.println( "methodInvocation argument: " + arguments.get(i).toString() );
			}
			
			//��һЩû�з���ֵ��ȡ������newNode.getNodeId()�������Щ�ò�������ֵ
			IMethodBinding bind = invoc.resolveMethodBinding();
			if ( bind != null)	
				System.out.println (  "------------------" + bind.getReturnType().getName() );
			
			//System.out.println( "methodInvocation name: " + invoc.getName() );
			
			
			//bindingһֱ��ʾnull
			//System.out.println("method invocation name: "  +  bind.toString() );
			return true;
		}
		
		/*
		 * �� node.toString() �����ȡ��F15,F17
		 * .getReturnType().getName() ���� getQualifiedName(), F16
		 */
		public boolean visit(MethodDeclaration node){
			System.out.println ( node.toString() );
			IMethodBinding bind = node.resolveBinding();
			if ( bind != null ){
				System.out.println ( "methodDeclaration name: " + bind.getName() );
				System.out.println( "methodDeclaration return type: " + bind.getReturnType().getName() );
				ITypeBinding[] bindType = bind.getTypeArguments();
				if ( bindType.length > 0 )	{
					for ( int i = 0; i < bindType.length; i++ ){
						//System.out.println( "methodDeclaration : " + bindType[i].getQualifiedName() );
					}
				}
			}
			return true;
		}
	}

	private String readJavaFile(String filename) {
		FileInputStream reader;
		try {
			reader = new FileInputStream(new File(filename));
			byte[] b = new byte[reader.available()];
			reader.read(b, 0, reader.available());
			String javaCode = new String(b);
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
	
	public static void main ( String args[] ) throws Exception{
		SourceCodeParserExample method = new SourceCodeParserExample ();
		method.obtainMethodDetails();
	}
	
}

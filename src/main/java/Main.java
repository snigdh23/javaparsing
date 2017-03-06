/**
 * Created by snigdhc on 16/2/17.
 **/

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.*;


/**.
 * Parsing of Java Code
 **/

class Main {

    /**.
     * Declaring List and HashMap variable for storing the parameters used and storing (parametername,parameterType)
     */
    private static List parameterList;
    private static HashMap<String, String> parameterTypeMap;

    /**
     * Reading the Input File to be parsed
     * @return the file content in a character array
     * @throws IOException
     */

    public static char[] readFile() throws IOException{
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter File Path");
        File file = new File("/home/snigdhc/Projects/AST/src/main/java/Main.java");
        FileReader fileReader;
        fileReader = new FileReader(file);
        int size = (int) file.length();
        int emptyFileFlag = 1;
        char[] filecontent = new char[size];
        if (fileReader.read(filecontent) != -1) {
            emptyFileFlag = 1;
        }
        else { emptyFileFlag = 0; System.out.println("Empty File"); System.exit(0); }

        return (filecontent);
    }

    public void settingParsers(ASTParser parser){
        parser.setResolveBindings(true);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setBindingsRecovery(true);

    }


    public void mapParameterNameToType(List paramList, HashMap<String, String> paramterTypeMap, MethodDeclaration node){
        for (int i = 0; i < paramList.size(); i++) {
            String parameterName = paramList.get(i).toString();
            String parameterType = ((SingleVariableDeclaration) node.parameters().get(i)).getType().toString();
            String actualparametername = parameterName.substring(parameterType.length() + 1);
            paramterTypeMap.put(actualparametername, parameterType);

        } // End of paramterlist for loop
    }

    public void checkingMethodInvocation(MethodDeclaration node,final HashMap<String, List<String>> innerMap,final CompilationUnit compilationUnit,final HashMap<String,List<Integer>> methodUsage){
        Block block = node.getBody();
        block.accept(new ASTVisitor() {
            public boolean visit(MethodInvocation node) {

                List<String> methodList = new ArrayList<>();
                List<Integer> methodUsageList = new ArrayList<>();

                Expression parameterName = node.getExpression();


                if (parameterName != null) {

                    String parameterType = parameterTypeMap.get(parameterName.toString());
                    String methodName=(node.getName().toString());

                    if(!innerMap.containsKey(parameterType)){
                        innerMap.put(parameterType,methodList);
                    }


                    if (parameterType != null) {
                        if (innerMap.containsKey(parameterType)) {
                            if ((parameterTypeMap.containsKey(parameterName.toString())) && (Objects.equals(parameterTypeMap.get(parameterName.toString()), parameterType))) {
                                //methodList.add(node.getName().toString());
                                if(!methodUsage.containsKey(methodName)){
                                    methodUsage.put(methodName,methodUsageList);
                                }

                                int linenumber=compilationUnit.getLineNumber(node.getStartPosition());

                                if(methodUsage.containsKey(methodName)){
                                    methodUsage.get(methodName).add(linenumber);
                                }

                                if(!innerMap.get(parameterType).contains(methodName)){
                                    innerMap.get(parameterType).add(methodName);
                                }
                            }
                        }
                    }
                }
                return true;
            } // End of MethodInvocation Visit
        }); // End of Block accept
    }

    public void printMap(List paralist, HashMap<String, HashMap<String, List<String>>> outerMap, HashMap<String, List<String>> innerMap,CompilationUnit cu, MethodDeclaration node,HashMap<String,List<Integer>> mU) {
        int flag=0;
        if(paralist.size()>0){
            flag=1;
        }
        for (HashMap.Entry<String, HashMap<String, List<String>>> entry : outerMap.entrySet()) {
            String mainMethod = entry.getKey();
            System.out.println(""+mainMethod + "() Called at line " +	cu.getLineNumber(node.getStartPosition())+" :-");
            //System.out.println(");
            if(flag==1) {
                System.out.print("--- Paramters ---> ");
                for (int i = 0; i < paralist.size(); i++) {
                    if (i == (paralist.size() - 1)) {
                        System.out.print(paralist.get(i) + ".");
                    } else {
                        System.out.print(paralist.get(i) + ", ");
                    }
                }
            } else{
                System.out.println("--- No Paramters for this method.");
            }
        }
        System.out.println();
        if(flag==1){
            if (innerMap.size() == 0) {
                System.out.println("------ No methods invoked by the parameters.");
            } else {
                for (HashMap.Entry<String, List<String>> entry1 : innerMap.entrySet()) {
                    String key = entry1.getKey();
                    List<String> values = entry1.getValue();
                    System.out.println("------ "+key+" calls -> " + values);
                }
                for(HashMap.Entry<String, List<Integer>> entry2 : mU.entrySet()){
                    String key = entry2.getKey();
                    List<Integer> value = entry2.getValue();
                    System.out.println("------------- "+key+" called at line(s) -> " + value);
                }
            }
        }
    }



    public static void main(String[] args) throws IOException {

        final Main main = new Main();

        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(readFile());
        main.settingParsers(parser);

        final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

        cu.accept(new ASTVisitor() {

            Set names = new HashSet();

            public boolean visit(MethodDeclaration node) {

                System.out.println();
                SimpleName name = node.getName();
                this.names.add(name.getIdentifier());

                parameterList = node.parameters();
                parameterTypeMap = new HashMap<>();

                HashMap<String, HashMap<String, List<String>>> outerMap = new HashMap<>();
                HashMap<String, List<String>> innerMap = new HashMap<>();
                HashMap<String, List<Integer>> methodUsage = new HashMap<>();

                main.mapParameterNameToType(parameterList, parameterTypeMap, node);

                main.checkingMethodInvocation(node, innerMap, cu,methodUsage);

                outerMap.put(name.toString(), innerMap);

                if(innerMap.keySet().contains(null)){
                    innerMap.remove(null);
                }

                if(methodUsage.keySet().contains(null)){
                    methodUsage.remove(null);
                }

                main.printMap(parameterList, outerMap, innerMap,cu,node,methodUsage);

                return false;
            }// End of MethodDeclaration Visit
        });
    }
}

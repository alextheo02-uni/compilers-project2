import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import syntaxtree.*;

public class Main {
    public static void main(String[] args) throws Exception {
        if(args.length < 1){
            System.err.println("Usage: java Main [file1] [file2] ... [fileN]");
            System.exit(1);
        }

        for (int i=0; i<args.length; i++) {

            System.out.println("Performing semantic analysis for: " + args[i]);
            
            FileInputStream fis = null;
            try{
                fis = new FileInputStream(args[i]);
                MiniJavaParser parser = new MiniJavaParser(fis);
                
                Goal root = parser.Goal();
                
                System.err.println("Program parsed successfully.");
                
                // Create SymbolTable object
                SymbolTable ST = new SymbolTable();
                
                // 1st visitor pass, create the symbol table
                System.err.println("Running first visitor pass.");
                Context context = new Context(null, null);
                FirstPassVisitor fpv = new FirstPassVisitor(ST, context);
                root.accept(fpv, null);

                System.out.println("SYMBOL TABLE DISPLAY");
                ST.display();
                
                // 2nd visitor pass, type checking
                // SecondPassVisitor spv = new SecondPassVisitor(ST);
                
                // MyVisitor eval = new MyVisitor();
                // root.accept(eval, null);
            }
            catch(ParseException ex){
                System.out.println(ex.getMessage());
            }
            catch(FileNotFoundException ex){
                System.err.println(ex.getMessage());
            }
            finally{
                try{
                    if(fis != null) fis.close();
                }
                catch(IOException ex){
                    System.err.println(ex.getMessage());
                }
            }
        }
    }
}
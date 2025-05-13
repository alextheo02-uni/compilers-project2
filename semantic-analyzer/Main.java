import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import syntaxtree.*;

public class Main {
    public static void main(String[] args) {
        if(args.length < 1){
            System.err.println("Usage: java Main [file1] [file2] ... [fileN]");
            System.exit(1);
        }

        for (int i=0; i<args.length; i++) {

            // System.out.println("\nPerforming semantic analysis for: " + args[i]);
            
            FileInputStream fis = null;
            try{
                fis = new FileInputStream(args[i]);
                MiniJavaParser parser = new MiniJavaParser(fis);
                
                Goal root = parser.Goal();
                
                // System.err.println("Program parsed successfully.");
                
                // Create SymbolTable object
                SymbolTable ST = new SymbolTable();
                
                // 1st visitor pass, create the symbol table
                Context context = new Context(null, null);
                FirstPassVisitor fpv = new FirstPassVisitor(ST, context);
                try {
                    
                    // System.err.println("Running first visitor pass.");
                    // First visitor pass
                    root.accept(fpv, null);
                    
                    // System.out.println("SYMBOL TABLE DISPLAY");
                    // ST.display();
                    
                    // After gathering all the info about classes,
                    // Parse the Symbol Table once for name checking
                    
                    ST.declarationCheck();
                    
                    // 2nd visitor pass, type checking
                    SecondPassVisitor spv = new SecondPassVisitor(ST, context);
                    // System.err.println("Running second visitor pass.");
                    root.accept(spv, null);

                } catch(Exception ex){
                    System.err.println("ERROR: " + ex.getMessage());
                    continue;
                }

                
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
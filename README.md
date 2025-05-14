# Compilers Project 2 - 2025
## **Alexandros Theofylaktou** - 1115202100220

### Execution instructions

There are 3 bash scripts responsible for running the program for all the files in each given test directory.
* minijava-extra-script.sh
* minijava-examples-new-script.sh
* minijava-error-extra-script.sh

To run any of the makefile commands, you must first enter the code directory by running the following command:
```cd semantic-analyzer```

Compile the program:
```make```

Execute the program for specific files:
```java Main [file1] [file2] [file3]```

Run all three scripts:
```make tests```

Run individual script:
```bash [script_name.sh]```

Cleanup:
```make clean```

### General
For both passes, a custom Context class is used to keep track of where the program is (kind of like the scope of the program).

The symbol table is a custom class that consists of a LinkedHashMap containing another custom class named ClassSymbol, which in turn contains two LinkedHashMaps, for FieldSymbol and MethodSymbol objects. MethodSymbol objects in turn contain similar structures for their internal handling of local variables, parameters, etc.

### First visitor pass
First visitor pass is responsible for creating the Symbol Table structure. In between of first and second visitor pass, a checking is performed on the Symbol Table to ensure that no declarations conflict with each other.

### Second visitor pass
Second visitor pass is responsible for type checking variables, assignments, method calls and method return types.

### Offsets
Offsets are only printed when the program passes the semantic analysis check.

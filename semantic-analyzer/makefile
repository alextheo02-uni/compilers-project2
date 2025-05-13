all: compile

compile:
	java -jar ../jtb132di.jar -te minijava.jj
	java -jar ../javacc5.jar minijava-jtb.jj
	javac Main.java

clean:
	rm -rf *.class syntaxtree visitor minijava-jtb.jj *~

test:
	java Main ../minijava-examples-new/minijava-extra/Classes.java

# ./minijava-examples-new/minijava-extra/ArrayTest.java
NAME=CondorAPI-for6

javadoc:
	LANG=C javadoc -public -classpath src:lib/classad.jar -sourcepath src -d doc `find src -name *.java`

compile:
	javac -cp lib/classad-2.4.jar -d bin `find src -name *.java` 

jar: compile
	cd bin; jar cvf ../lib/condorAPI.jar *

tar.gz: jar javadoc
	cd ..; tar zcvf $(NAME)/$(NAME).tar.gz $(NAME)/src $(NAME)/lib $(NAME)/doc $(NAME)/README $(NAME)/INSTALL

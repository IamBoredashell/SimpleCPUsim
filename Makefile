
#variables
SRC := $(shell find src -name "*.java")
OUT := target
#classpath (Java)
CP := $(OUT)
#Make sure that make does not this these are files and are commands instead
.PHONY: all run clean
# Only writing make runs the first one
all:
	mkdir -p $(OUT)
	javac -d $(OUT) $(SRC)

run:
	java -cp $(CP) Main

clean:
	rm -rf $(OUT)



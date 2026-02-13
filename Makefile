
#variables
SRC := $(shell find src -name "*.java")
OUT := target
TEST := $(shell find test -name "*.java")
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
# Run all test classes automatically
# Echo means print basically
# first run all
# then compile test classes 
# then for loop to run 
# Loop is hardcoded so add class if need to run that test
test: all
	@echo "Compiling test classes..."
	javac -cp $(OUT) -d $(OUT) $(TEST)
	@echo "Running all test classes..."
	@for cls in BufferTest MemoryTest; do \
		echo "Running $$cls..."; \
		java -cp $(CP) $$cls; \
	done


# Run a single test class (user specifies CLASS)
# testi: all
# 	@echo "Compiling test classes..."
# 	javac -cp $(OUT) -d $(OUT) $(TEST)
# 	@if [ -z "$(CLASS)" ]; then \
# 		echo "Error: specify CLASS variable, e.g. make testi CLASS=BufferTest"; \
# 		exit 1; \
# 	fi
# 	java -cp $(CP) $(CLASS)

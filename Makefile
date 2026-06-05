
#variables
SRC := $(shell find src -name "*.java")
OUT := target
TEST := $(shell find test -name "*.java")
#classpath (Java)
CP := $(OUT):src/libs/*
JFX_PATH := /usr/share/openjfx/lib
JFX_MODULES := javafx.controls,javafx.fxml,javafx.graphics

#Make sure that make does not this these are files and are commands instead
.PHONY: all run run-ui test clean
# Only writing make runs the first one
all:
	mkdir -p $(OUT)
	javac --module-path $(JFX_PATH) --add-modules $(JFX_MODULES) -cp $(CP) -d $(OUT) $(SRC) -Xlint:unchecked

run: all
	java --module-path $(JFX_PATH) --add-modules $(JFX_MODULES) -cp $(CP) Main
run-ui: all
	java --module-path $(JFX_PATH) --add-modules $(JFX_MODULES) -cp $(CP) CpuSimulatorUI

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
	# javac -cp $(OUT) -d $(OUT) $(TEST)
	javac --module-path $(JFX_PATH) --add-modules $(JFX_MODULES) -cp $(CP) -d $(OUT) $(TEST)	
	@echo "Running all test classes..."
	@for cls in BufferTest MemoryTest YamlTest RegistersTest MicroOpTest ControlUnitTest CPUTest CPUTestGUI; do \
		echo "Running $$cls..."; \
		java --module-path $(JFX_PATH) --add-modules $(JFX_MODULES) -cp $(CP) $$cls || exit 1; \
	done




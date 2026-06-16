.PHONY: all run run-ui test clean test-all \
        runBufferTest runMemoryTest runRegistersTest runYamlTest \
        runMicroOpTest runControlUnitTest runCPUTest runCPUGUITest

GRADLE := ./gradlew

all:
	$(GRADLE) build

run:
	$(GRADLE) run

run-ui:
	$(GRADLE) run

test: test-all

test-all:
	$(GRADLE) runTest

runBufferTest:
	$(GRADLE) runBufferTest

runMemoryTest:
	$(GRADLE) runMemoryTest

runRegistersTest:
	$(GRADLE) runRegistersTest

runYamlTest:
	$(GRADLE) runYamlTest

runMicroOpTest:
	$(GRADLE) runMicroOpTest

runControlUnitTest:
	$(GRADLE) runControlUnitTest

runCPUTest:
	$(GRADLE) runCPUTest

runCPUGUITest:
	$(GRADLE) runCPUGUITest

clean:
	$(GRADLE) clean
	rm -rf target/

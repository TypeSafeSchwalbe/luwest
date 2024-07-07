
OUT = luwest.jar
JAVAC = javac
JAR = jar
RES_DIR = res
SRC_DIR = src
CLASSES_DIR = out
MAIN = typesafeschwalbe.luwest.Main

rwildcard = $(foreach d,$(wildcard $(1:=/*)),$(call rwildcard,$d,$2) $(filter $(subst *,%,$2),$d))

SOURCES = $(call rwildcard, $(SRC_DIR), *.java)

$(OUT): $(SOURCES)
	$(JAVAC) $(SOURCES) -d $(CLASSES_DIR)
	rsync -a $(RES_DIR) $(CLASSES_DIR)
	cd $(CLASSES_DIR); $(JAR) -cvef $(MAIN) ../$(OUT) *

clean:
	rm -rf $(CLASSES_DIR)
	rm -f $(OUT)
#!/bin/sh

# requires java and grephviz to be installed and set in the systems PATH

java -cp bin Generator

dot -Tpng -oqcadoo-plugin.png qcadoo-plugin.gv

exit 0

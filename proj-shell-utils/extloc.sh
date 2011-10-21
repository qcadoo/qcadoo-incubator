#!/bin/sh

if [ -n "$1" ]
then
  EXTIN=$1
else
  EXTIN=en
fi

mkdir -p ./ext_locales_$EXTIN
mkdir -p ./ext_locales_$EXTIN/qcadoo
mkdir -p ./ext_locales_$EXTIN/mes

find ./qcadoo -name "*_$EXTIN.properties" -print0 | xargs -I{} -0 -x cp -u {} ./ext_locales_$EXTIN/qcadoo
find ./mes -name "*_$EXTIN.properties" -print0 | xargs -I{} -0 cp -u {} ./ext_locales_$EXTIN/mes

exit 0


#!/usr/bin/env bash
set -e
VERSION=`git describe`
cat pom.xml | sed -e "s/1-NO-VERSION-SNAPSHOT/$VERSION/g"  > tmp-release.pom
mvn -f tmp-release.pom clean package
cp tmp-release.pom target/summer-migration-$VERSION.pom
ARTIFACTS=("summer-migration-$VERSION.pom" "summer-migration-$VERSION.jar" "summer-migration-$VERSION-sources.jar" "summer-migration-$VERSION-javadoc.jar")

pushd target/ > /dev/null

GENERATED=()
for i in "${ARTIFACTS[@]}"
do
   :
   gpg --use-agent --armor --detach-sign $i
   GENERATED+=("$i.asc")
done
jar -cf summer-migration-$VERSION-bundle.jar ${ARTIFACTS[*]} ${GENERATED[*]}
popd > /dev/null

rm tmp-release.pom
echo "Built target/summer-migration-$VERSION-bundle.jar"

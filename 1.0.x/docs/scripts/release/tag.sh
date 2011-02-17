#!/bin/bash

#
# Branch a Module
#
BRANCH_VER=1.0-SNAPSHOT
TAG_VER=1.0
BRANCH_NAME=1.0.x
TAG_NAME=1.0.8244
JIRA_TKT=MH-4872

WORK_DIR=/Users/mtrehan/Matterhorn/svn

SVN_DIR=$WORK_DIR/$JIRA_TKT
SVN_URL=https://opencast.jira.com/svn/MH
BRANCH_URL=$SVN_URL/branches/$BRANCH_NAME
TAG_URL=$SVN_URL/tags/$TAG_NAME

svn copy $BRANCH_URL $TAG_URL -m "$JIRA_TKT Creating $TAG_NAME Tag"

cd $WORK_DIR

svn co $TAG_URL $JIRA_TKT

cd $SVN_DIR

echo "Main:"

sed "1,\$s/\>$BRANCH_VER\</\>$TAG_VER\</" $SVN_DIR/pom.xml >/tmp/mh-branch-pom.xml
cp /tmp/mh-branch-pom.xml $SVN_DIR/pom.xml

for i in modules/matterhorn-*
do
    echo " Module: $i"

    if [ -f $SVN_DIR/$i/pom.xml ]; then
        echo " $TAG_VER: $i"
        sed "1,\$s/\>$BRANCH_VER\</\>$TAG_VER\</" $SVN_DIR/$i/pom.xml >/tmp/mh-branch-pom.xml
        cp /tmp/mh-branch-pom.xml $SVN_DIR/$i/pom.xml
        sleep 1
    fi
done

svn commit -m "$JIRA_TKT Updated pom.xml files to reflect correct version"

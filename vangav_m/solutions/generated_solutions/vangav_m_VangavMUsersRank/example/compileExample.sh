#!/bin/sh

javac -cp ../vangav_m_VangavMUsersRank.jar ./VangavMUsersRankExample.java

jar -cvfm ./VangavMUsersRankExample.jar ./ExampleManifest.txt ./VangavMUsersRankExample.class

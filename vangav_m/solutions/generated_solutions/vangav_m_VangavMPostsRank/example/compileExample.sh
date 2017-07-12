#!/bin/sh

javac -cp ../vangav_m_VangavMPostsRank.jar ./VangavMPostsRankExample.java

jar -cvfm ./VangavMPostsRankExample.jar ./ExampleManifest.txt ./VangavMPostsRankExample.class

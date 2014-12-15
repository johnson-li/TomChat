#!/bin/sh

for i in $(seq 100)
do
	url="http://pub.idqqimg.com/lib/qqface/$i.gif"
	wget $url
done


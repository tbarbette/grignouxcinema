#!/bin/bash
cd /home/tom/public_html/grignoux/

if [ "$1" = "-q" ] ; then
	verbose=false
else
	verbose=true
fi
last=`cat last.html`
url=`wget -q -S -O - grignoux.be 2>/dev/null | egrep -o 'href="(/system/papers/pdfs/000/000/[0-9]{3}/original/[a-zA-Z0-9_?.]+)"' | sed -r 's/^.{6}//' | sed -r 's/.{1}$//'`
if [ "$verbose" = true ] ; then
	echo wget grignoux.be/${url} -O journalnew.pdf
	wget grignoux.be/${url} -O journalnew.pdf
else
	wget grignoux.be/${url} -O journalnew.pdf &> /dev/null
fi
new=`md5sum journalnew.pdf`
last=${last:0:32}
new=${new:0:32}
if [ "$verbose" = true ] ; then
	echo $last
	echo $new
fi
if [ $last == $new ]
then
  if [ "$verbose" = true ] ; then
    echo "Same... Do nothing!"
  fi
else
  echo "Different ! Extracting..."
  rm -rf $new-*.jpg
  echo  convert -density 200x200 -quality 100 journalnew.pdf $new.jpg
  convert -density 200x200 -quality 100 journalnew.pdf $new.jpg

  nimages=`ls $new-*.jpg | wc -l`
  if [ $nimages -gt 1 ]
  then
    ./grignoux-pages.sh $new
  fi
fi
chmod -R 775 *

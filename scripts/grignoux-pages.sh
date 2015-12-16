#!/bin/bash
cd /home/tom/public_html/grignoux/
last=`cat last.html`
new=$1
mkdir -p $new
  nimages=`ls $new-*.jpg | wc -l`
  for ((i = 0; i < $nimages; i += 1))
  do
    src="${new}-${i}.jpg"

    cwidth=`identify -format "%w" ${src}`
    cheight=`identify -format "%h" ${src}`

    let dw=${cwidth}/16
    let dh=${cheight}/16
    let width=$((${dw}*16))
    let height=$((${dh}*16))
    
    totlevel=16
    
    for ((level = $totlevel; level >0; level /= 2))
    do
      let n=$totlevel/$level
      let tw=${width}/${n}
      let th=${height}/${n}
      let bw=${tw}/${level}
      let bh=${th}/${level}
      
      zoomsrc=$new-${i}-${level}.bmp
      
      echo "Converting zoom version to ${tw}x${th}"
      convert $src -resize ${tw}x${th} -quality 100 $zoomsrc
      for ((bi = 0; bi < level; bi += 1))
      do 
        for ((bj = 0; bj < level; bj += 1))
        do
          let left=${bi}*${bw}
          let top=${bj}*${bh}
          let quality=$((50+$((${level}))))
	
          echo " convert $zoomsrc -quality ${quality} -crop ${bw}x${bh}+${left}+${top} $new/${i}-${level}-${bj}-${bi}"
      	convert $zoomsrc -quality ${quality} -crop ${bw}x${bh}+${left}+${top} $new/${i}-${level}-${bj}-${bi}.jpg
	convert $zoomsrc -quality ${quality} -crop ${bw}x${bh}+${left}+${top} $new/${i}-${level}-${bj}-${bi}.png
#	convert $zoomsrc -quality ${quality} -crop ${bw}x${bh}+${left}+${top} +repage $new-${i}-${level}-${bj}-${bi}.gif
	done
    done
  done 
done
  echo "$new $nimages " > last.html 
  echo $last >> olds.html
  mv -f journalnew.pdf $new.pdf
  echo "Done !"


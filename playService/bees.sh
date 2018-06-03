#!/usr/bin/env bash
wget https://www.freeiconspng.com/download/29439 -O bee.png

convert bee.png -fuzz 10% -fill blue    +opaque none -resize 50x50 public/player1.png
convert bee.png -fuzz 10% -fill green   +opaque none -resize 50x50 public/player2.png
convert bee.png -fuzz 10% -fill red     +opaque none -resize 50x50 public/player3.png
convert bee.png -fuzz 10% -fill orange  +opaque none -resize 50x50 public/player4.png
convert bee.png -fuzz 10% -fill cyan    +opaque none -resize 50x50 public/player5.png
convert bee.png -fuzz 10% -fill magenta +opaque none -resize 50x50 public/player6.png

convert bee.png -resize 48x48 public/favicon.png

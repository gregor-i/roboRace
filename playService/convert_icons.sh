#!/usr/bin/env bash
convert raw_icons/bee.png -fuzz 10% -fill blue    +opaque none -resize 50x50 public/player1.png
convert raw_icons/bee.png -fuzz 10% -fill green   +opaque none -resize 50x50 public/player2.png
convert raw_icons/bee.png -fuzz 10% -fill red     +opaque none -resize 50x50 public/player3.png
convert raw_icons/bee.png -fuzz 10% -fill orange  +opaque none -resize 50x50 public/player4.png
convert raw_icons/bee.png -fuzz 10% -fill cyan    +opaque none -resize 50x50 public/player5.png
convert raw_icons/bee.png -fuzz 10% -fill magenta +opaque none -resize 50x50 public/player6.png

convert raw_icons/bee.png -resize 48x48 public/favicon.png

convert raw_icons/sleep.png -resize 50x50 public/action_Sleep.png

convert raw_icons/uturn.png -distort SRT 90 -flip -resize 50x50  public/action_UTurn.png

convert raw_icons/double_arrow.png -resize 50x50  public/action_MoveTwiceForward.png

convert raw_icons/arrow.png -resize 50x50  public/action_MoveForward.png
convert raw_icons/arrow.png -flip -resize 50x50  public/action_MoveBackward.png

convert raw_icons/arrow_left.png -resize 50x50  public/action_TurnLeft.png
convert raw_icons/arrow_left.png -flop -resize 50x50  public/action_TurnRight.png

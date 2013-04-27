#!/usr/bin/python
# -*- coding: utf-8 -*-

import os
import shutil

size_suffix = ("-small", "", "-large", "-xlarge")
dpi_suffix = ("-ldpi", "-mdpi", "-hdpi", "-xhdpi")
dpi = (3, 4, 6, 8)

for ss in size_suffix:
  for ds in dpi_suffix:
    dir_name = "drawable" + ss + ds
    if os.path.exists(dir_name):
      shutil.rmtree(dir_name)
    os.mkdir(dir_name)

data = (
  ("ic_launcher", (None, 48, None, None)),
  ("board_first_n", (None, 60, 100, None)),
  ("board_first_p", (None, 60, 100, None)),
  ("board_last_n", (None, 60, 100, None)),
  ("board_last_p", (None, 60, 100, None)),
  ("board_next_n", (None, 60, 100, None)),
  ("board_next_p", (None, 60, 100, None)),
  ("board_previous_n", (None, 60, 100, None)),
  ("board_previous_p", (None, 60, 100, None)),
  ("board_colors_default", (None, 64, None, None)),
  ("board_colors_red", (None, 64, None, None)),
  ("board_colors_green", (None, 64, None, None)),
  ("board_colors_blue", (None, 64, None, None)),
  ("board_colors_butter_chameleon", (None, 64, None, None)),
  ("board_colors_sky_plum", (None, 64, None, None)),
  ("board_colors_scarlet_aluminium", (None, 64, None, None)),
)
for d in data:
  name = d[0]
  sizes = d[1]
  for s, suffix in zip(sizes, size_suffix):
    if s:
      for scale, suffix_2 in zip(dpi, dpi_suffix):
        w = s * scale / 4
        h = s * scale / 4
        cmd = "inkscape --export-png=drawable" + suffix + suffix_2 + "\\" + name + ".png --export-width=" + str(w) + " --export-height=" + str(h) + " " + name + ".svg"
        print cmd
        os.system(cmd)
        cmd = "optipng -o7 drawable" + suffix + suffix_2 + "\\" + name + ".png"
        print cmd
        os.system(cmd)
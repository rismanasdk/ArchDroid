cmake_minimum_required(VERSION 3.22.1)
project(archdroidpty)

add_library(archdroidpty SHARED pty_bridge.c)
target_link_libraries(archdroidpty android log)

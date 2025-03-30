cmake_minimum_required(VERSION 3.10.2)
project("sitdoctors")

# ✅ Fix for page alignment issue
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,-z,max-page-size=16384")

add_library(
        native-lib
        SHARED
        src/main/cpp/native-lib.c)

find_library(
        log-lib
        log)

target_link_libraries(
        native-lib
        ${log-lib})

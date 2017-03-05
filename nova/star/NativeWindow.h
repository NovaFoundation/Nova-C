#ifndef NOVA_NATIVE_WINDOW
#define NOVA_NATIVE_WINDOW

#include <Nova.h>

#include <string.h>
#include <stdlib.h>

#ifdef _WIN32
#	include <windows.h>

#	define WINDOW_ID_TYPE HWND
#else
#   define WINDOW_ID_TYPE void*
#endif

#ifdef _WIN32
typedef void (*nova_star_window_paint_function)(nova_star_Nova_Window* window);
#endif

WINDOW_ID_TYPE nova_createWindow(nova_star_Nova_Window* window, nova_star_window_paint_function paintFunc);

void GetDesktopResolution(int* horizontal, int* vertical);

#endif
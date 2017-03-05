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
typedef void (*nova_star_window_draw_function)(HWND hwnd, HDC hdc, PAINTSTRUCT ps);
#endif

WINDOW_ID_TYPE nova_createWindow(int x, int y, int width, int height, char* title, nova_star_window_draw_function drawHandle);

void GetDesktopResolution(int* horizontal, int* vertical);

#endif
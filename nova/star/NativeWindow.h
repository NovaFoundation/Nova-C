#ifndef NOVA_NATIVE_WINDOW
#define NOVA_NATIVE_WINDOW

#include <Nova.h>

#include <string.h>
#include <stdlib.h>

#define WM_USER_INVALRECT (WM_USER + 100)

#ifdef _WIN32
#	define WINDOW_ID_TYPE HWND
#else
#   define WINDOW_ID_TYPE void*
#endif

#ifdef _WIN32
typedef void (*nova_star_window_function)(void*, void*);
#endif

WINDOW_ID_TYPE nova_createWindow(nova_star_Nova_Window* window, nova_funcStruct* paintFunc, nova_funcStruct* addedFunc);

void GetDesktopResolution(int* horizontal, int* vertical);

#endif
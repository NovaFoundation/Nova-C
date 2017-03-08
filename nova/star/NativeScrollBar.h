#ifndef NOVA_NATIVE_SCROLLBAR
#define NOVA_NATIVE_SCROLLBAR

#define SPI_GETWHEELSCROLLCHARS 0x006C
#define WM_MOUSEHWHEEL          0x020E

#include <Nova.h>

#include <string.h>
#include <stdlib.h>

void nova_init_scrollbar();
int WheelScrollLines(HWND hwnd, int iDelta, UINT nPage, BOOL isVertical);
void CustomHandleMouseWheel(HWND hwnd, int iDelta, BOOL isVertical);

#endif
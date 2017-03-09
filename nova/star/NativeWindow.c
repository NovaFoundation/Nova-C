#include "NativeWindow.h"

#ifdef _WIN32
void DrawPixels(HWND hwnd, HDC hdc, PAINTSTRUCT ps)
{
	RECT r;
	int i = 0;

	GetClientRect(hwnd, &r);

	for (i = 0; i < 1000; i++)
	{
		int x = (rand() % r.right - r.left);
		int y = (rand() % r.bottom - r.top);

		SetPixel(hdc, x, y, RGB(255, 0, 0));
	}

}

int nova_uiwindow_closing(uiWindow *w, void *data) {
	uiQuit();
	return 1;
}

int nova_uiwindow_quit(void *data) {
	uiWindow *mainwin = uiWindow(data);

	uiControlDestroy(uiControl(mainwin));
	return 1;
}

int nova_init_ui() {
	uiInitOptions o;
	const char* err;
	memset(&o, 0, sizeof (uiInitOptions));
	err = uiInit(&o);
	if (err != NULL) {
		fprintf(stderr, "error initializing ui: %s\n", err);
		uiFreeInitError(err);
		return 1;
	}
	
	return 0;
}

__thread nova_star_Nova_Window* threadWindow;
__thread nova_funcStruct* threadPaintFunc;
__thread nova_funcStruct* threadAddedFunc;

LRESULT CALLBACK WndProc(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam)
{
	PAINTSTRUCT ps;  
	HDC hdc;
	INITCOMMONCONTROLSEX icex;
	LRESULT result;
	
	switch (msg)
	{
		case WM_CREATE:
		    icex.dwSize = sizeof(INITCOMMONCONTROLSEX);
		    icex.dwICC = ICC_STANDARD_CLASSES;
		    InitCommonControlsEx(&icex);
		    nova_init_scrollbar();
		    
		    break;
		case WM_USER_INVALRECT:
			InvalidateRect(hwnd, NULL, FALSE);
        	UpdateWindow(hwnd);
        	break;
		case WM_ADD_COMPONENT:
			((nova_star_window_function)threadAddedFunc->func)(threadAddedFunc->ref, threadAddedFunc->context);
        	break;
		case WM_ERASEBKGND:
			{
				RECT r;
				GetClientRect(hwnd, &r);
				HBRUSH brush = CreateSolidBrush(RGB(255, 255, 255));
				FillRect(threadWindow->hdc, &r, brush);
				DeleteObject(brush);
			}
			break;
		case WM_COMMAND:
			nova_star_Nova_UIComponent_virtual_Nova_searchActionTarget((nova_star_Nova_UIComponent*)threadWindow->frame, (int)LOWORD(wParam));
			
            break;
		case WM_PAINT:
			hdc = BeginPaint(hwnd, &ps);
			
			threadWindow->ps = ps;
			threadWindow->hdc = hdc;
			
			SetBkMode(hdc, TRANSPARENT);
			
			((nova_star_window_function)threadPaintFunc->func)(threadPaintFunc->ref, threadPaintFunc->context);
			
			EndPaint(hwnd, &ps);
			break;
		case WM_DESTROY:
			PostQuitMessage(0);
			return 0;
		default: 
			if (result = nova_scroll_proc(hwnd, msg, wParam, lParam)) {
				return result;
			}
	}

	return DefWindowProcW(hwnd, msg, wParam, lParam);
}

#endif

WINDOW_ID_TYPE nova_createWindow(nova_star_Nova_Window* window, nova_funcStruct* paintFunc, nova_funcStruct* addedFunc)
{
#ifdef _WIN32
	MSG msg;
	HWND hwnd;
	WNDCLASSW wc;

	HINSTANCE hInstance = GetModuleHandle(NULL);

	size_t size = window->title->nova_Nova_String_Nova_count + 1;
	wchar_t* wa = (wchar_t*)NOVA_MALLOC(sizeof(wchar_t) * size);

	wc.style         = CS_HREDRAW | CS_VREDRAW;
	wc.cbClsExtra    = 0;
	wc.cbWndExtra    = 0;
	wc.lpszClassName = L"Window";
	wc.hInstance     = hInstance;
	wc.hbrBackground = GetSysColorBrush(COLOR_3DFACE);
	wc.lpszMenuName  = NULL;
	wc.lpfnWndProc   = WndProc;
	wc.hCursor       = LoadCursor(NULL, IDC_ARROW);
	wc.hIcon         = LoadIcon(NULL, IDI_APPLICATION);
	
	mbstowcs(wa, window->title->nova_Nova_String_Nova_chars->nova_datastruct_list_Nova_StringCharArray_Nova_data, size);

	RegisterClassW(&wc);
	
	threadWindow = window;
	threadPaintFunc = paintFunc;
	threadAddedFunc = addedFunc;
	
	hwnd = CreateWindowW(wc.lpszClassName, wa, WS_OVERLAPPEDWINDOW | WS_VSCROLL | ES_AUTOVSCROLL, window->x, window->y, window->width, window->height, NULL, NULL, hInstance, window);
	
	window->hwnd = hwnd;
	window->hinstance = hInstance;
	
	while (GetMessage(&msg, NULL, 0, 0))
	{
		DispatchMessage(&msg);
	}
	
	return hwnd;
#else
    return 0;
#endif
}

#ifdef _WIN32
void nova_showWindow(HWND hwnd) {
	ShowWindow(hwnd, SW_SHOWDEFAULT);
	UpdateWindow(hwnd);
}
#endif

// Get the horizontal and vertical screen sizes in pixel
void GetDesktopResolution(int* horizontal, int* vertical) {
#ifdef _WIN32
	RECT desktop;
	// Get a handle to the desktop window
	const HWND hDesktop = GetDesktopWindow();
	// Get the size of screen to the variable desktop
	GetWindowRect(hDesktop, &desktop);
	// The top left corner will have coordinates (0,0)
	// and the bottom right corner will have coordinates
	// (horizontal, vertical)
	*horizontal = desktop.right;
	*vertical = desktop.bottom;
#endif
}
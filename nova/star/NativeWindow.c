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

void DrawButton(HDC hdc, int x, int y, int width, int height) {
	RECT r;
	
	r.left   = x;
	r.right  = x + width;
	r.top    = y;
	r.bottom = y + height;
	
	DrawFrameControl(hdc, &r, DFC_BUTTON, DFCS_BUTTONPUSH);
}

void DrawComponents(nova_star_Nova_Window* this) {
	DrawPixels(*this->hwnd, *this->hdc, *this->ps);
	
	DrawButton(*this->hdc, 100, 100, 100, 20);
	
	TextOut(*this->hdc, 5, 5, "trest", strlen("trest"));
}

LRESULT CALLBACK WndProc(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam)
{
	PAINTSTRUCT ps;  
	HDC hdc;
	
	nova_funcStruct* paintFunc = (nova_funcStruct*)GetProp(hwnd, (LPCSTR)L"paint function");
	nova_star_Nova_Window* window = (nova_star_Nova_Window*)GetProp(hwnd, (LPCSTR)L"window");
	
	switch (msg)
	{
		case WM_PAINT:
			hdc = BeginPaint(hwnd, &ps);
			window->hwnd = &hwnd;
			window->hdc = &hdc;
			window->ps = &ps;
			
			SetBkMode(hdc, TRANSPARENT);
			
			((nova_star_window_paint_function)paintFunc->func)(paintFunc->ref, paintFunc->context);
			
			EndPaint(hwnd, &ps);
			break;
		case WM_DESTROY:
			PostQuitMessage(0);
			return 0;
	}

	return DefWindowProcW(hwnd, msg, wParam, lParam);
}

#endif

WINDOW_ID_TYPE nova_createWindow(nova_star_Nova_Window* window, nova_funcStruct* paintFunc)
{
#ifdef _WIN32
	MSG  msg;
	HWND hwnd;
	WNDCLASSW wc;

	HINSTANCE hInstance = GetModuleHandle(NULL);

	size_t size = window->title->nova_Nova_String_Nova_count + 1;
	wchar_t* wa = (wchar_t*)NOVA_MALLOC(sizeof(wchar_t) * size);

	wc.style         = CS_HREDRAW | CS_VREDRAW;
	wc.cbClsExtra    = 0;
	wc.cbWndExtra    = 0;
	wc.lpszClassName = L"Pixels";
	wc.hInstance     = hInstance;
	wc.hbrBackground = GetSysColorBrush(COLOR_3DFACE);
	wc.lpszMenuName  = NULL;
	wc.lpfnWndProc   = WndProc;
	wc.hCursor       = LoadCursor(NULL, IDC_ARROW);
	wc.hIcon         = LoadIcon(NULL, IDI_APPLICATION);

	mbstowcs(wa, window->title->nova_Nova_String_Nova_chars->nova_datastruct_list_Nova_StringCharArray_Nova_data, size);

	RegisterClassW(&wc);
	
	hwnd = CreateWindowW(wc.lpszClassName, wa, WS_OVERLAPPEDWINDOW | WS_VISIBLE, window->x, window->y, window->width, window->height, NULL, NULL, hInstance, NULL);
	window->hwnd = &hwnd;
	
	SetProp(hwnd, (LPCSTR)L"paint function", paintFunc);
	SetProp(hwnd, (LPCSTR)L"window", window);
	
	ShowWindow(hwnd, SW_SHOWDEFAULT);
	UpdateWindow(hwnd);

	while (GetMessage(&msg, NULL, 0, 0))
	{
		DispatchMessage(&msg);
	}

	return hwnd;
#else
    return 0;
#endif
}

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
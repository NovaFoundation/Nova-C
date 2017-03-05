#include "NativeWindow.h"

#ifdef _WIN32
void DrawPixels(HWND hwnd)
{
	PAINTSTRUCT ps;
	RECT r;
	int i = 0;

	HDC hdc = BeginPaint(hwnd, &ps);

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

void DrawComponents(HWND hwnd, HDC hdc, PAINTSTRUCT ps) {
	DrawPixels(hwnd, hdc, ps);
	
	SetBkMode(hdc, TRANSPARENT);
	
	DrawButton(hdc, 100, 100, 100, 20);
	
	TextOut(hdc, 5, 5, "trest", strlen("trest"));
}

LRESULT CALLBACK WndProc(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam)
{
	switch (msg)
	{
		case WM_PAINT:
			DrawPixels(hwnd);
			break;
		case WM_DESTROY:
			PostQuitMessage(0);
			return 0;
	}

	return DefWindowProcW(hwnd, msg, wParam, lParam);
}

#endif

WINDOW_ID_TYPE nova_createWindow(int x, int y, int width, int height, char* title)
{
#ifdef _WIN32
	MSG  msg;
	HWND hwnd;
	WNDCLASSW wc;

	HINSTANCE hInstance = GetModuleHandle(NULL);

	size_t size = strlen(title) + 1;
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

	mbstowcs(wa, title, size);

	RegisterClassW(&wc);
	hwnd = CreateWindowW(wc.lpszClassName, wa, WS_OVERLAPPEDWINDOW | WS_VISIBLE, x, y, width, height, NULL, NULL, hInstance, NULL);

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
import os
import sys
import json
import threading
import subprocess
import ctypes
import webview

from downloader import VideoDownloader, get_base_dir, get_exe_dir

def get_clipboard_text():
    """Read UTF-16 text from Windows clipboard via Win32 API."""
    try:
        user32 = ctypes.windll.user32
        kernel32 = ctypes.windll.kernel32
        CF_UNICODETEXT = 13

        if user32.OpenClipboard(None):
            try:
                h_glb = user32.GetClipboardData(CF_UNICODETEXT)
                if h_glb:
                    p_glb = kernel32.GlobalLock(h_glb)
                    if p_glb:
                        text = ctypes.c_wchar_p(p_glb).value
                        kernel32.GlobalUnlock(h_glb)
                        return text or ""
            finally:
                user32.CloseClipboard()
    except Exception as e:
        print("Clipboard error:", e)
    return ""

class DownloaderBridge:
    def __init__(self):
        # NOTE: All non-callable internal variables MUST start with '_'
        # so pywebview does not inspect them recursively.
        self._download_dir = os.path.join(os.path.expanduser("~"), "Downloads", "Fluxion")
        os.makedirs(self._download_dir, exist_ok=True)
        self._downloader = VideoDownloader(self._download_dir)
        self._last_downloaded_file = None
        self._window = None

    def set_window(self, window):
        self._window = window

    def get_clipboard(self):
        return get_clipboard_text()

    def get_download_dir(self):
        return self._download_dir

    def select_directory(self):
        if not self._window:
            return self._download_dir
        result = self._window.create_file_dialog(
            webview.FOLDER_DIALOG,
            directory=self._download_dir
        )
        if result and len(result) > 0:
            chosen = result[0]
            self._download_dir = chosen
            self._downloader.download_dir = chosen
            return chosen
        return self._download_dir

    def get_info(self, url):
        try:
            return self._downloader.get_info(url)
        except Exception as e:
            print("get_info error:", e)
            return {'error': str(e)}

    def start_download(self, url, format_type, custom_dir):
        if custom_dir and os.path.isdir(custom_dir):
            self._download_dir = custom_dir
            self._downloader.download_dir = custom_dir

        def progress_callback(data):
            if self._window:
                try:
                    payload = json.dumps(data)
                    self._window.evaluate_js(f"if (window.onProgress) {{ window.onProgress({payload}); }}")
                except Exception as ex:
                    print("Progress callback error:", ex)

        try:
            res = self._downloader.download(
                url=url,
                format_type=format_type,
                progress_callback=progress_callback
            )
            self._last_downloaded_file = res.get('filepath')
            return res
        except Exception as e:
            raise Exception(str(e))

    def open_download_folder(self):
        try:
            if self._last_downloaded_file and os.path.exists(self._last_downloaded_file):
                abs_path = os.path.abspath(self._last_downloaded_file)
                subprocess.run(f'explorer /select,"{abs_path}"', shell=True)
            elif os.path.exists(self._download_dir):
                os.startfile(self._download_dir)
            else:
                os.makedirs(self._download_dir, exist_ok=True)
                os.startfile(self._download_dir)
        except Exception as e:
            print("Folder open error:", e)

def find_html_file():
    candidates = [
        os.path.join(get_base_dir(), "gui", "index.html"),
        os.path.join(get_exe_dir(), "gui", "index.html"),
        os.path.join(os.path.dirname(os.path.abspath(__file__)), "gui", "index.html"),
    ]
    for c in candidates:
        if os.path.isfile(c):
            return c
    return candidates[0]

def main():
    html_file = find_html_file()
    bridge = DownloaderBridge()
    window = webview.create_window(
        title="Fluxion",
        url=html_file,
        js_api=bridge,
        width=820,
        height=680,
        min_size=(760, 580),
        background_color='#09090b'
    )
    bridge.set_window(window)
    # Expose functions explicitly as well to guarantee they are bound to window.pywebview.api
    window.expose(
        bridge.get_clipboard,
        bridge.get_download_dir,
        bridge.select_directory,
        bridge.get_info,
        bridge.start_download,
        bridge.open_download_folder
    )
    webview.start(debug=False)

if __name__ == "__main__":
    main()

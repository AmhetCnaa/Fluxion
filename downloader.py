import os
import sys
import shutil
import glob
import yt_dlp

def get_base_dir():
    """Get the base directory whether running as script or frozen PyInstaller exe."""
    if getattr(sys, 'frozen', False):
        return getattr(sys, '_MEIPASS', os.path.dirname(sys.executable))
    return os.path.dirname(os.path.abspath(__file__))

def get_exe_dir():
    """Get the actual directory of the executable or script."""
    if getattr(sys, 'frozen', False):
        return os.path.dirname(sys.executable)
    return os.path.dirname(os.path.abspath(__file__))

def find_binary(binary_name):
    """Find a binary in local app dir, PyInstaller dir, or system PATH."""
    search_dirs = [
        get_exe_dir(),
        get_base_dir(),
        os.path.join(get_exe_dir(), "bin"),
        os.path.join(get_base_dir(), "bin"),
    ]
    
    for d in search_dirs:
        p = os.path.join(d, f"{binary_name}.exe")
        if os.path.isfile(p):
            return p

    which_path = shutil.which(binary_name)
    if which_path:
        return which_path

    # Common Windows winget / local paths
    user_profile = os.environ.get("USERPROFILE", "")
    local_app_data = os.environ.get("LOCALAPPDATA", "")
    additional_paths = [
        os.path.join(user_profile, "ffmpeg", "bin", f"{binary_name}.exe"),
        os.path.join("C:\\", "ffmpeg", "bin", f"{binary_name}.exe"),
        os.path.join(local_app_data, "Microsoft", "WinGet", "Links", f"{binary_name}.exe"),
    ]
    for ap in additional_paths:
        if os.path.isfile(ap):
            return ap

    return None

class VideoDownloader:
    def __init__(self, download_dir=None):
        if not download_dir:
            self.download_dir = os.path.join(os.path.expanduser("~"), "Downloads", "Fluxion")
        else:
            self.download_dir = download_dir
        
        os.makedirs(self.download_dir, exist_ok=True)
        self.ffmpeg_path = find_binary("ffmpeg")
        self.aria2c_path = find_binary("aria2c")

    def get_info(self, url):
        """Fetch video metadata quickly without downloading."""
        opts = {
            'extract_flat': False,
            'skip_download': True,
            'quiet': True,
            'no_warnings': True,
            'js_runtimes': {'node': {}},
            'remote_components': ['ejs:github'],
            'extractor_args': {
                'youtube': {
                    'player_client': ['web', 'tv_embedded', 'mweb'],
                }
            }
        }
        if self.ffmpeg_path:
            opts['ffmpeg_location'] = os.path.dirname(self.ffmpeg_path)

        with yt_dlp.YoutubeDL(opts) as ydl:
            info = ydl.extract_info(url, download=False)
            return {
                'title': info.get('title', 'Unknown Title'),
                'duration': info.get('duration', 0),
                'thumbnail': info.get('thumbnail', None),
                'uploader': info.get('uploader', 'Unknown Channel'),
            }

    def download(self, url, format_type='mp4', progress_callback=None):
        """
        Download video at absolute maximum speed:
        - 16 parallel connections per file via aria2c
        - 16 concurrent fragments for DASH/HLS
        - YouTube n-challenge solved via ejs solver (zero throttling)
        """
        def ydl_hook(d):
            if not progress_callback:
                return
            if d['status'] == 'downloading':
                total = d.get('total_bytes') or d.get('total_bytes_estimate') or 0
                downloaded = d.get('downloaded_bytes', 0)
                speed = d.get('speed', 0)
                eta = d.get('eta', 0)
                
                percent = (downloaded / total * 100) if total > 0 else 0
                progress_callback({
                    'status': 'downloading',
                    'percent': percent,
                    'downloaded': downloaded,
                    'total': total,
                    'speed': speed,
                    'eta': eta,
                    'filename': d.get('filename', '')
                })
            elif d['status'] == 'finished':
                progress_callback({
                    'status': 'processing',
                    'message': 'Dosya birleştiriliyor ve işleniyor...'
                })

        out_template = os.path.join(self.download_dir, '%(title)s.%(ext)s')

        # Turbo Speed Options
        common_opts = {
            'outtmpl': out_template,
            'progress_hooks': [ydl_hook],
            'noplaylist': True,
            'js_runtimes': {'node': {}},
            'remote_components': ['ejs:github'],
            'concurrent_fragment_downloads': 16,
            'retries': 10,
            'fragment_retries': 10,
            'file_access_retries': 5,
            'quiet': True,
            'no_warnings': True,
            'extractor_args': {
                'youtube': {
                    'player_client': ['web', 'tv_embedded', 'mweb'],
                }
            }
        }

        # Enable aria2c multi-connection accelerator if available
        if self.aria2c_path:
            common_opts['external_downloader'] = self.aria2c_path
            common_opts['external_downloader_args'] = {
                'aria2c': [
                    '-x16',
                    '-s16',
                    '-j16',
                    '-k1M',
                    '--file-allocation=none',
                    '--summary-interval=1',
                    '--retry-wait=1',
                    '--max-tries=5'
                ]
            }

        if self.ffmpeg_path:
            common_opts['ffmpeg_location'] = os.path.dirname(self.ffmpeg_path)

        if format_type == 'mp3':
            opts = {
                **common_opts,
                'format': 'bestaudio/best',
                'postprocessors': [{
                    'key': 'FFmpegExtractAudio',
                    'preferredcodec': 'mp3',
                    'preferredquality': '320',
                }],
            }
        else: # mp4 en yüksek kalite
            opts = {
                **common_opts,
                'format': 'bestvideo*+bestaudio/best',
                'merge_output_format': 'mp4',
            }

        with yt_dlp.YoutubeDL(opts) as ydl:
            info = ydl.extract_info(url, download=True)
            filename = ydl.prepare_filename(info)
            if format_type == 'mp3':
                base, _ = os.path.splitext(filename)
                filename = base + '.mp3'
            elif format_type == 'mp4':
                base, _ = os.path.splitext(filename)
                filename = base + '.mp4'

            return {
                'title': info.get('title'),
                'filepath': filename,
                'directory': self.download_dir
            }

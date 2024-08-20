
import os
import re
import time
import urllib
from urllib import request


dir = "debian/pool/"
link = f"https://archive.debian.org/{dir}"

base = f"https://archive.debian.org/{dir}"
dest = "/path/to/directory"


def fetch_rec(link):
    time.sleep(15)
    a = request.urlopen(link)
    for line in a.read().decode("utf-8").splitlines():
        if m := re.match(r'<tr><td valign="top"><img src="/icons/folder\.gif" alt="\[DIR]"></td><td><a href="(.*?)">.*?/</a></td><td align="right">', line):
            fetch_rec(link + m.group(1))
        elif m := re.match(r'<tr><td valign="top"><img src="/icons/.*?" alt=".*?"></td><td><a href="(.*?)">.*?</a></td><td align="right">.*?</td><td align="right">(.*?)</td>', line):
            url = link + m.group(1)
            dest_f = url.replace(base, dest)
            if os.path.exists(dest_f):
                continue
            os.makedirs(os.path.dirname(dest_f), exist_ok=True)
            a, b = urllib.request.urlretrieve(url, dest_f)
            print(b.get("Content-Length"), dest_f, sep="\t")


fetch_rec(link)

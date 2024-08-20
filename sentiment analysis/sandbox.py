# import re
# import time
import os
import re
import urllib
from urllib import request
#
dir = "debian/pool/"
link = f"https://archive.debian.org/{dir}"

base = "https://archive.debian.org/debian/pool/"
dest = "/home/ubu/vboxshared/all_downloads/debian/pool/"


def fetch_rec(link):
    a = request.urlopen(link)
    for line in a.read().decode("utf-8").splitlines():

        if m := re.match(r'<tr><td valign="top"><img src="/icons/folder\.gif" alt="\[DIR]"></td><td><a href="(.*?)">.*?/</a></td><td align="right">', line):
            # print("fold", m.groups())
            fetch_rec(link + m.group(1))
        elif m := re.match(r'<tr><td valign="top"><img src="/icons/.*?" alt=".*?"></td><td><a href="(.*?)">.*?</a></td><td align="right">.*?</td><td align="right">(.*?)</td>', line):
            # print("file", m.groups())
            # if "ia64" in m.group(1):
            # print(m.group(2), link+m.group(1), sep="\t")
            url = link + m.group(1)
            dest_f = url.replace(base, dest)
            if os.path.exists(dest_f):
                continue
            os.makedirs(os.path.dirname(dest_f), exist_ok=True)
            a, b = urllib.request.urlretrieve(url, dest_f)
            print(b.get("Content-Length"), dest_f, sep="\t")

            # f.write(f"{link+m.group(1)}\t{m.group(2)}\n")
        # print(line)

fetch_rec(link)

# with open("links.txt", "a") as f:
#     fetch_rec(link, f)
# a = request.urlopen(link)
# for line in a.read().decode("utf-8").splitlines():
#     # print(line)
#     m = re.match(r'.*<td><a href="(.*?)">.*?</a></td>', line)
#     if m:
#         if m.group(1).startswith("/"):
#             continue
#         print(m.group(1))
#         time.sleep(1)
#         aa = request.urlopen(link + m.group(1))
#         for linee in aa.read().decode("utf-8").splitlines():
#             mm = re.match(r'.*<td><a href="(.*?)">.*?</a></td>', linee)
#             if mm and "ia64" in mm.group(1):
#                 get_ia64_binary(mm.groups(1))

            # print(linee)



# ls = []
# with open("links.txt", "r") as f:
#     for l in f.read().splitlines():
#
#         url = l.split("\t")[0]
#         dest_f = url.replace(base, dest)
#         os.makedirs(os.path.dirname(dest_f), exist_ok=True)
#         a, b = urllib.request.urlretrieve(url, dest_f)
#         print(b.get("Content-Length"), dest_f, sep="\t")


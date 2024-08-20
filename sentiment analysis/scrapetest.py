import gzip
import os
import time

import requests
import re




# skip = 6
# for year in range(1996, 2025):
#     for month in range(1, 13):
#         if skip > 0:
#             skip -= 1
#             continue
#         with open(f"tomshardware_archive/{year}-{month}.txt", "w") as f:
#             text = requests.get(f"https://www.tomshardware.com/archive/{year}/{month:02}").text
#             f.write(text)
#             time.sleep(1)

test = 0
for file in sorted(os.listdir("tomshardware_archive")):
    with open("tomshardware_archive/"+file) as f:
        for m in re.finditer(r"""<li class="day-article">\n<a href="(.*?)">\n(.*?)\n</a>\n</li>""", f.read()):
            test += 1
            # if "itanium" in m.group(2)
            # print(test, m.group(1), m.group(2))

            text = requests.get(m.group(1)).text
            title = re.sub("[^a-zA-Z0-9 ()\"'-]", "", m.group(2))
            with gzip.open(f"tomshardware_articles/{file[:-4]} - {title}.gzip", "tw") as w:
                print(test, len(text), m.group(1), sep="\t")
                w.write(text)
            time.sleep(1)
            # exit()


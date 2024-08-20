import os
from datetime import datetime, timedelta

import gemini_lib

with open(f"{os.path.dirname(gemini_lib.__file__)}/logs/requests.txt") as f:
    dates = []
    for l in f:
        if l.startswith(">"):
            dates.append(datetime.fromtimestamp(float(l[1:].split("\t")[0])))

    last_minute = len([d for d in dates if (datetime.now() - d) < timedelta(seconds=60) * 1.1])
    last_day = len([d for d in dates if (datetime.now() - d) < timedelta(seconds=60) * 60 * 24 * 1.1])
    len2 = len([d for d in dates])

    print(f"min={last_minute}\tday={last_day}\tall={len2}")


import os.path
import re
import time

import gemini_lib


for full_iteration in range(1, 3):
    if os.path.exists(f"scopus/keywords{full_iteration}"):
        skip = max([int(t.split("\t")[0]) for t in open(f"scopus/keywords{full_iteration}").read().splitlines()], default=0)
        print(f"exists! Skipping {skip}")
    else:
        skip = -1

    with open("scopus/scopus.txt", "rb") as f, open(f"scopus/keywords{full_iteration}", "a") as values_file, open(f"scopus/klogs{full_iteration}", "a") as log_file:
        for n, line in zip(range(10000), f.read().decode("ascii", "ignore").splitlines()):
            if n <= skip:
                continue
            line = (line+";").split("\t")
            print(line[0])
            prompt = f"""Given this article:
Title:
{line[0]}

Article:
{line[1]}


I want you to analyze the key points of criticism or acclimation of this article towards Itanium. Please respond following these steps:
1. Extensively analyze the topic/goal of the article.
2. Extract which aspects are opinionated (positive or negative) towards Itanium.
3. Conclude with a list of the top 5 sentiment labels and corresponding grades, between square brackets [], like in the format below:
[<sentiment label>: -2] - Major issue with Itanium
[<sentiment label>: -1] - Minor issue with Itanium
[<sentiment label>: 0] - Neutral (Indifferent, or mixed with no conclusion)
[<sentiment label>: 1] - Good aspect of Itanium
[<sentiment label>: 2] - Excellent aspect of Itanium

Example of a good concluding answer:
[Performance: 1], [Development delays: -2], [Software support: -1], [Error detection and correction: 1], [Audience targeting: 1]
or please be specific if the article does not express any opinions relevant to Itanium
[Irrelevant: 0]
"""
            # print(prompt)
            for attempts in range(5):
                try:
                    response = gemini_lib.get_response(prompt)
                    if ms := re.findall(r"\[ *(.*?) *: *\+?(-?\d+) *]", response.replace("*", "")):
                        print(">>>>>>", n, ms)
                        values_file.write(f"{n}\t{ms}\n")
                        values_file.flush()
                        log_file.write(f"Prompt:\n{prompt}\n============================================================================\nResponse:\n{response}\n============================================================================\n")
                        log_file.flush()
                        break
                    else:
                        log_file.write(f"Prompt:\n{prompt}\n============================================================================\nResponse:\n{response}\n============================================================================\n")
                        print(">>>>>>", n, "UNDECIDED")
                except Exception as e:

                    print(e)
                    time.sleep(30 * (10**attempts))
import re
import time
from datetime import datetime

import google.generativeai as genai
import os

genai.configure(api_key="AIzaSyCAm-4x_27DETiCjILk2BiOxE3G5xTwXaA")

now = datetime.now()
log = open(f"{os.path.dirname(__file__)}/logs/session_{int(now.timestamp())}.txt", "w")
log.write(f"Started session {now}\n\n")
model = genai.GenerativeModel(model_name="gemini-1.5-flash")

for full_iteration in range(1, 11):
    if os.path.exists(f"scopus/values{full_iteration}"):
        skip = max([int(t.split("\t")[0]) for t in open(f"scopus/values{full_iteration}").read().splitlines()], default=0)
        print(f"exists! Skipping {skip}")
    else:
        skip = -1
    # exit()
    with open("scopus/scopus.txt", "rb") as f, open(f"scopus/values{full_iteration}", "a") as values_file, open(f"scopus/logs{full_iteration}", "a") as log_file:
        for n, line in enumerate(f.read().decode("ascii", "ignore").splitlines()):
            if n <= skip:
                continue
            line = (line+";").split("\t")
            print(line[0])
            prompt = f"""Given this article:
Title:
{line[0]}

Article:
{line[1]}


I want you to analyze the sentiment of this article towards Itanium. Can you anwer by first extensively analyzing the topic/goal of the article, then explain how positive or negative they talk about Itanium, concluding with a grade between brackets [] according to the scoring I provide below?
Please choose the most fitting Sentiment score
[-2] - Negative (Clearly shedding negative light on Itanium)
[-1] - Signs of critique (Sceptical of Itanium, or indirectly negative)
[0] - Neutral (Indifferent or no answer)
[1] - Somewhat positive (Advocate of Itanium, or indirectly supportive)
[2] - Positive (Putting Itanium on a positive spotlight)
"""
            for attempts in range(5):
                try:
                    response = model.generate_content(prompt).text

                    log_file.write(f"Prompt:\n{prompt}\n============================================================================\nResponse:\n{response}\n============================================================================\n")
                    log_file.flush()
                    if m := re.search(r"\[ *\+?(-?\d+) *]", response.replace("*", "")):
                        print(">>>>>>", n, m.group(1))
                        values_file.write(f"{n}\t{m.group(1)}\n")
                        values_file.flush()
                        break
                    else:
                        print(">>>>>>", n, "UNDECIDED")
                except Exception as e:
                    print(e)
                    time.sleep(30 * (10**attempts))
                finally:
                    time.sleep(3)
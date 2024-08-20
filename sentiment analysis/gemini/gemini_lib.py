import time
from datetime import datetime, timedelta

import google.generativeai as genai
import os

from google.ai.generativelanguage_v1beta import Content, Part

genai.configure(api_key="AIzaSyCAm-4x_27DETiCjILk2BiOxE3G5xTwXaA")

now = datetime.now()
log = open(f"{os.path.dirname(__file__)}/logs/session_{int(now.timestamp())}.txt", "w", encoding='utf-8')
log.write(f"Started session {now}\n\n")

model = genai.GenerativeModel(model_name="gemini-1.5-flash")


def count_tokens(prompt: str) -> int:
    prompt_tokens = model.count_tokens(prompt)
    return prompt_tokens.total_tokens

def get_response(prompt: str, agent_start: str = None) -> str:
    prompt_tokens = model.count_tokens(prompt)
    # with open(f"{os.path.dirname(__file__)}/logs/requests.txt") as f:
    #     dates = []
    #     for l in f:
    #         if l.startswith(">"):
    #             dates.append(datetime.fromtimestamp(float(l[1:].split("\t")[0])))
    #             # print(datetime.now() - datetime.fromtimestamp(float(l[1:].split("\t")[0])))
    #
    # while True:
    #     time.sleep(3)
    #     last_minute = len([d for d in dates if (datetime.now() - d) < timedelta(seconds=60) * 1.1])
    #     last_day = len([d for d in dates if (datetime.now() - d) < timedelta(seconds=60) * 60 * 24 * 1.1])
    #     len2 = len([d for d in dates])
    #     # print(f"min={last_minute}\tday={last_day}\tall={len2}")
    #     if last_minute < 12 and last_day < 1499:
    #         break
    #     print(f"min={last_minute}\tday={last_day}\tall={len2}")
    time.sleep(3)
    open(f"{os.path.dirname(__file__)}/logs/requests.txt", "a").write(f">{datetime.now().timestamp()}\t{prompt_tokens.total_tokens}\t{model.model_name}\n")

    if agent_start:
        prompt = [Content(role="user", parts=[Part(text=prompt)]), Content(role="model", parts=[Part(text=agent_start)])]

    response = model.generate_content(prompt, request_options={"timeout": 1000})
    log.write(
        f"Prompt:\n{prompt}\n============================================================================\nResponse:\n{response}\n============================================================================\n")
    log.flush()
    response_tokens = model.count_tokens(response.text)
    open(f"{os.path.dirname(__file__)}/logs/requests.txt", "a").write(f"<{datetime.now().timestamp()}\t{response_tokens.total_tokens}\t{model.model_name}\n")

    return response.text

def get_response_iter(prompt: str):
    prompt_tokens = model.count_tokens(prompt)
    time.sleep(3)
    open(f"{os.path.dirname(__file__)}/logs/requests.txt", "a").write(
        f">{datetime.now().timestamp()}\t{prompt_tokens.total_tokens}\t{model.model_name}\n")
    response = model.generate_content(prompt, stream=True, request_options={"timeout": 1000})
    return response


def log_response_iter(prompt: str, response):
    log.write(
        f"Prompt:\n{prompt}\n============================================================================\nResponse:\n{response}\n============================================================================\n")
    log.flush()
    response_tokens = model.count_tokens(response.text)
    open(f"{os.path.dirname(__file__)}/logs/requests.txt", "a").write(
        f"<{datetime.now().timestamp()}\t{response_tokens.total_tokens}\t{model.model_name}\n")


# for i in range(100):
# get_response("Why did Itanium fail?")
# print(type(response))
# print(response)
# print(response.text)


# for m in genai.list_models():
#     print(m)
#
# exit()
# model = genai.GenerativeModel('gemini-pro')
# response = model.generate_content('Please summarise this document: ...')
#
# print(response.text)


# model = genai.GenerativeModel(model_name="gemini-1.0-pro")
#
# # with open("random stuff.txt", "w") as f:
# for i in range(100):
#     responses = model.generate_content(["Why did Itanium fail?"], stream=True)
#     # print(response)
#     for chunk in responses:
#         print(chunk.text)
#     exit()
        # f.write(response.text)
        # f.write("\n\n====================================================================\n\n")

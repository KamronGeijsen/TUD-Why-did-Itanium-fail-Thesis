import gzip
import os
import re
from bs4 import BeautifulSoup





for file in sorted(os.listdir("tomshardware_articles")):
    with gzip.open("tomshardware_articles/"+file, "tr") as f:
        text = f.read()

        parsed_html = BeautifulSoup(text, 'html.parser')
        article = parsed_html.body.find('div', attrs={'class': 'text-copy bodyCopy auto', "id": "article-body"})

        if article and "Itanium" in article.text:
            print(file + "\t" + article.text)
        # print(article)

        # exit()
        # if m := re.search("""<div id="article-body" class="text-copy bodyCopy auto">\n(.*?)""", text):
        #     print(m.groups(1))
        #     pass
        # else:
        #     print(file)
        #     print(text)
        #     exit()
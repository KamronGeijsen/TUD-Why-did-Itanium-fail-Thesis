import datetime
import os.path

import matplotlib
import matplotlib.pyplot as plt
import matplotlib.dates as mdates
from datetime import datetime
from collections import defaultdict

import numpy as np

thing = "ieee"
thing2 = "IEEE"
ls = [t.split("\t",1) for t in open(f"{thing}/{thing}.txt","br").read().decode("ascii","ignore").splitlines()]
dates = [t[0].split(" - ", 1)[0] for t in ls]
titles = [t[0].split(" - ", 1)[1] for t in ls]
data = [list() for t in ls]

for i in range(1,11):
    if os.path.exists(f"{thing}/values{i}"):
        for l in open(f"{thing}/values{i}").read().splitlines():
            n, s = l.split("\t")
            data[int(n)].append(int(s))

entries = []
for n, d,t,data in zip(range(1000000), dates, titles, data):
    print(n, *data, d, t, sep="\t")
    # print(*data, sep="\t")
    if len(data) == 0:
        continue
    obj = type("", (), {})()
    obj.value = sum(data)/len(data)
    for fmt in ["%Y-%m-%d", "%B %d, %Y %I:%M %p", "%d %b %Y"]:
        try:
            obj.date = datetime.strptime(d, fmt)

            break
        except:
            pass
    else:
        raise Exception("e")

    # obj.date.month
    entries.append(obj)


def aggregate_by_quarter(entries):
    quarter_counts = defaultdict(int)

    for entry in entries:
        year = entry.date.year
        quarter = (entry.date.month - 1) // 3 + 1
        key = (year, quarter)
        quarter_counts[key] += 1

    return quarter_counts


# Function to aggregate values by year
def aggregate_by_year(entries):
    year_counts = defaultdict(list)

    for entry in entries:
        year_counts[entry.date.year].append(entry.value)

    return year_counts


# Aggregate data
quarter_counts = aggregate_by_quarter(entries)
year_counts = aggregate_by_year(entries)

# Prepare data for plotting bar chart
dates = []
values = []
for (year, quarter), count in sorted(quarter_counts.items()):
    quarter_start_date = datetime(year, (quarter - 1) * 3 + 1, 1)
    dates.append(quarter_start_date)
    values.append(count)
years = list(range(1995, 2025))

matplotlib.rcParams.update({'font.size': 17})
# Plotting bar chart
fig, ax = plt.subplots(figsize=(10, 6))

ax.bar(dates, values, width=90)  # Adjust width for better visual separation

# Formatting the x-axis to show dates
ax.xaxis.set_major_locator(mdates.YearLocator())
ax.xaxis.set_minor_locator(mdates.MonthLocator(bymonth=[1, 4, 7, 10]))
ax.xaxis.set_major_formatter(mdates.DateFormatter('%Y'))
# ax.xaxis.set_minor_formatter(mdates.DateFormatter('%b'))

ax.set_xlim(datetime(1995, 1, 1), datetime(2024, 12, 31))
plt.xticks(rotation=90)
plt.xlabel('Year')
plt.ylabel('Number of Articles')
plt.title(f'Number of {thing2} Articles each Quarter from {min(years)} to {max(years)}')

plt.tight_layout()
# plt.show()
plt.savefig(f"{thing}_paper_count.png")


# Prepare data for plotting box plot
data = [year_counts[year] for year in years]
means = [np.nan]+[np.mean(year_counts[year]) for year in years]
# print(data)
# print(means)
# Plotting box plot
fig, ax = plt.subplots(figsize=(10, 6))

axes = ax.boxplot(data, labels=years)
# ax.set_xlim(1990, 2025)
# for axs in axes.values():
#     axs.set_ylim(-2, 2)
ax.spines['bottom'].set_position(('data', 0))
ax.plot(means, marker='.', linestyle='-', color='red', label='Mean Values')
ax.legend()

plt.ylim(-2, 2)
plt.xlabel('Year')
plt.ylabel('Sentiment')
plt.title(f'Distribution of Yearly Sentiment Values for {thing2}')

plt.xticks(rotation=90)
plt.tight_layout()
# plt.show()


plt.savefig(f"{thing}_whisker.png")
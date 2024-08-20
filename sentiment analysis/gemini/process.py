

ls = [t.split("\t",1) for t in open("cnet/cnet.txt").read().splitlines()]
dates = [t[0].split(" - ", 1)[0] for t in ls]
titles = [t[0].split(" - ", 1)[1] for t in ls]
data = [list() for t in ls]

for i in range(1,3):
    for l in open(f"cnet/values{i}").read().splitlines():
        n, s = l.split("\t")
        data[int(n)].append(int(s))

for n, d,t,data in zip(range(1000000), dates, titles, data):
    print(n, *data, d, t, sep="\t")
    # print(*data, sep="\t")
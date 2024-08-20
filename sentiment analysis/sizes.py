import os

files = os.listdir(r"C:\Users\kgeijsen\Desktop\Virtual Machines\Ubuntu 22.04\shared\all_headers\debian\pool\main\b\binutils\binutils_2.12.90.0.1-4_alpha.deb\data.tar.gz\usr\bin")

for f in os.listdir(r"C:\Users\kgeijsen\Desktop\Virtual Machines\Ubuntu 22.04\shared\all_headers\debian\pool\main\b\binutils"):
    if f.startswith("binutils_2.12.90.0.1-4"):
        for ff in files:
            for l in open(fr"C:\Users\kgeijsen\Desktop\Virtual Machines\Ubuntu 22.04\shared\all_headers\debian\pool\main\b\binutils\{f}\data.tar.gz\usr\bin\{ff}"):
                if ".text" in l:
                    print(f.replace("binutils_2.12.90.0.1-4_", "").replace(".deb", ""), ff.replace(".txt", ""), int(l.split("     ")[1].strip().split(" ")[0], 16), sep="\t")
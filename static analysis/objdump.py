import os
import subprocess
import tempfile
import tarfile

# root = "/home/ubu/vboxshared/all_downloads/debian/pool/main/b/binutils"
# shared = "/home/ubu/vboxshared/all_headers/debian/pool/main/b/binutils"
# replace = "/home/ubu/vboxshared/all_downloads/debian/pool/main/b/binutils"
root = "/media/ubu/WXPIFPP_EN"
shared = "/home/ubu/vboxshared/headers_winXP_IA64/WXPIFPP_EN"
replace = "/media/ubu/WXPIFPP_EN"

def recursive(ap: str, p):
    f = os.path.basename(p)
    if os.path.islink(p):
        return
    if os.path.isdir(p):
        for f in os.listdir(p):
            recursive(f"{ap}/{f}", f"{p}/{f}")
    else:
        try:
            with tempfile.TemporaryDirectory() as temp_dir:
                subprocess.run(['tar', '-xf', p], cwd=temp_dir, check=True, stderr=subprocess.DEVNULL)
                for f in os.listdir(temp_dir):
                    recursive(f"{ap}/{f}", f"{temp_dir}/{f}")
        except Exception as e:
            try:
                with tempfile.TemporaryDirectory() as temp_dir:
                    # Extract the .udeb archive using dpkg-deb
                    subprocess.run(['ar', 'x', p], cwd=temp_dir, check=True, stderr=subprocess.DEVNULL)
                    # print(os.listdir(temp_dir))
                    for f in os.listdir(temp_dir):
                        recursive(f"{ap}/{f}", f"{temp_dir}/{f}")

            except Exception as e:
                # print(ap, p, type(e), sep='\t')
                # subprocess.run(['objdump', '-d', p], check=True)
                try:
                    disasm = subprocess.check_output(["objdump", "-h", p], stderr=subprocess.DEVNULL)
                    # print(len(disasm))
                    output_file = shared + ap.replace(replace, "", 1) + ".txt"
                    if os.path.exists(output_file):
                        return
                    # print(output_file)
                    # exit()
                    # os.mkdir(os.path.dirname(output_file), parents=True)
                    os.makedirs(os.path.dirname(output_file), exist_ok=True)

                    with open(output_file, "wb") as f:
                        f.write(disasm)
                    print(len(disasm), output_file, sep="\t")
                    # exit()
                except Exception as e:
                    pass

                pass


recursive(root, root)



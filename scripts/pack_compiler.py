import hashlib
import sys
import zipfile
import os


# Declare the function to return all file paths of the particular directory
def retrieve_file_paths(dir_name: str) -> list[str]:
    paths = []

    for root, directories, files in os.walk(dir_name):
        for filename in files:
            path = os.path.join(root, filename)
            paths.append(path)

    return paths


def zip_dir(path: str):
    if path is None:
        return
    file = zipfile.ZipFile("../run/macrocosm/§5§lMacrocosm §d§lPack.zip", 'w')
    files = retrieve_file_paths(path)
    with file:
        for f in files:
            file.write(f, f.replace(path, ""))

    with open("../run/macrocosm/§5§lMacrocosm §d§lPack.zip", 'r') as pack:
        full = pack.buffer.read()
        sha = hashlib.sha1()
        sha.update(full)
        hashfile = open("../run/macrocosm/hashfile.sha", 'w')
        hashfile.write(sha.hexdigest())


if __name__ == "__main__":
    zip_dir(sys.argv[1] if len(sys.argv) > 1 else "../src/main/resources/pack")

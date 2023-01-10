import requests
import sys

def generate(url: str):
    response = requests.post(
        url = "https://api.mineskin.org/generate/url",
        data = { "name": "", "url": url, "variant": "", "visibility": 1 }
    )
    resp = response.json()
    print(f"textureProfile(\"{resp['data']['texture']['value']}\", \"{resp['data']['texture']['signature']}\")")

if __name__ == "__main__":
    generate(sys.argv[1])

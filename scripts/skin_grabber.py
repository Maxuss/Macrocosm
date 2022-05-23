import json
import sys
from requests import get


def uuid_from_mojang(name: str) -> str:
    return get(f"https://api.mojang.com/users/profiles/minecraft/{name}").json()["id"]


def grab_from_uuid(uid: str):
    profile = json.dumps(get(
        f"https://sessionserver.mojang.com/session/minecraft/profile/{uid.replace('-', '')}?unsigned=false").json())

    print(profile.replace(" ", ""))


def grab(argv: list[str]):
    arg = argv[1] if len(argv) >= 2 else "None"
    if arg.startswith("-u="):
        grab_from_uuid(arg.replace("-u=", ""))
    elif arg.startswith("-n="):
        grab_from_uuid(uuid_from_mojang(arg.replace("-n=", "")))
    elif arg.startswith("-me"):
        # my uuid and skin
        grab_from_uuid("13e76730de524197909a6d50e0a2203b")
    else:
        while True:
            name = input("Please input player name: ")
            if name.startswith("!"):
                break
            grab_from_uuid(uuid_from_mojang(name))


if __name__ == "__main__":
    grab(sys.argv)

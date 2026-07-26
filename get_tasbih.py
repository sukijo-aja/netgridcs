import urllib.request
import re

url = "https://raw.githubusercontent.com/tabler/tabler-icons/master/icons/outline/pray.svg"
try:
    with urllib.request.urlopen(url) as response:
         print("PRAY:", response.read().decode())
except Exception as e:
    pass

url = "https://raw.githubusercontent.com/lucide-icons/lucide/main/icons/gem.svg"
try:
    with urllib.request.urlopen(url) as response:
         print("GEM:", response.read().decode())
except Exception as e:
    pass


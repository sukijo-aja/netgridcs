import urllib.request

url = "https://raw.githubusercontent.com/Templarian/MaterialDesign/master/svg/rosary.svg"
try:
    with urllib.request.urlopen(url) as response:
         html = response.read().decode()
         print(html)
except Exception as e:
    pass

url = "https://raw.githubusercontent.com/Templarian/MaterialDesign/master/svg/necklace.svg"
try:
    with urllib.request.urlopen(url) as response:
         html = response.read().decode()
         print(html)
except Exception as e:
    pass


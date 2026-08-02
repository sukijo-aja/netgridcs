import os
import re

replacements = {
    'values': 'Welcome',
    'values-en': 'Welcome',
    'values-id': 'Selamat Datang',
    'values-in': 'Selamat Datang',
    'values-ar': 'مرحباً',
    'values-zh': '欢迎'
}

base_dir = '/media/masjo/RX7A/Projects/Android/AndroidStarter/app/src/main/res'

for folder, new_text in replacements.items():
    file_path = os.path.join(base_dir, folder, 'strings.xml')
    if os.path.exists(file_path):
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        content = re.sub(r'<string name="assalamu_alaikum">.*?</string>', f'<string name="welcome_message">{new_text}</string>', content)
        
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)

print("Update complete!")

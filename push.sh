#!/bin/bash

# Memeriksa apakah pesan commit diberikan
if [ -z "$1" ]; then
    echo "Gunakan: ./push.sh \"Pesan commit\""
    exit 1
fi

echo "Menambahkan perubahan..."
git add .

echo "Menyimpan commit: $1"
git commit -m "$1"

echo "Mendorong ke remote repository..."
git push

echo "Selesai!"

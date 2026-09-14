# Для запуска

## 1 Файлы
Структура файлов (джава придирается к названиям папок, так что делайте точно так, как тут написано)

копируйте проект в корень ОБЯЗАТЕЛЬНО с именем pksmatches

## 2 Компилируйте проект
в папке проекта в Windows PowerShell (терминал в вс коде и тд)

javac -d out (Get-ChildItem -Recurse -Filter *.java | % { $_.FullName })

(запускать каждый раз, когда меняешь .java)

## 3 Запуск

в той же папке повершела 

java -cp out pksmatches.Main

Переходить в ветку dev 

git switch dev

Копирование репозитория 

git clone https://github.com/TheSkelies/pks_matches

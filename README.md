### Инструкция пользователя
#### Аргументы при запуске
Для управления программой используются аргументы командной строки.
В программе определены следующие ключи:
* `--path` - обязательный ключ для указания путей файлов или url, в которых
находятся логи. Можно указывать несколько файлов, указывать шаблоны файлов,
указывать несколько url, указывать url и пути файлов одновременно. Если не
указать этот ключ, или не ввести никаких путей, то программа завершится с ошибкой.
* `--from` - ключ для указания начальной даты для фильтрации читаемых логов.
Логи, записанные ранее этой даты, будут игнорироваться. Дата вводится в формате ISO 8601,
например 2024-10-15T12:34:59Z, 2024-10-15T12:34:56+02:00.
* `--to` - ключ для указания конечной даты для фильтрации читаемых логов.
Логи, записанные после этой даты, будут игнорироваться. Дата вводится в формате ISO 8601.
* `--format` - ключ для указания формата выходного отчета. Можно выбрать формат `markdown` или `adoc`.
* `--filter-field` - ключ для указания поля, по которому будет производиться фильтрация.
Можно указать поля `address`, `user`, `method`, `resource`, `httpVersion`, `status`, `referer`, `userAgent`.
Если указать какое-либо другое поле, то программа завершится с ошибкой. Ключ применяется только с ключом
`--filter-value`. Запуск программы с одним из ключей `--filter-field` или `--filter-value` без другого
приведет к завершению программы с ошибкой.
* `--filter-value` - ключ для указания значение, по которому будет производиться фильтрация указанного
в предыдущем ключе поля. Для фильтрации по значению необходимо ввести конкретное значение. Для фильтрации
по шаблону необходимо ввести _регулярное выражение_. Ключ применяется только с ключом
`--filter-value`. Запуск программы с одним из ключей `--filter-field` или `--filter-value` без другого
приведет к завершению программы с ошибкой.

Ключи, в описании которых явно не указано, что их использование является обязательным, необязательные.
#### Поля фильтрации
NGINX лог имеет формат `'$remote_addr - $remote_user [$time_local] ' '"$request" $status $body_bytes_sent ' '"$http_referer" "$http_user_agent"'`.
Для удобства ввода каждое поле лога имеет свое название в программе.
* `address` - `$remote_addr`
* `user` - `$remote_user`
* `method`, `resource`, `httpVersion` - три этих имени вместе представляют `$request`. Для удобства
запрос разбит на HTTP-метод - `method`, запрашиваемый ресурс - `resource`, и версию HTTP - `httpVersion`
* `status` - `$status`
* `referer` - `$http_referer`
* `userAgent` - `$http_user_agent`

Для фильтрации по дате используются ключи `--from` и `--to`, поэтому для `$time_local` псевдонима нет.
#### Форматы выходного отчета
Программа формирует отчет со статистикой по прочитанным логам. В программе возможен вывод в
следующих форматах:
* Markdown (`.md`). Для выбора этого формата можно использовать ключ `--format` с параметром `markdown`.
Формат используется по умолчанию, поэтому, если опустить ключ `--format`, отчет будет иметь формат Markdown.
* AsciiDocument (`.adoc`). Для выбора этого формата необходимо использовать ключ `--format` с параметром `adoc`.

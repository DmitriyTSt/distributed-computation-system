# DCS (Distributed computation system)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/DmitriyTSt/distributed-computation-system?color=green&display_name=tag)](https://github.com/DmitriyTSt/distributed-computation-system/releases/latest)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/DmitriyTSt/dcs-core?display_name=tag&label=dcs-core)](https://github.com/DmitriyTSt/dcs-core/releases/latest)
## Распределенная система вычисления инвариантов графов

## Архитектура
### core
Содержит базовые классы, необходимые для client и server модулей.
### client
Клиентское приложение, позволяет запустить решение задач на нахождение инвариантов графов, получаемых с сервера.
### server
Приложение-сервер. Запускается для старта подсчета определенного инварианта на заданных входных данных. Входные данные должны поступать в стандартный поток ввода приложения.
### proto
Содержит описание интерфейсов взаимодействия клиент-сервер в формате protobuf. Общение клиент-сервер осуществляется с помощью [gRPC](https://grpc.io/).
## Сборка
```bash
git clone git@github.com:DmitriyTSt/distributed-computation-system.git
cd distributed-computation-system
```
**Сборка сервера:**
```
./gradlew :server:jar
```
Расположение файла: ```server/build/libs```  
**Сборка клиента для машин с установленной jre:**
```
./gradlew :client:jar
```
Расположение файла: ```client/build/libs```  
**Сборка клиента в exe (с отдельным jre):**
```
./gradlew :client:packageExecutableDistribution
```
Расположение архива: ```client/build/distExecutable```


## Пример запуска
### Сервер
```bash
geng 9 | java -jar server.jar
```
Параметры:
```cpp
--port <port> // Порт, на котором запускается сервер. По-умолчанию: 9999
-p <part_size> // Размер пачки, который посылается на клиент для подсчета. По-умолчанию: 1000
-j // classPath инварианта, лежащего в папке tasks. Обязательный параметр.
-s // Включение сохранения результатов в файл строками вида "graph6;invariant"
```
### Клиент
```bash
java -jar client.jar
```
```bash
./client.exe
```
Параметры:
```cpp
--server <ip_address> // IP-адрес, на котором запущен сервер. По-умолчанию: 127.0.0.1
--port <port> // Порт, на котором запущен сервер. По-умолчанию: 9999
-m // Включение многопоточного режима 
-j // classPath инварианта, лежащего в папке tasks, для локального подсчета
-s // Включение сохранения результатов в файл строками вида "graph6;invariant"
```

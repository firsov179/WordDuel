import sys
import selectors
import types
import json
from _thread import *


from Requester import Requester

import socket
from _thread import *

requester = Requester()
# функция для обработки каждого клиента
def client_thread(con):
    while True:
        data = con.recv(1024)  # получаем данные от клиента
        message = data.decode()  # преобразуем байты в строку
        response = requester.request_to_client(message)
        if response is None:
            break
        con.sendall(str.encode(json.dumps(response) + '\n'))  # отправляем сообщение клиенту
    con.close()  # закрываем подключение


server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)  # создаем объект сокета сервера
hostname = '192.168.0.150'  # получаем имя хоста локальной машины
port = 7007  # устанавливаем порт сервера
server.bind((hostname, port))  # привязываем сокет сервера к хосту и порту
server.listen(5)  # начинаем прослушиваение входящих подключений



print("Server running")
while True:
    client, _ = server.accept()  # принимаем клиента
    start_new_thread(client_thread, (client,))  # запускаем поток клиента
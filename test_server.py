#!/usr/bin/python3

import socket
import time
import json
import random


TCP_IP = '0.0.0.0'
TCP_PORT = 5000


WIDTH = 8
HEIGHT = 8

#BUFFER_SIZE = 20  # Normally 1024, but we want fast response

def main():
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    s.bind((TCP_IP, TCP_PORT))
    s.listen(1)

    while 1:
        conn, addr = s.accept()
        print('Connection address:', addr)

        try:
            handle(conn)
        except KeyboardInterrupt:
            break


    print('Shutting down socket')
    s.shutdown(socket.SHUT_RDWR)
    s.close()


def handle(conn):
    pixels = [i * 13 % 100 for i in range(WIDTH * HEIGHT)]
    while 1:
        try:
            randomize(pixels)
            data = {
                'battery': 0.5,
                'width': WIDTH,
                'height': HEIGHT,
                'temperatures': pixels,
                'temperatureRangeMin': 0.,
                'temperatureRangeMax': 100.,
            }
            conn.send(json.dumps(data).encode() + b'\n')
            print('sending data')
            time.sleep(1)
        except socket.error as e:
            print('Socket error', e)
            break



def randomize(pixels):
    for i in range(len(pixels)):
        pixels[i] += (random.random() - 0.5) * 10
        pixels[i] = max(0, min(50., pixels[i]))



if __name__ == '__main__':
    main()

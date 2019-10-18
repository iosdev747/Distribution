import time
import sys
import win32pipe, win32file, pywintypes
import json
from connection import client


def pipe_server():
    print("pipe server")
    reader = win32pipe.CreateNamedPipe(
        r'\\.\pipe\addComment',
        win32pipe.PIPE_ACCESS_DUPLEX,
        win32pipe.PIPE_TYPE_MESSAGE | win32pipe.PIPE_READMODE_MESSAGE | win32pipe.PIPE_WAIT,
        1, 65536, 65536,
        0,
        None)

    try:
        print("waiting for client")
        sys.stdout.flush()
        win32pipe.ConnectNamedPipe(reader, None)

        print("got client")

        resp = win32file.ReadFile(reader, 64 * 1024)
        my_json = resp[1].decode('utf8').replace("'", '"')

        result = client.execute(my_json)
        json_obj = json.loads(result)
        res = json_obj['data']['insert_comments']

        some_data = str.encode(f"{res}")
        print(some_data)
        win32file.WriteFile(reader, some_data)
        print("finished now")
    finally:
        win32file.CloseHandle(reader)


if __name__ == '__main__':
    pipe_server()

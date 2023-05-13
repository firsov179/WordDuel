import json
import random
import time

from CrosswordGenerator import CrosswordGenerator
from threading import *

from DbWork import select_from_db, insert_to_db


class Requester:
    games = dict()
    crossword_id = 1
    queue = []
    users_status = dict()
    queue_semaphore = Semaphore()

    def update_results(self, request):
        uid = int(request['uid'])
        game_result = request['game_result']
        game_type = request['game_type']
        query = f'''SELECT * FROM users WHERE uid = {uid}'''
        info = select_from_db(query)[0]
        res = {}
        res['rang_category'] = int(info[4])
        res['rang_subcategory'] = int(info[5])
        res['rang_points'] = int(info[6])
        res['count_games'] = int(info[7]) + 1
        res['games_wins'] = int(info[8]) + 1 if game_result == 'win' else 0
        res['words_wins'] = int(info[9]) + int(request['words_wins'])
        res['rangs_games'] = int(info[10])
        res['rangs_wins'] = int(info[11])
        res['series_rang'] = int(info[12])
        res['series_rang_cur'] = int(info[13])
        res['series_comm'] = int(info[14])
        res['series_comm_cur'] = int(info[15])

        if game_type == 'rang':
            if game_result == 'win':
                res['rang_points'] += 1
                if res['rang_points'] == 3:
                    res['rang_points'] = 0
                    res['rang_subcategory'] += 1
                    if res['rang_subcategory'] == 3:
                        res['rang_subcategory'] = 0
                        if res['rang_category'] != 6:
                            res['rang_category'] += 1
            else:
                res['rang_points'] = max(0, res['rang_points'] - 1)
            res['rangs_games'] += 1
            res['rangs_wins'] += 1 if game_result == 'win' else 0
            res['series_rang_cur'] = (res['series_rang_cur'] + 1) if game_result == 'win' else (
                0 if game_result == 'win' else res['series_rang_cur'])
            if res['series_rang'] < res['series_rang_cur']:
                res['series_rang'] = res['series_rang_cur']
        elif game_type == "common":
            res['series_comm_cur'] = (res['series_comm_cur'] + 1) if game_result == 'win' else (
                0 if game_result == 'win' else res['series_comm_cur'])
            if res['series_comm'] < res['series_comm_cur']:
                res['series_comm'] = res['series_comm_cur']
        query = f'''UPDATE users
        SET 
        rang_category = {res['rang_category']},
        rang_subcategory = {res['rang_subcategory']},
        rang_points = {res['rang_points']},
        count_games = {res['count_games']},
        games_wins = {res['games_wins']},
        words_wins = {res['words_wins']},
        rangs_games = {res['rangs_games']},
        rangs_wins = {res['rangs_wins']},
        series_rang = {res['series_rang']},
        series_rang_cur = {res['series_rang_cur']},
        series_comm = {res['series_comm']},
        series_comm_cur = {res['series_comm_cur']}
        WHERE uid = {uid};'''
        insert_to_db(query)
        return {'status': 200}

    def generate_crossword(self):
        query = "SELECT * FROM words WHERE word_id in ("
        for elem in random.sample(range(1, 31), 12):
            query += str(elem) + ', '

        query = query[:-2] + ')'
        words_info = select_from_db(query)
        cur = [words_info[i][1].upper() for i in range(12)]
        words_dict = dict()
        for i in range(12):
            words_dict[words_info[i][1].upper()] = words_info[i]
        crossword = CrosswordGenerator(cur)
        mapp, width, height, info = crossword.get_crossword()

        crossword = {'size': len(info),
                     'height': height,
                     'width': width,
                     'words': [{'i': elem[0][0],
                                'j': elem[0][1],
                                'size': len(elem[2]),
                                'orientation': elem[1],
                                'definition': words_dict[elem[2]][2],
                                'word_id': str(words_dict[elem[2]][0])
                                } for elem in info]}

        self.games[self.crossword_id] = {'ans': {words_dict[elem[2]][0]: elem[2] for elem in info}, 'guessed': dict(),
                                         'active_words': crossword['size'], 'start_time': time.time()}
        print(crossword)
        self.crossword_id += 1
        return crossword, self.crossword_id - 1

    def login(self, request: dict):
        login = request['email']
        query = f'SELECT * FROM users WHERE email = "{login}"'
        info = select_from_db(query)
        if len(info) != 0 and info[0][2] == request['password']:
            res = {'status': 200}
            values = list(info[0])
            values.pop(2)
            keys = ['uid', 'email', 'game_name', 'rang_category', 'rang_subcategory', 'rang_points', 'count_games',
                    'games_wins', 'words_wins', 'rangs_games',
                    'rangs_wins', 'series_rang', 'series_rang_cur', 'series_comm', 'series_comm_cur']
            for i in range(len(keys)):
                res[keys[i]] = values[i]
            return res
        return {'status': 400}

    def register(self, request: dict):
        login = request['email']
        password = request['password']
        game_name = request['game name']
        query = f'SELECT password, uid FROM users WHERE email = "{login}"'
        info = select_from_db(query)
        if len(info) != 0:
            return {'status': 409}
        query = f'''INSERT INTO 
        users(email, password, game_name, rang_category, rang_subcategory, rang_points, count_games, games_wins, words_wins, rangs_games,
         rangs_wins, series_rang, series_rang_cur, series_comm, series_comm_cur)
        VALUES ("{login}", "{password}", "{game_name}", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);'''
        insert_to_db(query)
        return {'status': 201}

    def check_word(self, request: dict):
        print("-----------------")
        print(request)
        word_id = int(request['word_id'])
        crossword_id = int(request['crossword_id'])
        print(self.games[crossword_id]['ans'][word_id], request['word'])
        if self.games[crossword_id]['ans'][word_id].upper() == request['word']:
            self.games[crossword_id]['guessed'][str(word_id)] = request['word']
            self.games[crossword_id]['active_words'] -= 1
            print(200)
            print("-----------------")
            return {'status': 200}
        print(400)
        print("-----------------")
        return {'status': 400}

    def add_to_queue(self, request: dict):
        self.queue_semaphore.acquire()
        if len(self.queue) == 0:
            self.users_status[request['uid']] = None
            self.queue.append(request['uid'])
        else:
            crossword, crossword_id = self.generate_crossword()
            self.games[crossword_id]['users'] = [self.queue[-1], request['uid']]
            self.users_status[self.queue[-1]] = (crossword, crossword_id, request['uid'])
            self.users_status[request['uid']] = (crossword, crossword_id, self.queue[-1])
            self.queue.pop()
        self.queue_semaphore.release()
        return {'status': 200}

    def check_queue(self, request: dict):
        if self.users_status[request['uid']] is None:
            return {'status': 304}
        enemy_uid = self.users_status[request['uid']][2]
        query = f'SELECT email, rang_category, rang_subcategory FROM users WHERE uid = "{enemy_uid}"'
        info = select_from_db(query)
        return {'status': 200, 'crossword': self.users_status[request['uid']][0],
                'crossword_id': self.users_status[request['uid']][1],
                'enemy': {'email': info[0][0], 'rang_category': info[0][1], 'rang_subcategory': info[0][2]}}

    def game_status(self, request: dict):
        crossword_id = int(request['crossword_id'])
        if self.games[crossword_id]['active_words'] != 0 and 61 + self.games[crossword_id][
            'start_time'] - time.time() > 0:
            return {'status': 200, 'guessed': self.games[crossword_id]['guessed'],
                    'time': 61 + self.games[crossword_id]['start_time'] - time.time()}
        return {'status': 204,
                'guessed': self.games[crossword_id]['guessed'],
                'time': 0}

    def cansel_queue(self, request: dict):
        self.queue.remove(request['uid'])
        if request['uid'] not in self.users_status or self.users_status[request['uid']] is None or request[
            'uid'] in self.queue:
            del self.users_status[request['uid']]
        return {'status': 200}

    def request_to_client(self, data: str):
        if data == "":
            return None
        request = json.loads(data)
        match request['type']:
            case 'login':
                print(request)
                return self.login(request)
            case 'register':
                print(request)
                return self.register(request)
            case 'check word':
                print(request)
                return self.check_word(request)
            case 'add to queue':
                print(request)
                return self.add_to_queue(request)
            case 'check queue':
                return self.check_queue(request)
            case 'game status':
                return self.game_status(request)
            case 'cancel queue':
                print(request)
                return self.cansel_queue(request)
            case 'update_results':
                print(request)
                return self.update_results(request)
        return {'status': 400}

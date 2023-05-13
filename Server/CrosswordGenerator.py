import copy
import random

class CrosswordGenerator:
    word_grid = [[]]
    words = []
    character_comparison_grid = []
    word_location_info = []

    def __init__(self, input):
        self.word_grid = [[]]
        self.words = []
        self.character_comparison_grid = []
        self.word_location_info = []
        self.words = input
        original_words = copy.deepcopy(self.words)
        original_character_comparison_grid = copy.deepcopy(self.character_comparison_grid)
        original_grid = copy.deepcopy(self.word_grid)
        original_word_location_info = copy.deepcopy(self.word_location_info)
        best_words = copy.deepcopy(original_words)
        best_character_comparison_grid = copy.deepcopy(original_character_comparison_grid)
        best_grid = copy.deepcopy(original_grid)
        best_word_location_info = copy.deepcopy(original_word_location_info)
        num_words_remaining = len(original_words)
        self.words.sort()
        self.words.sort(key=len)
        self.words.reverse()
        for i in range(20):
            self.character_comparison_grid = self.find_comparisons()
            self.create_grid()
            if len(self.words) < num_words_remaining:
                best_words = copy.deepcopy(self.words)
                best_character_comparison_grid = copy.deepcopy(self.character_comparison_grid)
                best_grid = copy.deepcopy(self.word_grid)
                best_word_location_info = copy.deepcopy(self.word_location_info)
                num_words_remaining = len(best_words)
            if num_words_remaining == 0:
                break
            self.character_comparison_grid = copy.deepcopy(original_character_comparison_grid)
            self.word_grid = copy.deepcopy(original_grid)
            self.word_location_info = copy.deepcopy(original_word_location_info)
            self.words = copy.deepcopy(original_words)
            random.shuffle(self.words)
        self.character_comparison_grid = copy.deepcopy(best_character_comparison_grid)
        self.word_grid = copy.deepcopy(best_grid)
        self.word_location_info = copy.deepcopy(best_word_location_info)
        self.words = copy.deepcopy(best_words)
        clear = ["" for i in range(len(self.word_grid))]
        count = 0
        while self.word_grid[0] == clear:
            count += 1
            self.word_grid.pop(0)
        while self.word_grid[-1] == clear:
            self.word_grid.pop(-1)
        for i in range(len(self.word_location_info)):
            self.word_location_info[i][0][0] -= count
        count = 0
        column_is_clear = self.empty_column_left()
        while column_is_clear:
            count += 1
            self.clear_left_column()
            column_is_clear = self.empty_column_left()
        column_is_clear = self.empty_column_right()
        while column_is_clear:
            self.clear_right_column()
            column_is_clear = self.empty_column_right()
        for i in range(len(self.word_location_info)):
            self.word_location_info[i][0][1] -= count
        res = []
        height = len(self.word_grid)
        width = len(self.word_grid[0])
        for i in range(len(self.word_grid)):
            cur = []
            for j in self.word_grid[i]:
                if j == '':
                    cur.append('*')
                else:
                    cur.append(j)
            res.append(cur)
        self.ans = (res, width, height, self.word_location_info)

    def get_crossword(self):
        return self.ans

    def find_comparisons(self):
        self.word_grid = [["" for i in self.words] for j in self.words]
        for i in range(len(self.words)):
            for j in range(len(self.words)):
                self.word_grid[i][j] = CrosswordGenerator.compare_words(self.words[i], self.words[j])
        return self.word_grid

    @staticmethod
    def compare_words(word1: str, word2: str):
        common_characters = ""
        if word1 == word2:
            return word1
        for i in range(len(word1)):
            if word1[i] in word2:
                if word1[i] not in common_characters:
                    common_characters += word1[i]
        return common_characters

    def create_grid(self):
        orientation = False
        first_word = self.words[0]
        self.word_grid = [["" for i in range(len(first_word) * 4)] for j in range(len(first_word) * 4)]
        for i in range(int(len(self.word_grid) / 2 - len(first_word) / 2),
                       int(len(self.word_grid) / 2 - len(first_word) / 2 + len(first_word))):
            self.word_grid[int(len(self.word_grid) / 2)][i] = first_word[i - (int(len(self.word_grid) / 2 - len(first_word) / 2))]
            if (i - (int(len(self.word_grid) / 2 - len(first_word) / 2))) == 0:
                starting_position = tuple([(int(len(self.word_grid) / 2)), i])
                self.word_location_info.append([list(starting_position), not orientation, first_word])
        self.words.pop(0)
        self.place_words(first_word, orientation, starting_position)

    def place_words(self, selected: str, orientation: bool, starting_position: tuple):
        for i in range(len(self.character_comparison_grid)):
            if self.character_comparison_grid[i][i] == selected:
                selected_word_key = i
        match_count = []
        word_list_traversal_key = 0
        if len(self.words) == 0:
            return
        while len(match_count) < 2 and word_list_traversal_key < len(self.character_comparison_grid):
            if self.character_comparison_grid[selected_word_key][word_list_traversal_key] != "" and \
                    self.character_comparison_grid[selected_word_key][word_list_traversal_key] != \
                    self.character_comparison_grid[selected_word_key][selected_word_key] and \
                    self.character_comparison_grid[word_list_traversal_key][word_list_traversal_key] in self.words:
                match_count.append(self.character_comparison_grid[word_list_traversal_key][word_list_traversal_key])
            word_list_traversal_key += 1
        if len(match_count) == 0:
            return
        elif len(match_count) == 1:
            is_placed = False
            possible_intersections = self.find_intersections(selected_word_key, match_count[0])
            possible_intersections_traversal_key = 0
            while possible_intersections_traversal_key < len(possible_intersections) and is_placed != True:
                (is_placed, starting_position) = self.test_placement(
                    possible_intersections[possible_intersections_traversal_key], orientation, starting_position,
                    match_count[0])
                possible_intersections_traversal_key += 1
            if is_placed:
                for i in range(len(self.character_comparison_grid)):
                    self.character_comparison_grid[i].pop(selected_word_key)
                self.character_comparison_grid.pop(selected_word_key)
                orientation = not orientation
                self.words.remove(match_count[0])
                return self.place_words(match_count[0], orientation, starting_position)
            else:
                return
        else:
            is_placed = [False, False]
            starting_position = [starting_position, starting_position]
            possible_intersections = [self.find_intersections(selected_word_key, match_count[0]),
                                      self.find_intersections(selected_word_key, match_count[1])]
            possible_intersections_traversal_key = 0
            while possible_intersections_traversal_key < len(possible_intersections) and False in is_placed:
                idx = 0
                while idx < len(possible_intersections[possible_intersections_traversal_key]) and is_placed[
                    possible_intersections_traversal_key] == False:
                    (is_placed[possible_intersections_traversal_key],
                     starting_position[possible_intersections_traversal_key]) = self.test_placement(
                        possible_intersections[possible_intersections_traversal_key][idx], orientation,
                        starting_position[possible_intersections_traversal_key],
                        match_count[possible_intersections_traversal_key])
                    idx += 1
                possible_intersections_traversal_key += 1
            if is_placed[0] or is_placed[1]:
                for i in range(len(self.character_comparison_grid)):
                    self.character_comparison_grid[i].pop(selected_word_key)
                self.character_comparison_grid.pop(selected_word_key)
                orientation = not orientation
                if not is_placed[1]:
                    self.words.remove(match_count[0])
                    return self.place_words(match_count[0], orientation, starting_position[0])
                elif not is_placed[0]:
                    self.words.remove(match_count[1])
                    return self.place_words(match_count[1], orientation, starting_position[1])
                else:
                    self.words.remove(match_count[0])
                    self.words.remove(match_count[1])
                    self.place_words(match_count[0], orientation, starting_position[0])
                    return self.place_words(match_count[1], orientation, starting_position[1])
        return

    def test_placement(self, intersection: list, orientation: bool, possible: list, word: str):
        if not orientation:
            for i in range(possible[0] - intersection[1],
                           possible[0] - intersection[1] + len(word)):
                if i < 0 or i >= len(self.word_grid):
                    return (False, possible)
                if self.word_grid[i][possible[1] + intersection[0]] != '' and self.word_grid[i][
                    possible[1] + intersection[0]] != word[
                    i - (possible[0] - intersection[1])]:
                    return (False, possible)
            is_no_collision = self.test_collision(possible, intersection, word, orientation)
            if not is_no_collision:
                return (False, possible)
            for i in range(possible[0] - intersection[1],
                           possible[0] - intersection[1] + len(word)):
                self.word_grid[i][possible[1] + intersection[0]] = word[
                    i - (possible[0] - intersection[1])]
                if i - (possible[0] - intersection[1]) == 0:
                    new_position = tuple([i, possible[1] + intersection[0]])
                    self.word_location_info.append([list(new_position), orientation, word])
            return (True, new_position)
        else:
            for i in range(possible[1] - intersection[1],
                           possible[1] - intersection[1] + len(word)):
                if i < 0 or i >= len(self.word_grid):
                    return (False, possible)
                if self.word_grid[possible[0] + intersection[0]][i] != '' and \
                        self.word_grid[possible[0] + intersection[0]][i] != word[
                    i - (possible[1] - intersection[1])]:
                    return (False, possible)
            is_no_collision = self.test_collision(possible, intersection, word, orientation)
            if not is_no_collision:
                return (False, possible)
            for i in range(possible[1] - intersection[1],
                           possible[1] - intersection[1] + len(word)):
                self.word_grid[possible[0] + intersection[0]][i] = word[
                    i - (possible[1] - intersection[1])]
                if i - (possible[1] - intersection[1]) == 0:
                    new_position = tuple([possible[0] + intersection[0], i])
                    self.word_location_info.append([list(new_position), orientation, word])
            return (True, new_position)

    def test_collision(self, possible_intersections: list, intersection: list, word: str, orientation:bool):
        if not orientation:
            if possible_intersections[0] - intersection[1] > 0:
                if self.word_grid[possible_intersections[0] - intersection[1] - 1][
                    possible_intersections[1] + intersection[0]] != '':
                    return False
            if possible_intersections[0] + (len(word) - 1 - intersection[1]) < len(self.word_grid) - 1:
                if self.word_grid[possible_intersections[0] + len(word) - intersection[1]][
                    possible_intersections[1] + intersection[0]] != '':
                    return False
            for i in range(possible_intersections[0] - intersection[1],
                           possible_intersections[0] - intersection[1] + len(word)):
                if self.word_grid[i][possible_intersections[1] + intersection[0]] == '':
                    if (possible_intersections[1] + intersection[0] > 0) and (
                            possible_intersections[1] + intersection[0] < len(self.word_grid) - 1):
                        if (self.word_grid[i][possible_intersections[1] + intersection[0] - 1] != '') or (
                                self.word_grid[i][possible_intersections[1] + intersection[0] + 1] != ''):
                            return False
                    elif possible_intersections[1] + intersection[0] > 0:
                        if self.word_grid[i][possible_intersections[1] + intersection[0] - 1] != '':
                            return False
                    elif possible_intersections[1] + intersection[0] < len(self.word_grid) - 1:
                        if self.word_grid[i][possible_intersections[1] + intersection[0] + 1] != '':
                            return False
        else:
            if possible_intersections[1] - intersection[1] > 0:
                if self.word_grid[possible_intersections[0] + intersection[0]][
                    possible_intersections[1] - intersection[1] - 1] != '':
                    return False
            if possible_intersections[1] + (len(word) - 1 - intersection[1]) < len(self.word_grid) - 1:
                if self.word_grid[possible_intersections[0] + intersection[0]][
                    possible_intersections[1] + len(word) - intersection[1]] != '':
                    return False
            for i in range(possible_intersections[1] - intersection[1],
                           possible_intersections[1] - intersection[1] + len(word)):
                if self.word_grid[possible_intersections[0] + intersection[0]][i] == '':
                    if (possible_intersections[0] + intersection[0] > 0) and (
                            possible_intersections[0] + intersection[0] < len(self.word_grid) - 1):
                        if (self.word_grid[possible_intersections[0] + intersection[0] - 1][i] != '') or (
                                self.word_grid[possible_intersections[0] + intersection[0] + 1][i] != ''):
                            return False
                    elif possible_intersections[0] + intersection[0] > 0:
                        if self.word_grid[possible_intersections[0] + intersection[0] - 1][i] != '':
                            return False
                    elif possible_intersections[0] + intersection[0] < len(self.word_grid) - 1:
                        if self.word_grid[possible_intersections[0] + intersection[0] + 1][i] != '':
                            return False
        return True

    def find_intersections(self, key: int, count: list):
        intersections = []
        for j in range(len(count)):
            for k in range(len(self.character_comparison_grid[key][key])):
                if count[j] == self.character_comparison_grid[key][key][k]:
                    intersections.append(tuple([k, j]))
        return intersections

    def empty_column_left(self):
        for i in range(len(self.word_grid)):
            if self.word_grid[i][0] != '':
                return False
        return True

    def empty_column_right(self):
        for i in range(len(self.word_grid)):
            if self.word_grid[i][-1] != '':
                return False
        return True

    def clear_left_column(self):
        for i in range(len(self.word_grid)):
            self.word_grid[i].pop(0)

    def clear_right_column(self):
        for i in range(len(self.word_grid)):
            self.word_grid[i].pop(-1)
